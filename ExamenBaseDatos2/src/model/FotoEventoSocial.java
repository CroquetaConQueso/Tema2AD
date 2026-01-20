package model;

public class FotoEventoSocial {

    private int idFoto;
    private EventoSocial evento;
    private byte[] foto;
    private String descripcion;
    private int cantidad;

    public int getIdFoto() {
        return idFoto;
    }

    public void setIdFoto(int idFoto) {
        this.idFoto = idFoto;
    }

    public EventoSocial getEvento() {
        return evento;
    }

    public void setEvento(EventoSocial evento) {
        this.evento = evento;
    }

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
