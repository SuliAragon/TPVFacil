package com.tpvfacil.comercio.controller;

import com.tpvfacil.comercio.service.StockService;
import com.tpvfacil.config.ConfiguracionManager;
import com.tpvfacil.core.db.ProductoRepository;
import com.tpvfacil.core.modelo.Producto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador de la pantalla de gestión de inventario.
 * Muestra todos los productos con alertas visuales (rojo/naranja)
 * y permite editar el stock directamente en tabla.
 */
public class GestionStockController {

    private static final Logger log = LoggerFactory.getLogger(GestionStockController.class);

    @FXML private TextField buscadorField;
    @FXML private TableView<Producto> tablaStock;
    @FXML private TableColumn<Producto, Void> colAlerta;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colStock;

    private final ProductoRepository productoRepository = new ProductoRepository();
    private final StockService stockService = new StockService();
    private final ObservableList<Producto> productosData = FXCollections.observableArrayList();
    private List<Producto> todosLosProductos;

    /** Almacena cambios pendientes: productoId → nuevoStock. */
    private final Map<Integer, Integer> cambiosPendientes = new HashMap<>();
    private int umbralBajoStock = 5;

    @FXML
    public void initialize() {
        String umbralStr = ConfiguracionManager.getInstance().get("umbral_stock_bajo", "5");
        try {
            umbralBajoStock = Integer.parseInt(umbralStr);
        } catch (NumberFormatException e) {
            log.warn("Valor inválido para umbral_stock_bajo: '{}', usando 5 por defecto", umbralStr);
        }

        configurarTabla();
        cargarDatos();

        buscadorField.textProperty().addListener((obs, oldV, newV) -> filtrarTabla());
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNombre()));
        colCategoria.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCategoria()));

        // Columna de alerta visual (círculo rojo/naranja)
        colAlerta.setCellFactory(crearCellFactoryAlerta());

        // Columna de stock editable
        colStock.setCellFactory(crearCellFactoryStockEditable());

        tablaStock.setItems(productosData);
    }

    /** Crea la fábrica de celdas para el indicador de alerta de stock. */
    private Callback<TableColumn<Producto, Void>, TableCell<Producto, Void>> crearCellFactoryAlerta() {
        return param -> new TableCell<>() {
            private final Circle circulo = new Circle(6);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Producto p = getTableRow().getItem();
                    int stockActual = cambiosPendientes.getOrDefault(p.getId(), p.getStock());
                    if (stockActual == 0) {
                        circulo.setFill(Color.RED);
                        setGraphic(circulo);
                    } else if (stockActual > 0 && stockActual <= umbralBajoStock) {
                        circulo.setFill(Color.ORANGE);
                        setGraphic(circulo);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        };
    }

    /** Crea la fábrica de celdas con TextField editable para el stock. */
    private Callback<TableColumn<Producto, String>, TableCell<Producto, String>> crearCellFactoryStockEditable() {
        return param -> new TableCell<>() {
            private final TextField textField = new TextField();

            {
                textField.setOnAction(event -> aplicarCambioStock());
                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) aplicarCambioStock();
                });
            }

            private void aplicarCambioStock() {
                if (getTableRow() == null || getTableRow().getItem() == null) return;
                Producto p = getTableRow().getItem();
                try {
                    int nuevoStock = Integer.parseInt(textField.getText().trim());
                    cambiosPendientes.put(p.getId(), nuevoStock);
                    p.setStock(nuevoStock);
                    tablaStock.refresh();
                } catch (NumberFormatException e) {
                    textField.setText(String.valueOf(cambiosPendientes.getOrDefault(p.getId(), p.getStock())));
                }
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Producto p = getTableRow().getItem();
                    int stockActual = cambiosPendientes.getOrDefault(p.getId(), p.getStock());
                    textField.setText(String.valueOf(stockActual));
                    setGraphic(textField);
                }
            }
        };
    }

    private void cargarDatos() {
        todosLosProductos = productoRepository.findAll();
        productosData.setAll(todosLosProductos);
        cambiosPendientes.clear();
    }

    @FXML
    private void filtrarTabla() {
        String texto = buscadorField.getText().toLowerCase();
        if (texto.isEmpty()) {
            productosData.setAll(todosLosProductos);
        } else {
            productosData.setAll(todosLosProductos.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(texto))
                    .collect(Collectors.toList()));
        }
    }

    /** Guarda todos los cambios de stock pendientes en una transacción. */
    @FXML
    private void guardarCambios() {
        if (!cambiosPendientes.isEmpty()) {
            stockService.ajustarStock(cambiosPendientes);
            log.info("Stock actualizado: {} productos modificados", cambiosPendientes.size());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Gestión de Stock");
            alert.setHeaderText(null);
            alert.setContentText("Se han guardado " + cambiosPendientes.size() + " cambios de stock.");
            alert.showAndWait();

            cargarDatos();
        } else {
            new Alert(Alert.AlertType.INFORMATION, "No hay cambios pendientes.").show();
        }
    }
}
