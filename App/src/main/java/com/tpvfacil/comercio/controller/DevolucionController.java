package com.tpvfacil.comercio.controller;

import com.tpvfacil.comercio.service.StockService;
import com.tpvfacil.core.db.FacturaRepository;
import com.tpvfacil.core.modelo.Factura;
import com.tpvfacil.core.modelo.FormaPago;
import com.tpvfacil.core.modelo.LineaFactura;
import com.tpvfacil.verifactu.VerifactuManager;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador del modal de devoluciones.
 * Busca una factura original, permite seleccionar líneas a devolver
 * y genera una Factura Rectificativa R1 con importes negativos.
 */
public class DevolucionController {

    private static final Logger log = LoggerFactory.getLogger(DevolucionController.class);

    @FXML private TextField txtBuscadorFactura;
    @FXML private TableView<LineaFactura> tablaLineas;
    @FXML private TableColumn<LineaFactura, Boolean> colSelect;
    @FXML private TableColumn<LineaFactura, String> colProducto;
    @FXML private TableColumn<LineaFactura, String> colCantidad;
    @FXML private TableColumn<LineaFactura, String> colTotal;

    @FXML private Label lblTotalDevolver;
    @FXML private ComboBox<FormaPago> cmbFormaPago;
    @FXML private Button btnConfirmar;

    private final FacturaRepository facturaRepository = new FacturaRepository();
    private final StockService stockService = new StockService();

    private Factura facturaOriginal;
    private final ObservableList<LineaFactura> lineasOriginales = FXCollections.observableArrayList();
    private final Map<LineaFactura, SimpleBooleanProperty> seleccionMap = new HashMap<>();

    @FXML
    public void initialize() {
        cmbFormaPago.setItems(FXCollections.observableArrayList(FormaPago.EFECTIVO, FormaPago.TARJETA));
        cmbFormaPago.getSelectionModel().select(FormaPago.EFECTIVO);

        colProducto.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getDescripcion()));
        colCantidad.setCellValueFactory(v -> new SimpleStringProperty(String.format("%.0f", v.getValue().getCantidad())));
        colTotal.setCellValueFactory(v -> new SimpleStringProperty(
                String.format("%.2f €", v.getValue().getSubtotal() * (1 + v.getValue().getIvaPorcentaje() / 100.0))));

        colSelect.setCellValueFactory(param -> {
            LineaFactura linea = param.getValue();
            return seleccionMap.computeIfAbsent(linea, k -> {
                SimpleBooleanProperty bp = new SimpleBooleanProperty(false);
                bp.addListener((obs, oldV, newV) -> recalcularTotalDevolver());
                return bp;
            });
        });

        colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));
        tablaLineas.setEditable(true);
        tablaLineas.setItems(lineasOriginales);
    }

    /** Busca una factura por su número para iniciar el proceso de devolución. */
    @FXML
    private void buscarFactura() {
        try {
            int numeroFactura = Integer.parseInt(txtBuscadorFactura.getText().trim());
            Optional<Factura> fOpt = facturaRepository.findAll().stream()
                    .filter(f -> f.getNumero() == numeroFactura && !f.isAnulada())
                    .findFirst();

            if (fOpt.isPresent()) {
                facturaOriginal = facturaRepository.findById(fOpt.get().getId()).orElse(null);
                if (facturaOriginal != null && facturaOriginal.getLineas() != null) {
                    seleccionMap.clear();
                    lineasOriginales.setAll(facturaOriginal.getLineas());
                    recalcularTotalDevolver();
                    log.info("Factura encontrada para devolución: {}", facturaOriginal.getNumeroCompleto());
                }
            } else {
                new Alert(Alert.AlertType.WARNING, "No se encontró la factura número " + numeroFactura).show();
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Introduzca un número de factura válido.").show();
        }
    }

    private void recalcularTotalDevolver() {
        double total = 0;
        for (LineaFactura l : lineasOriginales) {
            SimpleBooleanProperty prop = seleccionMap.get(l);
            if (prop != null && prop.get()) {
                total += l.getSubtotal() * (1 + l.getIvaPorcentaje() / 100.0);
            }
        }
        lblTotalDevolver.setText(String.format("-%.2f €", total));
        btnConfirmar.setDisable(total == 0);
    }

    /** Confirma la devolución, creando una Factura Rectificativa R1. */
    @FXML
    private void confirmarDevolucion() {
        if (facturaOriginal == null) return;

        List<LineaFactura> lineasDevueltas = new ArrayList<>();
        double baseDevuelta = 0;
        double ivaDevuelto = 0;
        double totalDevuelto = 0;

        for (LineaFactura l : lineasOriginales) {
            SimpleBooleanProperty prop = seleccionMap.get(l);
            if (prop != null && prop.get()) {
                LineaFactura rect = new LineaFactura();
                rect.setProductoId(l.getProductoId());
                rect.setDescripcion(l.getDescripcion());
                rect.setCantidad(-l.getCantidad());
                rect.setPrecioUnitario(l.getPrecioUnitario());
                rect.setIvaPorcentaje(l.getIvaPorcentaje());

                double subTotalRect = -l.getSubtotal();
                double ivaRect = subTotalRect * (l.getIvaPorcentaje() / 100.0);
                rect.setSubtotal(subTotalRect);

                baseDevuelta += subTotalRect;
                ivaDevuelto += ivaRect;
                totalDevuelto += subTotalRect + ivaRect;

                lineasDevueltas.add(rect);

                // Reponer stock de la línea devuelta
                if (l.getProductoId() > 0) {
                    stockService.reponerStock(l.getProductoId(), (int) l.getCantidad());
                }
            }
        }

        if (lineasDevueltas.isEmpty()) return;

        // Crear Factura Rectificativa R1
        Factura fR1 = new Factura();
        fR1.setSerie("R" + facturaOriginal.getSerie());
        fR1.setNumero(facturaRepository.siguienteNumero("R" + facturaOriginal.getSerie()));
        fR1.setFecha(LocalDateTime.now());
        fR1.setClienteId(facturaOriginal.getClienteId());
        fR1.setTipoNegocio(facturaOriginal.getTipoNegocio());
        fR1.setFormaPago(cmbFormaPago.getValue());
        fR1.setEfectivoEntregado(totalDevuelto);
        fR1.setBaseImponible(baseDevuelta);
        fR1.setCuotaIva(ivaDevuelto);
        fR1.setTotal(totalDevuelto);
        fR1.setLineas(lineasDevueltas);
        fR1.setAnulada(false);

        facturaRepository.save(fR1);
        VerifactuManager.getInstance().procesarFactura(fR1);

        log.info("Devolución completada: {} — Rectifica factura: {}",
                fR1.getNumeroCompleto(), facturaOriginal.getNumeroCompleto());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Devolución Completada");
        alert.setHeaderText("Abono procesado");
        alert.setContentText(String.format("Factura Rectificativa: %s%nRectifica factura: %s%nTotal: %.2f €",
                fR1.getNumeroCompleto(), facturaOriginal.getNumeroCompleto(), totalDevuelto));
        alert.showAndWait();

        cancelar();
    }

    @FXML
    private void cancelar() {
        Stage s = (Stage) txtBuscadorFactura.getScene().getWindow();
        if (s != null) s.close();
    }
}
