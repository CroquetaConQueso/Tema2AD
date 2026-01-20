package model;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "empleados")
public class Empleado {

    @Id
    @Column(name = "emp_no")
    private int empNo;

    @Column(name = "apellido", length = 10)
    private String apellido;

    @Column(name = "oficio", length = 10)
    private String oficio;

    @Column(name = "dir")
    private Integer dir;

    @Column(name = "fecha_alt")
    private Date fechaAlt;

    @Column(name = "salario")
    private Integer salario;

    @Column(name = "comision")
    private Integer comision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_no")
    private Departamento departamento;

    public Empleado() {}

    // constructor "nuevo" (con Departamento)
    public Empleado(int empNo, String apellido, String oficio, Integer dir, Date fechaAlt,
                    Integer salario, Integer comision, Departamento departamento) {
        this.empNo = empNo;
        this.apellido = apellido;
        this.oficio = oficio;
        this.dir = dir;
        this.fechaAlt = fechaAlt;
        this.salario = salario;
        this.comision = comision;
        this.departamento = departamento;
    }

    // ✅ constructor "viejo" (con deptNo) para que no te rompa EmpleadoDialog
    public Empleado(int empNo, String apellido, String oficio, Integer dir, Date fechaAlt,
                    Integer salario, Integer comision, Integer deptNo) {
        this.empNo = empNo;
        this.apellido = apellido;
        this.oficio = oficio;
        this.dir = dir;
        this.fechaAlt = fechaAlt;
        this.salario = salario;
        this.comision = comision;
        setDeptNo(deptNo);
    }

    public int getEmpNo() { return empNo; }
    public void setEmpNo(int empNo) { this.empNo = empNo; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getOficio() { return oficio; }
    public void setOficio(String oficio) { this.oficio = oficio; }

    public Integer getDir() { return dir; }
    public void setDir(Integer dir) { this.dir = dir; }

    public Date getFechaAlt() { return fechaAlt; }
    public void setFechaAlt(Date fechaAlt) { this.fechaAlt = fechaAlt; }

    public Integer getSalario() { return salario; }
    public void setSalario(Integer salario) { this.salario = salario; }

    public Integer getComision() { return comision; }
    public void setComision(Integer comision) { this.comision = comision; }

    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }

    // alias para UI: dept_no como número
    public Integer getDeptNo() {
        return (departamento == null) ? null : departamento.getDeptNo();
    }

    public void setDeptNo(Integer deptNo) {
        if (deptNo == null) {
            this.departamento = null;
        } else {
            Departamento d = new Departamento();
            d.setDeptNo(deptNo);
            this.departamento = d;
        }
    }
}
