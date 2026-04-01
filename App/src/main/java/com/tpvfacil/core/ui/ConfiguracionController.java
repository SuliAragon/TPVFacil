package com.tpvfacil.core.ui;

import com.tpvfacil.config.ConfiguracionManager;
import com.tpvfacil.licencia.HardwareFingerprint;
import com.tpvfacil.licencia.LicenciaManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Controlador de la pantalla de configuración permanente (4 tabs).
 * Accesible desde el botón ⚙ en la pantalla de inicio.
 */
public class ConfiguracionController {

    // Tab 1: Datos del negocio
    @FXML private TextField campoNombreNegocio;
    @FXML private TextField campoNif;
    @FXML private TextField campoDireccion;
    @FXML private Label labelGuardadoNegocio;

    // Tab 2: Verifactu
    @FXML private TextField campoRutaCertificado;
    @FXML private PasswordField campoPasswordCertificado;
    @FXML private Label labelEstadoCertificado;

    // Tab 3: Impresión
    @FXML private TextField campoImpresoraTickets;
    @FXML private TextField campoImpresoraCocina;
    @FXML private RadioButton radio58mm;
    @FXML private RadioButton radio80mm;
    @FXML private Label labelEstadoImpresion;

    // Tab 4: Licencia
    @FXML private Label labelEstadoLicencia;
    @FXML private Label labelFingerprint;
    @FXML private TextField campoClaveLicencia;
    @FXML private Label labelResultadoActivacion;

    @FXML
    public void initialize() {
        cargarDatosNegocio();
        cargarDatosVerifactu();
        cargarDatosImpresion();
        cargarDatosLicencia();
    }

    // --- Tab 1: Negocio ---

    private void cargarDatosNegocio() {
        ConfiguracionManager cfg = ConfiguracionManager.getInstance();
        campoNombreNegocio.setText(cfg.getNombreNegocio());
        campoNif.setText(cfg.getNif());
        campoDireccion.setText(cfg.getDireccion());
    }

    @FXML
    private void guardarNegocio() {
        ConfiguracionManager cfg = ConfiguracionManager.getInstance();
        String nombre = campoNombreNegocio.getText().trim();
        String nif = campoNif.getText().trim().toUpperCase();
        String dir = campoDireccion.getText().trim();

        if (nombre.isBlank() || nif.isBlank() || dir.isBlank()) {
            labelGuardadoNegocio.setText("Todos los campos son obligatorios.");
            labelGuardadoNegocio.setStyle("-fx-text-fill: #E74C3C;");
            return;
        }

        cfg.set(ConfiguracionManager.NOMBRE_NEGOCIO, nombre);
        cfg.set(ConfiguracionManager.NIF, nif);
        cfg.set(ConfiguracionManager.DIRECCION, dir);
        labelGuardadoNegocio.setText("✓ Guardado correctamente");
        labelGuardadoNegocio.setStyle("-fx-text-fill: #27AE60;");
    }

    // --- Tab 2: Verifactu ---

    private void cargarDatosVerifactu() {
        ConfiguracionManager cfg = ConfiguracionManager.getInstance();
        campoRutaCertificado.setText(cfg.getRutaCertificado());
        String estado = cfg.getRutaCertificado().isBlank()
                ? "Sin certificado configurado" : "Certificado configurado";
        labelEstadoCertificado.setText(estado);
    }

    @FXML
    private void seleccionarCertificado() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar certificado digital");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Certificado (.p12, .pfx)", "*.p12", "*.pfx"));
        File archivo = fc.showOpenDialog(campoRutaCertificado.getScene().getWindow());
        if (archivo != null) {
            campoRutaCertificado.setText(archivo.getAbsolutePath());
        }
    }

    @FXML
    private void guardarVerifactu() {
        ConfiguracionManager cfg = ConfiguracionManager.getInstance();
        String ruta = campoRutaCertificado.getText().trim();
        String pwd = campoPasswordCertificado.getText();

        if (!ruta.isBlank()) {
            cfg.set(ConfiguracionManager.RUTA_CERTIFICADO, ruta);
        }
        if (!pwd.isBlank()) {
            cfg.set(ConfiguracionManager.PASSWORD_CERTIFICADO, pwd);
        }
        labelEstadoCertificado.setText("✓ Guardado correctamente");
        labelEstadoCertificado.setStyle("-fx-text-fill: #27AE60;");
    }

    @FXML
    private void testSandbox() {
        // Se implementa en Fase 2 con VerifactuManager
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Test Sandbox");
        alert.setHeaderText(null);
        alert.setContentText("Test de Verifactu sandbox — disponible en Fase 2.");
        alert.initOwner(campoRutaCertificado.getScene().getWindow());
        alert.showAndWait();
    }

    // --- Tab 3: Impresión ---

    private void cargarDatosImpresion() {
        ConfiguracionManager cfg = ConfiguracionManager.getInstance();
        campoImpresoraTickets.setText(cfg.get(ConfiguracionManager.IMPRESORA_TICKETS, ""));
        campoImpresoraCocina.setText(cfg.get(ConfiguracionManager.IMPRESORA_COCINA, ""));
        int ancho = cfg.getAnchoPapelMm();
        if (ancho == 58) {
            radio58mm.setSelected(true);
        } else {
            radio80mm.setSelected(true);
        }
    }

    @FXML
    private void guardarImpresion() {
        ConfiguracionManager cfg = ConfiguracionManager.getInstance();
        cfg.set(ConfiguracionManager.IMPRESORA_TICKETS, campoImpresoraTickets.getText().trim());
        cfg.set(ConfiguracionManager.IMPRESORA_COCINA, campoImpresoraCocina.getText().trim());
        cfg.set(ConfiguracionManager.ANCHO_PAPEL_MM, radio58mm.isSelected() ? "58" : "80");
        labelEstadoImpresion.setText("✓ Guardado correctamente");
        labelEstadoImpresion.setStyle("-fx-text-fill: #27AE60;");
    }

    @FXML
    private void imprimirTicketPrueba() {
        // Se implementa en Fase 2/3 con el módulo de impresión
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Impresión de prueba");
        alert.setHeaderText(null);
        alert.setContentText("Impresión de prueba — disponible en Fase 2.");
        alert.initOwner(campoImpresoraTickets.getScene().getWindow());
        alert.showAndWait();
    }

    // --- Tab 4: Licencia ---

    private void cargarDatosLicencia() {
        String fp = HardwareFingerprint.generar();
        labelFingerprint.setText(fp);

        if (LicenciaManager.isModoDemo()) {
            labelEstadoLicencia.setText("⚠ MODO DEMO (sin licencia)");
            labelEstadoLicencia.setStyle("-fx-text-fill: #E67E22; -fx-font-weight: bold;");
        } else {
            labelEstadoLicencia.setText("✓ LICENCIA ACTIVADA");
            labelEstadoLicencia.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void copiarFingerprint() {
        ClipboardContent content = new ClipboardContent();
        content.putString(labelFingerprint.getText());
        Clipboard.getSystemClipboard().setContent(content);
        labelResultadoActivacion.setText("ID copiado al portapapeles");
        labelResultadoActivacion.setStyle("-fx-text-fill: #27AE60;");
    }

    @FXML
    private void activarLicencia() {
        String clave = campoClaveLicencia.getText().trim();
        if (clave.isBlank()) {
            labelResultadoActivacion.setText("Introduce una clave de licencia.");
            labelResultadoActivacion.setStyle("-fx-text-fill: #E74C3C;");
            return;
        }
        if (LicenciaManager.activar(clave)) {
            labelResultadoActivacion.setText("¡Licencia activada correctamente! Reinicia la aplicación.");
            labelResultadoActivacion.setStyle("-fx-text-fill: #27AE60;");
            cargarDatosLicencia();
        } else {
            labelResultadoActivacion.setText("Clave incorrecta para este equipo.");
            labelResultadoActivacion.setStyle("-fx-text-fill: #E74C3C;");
        }
    }

    @FXML
    private void cerrar() {
        Stage stage = (Stage) campoNombreNegocio.getScene().getWindow();
        stage.close();
    }
}
