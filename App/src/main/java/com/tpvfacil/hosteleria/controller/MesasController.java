package com.tpvfacil.hosteleria.controller;

import com.tpvfacil.Main;
import com.tpvfacil.core.db.ComandaRepository;
import com.tpvfacil.core.db.MesaRepository;
import com.tpvfacil.core.modelo.TipoNegocio;
import com.tpvfacil.core.ui.CierreCajaController;
import com.tpvfacil.core.ui.componentes.TarjetaMesa;
import com.tpvfacil.hosteleria.modelo.Comanda;
import com.tpvfacil.hosteleria.modelo.EstadoMesa;
import com.tpvfacil.hosteleria.modelo.Mesa;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de la pantalla principal de mesas del módulo Hostelería.
 * Muestra un grid de TarjetaMesa con refresco automático cada 30 segundos.
 */
public class MesasController {

    private static final Logger log = LoggerFactory.getLogger(MesasController.class);

    @FXML private FlowPane mesasPane;

    private final MesaRepository mesaRepository = new MesaRepository();
    private final ComandaRepository comandaRepository = new ComandaRepository();
    private final List<TarjetaMesa> tarjetas = new ArrayList<>();
    private Timeline refreshTimeline;

    @FXML
    public void initialize() {
        cargarMesas();
        iniciarAutoRefresh();
    }

    /** Carga todas las mesas activas y crea sus tarjetas visuales. */
    private void cargarMesas() {
        // Detener timelines anteriores
        for (TarjetaMesa t : tarjetas) t.detener();
        tarjetas.clear();
        mesasPane.getChildren().clear();

        List<Mesa> mesas = mesaRepository.findAll();
        for (Mesa m : mesas) {
            // Buscar hora de apertura de comanda activa (si existe)
            java.time.LocalDateTime horaApertura = null;
            if (m.getEstado() != EstadoMesa.LIBRE) {
                Optional<Comanda> cmd = comandaRepository.findAbiertaByMesaId(m.getId());
                if (cmd.isPresent()) {
                    horaApertura = cmd.get().getFechaApertura();
                }
            }

            TarjetaMesa tarjeta = new TarjetaMesa(m, horaApertura);
            tarjeta.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    onClickMesa(m);
                }
            });

            // Menú contextual (clic derecho)
            ContextMenu ctx = crearMenuContextual(m);
            tarjeta.setOnContextMenuRequested(e ->
                    ctx.show(tarjeta, e.getScreenX(), e.getScreenY()));

            tarjetas.add(tarjeta);
            mesasPane.getChildren().add(tarjeta);
        }
    }

    /** Inicia el Timeline de refresco automático cada 30 segundos. */
    private void iniciarAutoRefresh() {
        refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> cargarMesas())
        );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    /** Maneja el clic en una mesa según su estado. */
    private void onClickMesa(Mesa mesa) {
        switch (mesa.getEstado()) {
            case LIBRE -> abrirComanda(mesa);
            case OCUPADA -> abrirComandaExistente(mesa);
            case PENDIENTE_PAGO -> abrirCobro(mesa);
        }
    }

    /** Abre una nueva comanda para una mesa libre. */
    private void abrirComanda(Mesa mesa) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tpvfacil/pantalla-comanda.fxml"));
            Scene scene = new Scene(loader.load(), 950, 650);
            ComandaController ctrl = loader.getController();
            ctrl.setMesa(mesa);

            Stage stage = new Stage();
            stage.setTitle("Comanda — " + mesa.getNombre() + " — TPVFácil");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarMesas();
        } catch (Exception e) {
            log.error("Error al abrir comanda para mesa: {}", mesa.getNombre(), e);
        }
    }

    /** Abre la comanda activa de una mesa ocupada. */
    private void abrirComandaExistente(Mesa mesa) {
        abrirComanda(mesa); // El ComandaController detecta si hay comanda abierta
    }

    /** Abre directamente la pantalla de cobro para una mesa pendiente de pago. */
    private void abrirCobro(Mesa mesa) {
        Optional<Comanda> cmdOpt = comandaRepository.findAbiertaByMesaId(mesa.getId());
        if (cmdOpt.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No se encontró comanda activa para esta mesa.").show();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tpvfacil/pantalla-cobro-hosteleria.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            CobroHosteleriaController ctrl = loader.getController();
            ctrl.setComandaYMesa(cmdOpt.get(), mesa);

            Stage stage = new Stage();
            stage.setTitle("Cobro — " + mesa.getNombre() + " — TPVFácil");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarMesas();
        } catch (Exception e) {
            log.error("Error al abrir cobro para mesa: {}", mesa.getNombre(), e);
        }
    }

    /** Crea el menú contextual para CRUD de mesas. */
    private ContextMenu crearMenuContextual(Mesa mesa) {
        ContextMenu menu = new ContextMenu();

        MenuItem editar = new MenuItem("✏️ Editar mesa");
        editar.setOnAction(e -> abrirDialogoMesa(mesa));

        MenuItem eliminar = new MenuItem("🗑 Eliminar mesa");
        eliminar.setOnAction(e -> {
            if (mesa.getEstado() != EstadoMesa.LIBRE) {
                new Alert(Alert.AlertType.WARNING, "No se puede eliminar una mesa ocupada.").show();
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar " + mesa.getNombre() + "?");
            confirm.showAndWait().ifPresent(bt -> {
                if (bt.getButtonData().isDefaultButton()) {
                    mesaRepository.deleteById(mesa.getId());
                    cargarMesas();
                }
            });
        });

        menu.getItems().addAll(editar, eliminar);
        return menu;
    }

    /** Abre el modal para añadir una mesa nueva. */
    @FXML
    private void abrirNuevaMesa() {
        abrirDialogoMesa(null);
    }

    /** Abre el diálogo modal de CRUD de mesa. */
    private void abrirDialogoMesa(Mesa mesa) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tpvfacil/dialogo-mesa.fxml"));
            Scene scene = new Scene(loader.load(), 400, 300);
            DialogoMesaController ctrl = loader.getController();
            if (mesa != null) ctrl.setMesa(mesa);

            Stage stage = new Stage();
            stage.setTitle(mesa == null ? "Nueva Mesa" : "Editar Mesa");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarMesas();
        } catch (Exception e) {
            log.error("Error al abrir diálogo de mesa", e);
        }
    }

    /** Abre la gestión de carta (productos). */
    @FXML
    private void abrirGestionCarta() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tpvfacil/pantalla-gestion-carta.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            Stage stage = new Stage();
            stage.setTitle("Gestión de Carta — TPVFácil");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            log.error("Error al abrir gestión de carta", e);
        }
    }

    /** Abre la pantalla de cierre de caja filtrada por hostelería. */
    @FXML
    private void abrirCierreCaja() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tpvfacil/pantalla-cierre-caja.fxml"));
            Scene scene = new Scene(loader.load(), 650, 550);
            CierreCajaController ctrl = loader.getController();
            ctrl.setTipoNegocio(TipoNegocio.HOSTELERIA);
            Stage stage = new Stage();
            stage.setTitle("Cierre de Caja — Hostelería");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            log.error("Error al abrir cierre de caja", e);
        }
    }

    /** Vuelve a la pantalla de inicio. */
    @FXML
    private void volverInicio() {
        if (refreshTimeline != null) refreshTimeline.stop();
        for (TarjetaMesa t : tarjetas) t.detener();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tpvfacil/inicio.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = Main.getStagePrincipal();
            stage.setMaximized(false);
            stage.setScene(scene);
            Main.actualizarTituloVentana(stage);
        } catch (Exception e) {
            log.error("Error al volver al inicio", e);
        }
    }

    @FXML
    private void salir() {
        if (refreshTimeline != null) refreshTimeline.stop();
        Platform.exit();
    }
}
