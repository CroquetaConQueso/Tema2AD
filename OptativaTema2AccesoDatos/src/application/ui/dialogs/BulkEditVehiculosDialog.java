package application.ui.dialogs;

import application.model.Vehiculo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Diálogo de edición múltiple de vehículos.
 * Permite editar matrícula, marca, modelo e idCliente con validación básica.
 */
public class BulkEditVehiculosDialog extends Dialog<List<Vehiculo>> {

    private static final int MAX_MARCA  = 60;
    private static final int MAX_MODELO = 60;
    private static final int MAX_MAT    = 10;

    // Aceptamos mayúsculas, dígitos y guiones, entre 4 y 10 chars (flexible para distintos formatos)
    private static final Pattern PAT_MAT = Pattern.compile("^[A-Z0-9-]{4,10}$");
    private static final Pattern PAT_INT = Pattern.compile("^\\d+$");

    private final TableView<Row> table;
    private final ObservableList<Row> rows;

    public static class Row {
        private final int idVehiculo;
        private String matricula;
        private String marca;
        private String modelo;
        private int idCliente;

        public Row(Vehiculo v){
            this.idVehiculo = v.getIdVehiculo();
            this.matricula  = nz(v.getMatricula()).toUpperCase();
            this.marca      = nz(v.getMarca());
            this.modelo     = nz(v.getModelo());
            this.idCliente  = v.getIdCliente();
        }
        public int getIdVehiculo() { return idVehiculo; }
        public String getMatricula() { return matricula; }
        public void setMatricula(String m) { this.matricula = m; }
        public String getMarca() { return marca; }
        public void setMarca(String marca) { this.marca = marca; }
        public String getModelo() { return modelo; }
        public void setModelo(String modelo) { this.modelo = modelo; }
        public int getIdCliente() { return idCliente; }
        public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

        private static String nz(String s){ return s==null? "": s; }
    }

    public BulkEditVehiculosDialog(List<Vehiculo> seleccion){
        setTitle("Edición múltiple de vehículos ("+seleccion.size()+")");
        getDialogPane().getButtonTypes().addAll(
                new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE),
                new ButtonType("Aceptar",  ButtonBar.ButtonData.OK_DONE)
        );

        rows  = FXCollections.observableArrayList();
        for (Vehiculo v: seleccion) rows.add(new Row(v));

        table = new TableView<>(rows);
        table.setEditable(true);
        table.setPrefHeight(420);

        // ID (solo lectura)
        TableColumn<Row, String> cId = new TableColumn<>("ID");
        cId.setPrefWidth(80);
        cId.setCellValueFactory(cd -> javafx.beans.binding.Bindings.createStringBinding(
                () -> String.valueOf(cd.getValue().getIdVehiculo())
        ));

        // Matrícula (editable, upper + regex + longitud)
        TableColumn<Row, String> cMat = new TableColumn<>("Matrícula");
        cMat.setPrefWidth(140);
        cMat.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getMatricula()));
        cMat.setCellFactory(TextFieldTableCell.forTableColumn());
        cMat.setOnEditCommit(ev -> {
            String v = nv(ev.getNewValue()).toUpperCase();
            ev.getRowValue().setMatricula(v);
            paintValidity(1, isMatOk(v));
        });

        // Marca
        TableColumn<Row, String> cMarca = new TableColumn<>("Marca");
        cMarca.setPrefWidth(160);
        cMarca.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getMarca()));
        cMarca.setCellFactory(TextFieldTableCell.forTableColumn());
        cMarca.setOnEditCommit(ev -> {
            String v = nv(ev.getNewValue());
            ev.getRowValue().setMarca(v);
            paintValidity(2, isLenOk(v, MAX_MARCA));
        });

        // Modelo
        TableColumn<Row, String> cModelo = new TableColumn<>("Modelo");
        cModelo.setPrefWidth(160);
        cModelo.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getModelo()));
        cModelo.setCellFactory(TextFieldTableCell.forTableColumn());
        cModelo.setOnEditCommit(ev -> {
            String v = nv(ev.getNewValue());
            ev.getRowValue().setModelo(v);
            paintValidity(3, isLenOk(v, MAX_MODELO));
        });

        // IdCliente (editable — entero)
        TableColumn<Row, String> cCli = new TableColumn<>("IdCliente");
        cCli.setPrefWidth(100);
        cCli.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cd.getValue().getIdCliente())));
        cCli.setCellFactory(TextFieldTableCell.forTableColumn());
        cCli.setOnEditCommit(ev -> {
            String v = nv(ev.getNewValue());
            if (PAT_INT.matcher(v).matches()) {
                ev.getRowValue().setIdCliente(Integer.parseInt(v));
                paintValidity(4, true);
            } else {
                paintValidity(4, false);
            }
        });

        table.getColumns().addAll(cId, cMat, cMarca, cModelo, cCli);

        // Valida estado inicial
        refreshValidity();

        BorderPane root = new BorderPane(table);
        root.setPadding(new Insets(10));
        getDialogPane().setContent(root);

        // Bloquear aceptar si hay errores
        final Button btnOk = (Button) getDialogPane().lookupButton(
                getDialogPane().getButtonTypes().filtered(bt -> bt.getButtonData()== ButtonBar.ButtonData.OK_DONE).get(0));
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String msg = firstError();
            if (msg != null) {
                e.consume();
                new Alert(Alert.AlertType.ERROR, msg).showAndWait();
            }
        });

        setResultConverter(bt -> {
            if (bt.getButtonData()!= ButtonBar.ButtonData.OK_DONE) return null;
            List<Vehiculo> out = new ArrayList<>();
            for (Row r: rows){
                out.add(new Vehiculo(r.getIdVehiculo(), r.getMatricula(), r.getMarca(), r.getModelo(), r.getIdCliente()));
            }
            return out;
        });
    }

    private void refreshValidity(){
        paintValidity(1, rows.stream().allMatch(r -> isMatOk(r.getMatricula())));
        paintValidity(2, rows.stream().allMatch(r -> isLenOk(r.getMarca(), MAX_MARCA)));
        paintValidity(3, rows.stream().allMatch(r -> isLenOk(r.getModelo(), MAX_MODELO)));
        paintValidity(4, rows.stream().allMatch(r -> r.getIdCliente() > 0));
    }

    private String firstError(){
        for (int i=0;i<rows.size();i++){
            Row r = rows.get(i);
            if (!isMatOk(r.getMatricula())) return "Fila "+(i+1)+": matrícula inválida";
            if (!isLenOk(r.getMarca(), MAX_MARCA)) return "Fila "+(i+1)+": marca demasiado larga";
            if (!isLenOk(r.getModelo(), MAX_MODELO)) return "Fila "+(i+1)+": modelo demasiado largo";
            if (r.getIdCliente()<=0) return "Fila "+(i+1)+": IdCliente debe ser entero positivo";
        }
        return null;
    }

    private static boolean isMatOk(String v){
        return v!=null && v.length()<=MAX_MAT && PAT_MAT.matcher(v).matches();
    }
    private static boolean isLenOk(String v, int max){
        return v!=null && v.length()<=max;
    }
    private static String nv(String s){ return s==null? "": s.trim(); }

    /** colorea entera la columna si hay algún error (solución simple y efectiva) */
    private void paintValidity(int colIndex, boolean ok){
        TableColumn<Row, ?> col = table.getColumns().get(colIndex);
        String cls = "col-error-"+colIndex;
        if (ok) col.getStyleClass().remove(cls);
        else if (!col.getStyleClass().contains(cls)) col.getStyleClass().add(cls);

        StringBuilder css = new StringBuilder();
        for (int i=0;i<table.getColumns().size();i++){
            String name = "col-error-"+i;
            if (table.getColumns().get(i).getStyleClass().contains(name)) {
                css.append(".table-column.").append(name).append(" .text { -fx-fill: red; }\n");
            }
        }
        table.setStyle(css.toString());
        table.refresh();
    }
}
