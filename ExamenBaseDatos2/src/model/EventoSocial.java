package model;

import java.time.LocalDate;

public class EventoSocial {

    private int idEvento;
    private String nombre;
    private LocalDate fechaEvento;

    public int getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(int idEvento) {
        this.idEvento = idEvento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(LocalDate fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    @Override
    public String toString() {
        return nombre + " (" + fechaEvento + ")";
    }
}
