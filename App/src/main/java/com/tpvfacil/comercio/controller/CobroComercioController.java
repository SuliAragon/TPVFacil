package com.tpvfacil.comercio.controller;

import com.tpvfacil.comercio.modelo.ItemCesta;
import com.tpvfacil.comercio.service.StockService;
import com.tpvfacil.comercio.service.VentaService;
import com.tpvfacil.core.modelo.Factura;
import com.tpvfacil.core.modelo.FormaPago;
import com.tpvfacil.licencia.ModoDemo;
import com.tpvfacil.verifactu.EstadoEnvio;
import com.tpvfacil.verifactu.RegistroFactura;
import com.tpvfacil.verifactu.VerifactuManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controlador de la pantalla de cobro del módulo Comercio.
 * Recibe la cesta desde VentaController, muestra el resumen
 * y coordina el flujo: VentaService → VerifactuManager → StockService.
 */
public class CobroComercioController {

    private static final Logger log = LoggerFactory.getLogger(CobroComercioController.class);

    @FXML private TableView<ItemCesta> tablaResumen;
    @FXML private TableColumn<ItemCesta, String> colDesc;
    @FXML private TableColumn<ItemCesta, String> colCant;
    @FXML private TableColumn<ItemCesta, String> colImporte;

    @FXML private Label lblBase;
    @FXML private Label lblIva;
    @FXML private Label lblTotal;

    @FXML private CheckBox chkFactura;
    @FXML private TextField txtNif;
    @FXML private Button btnBuscarCliente;

    @FXML private ToggleButton btnEfectivo;
    @FXML private ToggleButton btnTarjeta;
    @FXML private ToggleButton btnMixto;
    private ToggleGroup groupPago;

    @FXML private TextField txtEntregado;
    @FXML private Label lblCambio;

    private final VentaService ventaService = new VentaService();
    private final StockService stockService = new StockService();

    private List<ItemCesta> cesta;
    private double totalVenta;
    private double entregado;

    @FXML
    public void initialize() {
        groupPago = new ToggleGroup();
        btnEfectivo.setToggleGroup(groupPago);
        btnTarjeta.setToggleGroup(groupPago);
        btnMixto.setToggleGroup(groupPago);

        chkFactura.selectedProperty().addListener((obs, oldV, newV) -> {
            txtNif.setDisable(!newV);
            btnBuscarCliente.setDisable(!newV);
        });

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

    /** Recibe la cesta desde VentaController y calcula los totales. */
    public void setCesta(List<ItemCesta> cesta) {
        this.cesta = cesta;

        colDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProducto().getNombre()));
        colCant.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.0f", d.getValue().getCantidad())));
        colImporte.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f €", d.getValue().getSubtotalConIva())));

        ObservableList<ItemCesta> obsItems = FXCollections.observableArrayList(cesta);
        tablaResumen.setItems(obsItems);

        double base = 0;
        double iva = 0;
        totalVenta = 0;

        for (ItemCesta item : cesta) {
            double subtotalItem = item.getSubtotal();
            double ivaItem = subtotalItem * (item.getProducto().getIvaPorcentaje() / 100.0);
            base += subtotalItem;
            iva += ivaItem;
            totalVenta += subtotalItem + ivaItem;
        }

        lblBase.setText(String.format("%.2f €", base));
        lblIva.setText(String.format("%.2f €", iva));
        lblTotal.setText(String.format("%.2f €", totalVenta));

        txtEntregado.setText(String.format("%.2f", totalVenta).replace(",", "."));
    }

    /** Añade el importe del billete pulsado al campo de efectivo entregado. */
    @FXML
    private void addBillete(ActionEvent event) {
        Button btn = (Button) event.getSource();
        double valor = Double.parseDouble(btn.getText().replaceAll("[^0-9]", "").trim());
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
                lblCambio.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 28px;");
            } else {
                lblCambio.setText(String.format("%.2f €", cambio));
                lblCambio.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold; -fx-font-size: 28px;");
            }
        } catch (NumberFormatException e) {
            lblCambio.setText("0.00 €");
        }
    }

    /** Confirma el cobro: crea factura, envía a Verifactu y descuenta stock. */
    @FXML
    private void confirmarCobro() {
        if (!ModoDemo.puedeEmitirTicket()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("TPVFácil — Modo Demo");
            alert.setHeaderText("Límite diario alcanzado");
            alert.setContentText("Ha alcanzado el límite de 20 tickets diarios del modo demo.");
            alert.showAndWait();
            return;
        }

        FormaPago formaPago = FormaPago.EFECTIVO;
        if (btnTarjeta.isSelected()) formaPago = FormaPago.TARJETA;
        else if (btnMixto.isSelected()) formaPago = FormaPago.MIXTO;

        double cambio = Math.max(0, entregado - totalVenta);
        int clienteId = 0; // Ticket simplificado (F2)

        Factura factura = ventaService.construirFactura(cesta, formaPago, clienteId, entregado, cambio);
        RegistroFactura res = VerifactuManager.getInstance().procesarFactura(factura);
        stockService.descontarStock(factura);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("¡Venta completada!");
        alert.setHeaderText(String.format("Cambio: %.2f €", cambio));
        if (res.getEstado() == EstadoEnvio.PENDIENTE) {
            alert.setContentText("⚠ Ticket emitido — Registro Verifactu pendiente.");
        }
        alert.showAndWait();

        log.info("Venta completada: factura {} — total {} €", factura.getNumeroCompleto(), factura.getTotal());
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
