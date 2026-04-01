package com.tpvfacil.core.ui;

import com.tpvfacil.core.db.ProductoRepository;
import com.tpvfacil.core.modelo.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DialogoProductoController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<Double> cmbIva;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField txtBarras;
    @FXML private CheckBox chkActivo;
    @FXML private TextField txtStock;

    private Producto producto;
    private GestionCartaController parentController;
    private ProductoRepository repo = new ProductoRepository();

    @FXML
    public void initialize() {
        cmbIva.setItems(FXCollections.observableArrayList(4.0, 10.0, 21.0));
        cmbIva.getSelectionModel().select(10.0); // defecto hosteleria
    }

    public void setProducto(Producto p, ObservableList<String> categorias, GestionCartaController parentController) {
        this.parentController = parentController;
        this.cmbCategoria.setItems(categorias);
        
        if (p != null) {
            this.producto = p;
            txtNombre.setText(p.getNombre());
            txtPrecio.setText(String.valueOf(p.getPrecio()));
            cmbIva.getSelectionModel().select(p.getIvaPorcentaje());
            cmbCategoria.getSelectionModel().select(p.getCategoria());
            txtBarras.setText(p.getCodigoBarras() != null ? p.getCodigoBarras() : "");
            chkActivo.setSelected(p.isActivo());
            txtStock.setText(String.valueOf(p.getStock()));
        } else {
            this.producto = new Producto();
            // Si hay una categoría seleccionada en la ventana padre, la pre-seleccionamos
            if (!categorias.isEmpty()) {
                cmbCategoria.getSelectionModel().selectFirst();
            }
        }
    }

    @FXML
    private void guardar() {
        try {
            if (txtNombre.getText().trim().isEmpty() || 
                txtPrecio.getText().trim().isEmpty() || 
                cmbCategoria.getValue() == null) {
                mostrarError("Nombre, precio y categoría son obligatorios.");
                return;
            }

            producto.setNombre(txtNombre.getText().trim());
            producto.setPrecio(Double.parseDouble(txtPrecio.getText().trim().replace(",", ".")));
            producto.setIvaPorcentaje(cmbIva.getValue());
            
            // Permitir creación de categorías on-the-fly
            String categoria = cmbCategoria.getValue().trim();
            producto.setCategoria(categoria);
            
            String barras = txtBarras.getText().trim();
            producto.setCodigoBarras(barras.isEmpty() ? null : barras);
            
            producto.setActivo(chkActivo.isSelected());
            
            int stockStr = Integer.parseInt(txtStock.getText().trim());
            producto.setStock(stockStr);

            if (producto.getId() == 0) {
                repo.save(producto);
            } else {
                repo.update(producto);
            }

            parentController.recargarDatos();
            cerrar();

        } catch (NumberFormatException ex) {
            mostrarError("El precio y el stock deben ser números válidos.");
        }
    }

    @FXML
    private void cancelar() {
        cerrar();
    }

    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }
    
    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
