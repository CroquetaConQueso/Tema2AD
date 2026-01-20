package model;

public class Departamento {

    private int deptoNo;
    private String dnombre;
    private String loc;

    public Departamento() {}

    public Departamento(int deptoNo, String dnombre, String loc) {
        this.deptoNo = deptoNo;
        this.dnombre = dnombre;
        this.loc = loc;
    }

    // nombre "real"
    public int getDeptoNo() { return deptoNo; }
    public void setDeptoNo(int deptoNo) { this.deptoNo = deptoNo; }

    // alias para tu UI/otros códigos: getDeptNo()
    public int getDeptNo() { return deptoNo; }
    public void setDeptNo(int deptNo) { this.deptoNo = deptNo; }

    public String getDnombre() { return dnombre; }
    public void setDnombre(String dnombre) { this.dnombre = dnombre; }

    public String getLoc() { return loc; }
    public void setLoc(String loc) { this.loc = loc; }
}
