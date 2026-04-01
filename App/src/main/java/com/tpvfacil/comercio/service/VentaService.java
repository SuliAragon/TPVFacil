package com.tpvfacil.comercio.service;

import com.tpvfacil.comercio.modelo.ItemCesta;
import com.tpvfacil.core.db.FacturaRepository;
import com.tpvfacil.core.modelo.Factura;
import com.tpvfacil.core.modelo.FormaPago;
import com.tpvfacil.core.modelo.LineaFactura;
import com.tpvfacil.core.modelo.TipoNegocio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VentaService {

    private FacturaRepository facturaRepository = new FacturaRepository();

    /**
     * Construye y guarda una Factura a partir de la cesta de la compra.
     */
    public Factura construirFactura(List<ItemCesta> cesta, FormaPago formaPago, int clienteId, double efectivoEntregado, double cambio) {
        Factura factura = new Factura();
        factura.setSerie("C"); // Serie 'C' para Comercio, o lo que se configure
        factura.setNumero(facturaRepository.siguienteNumero("C"));
        factura.setFecha(LocalDateTime.now());
        factura.setClienteId(clienteId);
        factura.setFormaPago(formaPago);
        factura.setEfectivoEntregado(efectivoEntregado);
        factura.setCambio(cambio);
        factura.setTipoNegocio(TipoNegocio.COMERCIO);
        factura.setAnulada(false);

        List<LineaFactura> lineas = new ArrayList<>();
        double baseImponible = 0;
        double cuotaIva = 0;
        double total = 0;

        for (ItemCesta item : cesta) {
            LineaFactura linea = new LineaFactura();
            linea.setProductoId(item.getProducto().getId());
            linea.setDescripcion(item.getProducto().getNombre());
            linea.setCantidad(item.getCantidad());
            linea.setPrecioUnitario(item.getProducto().getPrecio());
            linea.setIvaPorcentaje(item.getProducto().getIvaPorcentaje());
            
            double subtotalLinea = item.getSubtotal();
            linea.setSubtotal(subtotalLinea);
            
            double cuotaLinea = subtotalLinea * (item.getProducto().getIvaPorcentaje() / 100.0);
            
            baseImponible += subtotalLinea;
            cuotaIva += cuotaLinea;
            total += subtotalLinea + cuotaLinea;
            
            lineas.add(linea);
        }
        
        factura.setBaseImponible(baseImponible);
        factura.setCuotaIva(cuotaIva);
        factura.setTotal(total);
        factura.setLineas(lineas);
        
        // Guardar factura en BD
        facturaRepository.save(factura);
        
        return factura;
    }
}
