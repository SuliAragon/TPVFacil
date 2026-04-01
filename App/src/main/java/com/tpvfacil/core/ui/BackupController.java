package com.tpvfacil.core.ui;

import com.tpvfacil.core.util.BackupManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class BackupController {

    @FXML private TableView<BackupManager.BackupInfo> tablaBackups;
    @FXML private TableColumn<BackupManager.BackupInfo, String> colNombre;
    @FXML private TableColumn<BackupManager.BackupInfo, String> colSize;
    @FXML private TableColumn<BackupManager.BackupInfo, Void> colAcciones;

    private BackupManager backupManager = new BackupManager();
    private ObservableList<BackupManager.BackupInfo> backupsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getFileName()));
        colSize.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getFormattedSize()));
        
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnRestaurar = new Button("Restaurar Este");
            {
                btnRestaurar.setOnAction(event -> {
                    BackupManager.BackupInfo info = getTableView().getItems().get(getIndex());
                    restaurarBackupFile(new File(info.getFullPath()));
                });
                btnRestaurar.setStyle("-fx-base: #F39C12; -fx-text-fill: white; -fx-font-size: 10px;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnRestaurar);
            }
        });

        recargarBackups();
    }

    private void recargarBackups() {
        backupsData.setAll(backupManager.listarBackupsDisponibles());
        tablaBackups.setItems(backupsData);
    }

    @FXML
    private void exportarManual(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar Copia de Seguridad");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite Database", "*.db"));
        chooser.setInitialFileName("backup_manual_" + System.currentTimeMillis() + ".db");
        
        File file = chooser.showSaveDialog(tablaBackups.getScene().getWindow());
        if (file != null) {
            try {
                backupManager.exportarBackup(file.toPath());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Copia exportada");
                alert.setHeaderText(null);
                alert.setContentText("Copia de seguridad exportada en:\n" + file.getAbsolutePath());
                alert.showAndWait();
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Error al exportar la base de datos: " + e.getMessage()).show();
            }
        }
    }

    @FXML
    private void restaurarDesdeArchivo(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar Copia de Seguridad");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite Database", "*.db"));
        
        File file = chooser.showOpenDialog(tablaBackups.getScene().getWindow());
        if (file != null) {
            restaurarBackupFile(file);
        }
    }

    private void restaurarBackupFile(File file) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("⚠ ADVERTENCIA CRÍTICA");
        alert.setHeaderText("Vas a restaurar una copia de seguridad");
        alert.setContentText("Esto reemplazará TODOS los datos actuales con los del archivo seleccionado (" + file.getName() + ").\n\n¿Estás completamente seguro?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = backupManager.restaurarBackup(file.toPath());
            if (success) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText(null);
                ok.setContentText("Copia de seguridad restaurada correctamente.\nReinicie la aplicación si nota comportamientos extraños.");
                ok.showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, "Hubo un error al restaurar la copia de seguridad.").show();
            }
        }
    }
}
