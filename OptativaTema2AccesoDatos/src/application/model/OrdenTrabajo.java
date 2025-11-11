package application.model;

import java.io.Serializable;
import java.time.LocalDate;

public class OrdenTrabajo implements Serializable {
    private int idOrden;
    private LocalDate fecha;
    private String descripcion;
    private String estado; // ABIERTA / EN_PROCESO / CERRADA / ANULADA
    private int idVehiculo;

    public OrdenTrabajo() {}

    public OrdenTrabajo(int idOrden, LocalDate fecha, String descripcion, String estado, int idVehiculo) {
        this.idOrden = idOrden;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.estado = estado;
        this.idVehiculo = idVehiculo;
    }

    public int getIdOrden() { return idOrden; }
    public void setIdOrden(int idOrden) { this.idOrden = idOrden; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }
}
