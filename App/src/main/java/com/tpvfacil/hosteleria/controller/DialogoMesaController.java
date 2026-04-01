package com.tpvfacil.hosteleria.controller;

import com.tpvfacil.core.db.MesaRepository;
import com.tpvfacil.hosteleria.modelo.EstadoMesa;
import com.tpvfacil.hosteleria.modelo.Mesa;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador del diálogo modal para crear o editar una mesa.
 */
public class DialogoMesaController {

    @FXML private TextField txtNombre;
    @FXML private Spinner<Integer> spnCapacidad;
    @FXML private ComboBox<String> cmbZona;

    private final MesaRepository mesaRepository = new MesaRepository();
    private Mesa mesaExistente;

    @FXML
    public void initialize() {
        spnCapacidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 4));
        cmbZona.getItems().addAll("Sala", "Terraza", "Barra", "Reservado");
        cmbZona.getSelectionModel().select("Sala");
    }

    /** Carga los datos de una mesa existente para edición. */
    public void setMesa(Mesa mesa) {
        this.mesaExistente = mesa;
        txtNombre.setText(mesa.getNombre());
        spnCapacidad.getValueFactory().setValue(mesa.getCapacidad());
        if (cmbZona.getItems().contains(mesa.getZona())) {
            cmbZona.getSelectionModel().select(mesa.getZona());
        } else {
            cmbZona.getItems().add(mesa.getZona());
            cmbZona.getSelectionModel().select(mesa.getZona());
        }
    }

    /** Guarda o actualiza la mesa en la base de datos. */
    @FXML
    private void guardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "El nombre de la mesa no puede estar vacío.").show();
            return;
        }

        if (mesaExistente != null) {
            mesaExistente.setNombre(nombre);
            mesaExistente.setCapacidad(spnCapacidad.getValue());
            mesaExistente.setZona(cmbZona.getValue());
            mesaRepository.update(mesaExistente);
        } else {
            Mesa nueva = new Mesa();
            nueva.setNombre(nombre);
            nueva.setCapacidad(spnCapacidad.getValue());
            nueva.setZona(cmbZona.getValue());
            nueva.setEstado(EstadoMesa.LIBRE);
            nueva.setActiva(true);
            mesaRepository.save(nueva);
        }
        cerrar();
    }

    @FXML
    private void cancelar() {
        cerrar();
    }

    private void cerrar() {
        Stage s = (Stage) txtNombre.getScene().getWindow();
        if (s != null) s.close();
    }
}
