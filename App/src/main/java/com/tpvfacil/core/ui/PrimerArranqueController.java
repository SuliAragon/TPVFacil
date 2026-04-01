package com.tpvfacil.core.ui;

import com.tpvfacil.Main;
import com.tpvfacil.config.ConfiguracionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Controlador del wizard de primera ejecución (3 pasos).
 * Paso 1: Datos del negocio (obligatorio)
 * Paso 2: Certificado Verifactu (opcional)
 * Paso 3: Impresoras (opcional)
 */
public class PrimerArranqueController {

    // Paso 1
    @FXML private StackPane paso1;
    @FXML private TextField campoNombreNegocio;
    @FXML private TextField campoNif;
    @FXML private TextField campoDireccion;
    @FXML private Label labelErrorPaso1;

    // Paso 2
    @FXML private StackPane paso2;
    @FXML private TextField campoRutaCertificado;
    @FXML private PasswordField campoPasswordCertificado;
    @FXML private Label labelErrorPaso2;

    // Paso 3
    @FXML private StackPane paso3;
    @FXML private TextField campoImpresoraTickets;
    @FXML private TextField campoImpresoraCocina;
    @FXML private ToggleGroup grupoPapel;
    @FXML private RadioButton radio58mm;
    @FXML private RadioButton radio80mm;

    // Indicadores de paso
    @FXML private Label indicador1;
    @FXML private Label indicador2;
    @FXML private Label indicador3;

    private int pasoActual = 1;

    @FXML
    public void initialize() {
        mostrarPaso(1);
    }

    @FXML
    private void siguiente() {
        if (pasoActual == 1) {
            if (!validarPaso1()) return;
            guardarPaso1();
            mostrarPaso(2);
        } else if (pasoActual == 2) {
            guardarPaso2();
            mostrarPaso(3);
        } else if (pasoActual == 3) {
            guardarPaso3();
            finalizarWizard();
        }
    }

    @FXML
    private void anterior() {
        if (pasoActual > 1) {
            mostrarPaso(pasoActual - 1);
        }
    }

    @FXML
    private void saltarPaso() {
        if (pasoActual == 2) {
            mostrarPaso(3);
        }
    }

    @FXML
    private void seleccionarCertificado() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar certificado digital");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Certificado (.p12, .pfx)", "*.p12", "*.pfx"));
        File archivo = fc.showOpenDialog(Main.getStagePrincipal());
        if (archivo != null) {
            campoRutaCertificado.setText(archivo.getAbsolutePath());
        }
    }

    private boolean validarPaso1() {
        String nombre = campoNombreNegocio.getText().trim();
        String nif = campoNif.getText().trim().toUpperCase();
        String dir = campoDireccion.getText().trim();

        if (nombre.isBlank()) {
            labelErrorPaso1.setText("El nombre del negocio es obligatorio.");
            return false;
        }
        if (nif.isBlank()) {
            labelErrorPaso1.setText("El NIF/CIF es obligatorio.");
            return false;
        }
        if (!validarNif(nif)) {
            labelErrorPaso1.setText("NIF/CIF no válido. Comprueba el dígito de control.");
            return false;
        }
        if (dir.isBlank()) {
            labelErrorPaso1.setText("La dirección es obligatoria.");
            return false;
        }
        labelErrorPaso1.setText("");
        return true;
    }

    private void guardarPaso1() {
        ConfiguracionManager cfg = ConfiguracionManager.getInstance();
        cfg.set(ConfiguracionManager.NOMBRE_NEGOCIO, campoNombreNegocio.getText().trim());
        cfg.set(ConfiguracionManager.NIF, campoNif.getText().trim().toUpperCase());
        cfg.set(ConfiguracionManager.DIRECCION, campoDireccion.getText().trim());
    }

    private void guardarPaso2() {
        String ruta = campoRutaCertificado.getText().trim();
        String pwd = campoPasswordCertificado.getText();
        if (!ruta.isBlank()) {
            ConfiguracionManager cfg = ConfiguracionManager.getInstance();
            cfg.set(ConfiguracionManager.RUTA_CERTIFICADO, ruta);
            if (!pwd.isBlank()) {
                cfg.set(ConfiguracionManager.PASSWORD_CERTIFICADO, pwd);
            }
        }
    }

    private void guardarPaso3() {
        ConfiguracionManager cfg = ConfiguracionManager.getInstance();
        if (!campoImpresoraTickets.getText().trim().isBlank()) {
            cfg.set(ConfiguracionManager.IMPRESORA_TICKETS, campoImpresoraTickets.getText().trim());
        }
        if (grupoPapel != null && grupoPapel.getSelectedToggle() != null) {
            String ancho = radio58mm.isSelected() ? "58" : "80";
            cfg.set(ConfiguracionManager.ANCHO_PAPEL_MM, ancho);
        }
    }

    private void finalizarWizard() {
        try {
            Stage stage = (Stage) campoNombreNegocio.getScene().getWindow();
            Main.mostrarInicio(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarPaso(int paso) {
        pasoActual = paso;
        paso1.setVisible(paso == 1);
        paso1.setManaged(paso == 1);
        paso2.setVisible(paso == 2);
        paso2.setManaged(paso == 2);
        paso3.setVisible(paso == 3);
        paso3.setManaged(paso == 3);

        // Actualizar indicadores
        actualizarIndicador(indicador1, paso, 1);
        actualizarIndicador(indicador2, paso, 2);
        actualizarIndicador(indicador3, paso, 3);
    }

    private void actualizarIndicador(Label label, int pasoActual, int numeroPaso) {
        if (label == null) return;
        if (pasoActual == numeroPaso) {
            label.setStyle("-fx-background-color: #1B4F8A; -fx-text-fill: white; " +
                    "-fx-background-radius: 50; -fx-min-width: 28; -fx-min-height: 28; " +
                    "-fx-alignment: CENTER; -fx-font-weight: bold;");
        } else if (pasoActual > numeroPaso) {
            label.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; " +
                    "-fx-background-radius: 50; -fx-min-width: 28; -fx-min-height: 28; " +
                    "-fx-alignment: CENTER; -fx-font-weight: bold;");
        } else {
            label.setStyle("-fx-background-color: #ddd; -fx-text-fill: #888; " +
                    "-fx-background-radius: 50; -fx-min-width: 28; -fx-min-height: 28; " +
                    "-fx-alignment: CENTER;");
        }
    }

    /**
     * Valida un NIF/CIF/NIE español.
     * Comprueba el formato y la letra de control.
     */
    private boolean validarNif(String nif) {
        if (nif == null || nif.isBlank()) return false;
        String letrasNif = "TRWAGMYFPDXBNJZSQVHLCKE";

        // NIF estándar: 8 dígitos + letra
        if (nif.matches("\\d{8}[A-Z]")) {
            int num = Integer.parseInt(nif.substring(0, 8));
            char letraEsperada = letrasNif.charAt(num % 23);
            return nif.charAt(8) == letraEsperada;
        }

        // NIE: X, Y, Z + 7 dígitos + letra
        if (nif.matches("[XYZ]\\d{7}[A-Z]")) {
            String temp = nif.replace('X', '0').replace('Y', '1').replace('Z', '2');
            int num = Integer.parseInt(temp.substring(0, 8));
            char letraEsperada = letrasNif.charAt(num % 23);
            return nif.charAt(8) == letraEsperada;
        }

        // CIF: letra + 7 dígitos + control (letra o dígito)
        if (nif.matches("[ABCDEFGHJKLMNPQRSUVW]\\d{7}[A-Z0-9]")) {
            return true; // Validación básica de formato
        }

        return false;
    }
}
