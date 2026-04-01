package com.tpvfacil.licencia;

import com.tpvfacil.config.ConfiguracionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;

/**
 * Controlador de la pantalla de activación de licencia.
 * Muestra el ID de hardware del equipo y permite introducir la clave de licencia.
 */
public class ActivacionController {

    @FXML private Label labelFingerprint;
    @FXML private TextField campoClaveLinea1;
    @FXML private TextField campoClaveLinea2;
    @FXML private TextField campoClaveLinea3;
    @FXML private TextField campoClaveLine4;
    @FXML private TextField campoClave;
    @FXML private Label labelResultado;
    @FXML private Button btnActivar;

    private String fingerprint;

    @FXML
    public void initialize() {
        fingerprint = HardwareFingerprint.generar();
        labelFingerprint.setText(fingerprint);
    }

    /** Copia el fingerprint al portapapeles. */
    @FXML
    private void copiarFingerprint() {
        ClipboardContent content = new ClipboardContent();
        content.putString(fingerprint);
        Clipboard.getSystemClipboard().setContent(content);
        mostrarInfo("ID copiado al portapapeles");
    }

    /** Intenta activar la licencia con la clave introducida. */
    @FXML
    private void activar() {
        String clave = campoClave.getText().trim().toUpperCase();

        if (clave.isBlank()) {
            mostrarError("Introduce una clave de licencia.");
            return;
        }

        if (LicenciaManager.activar(clave)) {
            mostrarInfo("¡Licencia activada correctamente! Reinicia la aplicación.");
            btnActivar.setDisable(true);
            // Cerrar ventana tras un breve retraso
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> cerrar());
            }).start();
        } else {
            mostrarError("Clave incorrecta para este equipo. Verifica que el ID es correcto.");
            campoClave.selectAll();
            campoClave.requestFocus();
        }
    }

    /** Abre el navegador en la URL de compra del software. */
    @FXML
    private void irAComprar() {
        String url = ConfiguracionManager.getInstance().getWebUrlCompra();
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            mostrarInfo("Visita: " + url);
        }
    }

    /** Cierra esta ventana. */
    @FXML
    private void cerrar() {
        Stage stage = (Stage) labelFingerprint.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String mensaje) {
        labelResultado.setText(mensaje);
        labelResultado.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
    }

    private void mostrarInfo(String mensaje) {
        labelResultado.setText(mensaje);
        labelResultado.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
    }
}
