package com.tpvfacil.hosteleria.controller;

import com.tpvfacil.core.db.ComandaRepository;
import com.tpvfacil.core.db.ProductoRepository;
import com.tpvfacil.core.modelo.Producto;
import com.tpvfacil.hosteleria.modelo.Comanda;
import com.tpvfacil.hosteleria.modelo.LineaComanda;
import com.tpvfacil.hosteleria.modelo.Mesa;
import com.tpvfacil.hosteleria.service.ComandaService;
import com.tpvfacil.hosteleria.service.TicketCocina;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador de la pantalla de comanda de hostelería.
 * Panel izquierdo: líneas de la comanda con +/-/🗑 y total.
 * Panel derecho: carta de productos filtrada por categoría.
 */
public class ComandaController {

    private static final Logger log = LoggerFactory.getLogger(ComandaController.class);

    // Cabecera
    @FXML private Label lblMesaInfo;
    @FXML private Label lblTiempoAbierta;
    @FXML private Spinner<Integer> spnComensales;

    // Comanda
    @FXML private TableView<LineaComanda> tablaComanda;
    @FXML private TableColumn<LineaComanda, String> colProducto;
    @FXML private TableColumn<LineaComanda, String> colCant;
    @FXML private TableColumn<LineaComanda, String> colPrecio;
    @FXML private TableColumn<LineaComanda, Void> colAcciones;
    @FXML private Label lblTotal;

    // Carta
    @FXML private HBox categoriaBar;
    @FXML private FlowPane cartaPane;

    private final ProductoRepository productoRepository = new ProductoRepository();
    private final ComandaRepository comandaRepository = new ComandaRepository();
    private final ComandaService comandaService = new ComandaService();
    private final TicketCocina ticketCocina = new TicketCocina();

    private Mesa mesa;
    private Comanda comanda;
    private final ObservableList<LineaComanda> lineasData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTabla();
    }

    /** Recibe la mesa e inicializa (carga o crea comanda). */
    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
        this.comanda = comandaService.obtenerOCrear(mesa);

        lblMesaInfo.setText(mesa.getNombre() + " — " + mesa.getZona());

        if (comanda.getFechaApertura() != null) {
            long mins = Duration.between(comanda.getFechaApertura(), LocalDateTime.now()).toMinutes();
            lblTiempoAbierta.setText(String.format("Abierta: %dh %02dm", mins / 60, mins % 60));
        }

        spnComensales.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, comanda.getNumComensales()));
        spnComensales.valueProperty().addListener((obs, oldV, newV) -> {
            comanda.setNumComensales(newV);
            comandaRepository.update(comanda);
        });

        recargarLineas();
        cargarBotonesCategorias();
        cargarCarta(null);
    }

    // ── Tabla de comanda ───────────────────────────────────────────

    private void configurarTabla() {
        colProducto.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getNombreProducto()));
        colCant.setCellValueFactory(cd ->
                new SimpleStringProperty(String.format("%.0f", cd.getValue().getCantidad())));
        colPrecio.setCellValueFactory(cd ->
                new SimpleStringProperty(String.format("%.2f €", cd.getValue().getSubtotal())));

        colAcciones.setCellFactory(crearCellFactoryAcciones());
        tablaComanda.setItems(lineasData);
    }

    /** Crea botones +/-/🗑 (deshabilitados si la línea ya fue enviada a cocina 🔒). */
    private Callback<TableColumn<LineaComanda, Void>, TableCell<LineaComanda, Void>> crearCellFactoryAcciones() {
        return param -> new TableCell<>() {
            private final Button btnMenos = new Button("-");
            private final Button btnMas = new Button("+");
            private final Button btnQuitar = new Button("🗑");
            private final Label lblLock = new Label("🔒");
            private final HBox paneEditable = new HBox(4, btnMenos, btnMas, btnQuitar);
            {
                paneEditable.setAlignment(Pos.CENTER);
                btnMenos.setStyle("-fx-font-size: 10px;");
                btnMas.setStyle("-fx-font-size: 10px;");
                btnQuitar.setStyle("-fx-font-size: 10px; -fx-text-fill: red;");
                lblLock.setStyle("-fx-font-size: 14px;");

                btnMas.setOnAction(e -> {
                    LineaComanda l = getTableView().getItems().get(getIndex());
                    l.setCantidad(l.getCantidad() + 1);
                    // Actualizar en BD
                    comandaRepository.actualizarEstadoLinea(l.getId(), l.getEstado(), l.isEnviadoCocina());
                    // Re-save con cantidad actualizada
                    actualizarCantidadLinea(l);
                    tablaComanda.refresh();
                    actualizarTotal();
                });
                btnMenos.setOnAction(e -> {
                    LineaComanda l = getTableView().getItems().get(getIndex());
                    if (l.getCantidad() > 1) {
                        l.setCantidad(l.getCantidad() - 1);
                        actualizarCantidadLinea(l);
                        tablaComanda.refresh();
                        actualizarTotal();
                    }
                });
                btnQuitar.setOnAction(e -> {
                    LineaComanda l = getTableView().getItems().get(getIndex());
                    eliminarLinea(l);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    LineaComanda l = getTableRow().getItem();
                    setGraphic(l.isEnviadoCocina() ? lblLock : paneEditable);
                }
            }
        };
    }

    private void actualizarCantidadLinea(LineaComanda l) {
        try {
            var conn = com.tpvfacil.core.db.DatabaseManager.getInstance().getConexion();
            try (var stmt = conn.prepareStatement("UPDATE lineas_comanda SET cantidad = ? WHERE id = ?")) {
                stmt.setDouble(1, l.getCantidad());
                stmt.setInt(2, l.getId());
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            log.error("Error actualizando cantidad de línea", e);
        }
    }

    private void eliminarLinea(LineaComanda l) {
        try {
            var conn = com.tpvfacil.core.db.DatabaseManager.getInstance().getConexion();
            try (var stmt = conn.prepareStatement("DELETE FROM lineas_comanda WHERE id = ?")) {
                stmt.setInt(1, l.getId());
                stmt.executeUpdate();
            }
            recargarLineas();
        } catch (Exception e) {
            log.error("Error eliminando línea de comanda", e);
        }
    }

    private void recargarLineas() {
        List<LineaComanda> lineas = comandaRepository.findLineasByComandaId(comanda.getId());
        comanda.setLineas(lineas);
        lineasData.setAll(lineas);
        actualizarTotal();
    }

    private void actualizarTotal() {
        double total = lineasData.stream().mapToDouble(LineaComanda::getSubtotal).sum();
        lblTotal.setText(String.format("%.2f €", total));
    }

    // ── Carta de productos ─────────────────────────────────────────

    private void cargarBotonesCategorias() {
        categoriaBar.getChildren().clear();
        ToggleButton btnTodas = new ToggleButton("Todas");
        btnTodas.setSelected(true);
        btnTodas.setStyle("-fx-font-size: 12px; -fx-padding: 5 12;");
        btnTodas.setCursor(Cursor.HAND);
        btnTodas.setOnAction(e -> {
            deseleccionarOtros(btnTodas);
            cargarCarta(null);
        });
        categoriaBar.getChildren().add(btnTodas);

        List<String> categorias = productoRepository.findAll().stream()
                .map(Producto::getCategoria)
                .filter(c -> c != null && !c.isBlank())
                .distinct().sorted()
                .collect(Collectors.toList());

        for (String cat : categorias) {
            ToggleButton btn = new ToggleButton(cat);
            btn.setStyle("-fx-font-size: 12px; -fx-padding: 5 12;");
            btn.setCursor(Cursor.HAND);
            btn.setOnAction(e -> {
                deseleccionarOtros(btn);
                cargarCarta(cat);
            });
            categoriaBar.getChildren().add(btn);
        }
    }

    private void deseleccionarOtros(ToggleButton seleccionado) {
        for (var node : categoriaBar.getChildren()) {
            if (node instanceof ToggleButton tb && tb != seleccionado) tb.setSelected(false);
        }
        seleccionado.setSelected(true);
    }

    private void cargarCarta(String categoria) {
        cartaPane.getChildren().clear();
        List<Producto> productos = productoRepository.findAll();
        if (categoria != null) {
            productos = productos.stream()
                    .filter(p -> categoria.equalsIgnoreCase(p.getCategoria()))
                    .collect(Collectors.toList());
        }

        for (Producto p : productos) {
            VBox card = new VBox(3);
            card.setPadding(new Insets(8));
            card.setAlignment(Pos.CENTER);
            card.setPrefSize(120, 80);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 4, 0, 0, 2);");
            card.setCursor(Cursor.HAND);

            Label lblNombre = new Label(p.getNombre());
            lblNombre.setWrapText(true);
            lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

            Label lblPrecio = new Label(String.format("%.2f €", p.getPrecioConIva()));
            lblPrecio.setStyle("-fx-text-fill: #1B4F8A; -fx-font-size: 13px; -fx-font-weight: bold;");

            card.getChildren().addAll(lblNombre, lblPrecio);
            card.setOnMouseClicked(e -> agregarProductoAComanda(p));
            cartaPane.getChildren().add(card);
        }
    }

    /** Añade un producto a la comanda o incrementa cantidad si ya existe. */
    private void agregarProductoAComanda(Producto p) {
        // Buscar si ya existe una línea no enviada con este producto
        Optional<LineaComanda> existente = comanda.getLineas().stream()
                .filter(l -> l.getProductoId() == p.getId() && !l.isEnviadoCocina())
                .findFirst();

        if (existente.isPresent()) {
            LineaComanda l = existente.get();
            l.setCantidad(l.getCantidad() + 1);
            actualizarCantidadLinea(l);
        } else {
            LineaComanda nueva = new LineaComanda();
            nueva.setComandaId(comanda.getId());
            nueva.setProductoId(p.getId());
            nueva.setNombreProducto(p.getNombre());
            nueva.setCantidad(1);
            nueva.setPrecioUnitario(p.getPrecioConIva());
            nueva.setEnviadoCocina(false);
            nueva.setEstado(LineaComanda.Estado.PENDIENTE);
            comandaRepository.saveLinea(nueva);
        }
        recargarLineas();
    }

    // ── Acciones principales ───────────────────────────────────────

    /** Envía las líneas no enviadas a cocina. */
    @FXML
    private void enviarACocina() {
        List<LineaComanda> nuevas = comanda.getLineas().stream()
                .filter(l -> !l.isEnviadoCocina())
                .toList();

        if (nuevas.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No hay líneas nuevas para enviar a cocina.").show();
            return;
        }

        boolean ok = ticketCocina.enviarACocina(comanda, nuevas);
        if (ok) {
            recargarLineas();
            new Alert(Alert.AlertType.INFORMATION, "Enviadas " + nuevas.size() + " líneas a cocina.").show();
        }
    }

    /** Abre la pantalla de cobro para esta comanda. */
    @FXML
    private void cobrar() {
        if (lineasData.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "La comanda está vacía.").show();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tpvfacil/pantalla-cobro-hosteleria.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            CobroHosteleriaController ctrl = loader.getController();
            ctrl.setComandaYMesa(comanda, mesa);

            Stage stage = new Stage();
            stage.setTitle("Cobro — " + mesa.getNombre());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Si se cobró, cerrar esta ventana
            if (ctrl.isCobroCorrecto()) {
                cerrar();
            }
        } catch (Exception e) {
            log.error("Error al abrir cobro", e);
        }
    }

    /** Modal para añadir observaciones a la comanda. */
    @FXML
    private void editarObservaciones() {
        TextInputDialog dialog = new TextInputDialog(comanda.getObservaciones());
        dialog.setTitle("Observaciones");
        dialog.setHeaderText("Observaciones de la comanda");
        dialog.setContentText("Notas:");
        dialog.showAndWait().ifPresent(obs -> {
            comanda.setObservaciones(obs);
            comandaRepository.update(comanda);
        });
    }

    /** Vuelve a la pantalla de mesas. */
    @FXML
    private void volverMesas() {
        cerrar();
    }

    private void cerrar() {
        Stage s = (Stage) lblTotal.getScene().getWindow();
        if (s != null) s.close();
    }
}
