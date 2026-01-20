package model;

public class Dato {
    private Integer identificador; // puede ser null hasta que lo insertemos
    private String nombre;
    private Integer edad;

    public Dato() {}

    public Dato(Integer identificador, String nombre, Integer edad) {
        this.identificador = identificador;
        this.nombre = nombre;
        this.edad = edad;
    }

    public Integer getIdentificador() { return identificador; }
    public void setIdentificador(Integer identificador) { this.identificador = identificador; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }

    @Override
    public String toString() {
        return "Dato{id=" + identificador + ", nombre='" + nombre + "', edad=" + edad + "}";
    }
}

