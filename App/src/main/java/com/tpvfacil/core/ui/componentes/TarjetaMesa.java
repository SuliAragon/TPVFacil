package com.tpvfacil.core.ui.componentes;

import com.tpvfacil.hosteleria.modelo.EstadoMesa;
import com.tpvfacil.hosteleria.modelo.Mesa;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Componente visual que representa una mesa en el FlowPane de hostelería.
 * Muestra nombre, zona, capacidad, estado (color) y tiempo abierta.
 * El color de fondo cambia según el EstadoMesa:
 *   LIBRE → verde (#27AE60), OCUPADA → rojo (#E74C3C), PENDIENTE_PAGO → naranja (#F39C12).
 */
public class TarjetaMesa extends StackPane {

    private static final String COLOR_LIBRE = "#27AE60";
    private static final String COLOR_OCUPADA = "#E74C3C";
    private static final String COLOR_PENDIENTE = "#F39C12";

    private final Mesa mesa;
    private final Label lblNombre;
    private final Label lblZona;
    private final Label lblCapacidad;
    private final Label lblTiempo;
    private Timeline timeline;
    private LocalDateTime horaApertura;

    /** Crea una tarjeta visual para la mesa indicada. */
    public TarjetaMesa(Mesa mesa, LocalDateTime horaApertura) {
        this.mesa = mesa;
        this.horaApertura = horaApertura;

        setPrefSize(160, 130);
        setMinSize(140, 110);
        setCursor(Cursor.HAND);
        setPadding(new Insets(10));

        VBox contenido = new VBox(5);
        contenido.setAlignment(Pos.CENTER);

        lblNombre = new Label(mesa.getNombre());
        lblNombre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        lblZona = new Label(mesa.getZona());
        lblZona.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.85);");

        lblCapacidad = new Label("👤 " + mesa.getCapacidad());
        lblCapacidad.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.85);");

        lblTiempo = new Label();
        lblTiempo.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold;");

        contenido.getChildren().addAll(lblNombre, lblZona, lblCapacidad, lblTiempo);
        getChildren().add(contenido);

        actualizarEstilo();
        actualizarTiempo();

        // Actualizar tiempo cada 60 segundos
        if (mesa.getEstado() != EstadoMesa.LIBRE) {
            timeline = new Timeline(new KeyFrame(Duration.seconds(60), e -> actualizarTiempo()));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }
    }

    /** Devuelve la mesa asociada a esta tarjeta. */
    public Mesa getMesa() {
        return mesa;
    }

    /** Actualiza la hora de apertura (para refrescos desde BD). */
    public void setHoraApertura(LocalDateTime horaApertura) {
        this.horaApertura = horaApertura;
        actualizarTiempo();
    }

    /** Aplica el estilo visual según el estado de la mesa. */
    private void actualizarEstilo() {
        String color;
        switch (mesa.getEstado()) {
            case OCUPADA -> color = COLOR_OCUPADA;
            case PENDIENTE_PAGO -> color = COLOR_PENDIENTE;
            default -> color = COLOR_LIBRE;
        }

        setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 3);",
                color
        ));
    }

    /** Calcula y muestra el tiempo transcurrido desde la apertura. */
    private void actualizarTiempo() {
        if (mesa.getEstado() == EstadoMesa.LIBRE || horaApertura == null) {
            lblTiempo.setText("");
            return;
        }

        long minutos = ChronoUnit.MINUTES.between(horaApertura, LocalDateTime.now());
        long horas = minutos / 60;
        long mins = minutos % 60;

        if (horas > 0) {
            lblTiempo.setText(String.format("⏱ %dh %02dm", horas, mins));
        } else {
            lblTiempo.setText(String.format("⏱ %dm", mins));
        }
    }

    /** Detiene el timeline al destruir el componente. */
    public void detener() {
        if (timeline != null) {
            timeline.stop();
        }
    }
}
