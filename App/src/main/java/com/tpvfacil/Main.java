package com.tpvfacil;

import com.tpvfacil.config.ConfiguracionManager;
import com.tpvfacil.core.db.DatabaseManager;
import com.tpvfacil.licencia.LicenciaManager;
import com.tpvfacil.verifactu.VerifactuManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;

/**
 * Punto de entrada de la aplicación TPVFácil.
 * Orquesta el arranque: BD → Configuración → Licencia → Pantalla inicial.
 */
public class Main extends Application {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static Stage stagePrincipal;

    @Override
    public void start(Stage stage) throws Exception {
        stagePrincipal = stage;

        // 1. Inicializar la base de datos
        DatabaseManager.getInstance().inicializar();

        // 2. Cargar configuración
        ConfiguracionManager.getInstance().cargar();

        // 3. Comprobar licencia
        LicenciaManager.comprobar();

        // 4. Reenviar registros Verifactu pendientes al arrancar
        VerifactuManager.getInstance().reenviarPendientes();

        // 5. ¿Primera vez? → Wizard. Si no → Pantalla de inicio.
        if (!ConfiguracionManager.getInstance().isConfigurado()) {
            mostrarWizard(stage);
        } else {
            mostrarInicio(stage);
        }
    }

    /** Muestra el wizard de primera ejecución. */
    public static void mostrarWizard(Stage stage) throws Exception {
        URL fxml = Main.class.getResource("/com/tpvfacil/wizard-primer-arranque.fxml");
        FXMLLoader loader = new FXMLLoader(fxml);
        Scene scene = new Scene(loader.load(), 580, 480);
        aplicarEstilos(scene);
        stage.setScene(scene);
        stage.setTitle("TPVFácil — Configuración inicial");
        stage.setMinWidth(580);
        stage.setMinHeight(480);
        stage.setResizable(false);
        aplicarIcono(stage);
        stage.show();
    }

    /** Muestra la pantalla principal de inicio. */
    public static void mostrarInicio(Stage stage) throws Exception {
        URL fxml = Main.class.getResource("/com/tpvfacil/inicio.fxml");
        FXMLLoader loader = new FXMLLoader(fxml);
        Scene scene = new Scene(loader.load(), 800, 600);
        aplicarEstilos(scene);
        stage.setScene(scene);
        actualizarTituloVentana(stage);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setResizable(true);
        aplicarIcono(stage);
        stage.show();
    }

    /** Actualiza el título de la ventana según el modo (demo/completo). */
    public static void actualizarTituloVentana(Stage stage) {
        if (LicenciaManager.isModoDemo()) {
            stage.setTitle("TPVFácil [DEMO]");
        } else {
            stage.setTitle("TPVFácil");
        }
    }

    /** Devuelve el Stage principal para navegación entre pantallas. */
    public static Stage getStagePrincipal() {
        return stagePrincipal;
    }

    private static void aplicarEstilos(Scene scene) {
        URL css = Main.class.getResource("/com/tpvfacil/estilos.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }

    private static void aplicarIcono(Stage stage) {
        InputStream iconStream = Main.class.getResourceAsStream("/images/logo.png");
        if (iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
        }
    }

    @Override
    public void stop() {
        DatabaseManager.getInstance().cerrar();
        log.info("Aplicación cerrada.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
