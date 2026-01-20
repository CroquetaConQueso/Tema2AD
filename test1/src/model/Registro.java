package model;

import java.time.LocalDate;

public class Registro {
    private Integer id;
    private String nombre;
    private LocalDate fecha;
    private Integer cantidad;

    public Registro(Integer id, String nombre, LocalDate fecha, Integer cantidad) {
        this.id = id; this.nombre = nombre; this.fecha = fecha; this.cantidad = cantidad;
    }
    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public LocalDate getFecha() { return fecha; }
    public Integer getCantidad() { return cantidad; }
}
