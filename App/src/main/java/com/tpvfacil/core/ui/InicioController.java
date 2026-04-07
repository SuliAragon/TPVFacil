package com.tpvfacil.core.ui;

import com.tpvfacil.Main;
import com.tpvfacil.licencia.LicenciaManager;
import com.tpvfacil.licencia.ModoDemo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Controlador de la pantalla principal de inicio.
 * Muestra los dos módulos (Hostelería / Comercio) y el banner demo si aplica.
 */
public class InicioController {

    @FXML private HBox bannerDemo;
    @FXML private Label labelTicketsDemo;

    @FXML
    public void initialize() {
        actualizarBannerDemo();
    }

    /** Navega al TPV de Hostelería. */
    @FXML
    private void abrirHosteleria() {
        try {
            java.net.URL fxml = getClass().getResource("/com/tpvfacil/pantalla-mesas.fxml");
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxml);
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            javafx.stage.Stage stage = Main.getStagePrincipal();

            java.net.URL css = getClass().getResource("/com/tpvfacil/estilos.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setTitle("TPVFácil — Hostelería");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Navega al TPV de Comercio. */
    @FXML
    private void abrirComercio() {
        try {
            java.net.URL fxml = getClass().getResource("/com/tpvfacil/pantalla-venta.fxml");
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxml);
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            javafx.stage.Stage stage = Main.getStagePrincipal();
            
            // Add stylesheet if exists
            java.net.URL css = getClass().getResource("/com/tpvfacil/estilos.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setTitle("TPVFácil — Comercio");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarProximamente("Error al cargar el módulo de comercio: " + e.getMessage());
        }
    }

    /** Abre la pantalla de activación de licencia. */
    @FXML
    private void abrirActivacion() {
        try {
            URL fxml = getClass().getResource("/com/tpvfacil/activacion.fxml");
            FXMLLoader loader = new FXMLLoader(fxml);
            Scene scene = new Scene(loader.load(), 480, 400);
            Stage ventana = new Stage();
            ventana.setTitle("Activar TPVFácil");
            ventana.setScene(scene);
            ventana.initModality(Modality.APPLICATION_MODAL);
            ventana.initOwner(Main.getStagePrincipal());
            ventana.setResizable(false);
            ventana.showAndWait();
            // Actualizar banner tras posible activación
            actualizarBannerDemo();
            Main.actualizarTituloVentana(Main.getStagePrincipal());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Abre la pantalla de configuración. */
    @FXML
    private void abrirConfiguracion() {
        try {
            URL fxml = getClass().getResource("/com/tpvfacil/pantalla-configuracion.fxml");
            FXMLLoader loader = new FXMLLoader(fxml);
            Scene scene = new Scene(loader.load(), 600, 500);
            Stage ventana = new Stage();
            ventana.setTitle("Configuración — TPVFácil");
            ventana.setScene(scene);
            ventana.initModality(Modality.APPLICATION_MODAL);
            ventana.initOwner(Main.getStagePrincipal());
            ventana.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actualizarBannerDemo() {
        if (LicenciaManager.isModoDemo()) {
            bannerDemo.setVisible(true);
            bannerDemo.setManaged(true);
            int emitidos = ModoDemo.ticketsEmitidosHoy();
            int maximos = ModoDemo.MAX_TICKETS_DIA;
            labelTicketsDemo.setText("⚠ MODO DEMO — Tickets hoy: " + emitidos + "/" + maximos);

            if (emitidos >= maximos) {
                bannerDemo.setStyle("-fx-background-color: #E74C3C; -fx-padding: 8 16;");
            } else {
                bannerDemo.setStyle("-fx-background-color: #F39C12; -fx-padding: 8 16;");
            }
        } else {
            bannerDemo.setVisible(false);
            bannerDemo.setManaged(false);
        }
    }

    private void mostrarProximamente(String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("TPVFácil");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.initOwner(Main.getStagePrincipal());
        alert.showAndWait();
    }
}
