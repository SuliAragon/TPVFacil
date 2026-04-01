package com.tpvfacil.comercio.controller;

import com.tpvfacil.Main;
import com.tpvfacil.comercio.modelo.ItemCesta;
import com.tpvfacil.config.ConfiguracionManager;
import com.tpvfacil.core.db.ProductoRepository;
import com.tpvfacil.core.modelo.Producto;
import com.tpvfacil.core.modelo.TipoNegocio;
import com.tpvfacil.core.ui.CierreCajaController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador principal del TPV de Comercio.
 * Gestiona el catálogo de productos, la cesta de compra,
 * la detección de lector de código de barras y la navegación
 * a las pantallas de cobro, devolución, stock y cierre de caja.
 */
public class VentaController {

    private static final Logger log = LoggerFactory.getLogger(VentaController.class);

    @FXML private Label lblReloj;
    @FXML private TextField buscadorField;
    @FXML private FlowPane catalogoPane;
    @FXML private HBox categoriaBar;

    @FXML private TableView<ItemCesta> tablaCesta;
    @FXML private TableColumn<ItemCesta, String> colProducto;
    @FXML private TableColumn<ItemCesta, String> colCantidad;
    @FXML private TableColumn<ItemCesta, String> colSubtotal;
    @FXML private TableColumn<ItemCesta, Void> colAcciones;

    @FXML private Label labelTotal;
    @FXML private Button btnCobrar;

    private final ProductoRepository productoRepository = new ProductoRepository();
    private final ObservableList<ItemCesta> itemsCesta = FXCollections.observableArrayList();

    private long ultimaTeclaMs = 0;
    private final StringBuilder bufferBarcode = new StringBuilder();
    private String categoriaSeleccionada = null; // null = todas

    private static final DateTimeFormatter FMT_RELOJ = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        configurarTablaCesta();
        configurarBuscadorYLector();
        cargarBotonesCategorias();
        cargarCatalogo("", null);
        iniciarReloj();

        Platform.runLater(() -> buscadorField.requestFocus());
    }

    /** Inicia un ticker de 1 segundo que actualiza la hora en el header. */
    private void iniciarReloj() {
        Timeline reloj = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (lblReloj != null) {
                lblReloj.setText(LocalTime.now().format(FMT_RELOJ));
            }
        }));
        reloj.setCycleCount(Animation.INDEFINITE);
        reloj.play();
        if (lblReloj != null) lblReloj.setText(LocalTime.now().format(FMT_RELOJ));
    }

    // ── Tabla de cesta ──────────────────────────────────────────────

    private void configurarTablaCesta() {
        colProducto.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getProducto().getNombre())
        );
        colCantidad.setCellValueFactory(cd ->
            new SimpleStringProperty(String.format("%.0f", cd.getValue().getCantidad()))
        );
        colSubtotal.setCellValueFactory(cd ->
            new SimpleStringProperty(String.format("%.2f €", cd.getValue().getSubtotalConIva()))
        );

        colAcciones.setCellFactory(crearCellFactoryAcciones());
        tablaCesta.setItems(itemsCesta);
    }

    /** Crea la fábrica de celdas con botones +, - y 🗑 por cada línea de la cesta. */
    private Callback<TableColumn<ItemCesta, Void>, TableCell<ItemCesta, Void>> crearCellFactoryAcciones() {
        return param -> new TableCell<>() {
            private final Button btnMenos = new Button("-");
            private final Button btnMas = new Button("+");
            private final Button btnQuitar = new Button("🗑");
            private final HBox pane = new HBox(5, btnMenos, btnMas, btnQuitar);

            {
                btnMenos.setOnAction(e -> {
                    ItemCesta item = getTableView().getItems().get(getIndex());
                    item.decrementar();
                    tablaCesta.refresh();
                    actualizarTotal();
                });
                btnMas.setOnAction(e -> {
                    ItemCesta item = getTableView().getItems().get(getIndex());
                    item.incrementar();
                    tablaCesta.refresh();
                    actualizarTotal();
                });
                btnQuitar.setOnAction(e -> {
                    ItemCesta item = getTableView().getItems().get(getIndex());
                    itemsCesta.remove(item);
                    actualizarTotal();
                });
                btnMenos.setStyle("-fx-font-size: 10px;");
                btnMas.setStyle("-fx-font-size: 10px;");
                btnQuitar.setStyle("-fx-font-size: 10px; -fx-text-fill: red;");
                pane.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
    }

    // ── Lector de código de barras ──────────────────────────────────

    /** Configura la detección de entrada rápida (lector USB) vs. entrada humana. */
    private void configurarBuscadorYLector() {
        String umbralStr = ConfiguracionManager.getInstance().get("lector_barras_threshold_ms", "50");
        long thresholdMs;
        try {
            thresholdMs = Long.parseLong(umbralStr);
        } catch (NumberFormatException e) {
            thresholdMs = 50;
        }
        final long threshold = thresholdMs;

        buscadorField.setOnKeyPressed(event -> {
            long ahora = System.currentTimeMillis();
            long diff = ahora - ultimaTeclaMs;
            ultimaTeclaMs = ahora;

            if (event.getCode() == KeyCode.ENTER) {
                String codigo = bufferBarcode.toString().trim();
                if (!codigo.isEmpty()) {
                    buscarPorCodigoBarras(codigo);
                }
                bufferBarcode.setLength(0);
                buscadorField.clear();
            } else if (diff < threshold && event.getCode() != KeyCode.SHIFT) {
                bufferBarcode.append(event.getText());
            } else {
                bufferBarcode.setLength(0);
            }
        });

        buscadorField.textProperty().addListener((obs, oldV, newV) -> {
            if (bufferBarcode.isEmpty() && newV != null) {
                cargarCatalogo(newV, categoriaSeleccionada);
            }
        });
    }

    // ── Categorías ─────────────────────────────────────────────────

    /** Carga los botones de categoría dinámicamente desde las categorías de la BD. */
    private void cargarBotonesCategorias() {
        categoriaBar.getChildren().clear();

        // Botón "Todas"
        ToggleButton btnTodas = crearBotonCategoria("Todas");
        btnTodas.setSelected(true);
        btnTodas.setOnAction(e -> {
            categoriaSeleccionada = null;
            deseleccionarOtros(btnTodas);
            cargarCatalogo(buscadorField.getText(), null);
        });
        categoriaBar.getChildren().add(btnTodas);

        // Botones por categoría existente
        List<String> categorias = productoRepository.findAll().stream()
                .map(Producto::getCategoria)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        for (String cat : categorias) {
            ToggleButton btn = crearBotonCategoria(cat);
            btn.setOnAction(e -> {
                categoriaSeleccionada = cat;
                deseleccionarOtros(btn);
                cargarCatalogo(buscadorField.getText(), cat);
            });
            categoriaBar.getChildren().add(btn);
        }
    }

    private ToggleButton crearBotonCategoria(String texto) {
        ToggleButton btn = new ToggleButton(texto);
        btn.setStyle("-fx-font-size: 13px; -fx-padding: 6 14;");
        btn.setCursor(Cursor.HAND);
        return btn;
    }

    private void deseleccionarOtros(ToggleButton seleccionado) {
        for (var node : categoriaBar.getChildren()) {
            if (node instanceof ToggleButton tb && tb != seleccionado) {
                tb.setSelected(false);
            }
        }
        seleccionado.setSelected(true);
    }

    // ── Catálogo ───────────────────────────────────────────────────

    /** Carga los productos en el panel de catálogo, filtrados por texto y categoría. */
    private void cargarCatalogo(String texto, String categoria) {
        catalogoPane.getChildren().clear();
        Task<List<Producto>> task = new Task<>() {
            @Override
            protected List<Producto> call() {
                List<Producto> todos;
                if (texto == null || texto.trim().isEmpty()) {
                    todos = productoRepository.findAll();
                } else {
                    todos = productoRepository.findByNombre(texto.trim());
                }
                if (categoria != null) {
                    todos = todos.stream()
                            .filter(p -> categoria.equalsIgnoreCase(p.getCategoria()))
                            .collect(Collectors.toList());
                }
                return todos;
            }
        };

        task.setOnSucceeded(e -> {
            for (Producto p : task.getValue()) {
                catalogoPane.getChildren().add(crearTarjetaProducto(p));
            }
        });

        task.setOnFailed(e -> log.error("Error cargando catálogo", task.getException()));

        new Thread(task).start();
    }

    /** Crea una tarjeta visual para un producto del catálogo. */
    private VBox crearTarjetaProducto(Producto p) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        box.setPrefSize(130, 100);
        box.setAlignment(Pos.CENTER);

        Label lblNombre = new Label(p.getNombre());
        lblNombre.setWrapText(true);
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Label lblPrecio = new Label(String.format("%.2f €", p.getPrecioConIva()));
        lblPrecio.setStyle("-fx-text-fill: #1B4F8A; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Indicador stock
        if (p.tieneControlStock() && p.getStock() == 0) {
            Label lblAgotado = new Label("AGOTADO");
            lblAgotado.setStyle("-fx-text-fill: red; -fx-font-size: 10px; -fx-font-weight: bold;");
            box.getChildren().addAll(lblNombre, lblPrecio, lblAgotado);
            box.setStyle(box.getStyle() + "-fx-opacity: 0.5;");
        } else {
            box.getChildren().addAll(lblNombre, lblPrecio);
        }

        box.setOnMouseClicked(event -> {
            if (p.tieneControlStock() && p.getStock() == 0) {
                return; // No añadir productos agotados
            }
            agregarProducto(p);
        });
        box.setCursor(Cursor.HAND);

        return box;
    }

    // ── Búsqueda por código de barras ──────────────────────────────

    private void buscarPorCodigoBarras(String codigo) {
        Optional<Producto> prodOpt = productoRepository.findByCodigoBarras(codigo);
        if (prodOpt.isPresent()) {
            agregarProducto(prodOpt.get());
            log.info("Producto añadido por lector: {}", prodOpt.get().getNombre());
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Producto no encontrado con código: " + codigo);
            alert.show();
            log.warn("Código de barras no encontrado: {}", codigo);
        }
    }

    // ── Gestión de la cesta ────────────────────────────────────────

    /** Añade un producto a la cesta o incrementa su cantidad si ya existe. */
    private void agregarProducto(Producto p) {
        Optional<ItemCesta> existente = itemsCesta.stream()
                .filter(item -> item.getProducto().getId() == p.getId())
                .findFirst();

        if (existente.isPresent()) {
            existente.get().incrementar();
            tablaCesta.refresh();
        } else {
            itemsCesta.add(new ItemCesta(p, 1));
        }

        actualizarTotal();
        buscadorField.requestFocus();
    }

    /** Recalcula el total de la cesta. */
    private void actualizarTotal() {
        double total = itemsCesta.stream().mapToDouble(ItemCesta::getSubtotalConIva).sum();
        labelTotal.setText(String.format("%.2f €", total));
        btnCobrar.setText(String.format("💳 COBRAR %.2f €", total));
    }

    @FXML
    private void limpiarCesta() {
        itemsCesta.clear();
        actualizarTotal();
        buscadorField.requestFocus();
    }

    // ── Navegación ─────────────────────────────────────────────────

    /** Abre el modal de devolución de artículos. */
    @FXML
    private void abrirDevolucion() {
        abrirModal("/com/tpvfacil/pantalla-devolucion.fxml", "Devoluciones", 650, 500);
    }

    /** Abre la pantalla de cobro pasándole la cesta actual. */
    @FXML
    private void cobrar() {
        if (itemsCesta.isEmpty()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tpvfacil/pantalla-cobro-comercio.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            CobroComercioController ctrl = loader.getController();
            ctrl.setCesta(new ArrayList<>(itemsCesta));

            Stage stage = new Stage();
            stage.setTitle("Cobro — TPVFácil");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            limpiarCesta();
            cargarCatalogo(buscadorField.getText(), categoriaSeleccionada); // Refrescar stock
        } catch (Exception e) {
            log.error("Error al abrir pantalla de cobro", e);
        }
    }

    /** Vuelve a la pantalla de inicio del sistema. */
    @FXML
    private void volverInicio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tpvfacil/inicio.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) btnCobrar.getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(scene);
            Main.actualizarTituloVentana(stage);
        } catch (Exception e) {
            log.error("Error al volver al inicio", e);
        }
    }

    /** Cierra la aplicación. */
    @FXML
    private void salir() {
        Platform.exit();
    }

    /** Abre la gestión de carta (productos y categorías). */
    @FXML
    private void abrirGestionCarta() {
        abrirModal("/com/tpvfacil/pantalla-gestion-carta.fxml", "Gestión de Carta", 900, 600);
        cargarBotonesCategorias(); // Refrescar categorías tras posibles cambios
        cargarCatalogo(buscadorField.getText(), categoriaSeleccionada);
    }

    /** Abre la gestión de inventario. */
    @FXML
    private void abrirGestionStock() {
        abrirModal("/com/tpvfacil/pantalla-gestion-stock.fxml", "Gestión de Stock", 800, 600);
        cargarCatalogo(buscadorField.getText(), categoriaSeleccionada); // Refrescar
    }

    /** Abre la pantalla de copias de seguridad. */
    @FXML
    private void abrirBackups() {
        abrirModal("/com/tpvfacil/pantalla-backup.fxml", "Copias de Seguridad", 600, 400);
    }

    /** Abre la pantalla de cierre de caja, filtrada por módulo Comercio. */
    @FXML
    private void abrirCierreCaja() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tpvfacil/pantalla-cierre-caja.fxml"));
            Scene scene = new Scene(loader.load(), 650, 550);
            CierreCajaController ctrl = loader.getController();
            ctrl.setTipoNegocio(TipoNegocio.COMERCIO);
            Stage stage = new Stage();
            stage.setTitle("Cierre de Caja — Comercio");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            log.error("Error al abrir cierre de caja", e);
        }
    }

    /** Abre un modal genérico a partir de un FXML. */
    private void abrirModal(String rutaFxml, String titulo, int ancho, int alto) {
        try {
            URL fxml = getClass().getResource(rutaFxml);
            Scene scene = new Scene(new FXMLLoader(fxml).load(), ancho, alto);
            Stage stage = new Stage();
            stage.setTitle(titulo + " — TPVFácil");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            log.error("Error al abrir {}: {}", titulo, e.getMessage(), e);
        }
    }
}
