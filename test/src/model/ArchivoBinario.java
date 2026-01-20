package model;

public class ArchivoBinario {
    private Integer id;
    private String nombreOriginal;
    private String mime;
    private byte[] datos;
    private String creadoAt;

    public ArchivoBinario() {}
    public ArchivoBinario(Integer id, String nombreOriginal, String mime, byte[] datos, String creadoAt) {
        this.id = id; this.nombreOriginal = nombreOriginal; this.mime = mime; this.datos = datos; this.creadoAt = creadoAt;
    }

    public Integer getId() { return id; }
    public String getNombreOriginal() { return nombreOriginal; }
    public String getMime() { return mime; }
    public byte[] getDatos() { return datos; }
    public String getCreadoAt() { return creadoAt; }

    public void setId(Integer id) { this.id = id; }
    public void setNombreOriginal(String nombreOriginal) { this.nombreOriginal = nombreOriginal; }
    public void setMime(String mime) { this.mime = mime; }
    public void setDatos(byte[] datos) { this.datos = datos; }
    public void setCreadoAt(String creadoAt) { this.creadoAt = creadoAt; }
}
