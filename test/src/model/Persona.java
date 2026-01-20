package model;

public class Persona {
    private Integer id;
    private String nombre;
    private Integer edad;
    private Integer ciudadId;

    public Persona() {}
    public Persona(Integer id, String nombre, Integer edad, Integer ciudadId) {
        this.id = id; this.nombre = nombre; this.edad = edad; this.ciudadId = ciudadId;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public Integer getEdad() { return edad; }
    public Integer getCiudadId() { return ciudadId; }

    public void setId(Integer id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public void setCiudadId(Integer ciudadId) { this.ciudadId = ciudadId; }
}
