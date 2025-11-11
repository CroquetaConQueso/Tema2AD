package application.model;

import java.io.Serializable;

public class Vehiculo implements Serializable {
    private int idVehiculo;
    private String matricula;
    private String marca;
    private String modelo;
    private int idCliente;

    public Vehiculo() {}

    public Vehiculo(int idVehiculo, String matricula, String marca, String modelo, int idCliente) {
        this.idVehiculo = idVehiculo;
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
        this.idCliente = idCliente;
    }

    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    @Override public String toString() {
        return idVehiculo + ";" + matricula + ";" + marca + ";" + modelo + ";" + idCliente;
    }
}
