package application.model;

import java.io.Serializable;
import java.time.LocalDate;

public class OrdenTotal implements Serializable {
    private int idOrden;
    private LocalDate fecha;
    private String descripcion;
    private String estado;
    private int idVehiculo;
    private String matricula;
    private String nombreCliente;
    private double total;

    public OrdenTotal(int idOrden, LocalDate fecha, String descripcion, String estado, int idVehiculo,
                      String matricula, String nombreCliente, double total) {
        this.idOrden = idOrden;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.estado = estado;
        this.idVehiculo = idVehiculo;
        this.matricula = matricula;
        this.nombreCliente = nombreCliente;
        this.total = total;
    }

    public int getIdOrden() { return idOrden; }
    public LocalDate getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public String getEstado() { return estado; }
    public int getIdVehiculo() { return idVehiculo; }
    public String getMatricula() { return matricula; }
    public String getNombreCliente() { return nombreCliente; }
    public double getTotal() { return total; }
}
