package application.ui;

import application.dao.ClienteDAO;
import application.dao.OrdenDAO;
import application.dao.VehiculoDAO;
import application.model.Cliente;
import application.model.OrdenTotal;
import application.model.OrdenTrabajo;
import application.model.Vehiculo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static application.ui.Validation.*;

public class MainController {

    // -------- Raíz --------
    @FXML private TabPane tabs;

    // -------- CLIENTES --------
    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, Number> colClId;
    @FXML private TableColumn<Cliente, String> colClNom, colClTel, colClEmail;
    @FXML private TextField txtBuscarCliente, txtClId, txtClNombre, txtClTelefono, txtClEmail;
    @FXML private ChoiceBox<String> chTipoCliente;

    // Filtros avanzados
    @FXML private TextField fClNombre, fClTelefono, fClEmail;

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ObservableList<Cliente> modelClientes = FXCollections.observableArrayList();

    // -------- VEHÍCULOS --------
    @FXML private TableView<Vehiculo> tblVehiculos;
    @FXML private TableColumn<Vehiculo, Number> colVId, colVClId;
    @FXML private TableColumn<Vehiculo, String> colVMat, colVMarca, colVModelo;
    @FXML private TextField txtBuscarVeh, txtVId, txtVMatricula, txtVMarca, txtVModelo;
    @FXML private ComboBox<String> cmbVCliente;
    @FXML private ChoiceBox<String> chTipoVeh;

    // Filtros avanzados
    @FXML private TextField fVMatricula, fVMarca, fVModelo;
    @FXML private ComboBox<String> fVCliente;

    private final VehiculoDAO vehiculoDAO = new VehiculoDAO();
    private final ObservableList<Vehiculo> modelVehiculos = FXCollections.observableArrayList();

    // -------- ÓRDENES --------
    @FXML private TableView<OrdenTotal> tblOrdenes;
    @FXML private TableColumn<OrdenTotal, Number> colOId, colOTotal, colOVeh;
    @FXML private TableColumn<OrdenTotal, String> colODesc, colOEstado, colONomCli;
    @FXML private TableColumn<OrdenTotal, LocalDate> colOFecha;
    @FXML private TextField txtOId, txtODesc;
    @FXML private DatePicker dpOFecha;
    @FXML private ChoiceBox<String> chOEstado;
    @FXML private ComboBox<String> cmbOVehiculo;
    @FXML private ChoiceBox<String> chFiltroEstado;

    // Filtros avanzados
    @FXML private DatePicker fODesde, fOHasta;
    @FXML private TextField fOMatricula, fONombreCliente;

    private final OrdenDAO ordenDAO = new OrdenDAO();
    private final ObservableList<OrdenTotal> modelOrdenes = FXCollections.observableArrayList();

    // -------- INIT --------
    @FXML
    public void initialize() {
        // TextFormatters
        digitsOnly(txtClId, txtVId, txtOId);
        upperAndLimit(txtVMatricula, MAX_MAT);
        limitLen(txtClNombre, MAX_NOMBRE);
        limitLen(txtClTelefono, MAX_TLF);
        limitLen(txtClEmail, MAX_EMAIL);
        limitLen(txtVMarca, MAX_MARCA);
        limitLen(txtVModelo, MAX_MODELO);
        limitLen(txtODesc, 255);
        limitLen(fClNombre, MAX_NOMBRE); limitLen(fClTelefono, MAX_TLF); limitLen(fClEmail, MAX_EMAIL);
        limitLen(fVMatricula, MAX_MAT);  limitLen(fVMarca, MAX_MARCA);   limitLen(fVModelo, MAX_MODELO);

        // Clientes
        colClId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idCliente"));
        colClNom.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombre"));
        colClTel.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("telefono"));
        colClEmail.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("email"));
        tblClientes.setItems(modelClientes);
        tblClientes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        if (chTipoCliente != null) { chTipoCliente.getItems().setAll("Nombre","Teléfono","Email"); chTipoCliente.getSelectionModel().select("Nombre"); }
        onListarCliente();
        tblClientes.getSelectionModel().selectedItemProperty().addListener((o,old,sel)->{
            if(sel!=null){
                clearError(txtClId, txtClNombre, txtClTelefono, txtClEmail);
                txtClId.setText(String.valueOf(sel.getIdCliente()));
                txtClNombre.setText(sel.getNombre());
                txtClTelefono.setText(sel.getTelefono());
                txtClEmail.setText(sel.getEmail());
            }
        });

        // Vehículos
        colVId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idVehiculo"));
        colVMat.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("matricula"));
        colVMarca.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("marca"));
        colVModelo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("modelo"));
        colVClId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idCliente"));
        tblVehiculos.setItems(modelVehiculos);
        tblVehiculos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        if (chTipoVeh != null) { chTipoVeh.getItems().setAll("Matrícula","Marca","Modelo","IdCliente"); chTipoVeh.getSelectionModel().select("Matrícula"); }
        onListarVehiculo();
        recargarClientesCombo();
        tblVehiculos.getSelectionModel().selectedItemProperty().addListener((o,old,sel)->{
            if(sel!=null){
                clearError(txtVId, txtVMatricula, txtVMarca, txtVModelo, cmbVCliente);
                txtVId.setText(String.valueOf(sel.getIdVehiculo()));
                txtVMatricula.setText(sel.getMatricula());
                txtVMarca.setText(sel.getMarca());
                txtVModelo.setText(sel.getModelo());
                cmbVCliente.getSelectionModel().select(sel.getIdCliente() + " - " + nombreCliente(sel.getIdCliente()));
            }
        });

        if (fVCliente != null) {
            var items = FXCollections.<String>observableArrayList();
            for (var c : clienteDAO.findAll()) items.add(c.getIdCliente()+" - "+c.getNombre());
            fVCliente.setItems(items);
        }

        // Órdenes
        colOId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idOrden"));
        colOFecha.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fecha"));
        colODesc.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("descripcion"));
        colOEstado.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("estado"));
        colOVeh.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idVehiculo"));
        colONomCli.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombreCliente"));
        colOTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("total"));
        tblOrdenes.setItems(modelOrdenes);
        tblOrdenes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        chOEstado.setItems(FXCollections.observableArrayList("ABIERTA","EN_PROCESO","CERRADA","ANULADA"));
        if (chFiltroEstado != null) { chFiltroEstado.getItems().setAll("(Todos)","ABIERTA","EN_PROCESO","CERRADA","ANULADA"); chFiltroEstado.getSelectionModel().select("(Todos)"); }
        onListarOrden();
        recargarVehiculosCombo();
    }

    // -------- Menú --------
    @FXML public void onSalir(){ tabs.getScene().getWindow().hide(); }
    @FXML public void onAcercaDe(){ info("Taller — JavaFX + MySQL\nCRUD + Búsqueda avanzada + Edición múltiple + Consultar selección"); }

    // ===================== CLIENTES =====================
    @FXML public void onListarCliente(){ modelClientes.setAll(clienteDAO.findAll()); }
    @FXML public void onBuscarCliente(){
        String tipo = chTipoCliente == null ? "Nombre" : chTipoCliente.getValue();
        modelClientes.setAll(clienteDAO.searchBy(tipo, texto(txtBuscarCliente)));
    }
    @FXML public void onBuscarClientesAvanzado(){
        String n = fClNombre==null? "": texto(fClNombre);
        String t = fClTelefono==null? "": texto(fClTelefono);
        String e = fClEmail  ==null? "": texto(fClEmail);
        modelClientes.setAll(clienteDAO.searchAdvanced(n,t,e));
    }
    @FXML public void onInsertarCliente(){
        try{
            clearError(txtClId, txtClNombre, txtClTelefono, txtClEmail);
            Cliente c = new Cliente(
                requirePositiveInt(txtClId,"ID"),
                requireNonEmpty(txtClNombre,"Nombre", MAX_NOMBRE),
                optionalTelefono(txtClTelefono),
                optionalEmail(txtClEmail)
            );
            clienteDAO.insert(c);
            onListarCliente(); recargarClientesCombo();
            info("Cliente insertado");
        } catch (Exception e){ showError(e, "Insertar cliente"); }
    }
    @FXML public void onModificarCliente(){
        try{
            var sel = tblClientes.getSelectionModel().getSelectedItem();
            if(sel==null) throw new IllegalArgumentException("Sin selección");
            clearError(txtClNombre, txtClTelefono, txtClEmail);
            Cliente c = new Cliente(
                sel.getIdCliente(),
                texto(txtClNombre).isBlank()? sel.getNombre() : requireNonEmpty(txtClNombre,"Nombre", MAX_NOMBRE),
                texto(txtClTelefono).isBlank()? sel.getTelefono() : optionalTelefono(txtClTelefono),
                texto(txtClEmail).isBlank()? sel.getEmail() : optionalEmail(txtClEmail)
            );
            clienteDAO.update(c);
            onListarCliente(); recargarClientesCombo();
            info("Cliente modificado");
        } catch (Exception e){ showError(e, "Modificar cliente"); }
    }
    @FXML
    public void onModificarClienteMulti(){
        var seleccion = new ArrayList<>(tblClientes.getSelectionModel().getSelectedItems());
        if (seleccion.isEmpty()) { warn("Selecciona uno o más clientes"); return; }

        // Abre diálogo editable
        var dlg = new application.ui.dialogs.BulkEditClientesDialog(seleccion);
        var res = dlg.showAndWait();
        if (res.isEmpty()) return; // cancelado

        var editados = res.get(); // lista de Cliente con los cambios
        var resultado = clienteDAO.updateBatchPartial(editados);

        onListarCliente();  // refresca
        recargarClientesCombo();

        if (resultado.failIds.isEmpty()){
            info("Actualizados " + resultado.okIds.size() + " clientes");
        } else {
            error("Actualizados: "+resultado.okIds.size()+"\n" +
                  "Fallidos: "+resultado.failIds.size()+"\n\n" +
                  String.join("\n", resultado.failMsgs));
        }
    }

    /** NUEVO: Consultar selección de clientes (tabla de solo lectura en diálogo). */
    @FXML public void onConsultarClientes(){
        var sels = new ArrayList<>(tblClientes.getSelectionModel().getSelectedItems());
        if (sels.isEmpty()) { warn("Selecciona uno o más clientes"); return; }
        TableView<Cliente> tv = new TableView<>();
        tv.setItems(FXCollections.observableArrayList(sels));
        TableColumn<Cliente, Number> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idCliente"));
        TableColumn<Cliente, String> c2 = new TableColumn<>("Nombre");
        c2.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombre"));
        TableColumn<Cliente, String> c3 = new TableColumn<>("Teléfono");
        c3.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("telefono"));
        TableColumn<Cliente, String> c4 = new TableColumn<>("Email");
        c4.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("email"));
        tv.getColumns().addAll(c1,c2,c3,c4);
        tv.setPrefHeight(360);
        showTableDialog("Clientes seleccionados ("+sels.size()+")", tv);
    }
    
    @FXML
    public void onBorrarOrden() {
        // Tabla debe existir en el controlador y estar vinculada al FXML con fx:id="tblOrdenes"
        var seleccion = new java.util.ArrayList<>(tblOrdenes.getSelectionModel().getSelectedItems());
        if (seleccion.isEmpty()) {
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING, 
                    "Selecciona una o más órdenes").showAndWait();
            return;
        }

        var confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION, 
                "¿Borrar " + seleccion.size() + " orden(es)?",
                javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);
        confirm.showAndWait();
        if (confirm.getResult() != javafx.scene.control.ButtonType.OK) return;

        var ids = seleccion.stream().map(application.model.OrdenTotal::getIdOrden).toList();
        try {
            // ordenDAO debe existir en tu controlador (final OrdenDAO ordenDAO = new OrdenDAO();)
            ordenDAO.deleteMany(ids);

            // refresca la tabla: usa el que tengas implementado
            try {
                onFiltrarOrdenPorEstado(); // si existe este método
            } catch (Throwable ignored) {
                onListarOrden();           // o este, si no tienes el anterior
            }

            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, 
                    "Borradas " + ids.size() + " órdenes").showAndWait();
        } catch (Exception e) {
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, 
                    "Error borrando órdenes: " + e.getMessage()).showAndWait();
        }
    }

    
    @FXML public void onBorrarCliente(){
        var sels = new ArrayList<>(tblClientes.getSelectionModel().getSelectedItems());
        if (sels.isEmpty()) { warn("Selecciona uno o más clientes"); return; }
        if (!confirm("¿Borrar "+sels.size()+" cliente(s)?")) return;
        var ids = sels.stream().map(Cliente::getIdCliente).toList();
        try { clienteDAO.deleteMany(ids); onListarCliente(); recargarClientesCombo(); info("Borrados "+ids.size()+" clientes"); }
        catch (Exception e){ showError(e, "Borrando clientes"); }
    }

    // ===================== VEHÍCULOS =====================
    @FXML public void onListarVehiculo(){ modelVehiculos.setAll(vehiculoDAO.findAll()); }
    @FXML public void onBuscarVehiculo(){
        String tipo = chTipoVeh==null? "Matrícula" : chTipoVeh.getValue();
        modelVehiculos.setAll(vehiculoDAO.searchBy(tipo, texto(txtBuscarVeh)));
    }
    @FXML public void onBuscarVehiculosAvanzado(){
        String m  = fVMatricula==null? "": texto(fVMatricula);
        String ma = fVMarca    ==null? "": texto(fVMarca);
        String mo = fVModelo   ==null? "": texto(fVModelo);
        Integer idCli = null;
        if (fVCliente!=null && fVCliente.getValue()!=null && !fVCliente.getValue().isBlank())
            idCli = idFromCombo(fVCliente, "cliente");
        modelVehiculos.setAll(vehiculoDAO.searchAdvanced(m,ma,mo,idCli));
    }
    @FXML public void onInsertarVehiculo(){
        try{
            clearError(txtVId, txtVMatricula, txtVMarca, txtVModelo, cmbVCliente);
            int id = requirePositiveInt(txtVId,"ID vehículo");
            String mat = matricula(txtVMatricula, false);
            String marca = optionalLimited(txtVMarca, MAX_MARCA);
            String modelo = optionalLimited(txtVModelo, MAX_MODELO);
            int idCli = requireComboId(cmbVCliente,"cliente");
            vehiculoDAO.insert(new Vehiculo(id,mat,marca,modelo,idCli));
            onListarVehiculo(); recargarVehiculosCombo();
            info("Vehículo insertado");
        } catch (Exception e){ showError(e, "Insertar vehículo"); }
    }
    @FXML public void onModificarVehiculo(){
        try{
            var sel = tblVehiculos.getSelectionModel().getSelectedItem();
            if (sel==null) throw new IllegalArgumentException("Sin selección");
            clearError(txtVMatricula, txtVMarca, txtVModelo, cmbVCliente);
            String mat = texto(txtVMatricula).isBlank()? sel.getMatricula() : matricula(txtVMatricula, false);
            String marca = texto(txtVMarca).isBlank()? sel.getMarca() : optionalLimited(txtVMarca, MAX_MARCA);
            String modelo = texto(txtVModelo).isBlank()? sel.getModelo() : optionalLimited(txtVModelo, MAX_MODELO);
            int idCli = (cmbVCliente.getValue()==null || cmbVCliente.getValue().isBlank())
                    ? sel.getIdCliente()
                    : requireComboId(cmbVCliente,"cliente");
            vehiculoDAO.update(new Vehiculo(sel.getIdVehiculo(), mat, marca, modelo, idCli));
            onListarVehiculo(); recargarVehiculosCombo();
            info("Vehículo modificado");
        } catch (Exception e){ showError(e, "Modificar vehículo"); }
    }
    @FXML
    public void onModificarVehiculoMulti(){
        var sels = new java.util.ArrayList<>(tblVehiculos.getSelectionModel().getSelectedItems());
        if (sels.isEmpty()) { warn("Selecciona uno o más vehículos"); return; }

        var dlg = new application.ui.dialogs.BulkEditVehiculosDialog(sels);
        var res = dlg.showAndWait();
        if (res.isEmpty()) return; // cancelado

        var editados = res.get();
        try {
            vehiculoDAO.updateBatch(editados); // tu DAO ya soporta batch
            onListarVehiculo();
            recargarVehiculosCombo();
            info("Actualizados " + editados.size() + " vehículos");
        } catch (Exception e){
            showError(e, "Actualizar vehículos (multi)");
        }
    }

    /** NUEVO: Consultar selección de vehículos. */
    @FXML public void onConsultarVehiculos(){
        var sels = new ArrayList<>(tblVehiculos.getSelectionModel().getSelectedItems());
        if (sels.isEmpty()) { warn("Selecciona uno o más vehículos"); return; }
        TableView<Vehiculo> tv = new TableView<>();
        tv.setItems(FXCollections.observableArrayList(sels));
        TableColumn<Vehiculo, Number> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idVehiculo"));
        TableColumn<Vehiculo, String> c2 = new TableColumn<>("Matrícula");
        c2.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("matricula"));
        TableColumn<Vehiculo, String> c3 = new TableColumn<>("Marca");
        c3.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("marca"));
        TableColumn<Vehiculo, String> c4 = new TableColumn<>("Modelo");
        c4.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("modelo"));
        TableColumn<Vehiculo, Number> c5 = new TableColumn<>("IdCliente");
        c5.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idCliente"));
        tv.getColumns().addAll(c1,c2,c3,c4,c5);
        tv.setPrefHeight(360);
        showTableDialog("Vehículos seleccionados ("+sels.size()+")", tv);
    }
    @FXML public void onBorrarVehiculo(){
        var sels = new ArrayList<>(tblVehiculos.getSelectionModel().getSelectedItems());
        if (sels.isEmpty()) { warn("Selecciona uno o más vehículos"); return; }
        if (!confirm("¿Borrar "+sels.size()+" vehículo(s)?")) return;
        var ids = sels.stream().map(Vehiculo::getIdVehiculo).toList();
        try { vehiculoDAO.deleteMany(ids); onListarVehiculo(); recargarVehiculosCombo(); info("Borrados "+ids.size()+" vehículos"); }
        catch (Exception e){ showError(e, "Borrando vehículos"); }
    }

    // ===================== ÓRDENES =====================
    @FXML public void onListarOrden(){ modelOrdenes.setAll(ordenDAO.findTotales()); }
    @FXML public void onFiltrarOrdenPorEstado(){
        String est = chFiltroEstado==null? "(Todos)" : chFiltroEstado.getValue();
        modelOrdenes.setAll(ordenDAO.findByEstado(est));
    }
    @FXML public void onBuscarOrdenesAvanzado(){
        String estado = chFiltroEstado==null? "(Todos)" : chFiltroEstado.getValue();
        LocalDate desde = fODesde==null? null : fODesde.getValue();
        LocalDate hasta = fOHasta==null? null : fOHasta.getValue();
        String matricula = fOMatricula==null? "": texto(fOMatricula);
        String nomCli = fONombreCliente==null? "": texto(fONombreCliente);
        modelOrdenes.setAll(ordenDAO.findByCriterios(estado, desde, hasta, matricula, nomCli));
    }
    @FXML public void onInsertarOrden(){
        try{
            clearError(txtOId, txtODesc, chOEstado, cmbOVehiculo);
            int id = requirePositiveInt(txtOId,"ID orden");
            LocalDate fecha = optionalDateOr(LocalDate.now(), dpOFecha);
            String desc = requireNonEmpty(txtODesc,"Descripción", 255);
            String estado = requireChoice(chOEstado, "estado");
            int idVeh = requireComboId(cmbOVehiculo,"vehículo");
            ordenDAO.insert(new OrdenTrabajo(id, fecha, desc, estado, idVeh));
            onFiltrarOrdenPorEstado();
            info("Orden insertada");
        } catch (Exception e){ showError(e, "Insertar orden"); }
    }
    @FXML public void onModificarOrden(){
        try{
            var sel = tblOrdenes.getSelectionModel().getSelectedItem();
            if(sel==null) throw new IllegalArgumentException("Sin selección");
            clearError(txtODesc, chOEstado, cmbOVehiculo);
            LocalDate fecha = (dpOFecha.getValue()==null)? sel.getFecha() : dpOFecha.getValue();
            String desc = texto(txtODesc).isBlank()? sel.getDescripcion() : requireNonEmpty(txtODesc,"Descripción",255);
            String estado = (chOEstado.getValue()==null)? sel.getEstado() : chOEstado.getValue();
            int idVeh = (cmbOVehiculo.getValue()==null || cmbOVehiculo.getValue().isBlank())
                    ? sel.getIdVehiculo()
                    : requireComboId(cmbOVehiculo,"vehículo");
            ordenDAO.update(new OrdenTrabajo(sel.getIdOrden(), fecha, desc, estado, idVeh));
            onFiltrarOrdenPorEstado();
            info("Orden modificada");
        } catch (Exception e){ showError(e, "Modificar orden"); }
    }
    @FXML
    public void onModificarOrdenMulti(){
        var sels = new java.util.ArrayList<>(tblOrdenes.getSelectionModel().getSelectedItems());
        if (sels.isEmpty()) { warn("Selecciona una o más órdenes"); return; }

        var dlg = new application.ui.dialogs.BulkEditOrdenesDialog(sels);
        var res = dlg.showAndWait();
        if (res.isEmpty()) return;

        var editadas = res.get(); // List<OrdenTrabajo>
        try {
            ordenDAO.updateBatch(editadas);
            onFiltrarOrdenPorEstado(); // o onListarOrden()
            info("Actualizadas " + editadas.size() + " órdenes");
        } catch (Exception e){
            showError(e, "Actualizar órdenes (multi)");
        }
    }

    /** NUEVO: Consultar selección de órdenes. */
    @FXML public void onConsultarOrdenes(){
        var sels = new ArrayList<>(tblOrdenes.getSelectionModel().getSelectedItems());
        if (sels.isEmpty()) { warn("Selecciona una o más órdenes"); return; }
        TableView<OrdenTotal> tv = new TableView<>();
        tv.setItems(FXCollections.observableArrayList(sels));
        TableColumn<OrdenTotal, Number> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idOrden"));
        TableColumn<OrdenTotal, LocalDate> c2 = new TableColumn<>("Fecha");
        c2.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fecha"));
        TableColumn<OrdenTotal, String> c3 = new TableColumn<>("Descripción");
        c3.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("descripcion"));
        TableColumn<OrdenTotal, String> c4 = new TableColumn<>("Estado");
        c4.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("estado"));
        TableColumn<OrdenTotal, Number> c5 = new TableColumn<>("IdVeh");
        c5.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idVehiculo"));
        TableColumn<OrdenTotal, String> c6 = new TableColumn<>("Cliente");
        c6.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombreCliente"));
        TableColumn<OrdenTotal, Number> c7 = new TableColumn<>("Total (€)");
        c7.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("total"));
        tv.getColumns().addAll(c1,c2,c3,c4,c5,c6,c7);
        tv.setPrefHeight(360);
        showTableDialog("Órdenes seleccionadas ("+sels.size()+")", tv);
    }

    // -------- Exportar --------
    @FXML public void onExportBin(){ exportCurrent(true); }
    @FXML public void onExportCsv(){ exportCurrent(false); }

    private void exportCurrent(boolean bin){
        String tab = tabs.getSelectionModel().getSelectedItem().getText();
        if ("Clientes".equals(tab))      exportList(new ArrayList<>(modelClientes), bin, tblClientes);
        else if ("Vehículos".equals(tab))exportList(new ArrayList<>(modelVehiculos), bin, tblVehiculos);
        else                             exportList(new ArrayList<>(modelOrdenes),  bin, tblOrdenes);
    }
    private <T> void exportList(List<T> data, boolean bin, Control owner){
        FileChooser fc = new FileChooser();
        if (bin) fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichero binario (*.bin)", "*.bin"));
        else     fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
        File f = fc.showSaveDialog(owner.getScene().getWindow());
        if (f==null) return;
        try {
            if (bin) try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) { oos.writeObject(data); }
            else     try (PrintWriter pw = new PrintWriter(new FileWriter(f))) { for (T t: data) pw.println(String.valueOf(t)); }
            info("Exportados "+data.size()+" registros a "+f.getName());
        } catch (IOException e){ error("Exportando: "+e.getMessage()); }
    }

    // -------- Utils --------
    private void recargarClientesCombo(){
        var items = FXCollections.<String>observableArrayList();
        for (var c: clienteDAO.findAll()) items.add(c.getIdCliente()+" - "+c.getNombre());
        if (cmbVCliente!=null) cmbVCliente.setItems(items);
    }
    private void recargarVehiculosCombo(){
        var items = FXCollections.<String>observableArrayList();
        for (var v: vehiculoDAO.findAll()) items.add(v.getIdVehiculo()+" - "+v.getMatricula());
        if (cmbOVehiculo!=null) cmbOVehiculo.setItems(items);
    }
    private String nombreCliente(int id){
        for (var c: modelClientes) if (c.getIdCliente()==id) return c.getNombre();
        try { return clienteDAO.searchBy("Nombre","").stream().filter(c->c.getIdCliente()==id).findFirst().map(Cliente::getNombre).orElse(""); }
        catch (Exception ex){ return ""; }
    }
    private String texto(TextField tf){ return tf==null || tf.getText()==null? "": tf.getText().trim(); }

    private void info(String m){ new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
    private void warn(String m){ new Alert(Alert.AlertType.WARNING, m, ButtonType.OK).showAndWait(); }
    private void error(String m){ new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait(); }
    private boolean confirm(String msg){
        var a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        a.showAndWait(); return a.getResult()==ButtonType.OK;
    }
    private void showError(Exception e, String ctx){
        String msg = e.getMessage()==null? e.toString(): e.getMessage();
        if (e.getCause() instanceof SQLIntegrityConstraintViolationException || msg.contains("Duplicate"))
            error(ctx+" — Clave duplicada / restricción violada.\nDetalle: "+msg);
        else error(ctx+" — "+msg);
    }

    /** Diálogo genérico para mostrar una TableView de solo lectura. */
    private void showTableDialog(String title, TableView<?> tv){
        Dialog<Void> d = new Dialog<>();
        d.setTitle(title);
        d.getDialogPane().getButtonTypes().add(new ButtonType("Cerrar", ButtonBar.ButtonData.OK_DONE));
        d.getDialogPane().setContent(new BorderPane(tv));
        d.setResizable(true);
        d.getDialogPane().setPrefWidth(820);
        d.getDialogPane().setPrefHeight(420);
        d.showAndWait();
    }

    // TextFormatters
    private void digitsOnly(TextField... tfs){
        UnaryOperator<TextFormatter.Change> filter = ch -> ch.getControlNewText().matches("\\d*") ? ch : null;
        for (TextField tf: tfs) if (tf!=null) tf.setTextFormatter(new TextFormatter<>(filter));
    }
    private void upperAndLimit(TextField tf, int max){
        if (tf==null) return;
        tf.setTextFormatter(new TextFormatter<String>(change -> {
            String nt = change.getControlNewText().toUpperCase();
            if (nt.length()>max) return null;
            change.setText(change.getText().toUpperCase());
            return change;
        }));
    }
    private void limitLen(TextField tf, int max){
        if (tf==null) return;
        tf.setTextFormatter(new TextFormatter<String>(ch -> ch.getControlNewText().length()<=max ? ch : null));
    }

    private int idFromCombo(ComboBox<String> combo, String nombreCampo){
        var v = combo.getValue();
        if (v==null || v.isBlank()) throw new IllegalArgumentException("Selecciona "+nombreCampo);
        int idx = v.indexOf(" - ");
        return Integer.parseInt(idx>0? v.substring(0,idx) : v);
    }
}
