package application.ui.dialogs;

import application.model.Cliente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Diálogo de edición múltiple de clientes.
 * Muestra los seleccionados en una TableView editable y devuelve la lista editada al pulsar Aceptar.
 */
public class BulkEditClientesDialog extends Dialog<List<Cliente>> {

    // Reglas simples (reutiliza tus límites; si prefieres, iguala a Validation.MAX_*)
    private static final int MAX_NOMBRE = 80;
    private static final int MAX_TLF    = 20;
    private static final int MAX_EMAIL  = 120;

    private static final Pattern PAT_TLF   = Pattern.compile("^$|^[+\\d][\\d\\s\\-()]{6,}$"); // vacío o formato sencillo
    private static final Pattern PAT_EMAIL = Pattern.compile("^$|^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final TableView<Row> table;
    private final ObservableList<Row> rows;

    /** Wrapper editable para no tocar los objetos originales hasta Aceptar. */
    public static class Row {
        private final int id;
        private String nombre;
        private String telefono;
        private String email;

        public Row(Cliente c){
            this.id = c.getIdCliente();
            this.nombre = nz(c.getNombre());
            this.telefono = nz(c.getTelefono());
            this.email = nz(c.getEmail());
        }
        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        private static String nz(String s){ return s==null? "": s; }
    }

    public BulkEditClientesDialog(List<Cliente> seleccion){
        setTitle("Edición múltiple de clientes ("+seleccion.size()+")");
        getDialogPane().getButtonTypes().addAll(
                new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE),
                new ButtonType("Aceptar",  ButtonBar.ButtonData.OK_DONE)
        );

        this.rows = FXCollections.observableArrayList();
        for (Cliente c: seleccion) rows.add(new Row(c));

        this.table = new TableView<>(rows);
        table.setEditable(true);
        table.setPrefHeight(420);

        // Col ID (solo lectura)
        TableColumn<Row, String> colId = new TableColumn<>("ID");
        colId.setPrefWidth(80);
        colId.setCellValueFactory(cd -> javafx.beans.binding.Bindings.createStringBinding(
                () -> String.valueOf(cd.getValue().getId())
        ));

        // Col Nombre (editable + validación)
        TableColumn<Row, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setPrefWidth(220);
        colNombre.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getNombre()));
        colNombre.setCellFactory(TextFieldTableCell.forTableColumn());
        colNombre.setOnEditCommit(ev -> {
            String v = nv(ev.getNewValue());
            ev.getRowValue().setNombre(v);
            paintValidity(ev.getTablePosition().getRow(), 1, isNombreOk(v));
        });

        // Col Teléfono (editable + validación)
        TableColumn<Row, String> colTel = new TableColumn<>("Teléfono");
        colTel.setPrefWidth(160);
        colTel.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getTelefono()));
        colTel.setCellFactory(TextFieldTableCell.forTableColumn());
        colTel.setOnEditCommit(ev -> {
            String v = nv(ev.getNewValue());
            ev.getRowValue().setTelefono(v);
            paintValidity(ev.getTablePosition().getRow(), 2, isTelefonoOk(v));
        });

        // Col Email (editable + validación de formato; unicidad la gestiona el DAO)
        TableColumn<Row, String> colEmail = new TableColumn<>("Email");
        colEmail.setPrefWidth(260);
        colEmail.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getEmail()));
        colEmail.setCellFactory(TextFieldTableCell.forTableColumn());
        colEmail.setOnEditCommit(ev -> {
            String v = nv(ev.getNewValue());
            ev.getRowValue().setEmail(v);
            paintValidity(ev.getTablePosition().getRow(), 3, isEmailOk(v));
        });

        table.getColumns().addAll(colId, colNombre, colTel, colEmail);

        // Pintar estado inicial
        for (int i=0;i<rows.size();i++){
            paintValidity(i, 1, isNombreOk(rows.get(i).getNombre()));
            paintValidity(i, 2, isTelefonoOk(rows.get(i).getTelefono()));
            paintValidity(i, 3, isEmailOk(rows.get(i).getEmail()));
        }

        BorderPane root = new BorderPane(table);
        root.setPadding(new Insets(10));
        getDialogPane().setContent(root);

        // Convierte filas a Clientes al pulsar Aceptar (si hay celdas inválidas, bloquea)
        final Button btnOk = (Button) getDialogPane().lookupButton(
                getDialogPane().getButtonTypes().filtered(bt -> bt.getButtonData()== ButtonBar.ButtonData.OK_DONE).get(0));
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            var invalid = findFirstInvalid();
            if (invalid != null){
                e.consume();
                var rowIdx = invalid.row;
                var colName = invalid.colName;
                table.getSelectionModel().clearAndSelect(rowIdx);
                table.scrollTo(rowIdx);
                new Alert(Alert.AlertType.ERROR, "Corrige la celda inválida en fila "+(rowIdx+1)+", columna "+colName).showAndWait();
            }
        });

        setResultConverter(bt -> {
            if (bt.getButtonData()!= ButtonBar.ButtonData.OK_DONE) return null;
            List<Cliente> editados = new ArrayList<>();
            for (Row r: rows){
                editados.add(new Cliente(r.getId(), r.getNombre(), r.getTelefono(), r.getEmail()));
            }
            return editados;
        });
    }

    private static String nv(String s){ return s==null? "": s.trim(); }

    private static boolean isNombreOk(String v){
        return v!=null && !v.isBlank() && v.length()<=MAX_NOMBRE;
    }
    private static boolean isTelefonoOk(String v){
        return v!=null && v.length()<=MAX_TLF && PAT_TLF.matcher(v).matches();
    }
    private static boolean isEmailOk(String v){
        return v!=null && v.length()<=MAX_EMAIL && PAT_EMAIL.matcher(v).matches();
    }

    /** Pinta estilo de celda según validez (simplemente usa pseudo-clase 'error' en la fila-columna). */
    private void paintValidity(int row, int colIndex, boolean ok){
        // Aplicamos un estilo simple en base a validez (JavaFX no da acceso directo por celda antes de render;
        // lo resolvemos forzando refresh y usando rowFactory con CSS).
        table.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Row item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item!=null) {
                    // Nada: la marca ocurre al editar; como fallback, podríamos activar CSS de tabla completa.
                }
            }
        });
        // Para feedback inmediato, usamos el estilo de columna completo cuando hay error (práctico y sencillo)
        TableColumn<Row, ?> col = table.getColumns().get(colIndex);
        if (ok) col.getStyleClass().remove("col-error-"+colIndex);
        else if (!col.getStyleClass().contains("col-error-"+colIndex)) col.getStyleClass().add("col-error-"+colIndex);

        // CSS inline para remarcar columnas con error
        StringBuilder css = new StringBuilder();
        for (int i=0;i<table.getColumns().size();i++){
            if (table.getColumns().get(i).getStyleClass().contains("col-error-"+i)) {
                css.append(".table-column.col-error-").append(i).append(" .text { -fx-fill: red; }\n");
            }
        }
        table.setStyle(css.toString());
        table.refresh();
    }

    private InvalidCell findFirstInvalid(){
        for (int i=0;i<rows.size();i++){
            Row r = rows.get(i);
            if (!isNombreOk(r.getNombre())) return new InvalidCell(i, "Nombre");
            if (!isTelefonoOk(r.getTelefono())) return new InvalidCell(i, "Teléfono");
            if (!isEmailOk(r.getEmail())) return new InvalidCell(i, "Email");
        }
        return null;
    }

    private static class InvalidCell {
        final int row; final String colName;
        InvalidCell(int row, String colName){ this.row=row; this.colName=colName; }
    }
}
