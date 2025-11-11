package application.ui.dialogs;

import application.model.OrdenTotal;
import application.model.OrdenTrabajo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo de edición múltiple de órdenes.
 * Edita fecha (yyyy-MM-dd), descripción (<=255), estado (combo), idVehículo (int).
 * Devuelve una lista de OrdenTrabajo lista para persistir.
 */
public class BulkEditOrdenesDialog extends Dialog<List<OrdenTrabajo>> {

    private static final int MAX_DESC = 255;
    private static final String[] ESTADOS = {"ABIERTA","EN_PROCESO","CERRADA","ANULADA"};

    private final TableView<Row> table;
    private final ObservableList<Row> rows;

    public static class Row {
        private final int idOrden;
        private LocalDate fecha;
        private String descripcion;
        private String estado;
        private int idVehiculo;

        public Row(OrdenTotal o){
            this.idOrden    = o.getIdOrden();
            this.fecha      = o.getFecha();
            this.descripcion= nz(o.getDescripcion());
            this.estado     = nz(o.getEstado());
            this.idVehiculo = o.getIdVehiculo();
        }
        public int getIdOrden() { return idOrden; }
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate f) { this.fecha = f; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String d) { this.descripcion = d; }
        public String getEstado() { return estado; }
        public void setEstado(String e) { this.estado = e; }
        public int getIdVehiculo() { return idVehiculo; }
        public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }

        private static String nz(String s){ return s==null? "": s; }
    }

    public BulkEditOrdenesDialog(List<OrdenTotal> seleccion){
        setTitle("Edición múltiple de órdenes ("+seleccion.size()+")");
        getDialogPane().getButtonTypes().addAll(
                new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE),
                new ButtonType("Aceptar",  ButtonBar.ButtonData.OK_DONE)
        );

        rows = FXCollections.observableArrayList();
        for (OrdenTotal o: seleccion) rows.add(new Row(o));

        table = new TableView<>(rows);
        table.setEditable(true);
        table.setPrefHeight(440);

        // ID (solo lectura)
        TableColumn<Row, String> cId = new TableColumn<>("ID");
        cId.setPrefWidth(70);
        cId.setCellValueFactory(cd -> javafx.beans.binding.Bindings.createStringBinding(
                () -> String.valueOf(cd.getValue().getIdOrden())
        ));

        // Fecha (editable como texto yyyy-MM-dd)
        TableColumn<Row, String> cFecha = new TableColumn<>("Fecha (yyyy-MM-dd)");
        cFecha.setPrefWidth(150);
        cFecha.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getFecha()==null? "" : cd.getValue().getFecha().toString()
        ));
        cFecha.setCellFactory(TextFieldTableCell.forTableColumn());
        cFecha.setOnEditCommit(ev -> {
            String v = nv(ev.getNewValue());
            try {
                LocalDate f = v.isBlank()? ev.getRowValue().getFecha() : LocalDate.parse(v);
                ev.getRowValue().setFecha(f);
                paintValidity(1, true);
            } catch (DateTimeParseException ex){
                paintValidity(1, false);
            }
        });

        // Descripción
        TableColumn<Row, String> cDesc = new TableColumn<>("Descripción");
        cDesc.setPrefWidth(260);
        cDesc.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getDescripcion()));
        cDesc.setCellFactory(TextFieldTableCell.forTableColumn());
        cDesc.setOnEditCommit(ev -> {
            String v = nv(ev.getNewValue());
            ev.getRowValue().setDescripcion(v);
            paintValidity(2, v.length()<=MAX_DESC && !v.isBlank());
        });

        // Estado (combo)
        TableColumn<Row, String> cEst = new TableColumn<>("Estado");
        cEst.setPrefWidth(140);
        cEst.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getEstado()));
        cEst.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(ESTADOS)));
        cEst.setOnEditCommit(ev -> {
            String v = nv(ev.getNewValue());
            ev.getRowValue().setEstado(v);
            paintValidity(3, isEstadoOk(v));
        });

        // IdVehículo (int)
        TableColumn<Row, String> cVeh = new TableColumn<>("IdVehículo");
        cVeh.setPrefWidth(110);
        cVeh.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cd.getValue().getIdVehiculo())));
        cVeh.setCellFactory(TextFieldTableCell.forTableColumn());
        cVeh.setOnEditCommit(ev -> {
            String v = nv(ev.getNewValue());
            try{
                int idv = Integer.parseInt(v);
                ev.getRowValue().setIdVehiculo(idv);
                paintValidity(4, idv>0);
            }catch (Exception ex){
                paintValidity(4, false);
            }
        });

        table.getColumns().addAll(cId, cFecha, cDesc, cEst, cVeh);

        // Validación inicial
        refreshValidity();

        BorderPane root = new BorderPane(table);
        root.setPadding(new Insets(10));
        getDialogPane().setContent(root);

        // Bloquear aceptar si hay errores
        final Button btnOk = (Button) getDialogPane().lookupButton(
                getDialogPane().getButtonTypes().filtered(bt -> bt.getButtonData()== ButtonBar.ButtonData.OK_DONE).get(0));
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String msg = firstError();
            if (msg != null){
                e.consume();
                new Alert(Alert.AlertType.ERROR, msg).showAndWait();
            }
        });

        setResultConverter(bt -> {
            if (bt.getButtonData()!= ButtonBar.ButtonData.OK_DONE) return null;
            List<OrdenTrabajo> out = new ArrayList<>();
            for (Row r: rows){
                out.add(new OrdenTrabajo(r.getIdOrden(), r.getFecha(), r.getDescripcion(), r.getEstado(), r.getIdVehiculo()));
            }
            return out;
        });
    }

    private void refreshValidity(){
        paintValidity(1, rows.stream().allMatch(r -> r.getFecha()!=null));
        paintValidity(2, rows.stream().allMatch(r -> r.getDescripcion()!=null && !r.getDescripcion().isBlank() && r.getDescripcion().length()<=MAX_DESC));
        paintValidity(3, rows.stream().allMatch(r -> isEstadoOk(r.getEstado())));
        paintValidity(4, rows.stream().allMatch(r -> r.getIdVehiculo()>0));
    }

    private String firstError(){
        for (int i=0;i<rows.size();i++){
            Row r = rows.get(i);
            if (r.getFecha()==null) return "Fila "+(i+1)+": fecha inválida (usa yyyy-MM-dd)";
            if (r.getDescripcion()==null || r.getDescripcion().isBlank() || r.getDescripcion().length()>MAX_DESC)
                return "Fila "+(i+1)+": descripción vacía o demasiado larga";
            if (!isEstadoOk(r.getEstado())) return "Fila "+(i+1)+": estado inválido";
            if (r.getIdVehiculo()<=0) return "Fila "+(i+1)+": IdVehículo debe ser > 0";
        }
        return null;
    }

    private static boolean isEstadoOk(String v){
        if (v==null) return false;
        for (String e: ESTADOS) if (e.equals(v)) return true;
        return false;
    }
    private static String nv(String s){ return s==null? "": s.trim(); }

    /** Marca columna con error coloreando el texto en rojo (estilo simple). */
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
