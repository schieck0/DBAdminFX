package view.controller;

import model.ColumnEdit;
import model.Coluna;
import model.DeleteUpdateAction;
import model.ForeignKey;
import model.ForeignKeyEdit;
import model.PrimaryKey;
import model.Schema;
import model.Tabela;
import utils.MessageBox;
import utils.SqlLog;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class TableEditController implements Initializable {

    private Tabela tabela;
    private Schema schema;
    private final List<String> tipos = new ArrayList<>();

    TableColumn<ColumnEdit, String> colNome;
    TableColumn<ColumnEdit, String> colTipo;
    TableColumn<ColumnEdit, String> colTam;
    TableColumn<ColumnEdit, String> colDefVal;
    TableColumn<ColumnEdit, Boolean> colNN;
    TableColumn<ColumnEdit, Boolean> colPK;

    TableColumn<ForeignKeyEdit, String> colFKNome;
    TableColumn<ForeignKeyEdit, ColumnEdit> colFKlocal;
    TableColumn<ForeignKeyEdit, Tabela> colFKTab;
    TableColumn<ForeignKeyEdit, Coluna> colFKremote;
    TableColumn<ForeignKeyEdit, DeleteUpdateAction> colFKupdate;
    TableColumn<ForeignKeyEdit, DeleteUpdateAction> colFKdelete;

    @FXML
    private AnchorPane anchor;
    @FXML
    private TextField tfNome;
    @FXML
    private Button bRemove;
    @FXML
    private Button bAdd;
    @FXML
    private TableView<ColumnEdit> tColunas;
    @FXML
    private Button bRemoveFK;
    @FXML
    private Button bAddFK;
    @FXML
    private TableView<ForeignKeyEdit> tFKs;
    @FXML
    private Button bOK;
    @FXML
    private ComboBox<String> cbOwner;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Object o = PrincipalController.INSTANCE.getSelectedObject();
        if (o instanceof Tabela) {
            tabela = (Tabela) o;
            schema = tabela.getSchema();
        } else if (o instanceof Schema) {
            schema = (Schema) o;
        }

        tipos.add("int4");
        tipos.add("int8");
        tipos.add("serial");
        tipos.add("float8");
        tipos.add("varchar");
        tipos.add("text");
        tipos.add("date");
        tipos.add("time");
        tipos.add("timestamp");
        tipos.add("bool");

        colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setCellFactory(TextFieldTableCell.forTableColumn());
        colNome.setEditable(true);
        colNome.setMaxWidth(300);
        colNome.setMinWidth(150);
        tColunas.getColumns().add(colNome);

        colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colTipo.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(tipos)));
        colTipo.setEditable(true);
        colTipo.setMaxWidth(100);
        colTipo.setMinWidth(100);
        colTipo.setResizable(false);
        tColunas.getColumns().add(colTipo);

        colTam = new TableColumn<>("Tamanho");
        colTam.setCellValueFactory(new PropertyValueFactory<>("tamanho"));
        colTam.setCellFactory(TextFieldTableCell.forTableColumn());
        colTam.setEditable(true);
        colTam.setMaxWidth(100);
        colTam.setMinWidth(100);
        colTam.setResizable(false);
        tColunas.getColumns().add(colTam);

        colDefVal = new TableColumn<>("Default");
        colDefVal.setCellValueFactory(new PropertyValueFactory<>("defaultVal"));
        colDefVal.setCellFactory(TextFieldTableCell.forTableColumn());
        colDefVal.setEditable(true);
        colDefVal.setMaxWidth(300);
        colDefVal.setMinWidth(150);
        tColunas.getColumns().add(colDefVal);

        colNN = new TableColumn<>("Not Null");
        colNN.setCellValueFactory(new PropertyValueFactory<>("notNull"));
        colNN.setCellFactory(CheckBoxTableCell.forTableColumn(colNN));
        colNN.setEditable(true);
        colNN.setMaxWidth(60);
        colNN.setMinWidth(60);
        colNN.setResizable(false);
        tColunas.getColumns().add(colNN);

        colPK = new TableColumn<>("PK");
        colPK.setCellValueFactory(new PropertyValueFactory<>("pk"));
        colPK.setCellFactory(CheckBoxTableCell.forTableColumn(colPK));
        colPK.setEditable(true);
        colPK.setMaxWidth(60);
        colPK.setMinWidth(60);
        colPK.setResizable(false);
        tColunas.getColumns().add(colPK);

        tColunas.setEditable(true);

        colFKNome = new TableColumn<>("Nome");
        colFKNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colFKNome.setCellFactory(TextFieldTableCell.forTableColumn());
        colFKNome.setEditable(true);
        colFKNome.setMaxWidth(300);
        colFKNome.setMinWidth(150);
        tFKs.getColumns().add(colFKNome);

        colFKlocal = new TableColumn<>("Coluna local");
        colFKlocal.setCellValueFactory(new PropertyValueFactory<>("fkCol"));
//        colFKlocal.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(tColunas.getItems())));
        colFKlocal.setEditable(true);
        colFKlocal.setMaxWidth(300);
        colFKlocal.setMinWidth(150);
        tFKs.getColumns().add(colFKlocal);

        colFKTab = new TableColumn<>("Tabela");
        colFKTab.setCellValueFactory(new PropertyValueFactory<>("pkTab"));
        colFKTab.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(schema.getBanco().getTabelas())));
        colFKTab.setEditable(true);
        colFKTab.setMaxWidth(300);
        colFKTab.setMinWidth(150);
        tFKs.getColumns().add(colFKTab);

        colFKremote = new TableColumn<>("Coluna remota");
        colFKremote.setCellValueFactory(new PropertyValueFactory<>("pkCol"));
        colFKremote.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList()));
        colFKremote.setEditable(true);
        colFKremote.setMaxWidth(300);
        colFKremote.setMinWidth(150);
        tFKs.getColumns().add(colFKremote);

        colFKupdate = new TableColumn<>("on UPDATE");
        colFKupdate.setCellValueFactory(new PropertyValueFactory<>("onUpdate"));
        colFKupdate.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(DeleteUpdateAction.values())));
        colFKupdate.setEditable(true);
        colFKupdate.setMaxWidth(100);
        colFKupdate.setMinWidth(100);
        colFKupdate.setResizable(false);
        tFKs.getColumns().add(colFKupdate);

        colFKdelete = new TableColumn<>("on DELETE");
        colFKdelete.setCellValueFactory(new PropertyValueFactory<>("onDelete"));
        colFKdelete.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(DeleteUpdateAction.values())));
        colFKdelete.setEditable(true);
        colFKdelete.setMaxWidth(100);
        colFKdelete.setMinWidth(100);
        colFKdelete.setResizable(false);
        tFKs.getColumns().add(colFKdelete);

        tFKs.setEditable(true);

        try {
            List<String> roles = schema.getBanco().getServidor().getLoginRolesNames();
            cbOwner.setItems(FXCollections.observableArrayList(roles));

            if (tabela != null) {
                tabela.loadCols();
                tfNome.setText(tabela.getNome());
                cbOwner.getSelectionModel().select(tabela.getOwner());

                for (Coluna col : tabela.getColunas()) {
                    ColumnEdit ce = new ColumnEdit(col);
                    ce.nomeProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            colFKlocal.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(tColunas.getItems())));
                        }
                    });
                    tColunas.getItems().add(ce);
                }
                colFKlocal.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(tColunas.getItems())));

                for (ForeignKey fk : tabela.getFKs()) {
                    ForeignKeyEdit fke = new ForeignKeyEdit(fk);
                    fke.pkTabProperty().addListener(new ChangeListener<Tabela>() {
                        @Override
                        public void changed(ObservableValue<? extends Tabela> observable, Tabela oldValue, Tabela newValue) {
                            try {
                                if (newValue.getColunas().isEmpty()) {
                                    newValue.loadCols();
                                }

                                colFKremote.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(newValue.getColunas())));
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                    tFKs.getItems().add(fke);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        tColunas.getItems().addListener(new ListChangeListener<ColumnEdit>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends ColumnEdit> c) {
                colFKlocal.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(tColunas.getItems())));
            }
        });
    }

    @FXML
    private void removeCol(ActionEvent event) {
        ColumnEdit ce = tColunas.getSelectionModel().getSelectedItem();
        tColunas.getItems().remove(ce);
    }

    @FXML
    private void addCol(ActionEvent event) {
        ColumnEdit ce = new ColumnEdit();
        if (tColunas.getItems().isEmpty()) {
            ce.setNome("id");
            ce.setPk(true);
            ce.setTipo(tipos.get(2));//serial
            ce.setNotNull(true);
        }

        ce.nomeProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                colFKlocal.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(tColunas.getItems())));
                if (newValue.startsWith("id_")) {
                    ce.setTipo(tipos.get(0));

                    //ADD FK
                    ForeignKeyEdit fke = newFK();
                    fke.setFkCol(ce);
                    tFKs.getItems().add(fke);
                }
            }
        });
        tColunas.getItems().add(ce);
    }

    @FXML
    private void removeFK(ActionEvent event) {
        ForeignKeyEdit fke = tFKs.getSelectionModel().getSelectedItem();
        tFKs.getItems().remove(fke);
    }

    @FXML
    private void addFK(ActionEvent event) {
        tFKs.getItems().add(newFK());
    }

    private ForeignKeyEdit newFK() {
        ForeignKeyEdit fke = new ForeignKeyEdit();
        fke.setOnUpdate(DeleteUpdateAction.NO_ACTION);
        fke.setOnDelete(DeleteUpdateAction.NO_ACTION);

        fke.pkTabProperty().addListener(new ChangeListener<Tabela>() {
            @Override
            public void changed(ObservableValue<? extends Tabela> observable, Tabela oldValue, Tabela newValue) {
                try {
                    if (newValue.getColunas().isEmpty()) {
                        newValue.loadCols();
                    }

                    colFKremote.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(newValue.getColunas())));
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        fke.fkColProperty().addListener(new ChangeListener<ColumnEdit>() {
            @Override
            public void changed(ObservableValue<? extends ColumnEdit> observable, ColumnEdit oldValue, ColumnEdit newValue) {
                if (newValue.getNome().startsWith("id_")) {
                    for (Tabela t : schema.getBanco().getTabelas()) {
                        if (t.getSchema().equals(schema) && t.getNome().equals(newValue.getNome().replace("id_", ""))) {
                            fke.setPkTab(t);
                            break;
                        }
                    }

                    if (fke.getPkTab() == null) {
                        for (Tabela t : schema.getBanco().getTabelas()) {
                            if (t.getNome().equals(newValue.getNome().replace("id_", ""))) {
                                fke.setPkTab(t);
                                break;
                            }
                        }
                    }

                    if (fke.getPkTab() != null) {
                        for (Coluna c : fke.getPkTab().getColunas()) {
                            if (c.getNome().equals("id")) {
                                fke.setPkCol(c);
                                break;
                            }
                        }
                    }
                }
            }
        });

        return fke;
    }

    @FXML
    private void processar(ActionEvent event) {
        if (tColunas.getItems().isEmpty()) {
            MessageBox.warning("Deve ter no mínimo uma coluna.");
            return;
        }

        String ddl = "";
        try (Connection conn = schema.connect()) {
            bOK.setDisable(true);
            bOK.setText("Processando...");
            Tabela tabNova = new Tabela(schema, tfNome.getText());
            tabNova.setOwner(cbOwner.getSelectionModel().getSelectedItem());
//            tabNova.getColunas().addAll(tmCC.getCols());
//            tabNova.getFKs().addAll(tmFK.getFKs());

            List<Coluna> newCols = new ArrayList<>();
            for (ColumnEdit ce : tColunas.getItems()) {
                Coluna c = new Coluna(ce.getNome());
                c.setNomeAnt(ce.getNomeAnt());
                try {
                    c.setTamanho(Integer.parseInt(ce.getTamanho()));
                } catch (NumberFormatException e) {
                }
                c.setDefaultVal(ce.getDefaultVal());
                c.setPk(ce.isPk());
                c.setTipo(ce.getTipo());
                c.setNullable(ce.getNotNull() ? 0 : 1);
                newCols.add(c);
            }
            List<ForeignKey> newFKs = new ArrayList<>();
            for (ForeignKeyEdit fke : tFKs.getItems()) {
                ForeignKey fk = new ForeignKey();
                fk.setNome(fke.getNome());
                fk.setPkTab(fke.getPkTab());
                fk.setPkCol(fke.getPkCol());
                fk.setFkTab(tabela);
                fk.setFkCol(new Coluna(fke.getFkCol().getNome()));
                fk.setOnUpdate(fke.getOnUpdate());
                fk.setOnDelete(fke.getOnDelete());
                newFKs.add(fk);
            }

            tabNova.getColunas().addAll(newCols);
            tabNova.getFKs().addAll(newFKs);

            if (tabela == null) {
                if (tabNova.getPKs().isEmpty() && MessageBox.confirm("Nenhuma Chave Primária foi definida. Continuar?")) {
                    return;
                }
                ddl = "create table " + tabNova.getNome() + " (";

                for (Coluna c : tabNova.getColunas()) {
                    ddl += "\n\t" + c.getNome() + " " + c.getTipo() + (c.getTipo().equals("varchar") ? ("(" + c.getTamanho() + ")") : "")
                            + (c.getNullable() == 0 ? " NOT NULL " : "")
                            + (c.getDefaultVal() != null && !c.getDefaultVal().trim().isEmpty() ? " DEFAULT " + c.getDefaultVal() : "")
                            + ",";
                }

//                        if (tabNova.getFKs().isEmpty() && tabNova.getPKs().isEmpty()) {
//                            ddl = ddl.substring(0, ddl.length() - 1);
//                        }
                for (PrimaryKey pk : tabNova.getPKs()) {
                    ddl += "\n\tPRIMARY KEY (" + pk.getColuna().getNome() + "),";
                }

//                        if (tabNova.getFKs().isEmpty()) {
//                            ddl = ddl.substring(0, ddl.length() - 1);
//                        }
                for (ForeignKey fk : tabNova.getFKs()) {
                    String onUp = fk.getOnUpdate().getText();
                    String onDel = fk.getOnDelete().getText();

                    ddl += "\n\t" + (fk.getNome() != null && !fk.getNome().trim().isEmpty() ? "CONSTRAINT " + fk.getNome() : "")
                            + " FOREIGN KEY (" + fk.getFkCol().getNome() + ") REFERENCES "
                            + (fk.getPkTab().getSchema() != null ? (fk.getPkTab().getSchema().getNome() + ".") : "") + fk.getPkTab().getNome() + "(" + fk.getPkCol().getNome() + ")"
                            + " ON UPDATE " + onUp + " ON DELETE " + onDel + ",";
                }
//                        if (!tabNova.getFKs().isEmpty()) {
//                            ddl = ddl.substring(0, ddl.length() - 1);
//                        }
                if (ddl.endsWith(",")) {
                    ddl = ddl.substring(0, ddl.length() - 1);
                }

                ddl += "\n);\n";
                if (tabNova.getOwner() != null) {
                    ddl += "ALTER TABLE " + tabNova.getNome() + " OWNER TO " + tabNova.getOwner() + ";\n";
                }
            } else {
                tabela.loadCols();
                //COMPARA COLUNAS
                for (Coluna colAtual : tabNova.getColunas()) {
                    if (colAtual.getNomeAnt() == null) {
                        ddl += "ALTER TABLE " + tabela.getNome() + " ADD COLUMN "
                                + colAtual.getNome() + " " + colAtual.getTipo() + (colAtual.getTipo().equals("varchar") ? "(" + colAtual.getTamanho() + ")" : "")
                                + (colAtual.getNullable() == 0 ? " NOT NULL " : "")
                                + (colAtual.getDefaultVal() != null && !colAtual.getDefaultVal().trim().isEmpty() ? " DEFAULT " + colAtual.getDefaultVal() : "")
                                + ";\n";
                    } else {
                        Coluna cOld = null;
                        for (Coluna c : tabela.getColunas()) {
                            if (Objects.equals(c.getNomeAnt(), colAtual.getNomeAnt())) {
                                cOld = c;
                                break;
                            }
                        }

                        if (!colAtual.getTipo().equals(cOld.getTipo()) || colAtual.getTamanho() != cOld.getTamanho()) {
                            ddl += "ALTER TABLE " + tabela.getNome() + " ALTER COLUMN " + colAtual.getNome() + " TYPE " + colAtual.getTipo()
                                    + (colAtual.getTipo().equals("varchar") ? "(" + colAtual.getTamanho() + ")" : "") + ";\n";
                        }
                        if (colAtual.getNullable() != cOld.getNullable()) {
                            ddl += "ALTER TABLE " + tabela.getNome() + " ALTER COLUMN "
                                    + colAtual.getNome() + (colAtual.isNotNull() ? " SET" : " DROP") + " NOT NULL;\n";
                        }
                        if (!colAtual.getNome().equals(colAtual.getNomeAnt())) {
                            ddl += "ALTER TABLE " + tabela.getNome() + " RENAME " + colAtual.getNomeAnt() + " TO " + colAtual.getNome() + ";\n";
                        }
                        if (!Objects.equals(colAtual.getDefaultVal(), cOld.getDefaultVal())) {
                            boolean dropDef = colAtual.getDefaultVal() == null || colAtual.getDefaultVal().trim().isEmpty();
                            ddl += "ALTER TABLE " + tabela.getNome() + " ALTER COLUMN " + colAtual.getNome()
                                    + (dropDef ? " DROP" : " SET") + " DEFAULT " + (dropDef ? "" : colAtual.getDefaultVal()) + ";\n";
                        }
                    }
                }

                col:
                for (Coluna cOld : tabela.getColunas()) {
                    for (Coluna cNova : tabNova.getColunas()) {
                        if (Objects.equals(cNova.getNomeAnt(), cOld.getNome())) {
                            continue col;
                        }
                    }

                    ddl += "ALTER TABLE " + tabela.getNome() + " DROP COLUMN " + cOld.getNome() + ";\n";
                }

                //COMPARA FOREIGNKEYS
                for (ForeignKey fkAtual : tabNova.getFKs()) {
                    if (!tabela.getFKs().contains(fkAtual)) {
                        ddl += "ALTER TABLE " + tabela.getNome()
                                + " ADD " + (fkAtual.getNome() != null && !fkAtual.getNome().trim().isEmpty() ? "CONSTRAINT " + fkAtual.getNome() : "")
                                + " FOREIGN KEY (" + fkAtual.getFkCol().getNome() + ") REFERENCES "
                                + (fkAtual.getPkTab().getSchema() != null ? (fkAtual.getPkTab().getSchema().getNome() + ".") : "")
                                + fkAtual.getPkTab().getNome() + "(" + fkAtual.getPkCol().getNome()
                                + ") ON UPDATE " + fkAtual.getOnUpdate().getText() + " ON DELETE " + fkAtual.getOnDelete().getText() + ";\n";
                    } else {
                        ForeignKey fkOld = tabela.getFKs().get(tabNova.getFKs().indexOf(fkAtual));
                        if (!fkAtual.getOnUpdate().equals(fkOld.getOnUpdate()) || !fkAtual.getOnDelete().equals(fkOld.getOnDelete())) {
                            ddl += "ALTER TABLE " + tabela.getNome() + " DROP CONSTRAINT " + fkOld.getNome() + ";\n"
                                    + "ALTER TABLE " + tabela.getNome()
                                    + " ADD " + (fkAtual.getNome() != null && !fkAtual.getNome().trim().isEmpty() ? "CONSTRAINT " + fkAtual.getNome() : "")
                                    + " FOREIGN KEY (" + fkAtual.getFkCol().getNome() + ") REFERENCES "
                                    + (fkAtual.getPkTab().getSchema() != null ? (fkAtual.getPkTab().getSchema().getNome() + ".") : "")
                                    + fkAtual.getPkTab().getNome() + "(" + fkAtual.getPkCol().getNome()
                                    + ") ON UPDATE " + fkAtual.getOnUpdate().getText() + " ON DELETE " + fkAtual.getOnDelete().getText() + ";\n";
                        }
                    }
                }
                for (ForeignKey fkOld : tabela.getFKs()) {
                    if (!tabNova.getFKs().contains(fkOld)) {
                        ddl += "ALTER TABLE " + tabela.getNome()
                                + " DROP CONSTRAINT " + fkOld.getNome() + ";\n";
                    }
                }

                //verifica alteração de PK
//                        for (Coluna c : tabNova.getColunas()) {
//                            if (c.isPk()) {
//                                PrimaryKey pk = new PrimaryKey(c.getNome());
//                                if (tabela.getPKs().contains(pk)) {
//                                    pk.setNome(tabela.getPKs().get(tabela.getPKs().indexOf(pk)).getNome());
//                                }
//
//                                tabNova.getPKs().add(pk);
//                            }
//                        }
                boolean newPK = false;
                for (PrimaryKey pk : tabela.getPKs()) {
                    if (!tabNova.getPKs().contains(pk)) {
                        ddl += "ALTER TABLE " + tabela.getNome() + " DROP CONSTRAINT " + pk.getNome() + ";\n";
                        newPK = true;
                        break;
                    }
                }
                if (!newPK) {
                    for (PrimaryKey pk : tabNova.getPKs()) {
                        if (!tabela.getPKs().contains(pk) && !tabela.getPKs().isEmpty()) {
                            ddl += "ALTER TABLE " + tabela.getNome() + " DROP CONSTRAINT " + tabela.getPKs().get(0).getNome() + ";\n";
                            newPK = true;
                            break;
                        }
                    }
                }

                if (tabNova.getPKs().isEmpty() && MessageBox.confirm("Nenhuma Chave Primária foi definida. Continuar?")) {
                    return;
                }
                if ((newPK || tabNova.getPKs().size() != tabela.getPKs().size()) && !tabNova.getPKs().isEmpty()) {
                    String pks = "";
                    for (int i = 0; i < tabNova.getPKs().size(); i++) {
                        pks += tabNova.getPKs().get(i).getColuna().getNome();
                        if (i < tabNova.getPKs().size() - 1) {
                            pks += ", ";
                        }
                    }
                    ddl += "ALTER TABLE " + tabela.getNome() + " ADD PRIMARY KEY (" + pks + ");\n";
                }

                if (!tabNova.getNome().equals(tabela.getNome())) {
                    ddl += "ALTER TABLE " + tabela.getNome() + " RENAME TO " + tabNova.getNome() + ";";
                }

                if (!tabNova.getOwner().equals(tabela.getOwner())) {
                    ddl += "ALTER TABLE " + tabela.getNome() + " OWNER TO " + tabNova.getOwner() + ";";
                }
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Clique no botão abaixo para ver a sql gerada.", ButtonType.CANCEL, ButtonType.OK);

            Label label = new Label("Comando DDL:");
            TextArea textArea = new TextArea(ddl);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);
            alert.setHeaderText("Aplicar alterações?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                conn.createStatement().executeUpdate(ddl);
                SqlLog.append(ddl);
                PrincipalController.INSTANCE.refresh(schema);
                PrincipalController.INSTANCE.closeSelectedTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.errorMessage(ex);
        } finally {
            bOK.setText("OK");
            bOK.setDisable(false);
        }

    }
}
