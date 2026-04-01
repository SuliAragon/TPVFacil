package com.tpvfacil.core.ui;

import com.tpvfacil.core.db.CajaRepository;
import com.tpvfacil.core.db.DatabaseManager;
import com.tpvfacil.core.db.FacturaRepository;
import com.tpvfacil.core.modelo.Factura;
import com.tpvfacil.core.modelo.FormaPago;
import com.tpvfacil.core.modelo.LineaFactura;
import com.tpvfacil.core.modelo.TipoNegocio;
import com.tpvfacil.core.util.BackupManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CierreCajaController {

    private static final Logger log = LoggerFactory.getLogger(CierreCajaController.class);

    @FXML private Label lblNumTickets;
    @FXML private Label lblEfectivoTeorico;
    @FXML private Label lblTotalTarjeta;
    @FXML private Label lblTotalGeneral;

    @FXML private TableView<IvaRow> tablaDesgloseIva;
    @FXML private TableColumn<IvaRow, String> colIvaPorcentaje;
    @FXML private TableColumn<IvaRow, String> colIvaBase;
    @FXML private TableColumn<IvaRow, String> colIvaCuota;

    @FXML private TextField txtEfectivoReal;
    @FXML private Label lblDescuadre;
    @FXML private Label lblEstadoArqueo;

    private final FacturaRepository facturaRepository = new FacturaRepository();
    @SuppressWarnings("unused") // Se usará en futuras migraciones 
    private final CajaRepository cajaRepository = new CajaRepository();
    private final BackupManager backupManager = new BackupManager();
    
    private TipoNegocio tipoNegocioFilter;
    
    private double totalEfectivoTeorico = 0;
    private double totalTarjeta = 0;
    private int numTickets = 0;

    /** Objeto auxiliar para la tabla de desglose IVA */
    public static class IvaRow {
        public double porcentaje;
        public double base;
        public double cuota;
        public IvaRow(double porcentaje, double base, double cuota) {
            this.porcentaje = porcentaje;
            this.base = base;
            this.cuota = cuota;
        }
    }

    public void setTipoNegocio(TipoNegocio tipo) {
        this.tipoNegocioFilter = tipo;
        cargarDatos();
    }

    @FXML
    public void initialize() {
        colIvaPorcentaje.setCellValueFactory(v -> new SimpleStringProperty(String.format("%.0f%%", v.getValue().porcentaje)));
        colIvaBase.setCellValueFactory(v -> new SimpleStringProperty(String.format("%.2f €", v.getValue().base)));
        colIvaCuota.setCellValueFactory(v -> new SimpleStringProperty(String.format("%.2f €", v.getValue().cuota)));
        
        txtEfectivoReal.textProperty().addListener((obs, old, nev) -> calcularDescuadre());
    }

    private void cargarDatos() {
        if (cajaCerradaHoy()) {
            deshabilitarTodoPorCierrePrevio();
            return;
        }

        List<Factura> facturasHoy = facturaRepository.findAll().stream()
                .filter(f -> f.getFecha().toLocalDate().equals(LocalDate.now()))
                .filter(f -> !f.isAnulada() && f.getTipoNegocio() == tipoNegocioFilter)
                .toList();

        numTickets = facturasHoy.size();
        lblNumTickets.setText(String.valueOf(numTickets));

        Map<Double, IvaRow> desgloseIva = new HashMap<>();

        for (Factura f : facturasHoy) {
            Factura completa = facturaRepository.findById(f.getId()).orElse(null);
            if (completa == null) continue;
            
            if (completa.getFormaPago() == FormaPago.EFECTIVO) {
                totalEfectivoTeorico += completa.getTotal();
            } else if (completa.getFormaPago() == FormaPago.TARJETA) {
                totalTarjeta += completa.getTotal();
            } else if (completa.getFormaPago() == FormaPago.MIXTO) {
                // El campo efectivo entregado indica cuánto dio en mano efectivo en mixto
                totalEfectivoTeorico += completa.getEfectivoEntregado() - completa.getCambio();
                totalTarjeta += (completa.getTotal() - (completa.getEfectivoEntregado() - completa.getCambio())); 
            }
            
            for (LineaFactura l : completa.getLineas()) {
                double porc = l.getIvaPorcentaje();
                IvaRow row = desgloseIva.getOrDefault(porc, new IvaRow(porc, 0, 0));
                row.base += l.getSubtotal();
                row.cuota += l.getSubtotal() * (porc / 100.0);
                desgloseIva.put(porc, row);
            }
        }

        lblEfectivoTeorico.setText(String.format("%.2f €", totalEfectivoTeorico));
        lblTotalTarjeta.setText(String.format("%.2f €", totalTarjeta));
        lblTotalGeneral.setText(String.format("%.2f €", totalEfectivoTeorico + totalTarjeta));

        ObservableList<IvaRow> datosIva = FXCollections.observableArrayList(desgloseIva.values());
        tablaDesgloseIva.setItems(datosIva);
        
        // Poner efectivo real igual al teorico por defecto
        txtEfectivoReal.setText(String.format("%.2f", totalEfectivoTeorico).replace(",", "."));
    }

    private boolean cajaCerradaHoy() {
        String hoy = LocalDate.now().toString();
        try (Connection conn = DatabaseManager.getInstance().getConexion();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM caja WHERE DATE(fecha_apertura) <= ? AND DATE(fecha_cierre) = ? AND estado = 'CERRADA'")) {
            stmt.setString(1, hoy);
            stmt.setString(2, hoy);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            log.error("Error comprobando caja cerrada", e);
        }
        return false;
    }

    private void deshabilitarTodoPorCierrePrevio() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Caja Cerrada");
        alert.setHeaderText("Cierre de caja ya realizado");
        alert.setContentText("Ya existe un cierre de caja para el día de hoy.");
        alert.showAndWait();
        cancelar();
    }

    private void calcularDescuadre() {
        try {
            double real = Double.parseDouble(txtEfectivoReal.getText().replace(",", "."));
            double diff = real - totalEfectivoTeorico;
            
            lblDescuadre.setText(String.format("%.2f €", diff));
            lblEstadoArqueo.setVisible(true);

            if (diff > 0.01) {
                lblDescuadre.setStyle("-fx-text-fill: green; -fx-font-size: 20px; -fx-font-weight: bold;");
                lblEstadoArqueo.setText("(Sobrante de caja)");
                lblEstadoArqueo.setStyle("-fx-text-fill: green;");
            } else if (diff < -0.01) {
                lblDescuadre.setStyle("-fx-text-fill: red; -fx-font-size: 20px; -fx-font-weight: bold;");
                lblEstadoArqueo.setText("(Falta de caja)");
                lblEstadoArqueo.setStyle("-fx-text-fill: red;");
            } else {
                lblDescuadre.setStyle("-fx-text-fill: gray; -fx-font-size: 20px; -fx-font-weight: bold;");
                lblEstadoArqueo.setText("(Caja cuadrada)");
                lblEstadoArqueo.setStyle("-fx-text-fill: gray;");
            }
        } catch (NumberFormatException ignored) {}
    }

    @FXML
    private void cerrarEImprimir() {
        if (confirmarCierre()) {
            ejecutarCierre();
            // Lógica de impresión TicketPrinter.imprimirCierre()
            System.out.println("Imprimiendo ticket de cierre de caja...");
            cancelar();
        }
    }

    @FXML
    private void cerrarSinImprimir() {
        if (confirmarCierre()) {
            ejecutarCierre();
            cancelar();
        }
    }

    private boolean confirmarCierre() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Cierre");
        alert.setHeaderText("¿Está seguro que desea cerrar la caja?");
        alert.setContentText("No podrá realizar más operaciones de venta con fecha de hoy y se generará el backup automático.");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void ejecutarCierre() {
        try {
            // Se actualiza o inserta el cierre
            // Asumiendo que CajaRepository.cerrarCaja(efectivo_real, tarjeta, ...) funcione, o hacemos query directo
            double real = Double.parseDouble(txtEfectivoReal.getText().replace(",", "."));
            
            String updateSql = "UPDATE caja SET fecha_cierre = datetime('now'), total_efectivo = ?, total_tarjeta = ?, " +
                               "total_ventas = ?, estado = 'CERRADA' WHERE estado = 'ABIERTA'";
            
            try (Connection conn = DatabaseManager.getInstance().getConexion();
                 PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setDouble(1, real); // usamos el real en vez del teorico para la caja, o guardamos ambos
                stmt.setDouble(2, totalTarjeta);
                stmt.setDouble(3, numTickets);
                int regs = stmt.executeUpdate();
                
                // Si no había caja abierta (ej: la app nunca llamó a abrir caja en inicio día), insertamos una CERRADA directamente
                if (regs == 0) {
                    try (PreparedStatement insert = conn.prepareStatement(
                            "INSERT INTO caja (fecha_apertura, fecha_cierre, total_efectivo, total_tarjeta, total_ventas, estado) " +
                            "VALUES (datetime('now','start of day'), datetime('now'), ?, ?, ?, 'CERRADA')")) {
                        insert.setDouble(1, real);
                        insert.setDouble(2, totalTarjeta);
                        insert.setInt(3, numTickets);
                        insert.executeUpdate();
                    }
                }
            }
            
            backupManager.realizarBackupDiario();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Cierre completado y copia de seguridad realizada.");
            alert.showAndWait();
            
        } catch (Exception e) {
            log.error("Error ejecutando cierre de caja", e);
            new Alert(Alert.AlertType.ERROR, "Error al cerrar la caja: " + e.getMessage()).show();
        }
    }

    @FXML
    private void cancelar() {
        Stage s = (Stage) lblNumTickets.getScene().getWindow();
        if (s != null) s.close();
    }
}
