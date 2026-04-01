package com.tpvfacil.core.ui;

import com.tpvfacil.core.db.ProductoRepository;
import com.tpvfacil.core.modelo.Producto;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.LinkedHashSet;

public class GestionCartaController {

    @FXML private ListView<String> listaCategorias;
    @FXML private Label labelCategoriaSel;
    
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colProdNombre;
    @FXML private TableColumn<Producto, String> colProdPrecio;
    @FXML private TableColumn<Producto, String> colProdIva;
    @FXML private TableColumn<Producto, String> colProdBarras;
    @FXML private TableColumn<Producto, String> colProdEstado;

    private ProductoRepository productoRepository = new ProductoRepository();
    private ObservableList<String> categoriasData = FXCollections.observableArrayList();
    private ObservableList<Producto> productosData = FXCollections.observableArrayList();
    private List<Producto> todosLosProductos;

    @FXML
    public void initialize() {
        configurarTabla();
        configurarDragAndDropCategorias();
        recargarDatos();
        
        listaCategorias.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                labelCategoriaSel.setText("Productos - " + newV);
                filtrarProductos(newV);
            }
        });
    }

    private void configurarTabla() {
        colProdNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colProdPrecio.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("%.2f €", cellData.getValue().getPrecio()))
        );
        colProdIva.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("%.0f%%", cellData.getValue().getIvaPorcentaje()))
        );
        colProdBarras.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colProdEstado.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isActivo() ? "Activo" : "Inactivo")
        );
        tablaProductos.setItems(productosData);
    }

    private void configurarDragAndDropCategorias() {
        listaCategorias.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };

            cell.setOnDragDetected(event -> {
                if (cell.getItem() == null) return;
                Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(cell.getItem());
                db.setContent(content);
                event.consume();
            });

            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    ObservableList<String> items = listaCategorias.getItems();
                    int draggedIdx = items.indexOf(db.getString());
                    int thisIdx = cell.getIndex();
                    
                    if (thisIdx < 0 || thisIdx >= items.size()) {
                        thisIdx = items.size() - 1;
                    }
                    
                    String item = items.remove(draggedIdx);
                    items.add(thisIdx, item);
                    
                    // TODO: Guardar nuevo orden de categorías en configuracion
                    // ConfiguracionManager.getInstance().set("orden_categorias", String.join(",", items));
                    
                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            });

            return cell;
        });
    }

    public void recargarDatos() {
        todosLosProductos = productoRepository.findAll(); // Aquí podríamos necesitar traer incluso los inactivos si hacemos findAll() sin filtro
        Set<String> catSet = new LinkedHashSet<>();
        for (Producto p : todosLosProductos) {
            if (p.getCategoria() != null && !p.getCategoria().isEmpty()) {
                catSet.add(p.getCategoria());
            }
        }
        
        String seleccionada = listaCategorias.getSelectionModel().getSelectedItem();
        categoriasData.setAll(catSet);
        listaCategorias.setItems(categoriasData);
        
        if (seleccionada != null && categoriasData.contains(seleccionada)) {
            listaCategorias.getSelectionModel().select(seleccionada);
            filtrarProductos(seleccionada);
        } else if (!categoriasData.isEmpty()) {
            listaCategorias.getSelectionModel().selectFirst();
        } else {
            productosData.clear();
        }
    }

    private void filtrarProductos(String categoria) {
        List<Producto> filtrados = todosLosProductos.stream()
                .filter(p -> categoria.equals(p.getCategoria()))
                .collect(Collectors.toList());
        productosData.setAll(filtrados);
    }

    @FXML
    private void nuevaCategoria() {
        // En una app real podríamos pedir String con TextInputDialog y luego abrir modal de producto con esa cat
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nueva Categoría");
        dialog.setHeaderText("Añadir nueva categoría");
        dialog.setContentText("Nombre:");
        dialog.showAndWait().ifPresent(nombre -> {
            if (!nombre.trim().isEmpty() && !categoriasData.contains(nombre)) {
                categoriasData.add(nombre);
            }
        });
    }

    @FXML
    private void editarCategoria() {
        String catSel = listaCategorias.getSelectionModel().getSelectedItem();
        if (catSel == null) return;

        TextInputDialog dialog = new TextInputDialog(catSel);
        dialog.setTitle("Editar Categoría");
        dialog.setHeaderText("Modificar nombre de categoría");
        dialog.setContentText("Nombre:");
        dialog.showAndWait().ifPresent(nuevoNombre -> {
            if (!nuevoNombre.trim().isEmpty() && !nuevoNombre.equals(catSel)) {
                // Actualizar en todos los productos
                for (Producto p : todosLosProductos) {
                    if (catSel.equals(p.getCategoria())) {
                        p.setCategoria(nuevoNombre);
                        productoRepository.update(p);
                    }
                }
                recargarDatos();
            }
        });
    }

    @FXML
    private void nuevoProducto() {
        abrirDialogoProducto(null);
    }

    @FXML
    private void editarProducto() {
        Producto sel = tablaProductos.getSelectionModel().getSelectedItem();
        if (sel != null) {
            abrirDialogoProducto(sel);
        }
    }

    @FXML
    private void alternarEstadoProducto() {
        Producto sel = tablaProductos.getSelectionModel().getSelectedItem();
        if (sel != null) {
            sel.setActivo(!sel.isActivo());
            productoRepository.update(sel);
            tablaProductos.refresh(); // O recargarDatos()
        }
    }

    private void abrirDialogoProducto(Producto p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tpvfacil/dialogo-producto.fxml"));
            Parent root = loader.load();
            
            DialogoProductoController controller = loader.getController();
            controller.setProducto(p, categoriasData, this);
            
            Stage stage = new Stage();
            stage.setTitle(p == null ? "Nuevo Producto" : "Editar Producto");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
