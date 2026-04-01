package com.tpvfacil.hosteleria.controller;

import com.tpvfacil.core.db.FacturaRepository;
import com.tpvfacil.core.modelo.Factura;
import com.tpvfacil.core.modelo.FormaPago;
import com.tpvfacil.core.modelo.LineaFactura;
import com.tpvfacil.core.modelo.TipoNegocio;
import com.tpvfacil.hosteleria.modelo.Comanda;
import com.tpvfacil.hosteleria.modelo.LineaComanda;
import com.tpvfacil.hosteleria.modelo.Mesa;
import com.tpvfacil.hosteleria.service.ComandaService;
import com.tpvfacil.licencia.ModoDemo;
import com.tpvfacil.verifactu.EstadoEnvio;
import com.tpvfacil.verifactu.RegistroFactura;
import com.tpvfacil.verifactu.VerifactuManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de la pantalla de cobro del módulo Hostelería.
 * Recibe una Comanda, construye la factura y la procesa con Verifactu.
 * Al confirmar: comanda → PAGADA, mesa → LIBRE.
 */
public class CobroHosteleriaController {

    private static final Logger log = LoggerFactory.getLogger(CobroHosteleriaController.class);

    @FXML private TableView<LineaComanda> tablaResumen;
    @FXML private TableColumn<LineaComanda, String> colDesc;
    @FXML private TableColumn<LineaComanda, String> colCant;
    @FXML private TableColumn<LineaComanda, String> colImporte;

    @FXML private Label lblBase;
    @FXML private Label lblIva;
    @FXML private Label lblTotal;

    @FXML private ToggleButton btnEfectivo;
    @FXML private ToggleButton btnTarjeta;
    @FXML private ToggleButton btnMixto;
    private ToggleGroup groupPago;

    @FXML private TextField txtEntregado;
    @FXML private Label lblCambio;

    @FXML private Label lblDivision;

    private final FacturaRepository facturaRepository = new FacturaRepository();
    private final ComandaService comandaService = new ComandaService();

    private Comanda comanda;
    private Mesa mesa;
    private double totalVenta;
    private double entregado;
    private boolean cobroCorrecto = false;

    @FXML
    public void initialize() {
        groupPago = new ToggleGroup();
        btnEfectivo.setToggleGroup(groupPago);
        btnTarjeta.setToggleGroup(groupPago);
        btnMixto.setToggleGroup(groupPago);

        txtEntregado.textProperty().addListener((obs, old, nev) -> calcularCambio());

        groupPago.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == btnTarjeta) {
                txtEntregado.setText(String.format("%.2f", totalVenta).replace(",", "."));
                txtEntregado.setDisable(true);
            } else {
                txtEntregado.setDisable(false);
            }
        });
    }

    /** Recibe la comanda y mesa para inicializar la pantalla de cobro. */
    public void setComandaYMesa(Comanda comanda, Mesa mesa) {
        this.comanda = comanda;
        this.mesa = mesa;

        colDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreProducto()));
        colCant.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.0f", d.getValue().getCantidad())));
        colImporte.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f €", d.getValue().getSubtotal())));

        ObservableList<LineaComanda> items = FXCollections.observableArrayList(comanda.getLineas());
        tablaResumen.setItems(items);

        // IVA: en hostelería, por defecto 10%
        double base = comanda.getLineas().stream().mapToDouble(LineaComanda::getSubtotal).sum();
        // Los precios ya incluyen IVA (getPrecioConIva), así que base = total, IVA ya incluido
        // Para mostrar desglose: total / 1.10 para obtener la base neta (simplificación)
        double ivaRate = 10.0; // Asumimos 10% general para hostelería
        double baseNeta = base / (1 + ivaRate / 100.0);
        double iva = base - baseNeta;
        totalVenta = base;

        lblBase.setText(String.format("%.2f €", baseNeta));
        lblIva.setText(String.format("%.2f € (%.0f%%)", iva, ivaRate));
        lblTotal.setText(String.format("%.2f €", totalVenta));

        txtEntregado.setText(String.format("%.2f", totalVenta).replace(",", "."));
    }

    public boolean isCobroCorrecto() {
        return cobroCorrecto;
    }

    @FXML
    private void addBillete5() { addBillete(5); }
    @FXML
    private void addBillete10() { addBillete(10); }
    @FXML
    private void addBillete20() { addBillete(20); }
    @FXML
    private void addBillete50() { addBillete(50); }

    private void addBillete(double valor) {
        try {
            double actual = Double.parseDouble(txtEntregado.getText().replace(",", "."));
            txtEntregado.setText(String.format("%.2f", actual + valor).replace(",", "."));
        } catch (NumberFormatException e) {
            txtEntregado.setText(String.format("%.2f", valor).replace(",", "."));
        }
    }

    private void calcularCambio() {
        try {
            entregado = Double.parseDouble(txtEntregado.getText().replace(",", "."));
            double cambio = entregado - totalVenta;
            if (cambio < 0) {
                lblCambio.setText("Falta saldo");
                lblCambio.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 24px;");
            } else {
                lblCambio.setText(String.format("%.2f €", cambio));
                lblCambio.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold; -fx-font-size: 24px;");
            }
        } catch (NumberFormatException e) {
            lblCambio.setText("0.00 €");
        }
    }

    /** Modal de división de cuenta: total / N personas. */
    @FXML
    private void dividirCuenta() {
        TextInputDialog dialog = new TextInputDialog("2");
        dialog.setTitle("División de Cuenta");
        dialog.setHeaderText("Dividir entre comensales");
        dialog.setContentText("Nº de personas:");
        dialog.showAndWait().ifPresent(input -> {
            try {
                int n = Integer.parseInt(input.trim());
                if (n > 0) {
                    double porPersona = totalVenta / n;
                    lblDivision.setText(String.format("÷%d = %.2f € por persona", n, porPersona));
                }
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.WARNING, "Introduzca un número válido.").show();
            }
        });
    }

    /** Confirma el cobro: crea factura, Verifactu, comanda PAGADA, mesa LIBRE. */
    @FXML
    private void confirmarCobro() {
        if (!ModoDemo.puedeEmitirTicket()) {
            new Alert(Alert.AlertType.WARNING, "Límite de tickets diarios alcanzado (modo demo).").show();
            return;
        }

        FormaPago formaPago = FormaPago.EFECTIVO;
        if (btnTarjeta.isSelected()) formaPago = FormaPago.TARJETA;
        else if (btnMixto.isSelected()) formaPago = FormaPago.MIXTO;

        double cambio = Math.max(0, entregado - totalVenta);

        // Construir factura de hostelería
        Factura factura = new Factura();
        factura.setSerie("H");
        factura.setNumero(facturaRepository.siguienteNumero("H"));
        factura.setFecha(LocalDateTime.now());
        factura.setTipoNegocio(TipoNegocio.HOSTELERIA);
        factura.setFormaPago(formaPago);
        factura.setEfectivoEntregado(entregado);
        factura.setCambio(cambio);

        // Convertir líneas de comanda a líneas de factura
        List<LineaFactura> lineasFactura = new ArrayList<>();
        double base = 0, cuota = 0;
        double ivaDefault = 10.0;

        for (LineaComanda lc : comanda.getLineas()) {
            LineaFactura lf = new LineaFactura();
            lf.setProductoId(lc.getProductoId());
            lf.setDescripcion(lc.getNombreProducto());
            lf.setCantidad(lc.getCantidad());

            // PrecioUnitario ya incluye IVA → extraer base
            double precioConIva = lc.getPrecioUnitario();
            double precioSinIva = precioConIva / (1 + ivaDefault / 100.0);
            lf.setPrecioUnitario(precioSinIva);
            lf.setIvaPorcentaje(ivaDefault);
            double subtotal = precioSinIva * lc.getCantidad();
            lf.setSubtotal(subtotal);

            base += subtotal;
            cuota += subtotal * (ivaDefault / 100.0);
            lineasFactura.add(lf);
        }

        factura.setBaseImponible(base);
        factura.setCuotaIva(cuota);
        factura.setTotal(base + cuota);
        factura.setLineas(lineasFactura);
        factura.setAnulada(false);

        facturaRepository.save(factura);

        // Verifactu
        RegistroFactura res = VerifactuManager.getInstance().procesarFactura(factura);

        // Marcar comanda como pagada y liberar mesa
        comandaService.marcarPagada(comanda, factura.getId());

        cobroCorrecto = true;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("¡Venta completada!");
        alert.setHeaderText(String.format("Cambio: %.2f €", cambio));
        if (res.getEstado() == EstadoEnvio.PENDIENTE) {
            alert.setContentText("⚠ Ticket emitido — Registro Verifactu pendiente.");
        } else {
            alert.setContentText(mesa.getNombre() + " — Mesa liberada.");
        }
        alert.showAndWait();

        log.info("Cobro hostelería completado: factura {} — total {} €", factura.getNumeroCompleto(), factura.getTotal());
        cerrar();
    }

    @FXML
    private void cancelar() {
        cerrar();
    }

    private void cerrar() {
        Stage s = (Stage) lblBase.getScene().getWindow();
        if (s != null) s.close();
    }
}
