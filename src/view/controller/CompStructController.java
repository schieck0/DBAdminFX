package view.controller;

import model.Alteracao;
import model.Coluna;
import model.ForeignKey;
import model.Operacao;
import model.PrimaryKey;
import model.Schema;
import model.Sequence;
import model.Tabela;
import model.TipoObjeto;
import utils.MessageBox;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.WindowEvent;
import javax.swing.JOptionPane;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class CompStructController implements Initializable {

    private Schema schema1, schema2;
    private final ContextMenu contextMenu = new ContextMenu();

    @FXML
    private Button bComp;
    @FXML
    private Button bInvert;
    @FXML
    private Label lDe;
    @FXML
    private Label lPara;
    @FXML
    private Button bSql;
    @FXML
    private Button bExecDDL;
    @FXML
    private TableView<Alteracao> tAlter;
    @FXML
    private TextArea taSql;
    @FXML
    private Label lStatus;
    @FXML
    private AnchorPane anchor;
    @FXML
    private CheckBox ckSelect;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bInvert.setTooltip(new Tooltip("Inverter"));
        List sel = PrincipalController.INSTANCE.getSelectedObjects();
        schema1 = (Schema) sel.get(0);
        schema2 = (Schema) sel.get(1);

        lDe.setText(schema1.getBanco().getServidor().getNome() + "." + schema1.getBanco().getNome() + "." + schema1.getNome());
        lPara.setText(schema2.getBanco().getServidor().getNome() + "." + schema2.getBanco().getNome() + "." + schema2.getNome());
        tAlter.setPlaceholder(new Label(""));

        TableColumn<Alteracao, String> colOp = new TableColumn<>("Operação");
        colOp.setCellValueFactory((TableColumn.CellDataFeatures<Alteracao, String> p) -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return p.getValue().getOperacao().getValue();
            }
        });
//        colOp.setMaxWidth(150);
//        colOp.setMinWidth(150);
//        colOp.setResizable(false);
        tAlter.getColumns().add(colOp);

        TableColumn<Alteracao, String> colObj = new TableColumn<>("Objeto");
        colObj.setCellValueFactory((TableColumn.CellDataFeatures<Alteracao, String> p) -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                if (p.getValue().getObjeto() instanceof Coluna) {
                    Coluna c = (Coluna) p.getValue().getObjeto();
                    return c.getNome() + " (" + c.getTabela().getNome() + ")";
                } else if (p.getValue().getObjeto() instanceof ForeignKey) {
                    ForeignKey c = (ForeignKey) p.getValue().getObjeto();
                    return c.getFkCol().getNome() + " (" + c.getFkTab().getNome() + ")   ->   " + c.getPkCol().getNome() + " (" + c.getPkTab().getNome() + ")";
                } else {
                    return p.getValue().getObjeto().toString();
                }
            }
        });
//        colObj.setMinWidth(300);
        tAlter.getColumns().add(colObj);

        TableColumn<Alteracao, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory((TableColumn.CellDataFeatures<Alteracao, String> p) -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return p.getValue().getTipo().getValue();
            }
        });
//        colTipo.setMaxWidth(150);
//        colTipo.setMinWidth(150);
//        colTipo.setResizable(false);
        tAlter.getColumns().add(colTipo);

        TableColumn<Alteracao, Boolean> colGerar = new TableColumn<>("Gerar");
        colGerar.setCellValueFactory(new PropertyValueFactory<>("gerar"));
        colGerar.setCellFactory(CheckBoxTableCell.forTableColumn(colGerar));
        colGerar.setEditable(true);
//        colGerar.setMaxWidth(100);
//        colGerar.setMinWidth(100);
//        colGerar.setResizable(false);
        tAlter.getColumns().add(colGerar);
        tAlter.setEditable(true);
        
        
        tAlter.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colOp.setMaxWidth(1f * Integer.MAX_VALUE * 20);
        colObj.setMaxWidth(1f * Integer.MAX_VALUE * 40);
        colTipo.setMaxWidth(1f * Integer.MAX_VALUE * 25); 
        colGerar.setMaxWidth(1f * Integer.MAX_VALUE * 15); 
        
        

        MenuItem miRename = new MenuItem("Rename");
        contextMenu.getItems().add(miRename);
        miRename.setOnAction(new EventHandler() {
            public void handle(Event e) {
                Alteracao alteracao = tAlter.getSelectionModel().getSelectedItem();
//                new JDRenomearObjeto(tmAlter, alter).setVisible(true);
                List<Alteracao> alters = new ArrayList<>();
                for (Alteracao a : tAlter.getItems()) {
                    if (a.getTipo().equals(alteracao.getTipo()) && a.getOperacao().equals(Operacao.CRIAR)) {
                        if (a.getTipo().equals(alteracao.getTipo())) {
                            alters.add(a);
                        }
                    }
                }

                ChoiceDialog<Alteracao> dialog = new ChoiceDialog<>(null, alters);
                dialog.setTitle("Renomear " + alteracao.getTipo().getValue());
                dialog.setHeaderText("Selecione o item para qual deseja renomear:");
                dialog.setContentText(alteracao.getTipo().getValue() + ": ");

                Optional<Alteracao> result = dialog.showAndWait();
                if (result.isPresent()) {
                    if (alteracao.getTipo().equals(TipoObjeto.TABELA)) {
                        Tabela tOld = (Tabela) alteracao.getObjeto();
                        Tabela tNew = (Tabela) result.get().getObjeto();
                        Alteracao alter = new Alteracao(Operacao.ALTERAR, tOld, TipoObjeto.TABELA, true, true,
                                "ALTER TABLE " + tOld.getFullName() + " RENAME TO " + tNew.getNome() + ";");
                        tAlter.getItems().remove(alteracao);
                        tAlter.getItems().remove(new Alteracao(null, new Tabela(null, tNew.getNome()), null, false, false, null));
                        tAlter.getItems().add(alter);

//            for (Coluna c1 : tNew.getColunas()) {
//                if (!tOld.getColunas().contains(c1)) {
//                    tmAlter.addAlteracao(new Alteracao(Operacao.CRIAR, c1, TipoObjeto.COLUNA, true, true,
//                            "ALTER TABLE " + tNew.toString() + " ADD COLUMN "
//                            + c1.getNome() + " " + c1.getTipo() + (c1.getTipo().equals("varchar") ? "(" + c1.getTamanho() + ")" : "") + (c1.getNullable() == 0 ? " NOT NULL" : "") + ";"));
//                } else {
//                    Coluna c2 = tOld.getColunas().get(tOld.getColunas().indexOf(c1));
//                    if (!c1.getTipo().equals(c2.getTipo()) || c1.getTamanho() != c2.getTamanho()) {
//                        tmAlter.addAlteracao(new Alteracao(Operacao.ALTERAR, c1, TipoObjeto.COLUNA, true, true,
//                                "ALTER TABLE " + tNew.toString() + " ALTER COLUMN "
//                                + c1.getNome() + " TYPE " + c1.getTipo() + (c1.getTipo().equals("varchar") ? "(" + c1.getTamanho() + ")" : "") + ";"));
//                    }
//                    if (c1.getNullable() != c2.getNullable()) {
//                        tmAlter.addAlteracao(new Alteracao(Operacao.ALTERAR, c1, TipoObjeto.COLUNA, true, true,
//                                "ALTER TABLE " + tNew.toString() + " ALTER COLUMN "
//                                + c1.getNome() + (c1.getNullable() == 0 ? " SET" : " DROP") + " NOT NULL;"));
//                    }
//                }
//            }
//            for (Coluna c2 : tOld.getColunas()) {
//                if (!tNew.getColunas().contains(c2)) {
//                    tmAlter.addAlteracao(new Alteracao(Operacao.REMOVER, c2, TipoObjeto.COLUNA, true, true,
//                            "ALTER TABLE " + tOld.toString() + " DROP COLUMN " + c2.getNome() + ";"));
//                }
//            }
                    } else if (alteracao.getTipo().equals(TipoObjeto.SEQUENCE)) {
                        Sequence s = (Sequence) alteracao.getObjeto();
                        Sequence sNew = (Sequence) result.get().getObjeto();
                        Alteracao alter = new Alteracao(Operacao.ALTERAR, s, TipoObjeto.SEQUENCE, true, true,
                                "ALTER SEQUENCE " + s.getFullName() + " RENAME TO " + sNew.getNome() + ";");
                        tAlter.getItems().remove(alteracao);
                        tAlter.getItems().remove(new Alteracao(null, new Sequence(sNew.getNome()), null, false, false, null));
                        tAlter.getItems().add(alter);
                    } else if (alteracao.getTipo().equals(TipoObjeto.COLUNA)) {
                        Coluna c = (Coluna) alteracao.getObjeto();
                        Coluna cNew = (Coluna) result.get().getObjeto();
                        Alteracao alter = new Alteracao(Operacao.ALTERAR, c, TipoObjeto.COLUNA, true, true,
                                "ALTER TABLE " + c.getTabela().getFullName() + " RENAME " + c.getNome() + " TO " + cNew.getNome() + ";");
                        tAlter.getItems().remove(alteracao);
                        tAlter.getItems().remove(new Alteracao(null, new Coluna(cNew.getNome()), null, false, false, null));
                        tAlter.getItems().add(alter);
                    }
                }
            }
        });

        MenuItem miRecreate = new MenuItem("Recreate");
        contextMenu.getItems().add(miRecreate);
        miRecreate.setOnAction(new EventHandler() {
            public void handle(Event e) {
                Alteracao alter = tAlter.getSelectionModel().getSelectedItem();
                tAlter.getItems().remove(alter);

                Coluna c = (Coluna) alter.getObjeto();

                Alteracao drop = new Alteracao(Operacao.REMOVER, alter.getObjeto(), TipoObjeto.COLUNA, true, true,
                        "ALTER TABLE " + c.getTabela().toString() + " DROP COLUMN " + c.getNome() + ";");
                tAlter.getItems().add(drop);

                Alteracao create = new Alteracao(Operacao.CRIAR, alter.getObjeto(), TipoObjeto.COLUNA, true, true,
                        "ALTER TABLE " + c.getTabela().toString() + " ADD COLUMN "
                        + c.getNome() + " " + c.getTipo() + (c.getTipo().equals("varchar") ? "(" + c.getTamanho() + ")" : "")
                        + (c.getNullable() == 0 ? " NOT NULL" : "") + ";");
                tAlter.getItems().add(create);
            }
        });

        contextMenu.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Alteracao alter = tAlter.getSelectionModel().getSelectedItem();
                if (alter != null) {
                    if (alter.getOperacao().equals(Operacao.REMOVER)
                            && (alter.getTipo().equals(TipoObjeto.TABELA) || alter.getTipo().equals(TipoObjeto.SEQUENCE) || alter.getTipo().equals(TipoObjeto.COLUNA))) {
                        miRename.setVisible(true);
                        miRecreate.setVisible(false);
                    } else if (alter.getOperacao().equals(Operacao.ALTERAR) && alter.getTipo().equals(TipoObjeto.COLUNA)) {
                        miRename.setVisible(false);
                        miRecreate.setVisible(true);
                    } else {
                        miRename.setVisible(false);
                        miRecreate.setVisible(false);
                    }
                } else {
                    miRename.setVisible(false);
                    miRecreate.setVisible(false);
                }
            }
        });
    }

    @FXML
    private void comparar(ActionEvent event) {
        bComp.setDisable(true);
        lStatus.setText("Conectando...");
        tAlter.getItems().clear();
        taSql.setText("");

        Connection conn1;
        try {
            conn1 = schema1.getBanco().connect();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex.getMessage());
            bComp.setDisable(false);
            return;
        }
        Connection conn2;
        try {
            conn2 = schema2.getBanco().connect();
        } catch (SQLException ex) {
            if (ex.getMessage().endsWith("does not exist") && JOptionPane.showConfirmDialog(null, "Base não existe. Deseja criar?") == JOptionPane.YES_OPTION) {
                try {
                    conn2 = schema2.getBanco().connect();
                } catch (SQLException ex1) {
                    ex1.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Erro ao criar base:\n" + ex1.getMessage());
                    bComp.setDisable(false);
                    return;
                }

            } else {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, ex.getMessage());
                bComp.setDisable(false);
                return;
            }
        }

        try {
            //COMPARA TABELAS
            lStatus.setText("Lendo tabelas...");
            schema1.loadSequences();
            schema1.loadTables();
            List<Tabela> tabelas1 = new ArrayList<>(schema1.getTabelas());
            Set<String> tabNames1 = new HashSet<>();
            for (Tabela t : schema1.getTabelas()) {
                t.loadCols();
                tabNames1.add(t.getNome());
            }

            schema2.loadSequences();
            schema2.loadTables();
            List<Tabela> tabelas2 = new ArrayList<>(schema2.getTabelas());
            Set<String> tabNames2 = new HashSet<>();
            for (Tabela t : schema2.getTabelas()) {
                t.loadCols();
                tabNames2.add(t.getNome());
            }

            List<Alteracao> alteracoes = new ArrayList<>();

            for (Tabela t1 : tabelas1) {
                lStatus.setText("Analisando Tabelas...");

                if (!tabNames2.contains(t1.getNome())) {
                    String ddl = "CREATE TABLE " + t1.getFullName() + " (";
                    for (Coluna c : t1.getColunas()) {
                        ddl += "\n\t" + c.getNome() + " " + c.getTipo() + (c.getTipo().equals("varchar") ? "(" + c.getTamanho() + ")" : "") + (c.getNullable() == 0 ? " NOT NULL" : "") + ",";
                    }
                    if (t1.getFKs().isEmpty() && t1.getPKs().isEmpty()) {
                        ddl = ddl.substring(0, ddl.length() - 1);
                    }

                    for (PrimaryKey pk : t1.getPKs()) {
                        ddl += "\n\tPRIMARY KEY (" + pk.getColuna().getNome() + "),";
                    }

                    if (t1.getFKs().isEmpty()) {
                        ddl = ddl.substring(0, ddl.length() - 1);
                    }

                    for (ForeignKey fk : t1.getFKs()) {
                        String onUp = fk.getOnUpdate().getText();
                        String onDel = fk.getOnDelete().getText();

//                                if (fk.getOnUpdate() == 0) {
//                                    onUp = "CASCADE";
//                                }
//                                if (fk.getOnDelete() == 0) {
//                                    onDel = "CASCADE";
//                                }
                        ddl += "\n\tFOREIGN KEY (" + fk.getFkCol().getNome() + ") REFERENCES "
                                + t1.getSchema() + "." + fk.getPkTab().getNome() + "(" + fk.getPkCol().getNome() + ")"
                                + " ON UPDATE " + onUp + " ON DELETE " + onDel + ",";
                    }
                    if (!t1.getFKs().isEmpty()) {
                        ddl = ddl.substring(0, ddl.length() - 1);
                    }

                    ddl += "\n) WITH (OIDS = FALSE);\n"
                            + "ALTER TABLE " + t1.getFullName() + " OWNER TO iconvergence;";
                    alteracoes.add(new Alteracao(Operacao.CRIAR, t1, TipoObjeto.TABELA, true, true, ddl));
                } else {
                    Tabela t2 = null;
                    for (Tabela tt : tabelas2) {
                        if (tt.getNome().equals(t1.getNome())) {
                            t2 = tt;
                        }
                    }

                    //COMPARA COLUNAS
                    for (Coluna c1 : t1.getColunas()) {
                        if (!t2.getColunas().contains(c1)) {
                            alteracoes.add(new Alteracao(Operacao.CRIAR, c1, TipoObjeto.COLUNA, true, true,
                                    "ALTER TABLE " + t2.getFullName() + " ADD COLUMN "
                                    + c1.getNome() + " " + c1.getTipo() + (c1.getTipo().equals("varchar") ? "(" + c1.getTamanho() + ")" : "") + (c1.getNullable() == 0 ? " NOT NULL" : "") + ";"));
                        } else {
                            Coluna c2 = t2.getColunas().get(t2.getColunas().indexOf(c1));
                            if (!c1.getTipo().equals(c2.getTipo()) || c1.getTamanho() != c2.getTamanho()) {
                                alteracoes.add(new Alteracao(Operacao.ALTERAR, c1, TipoObjeto.COLUNA, true, true,
                                        "ALTER TABLE " + t2.getFullName() + " ALTER COLUMN "
                                        + c1.getNome() + " TYPE " + c1.getTipo() + (c1.getTipo().equals("varchar") ? "(" + c1.getTamanho() + ")" : "") + ";"));
                            }
                            if (c1.getNullable() != c2.getNullable()) {
                                alteracoes.add(new Alteracao(Operacao.ALTERAR, c1, TipoObjeto.COLUNA, true, true,
                                        "ALTER TABLE " + t2.getFullName() + " ALTER COLUMN " + c1.getNome()
                                        + (c1.getNullable() == 0 ? " SET" : " DROP") + " NOT NULL;"));
                            }
                            if (!Objects.equals(c1.getDefaultVal(), c2.getDefaultVal())) {
                                boolean dropDef = c1.getDefaultVal() == null || c1.getDefaultVal().trim().isEmpty();
                                alteracoes.add(new Alteracao(Operacao.ALTERAR, c1, TipoObjeto.COLUNA, true, true,
                                        "ALTER TABLE " + t2.getFullName() + " ALTER COLUMN " + c1.getNome()
                                        + (dropDef ? " DROP" : " SET") + " DEFAULT " + (dropDef ? "" : c1.getDefaultVal()) + ";"));
                            }
                        }
                    }
                    for (Coluna c2 : t2.getColunas()) {
                        if (!t1.getColunas().contains(c2)) {
                            alteracoes.add(new Alteracao(Operacao.REMOVER, c2, TipoObjeto.COLUNA, true, true,
                                    "ALTER TABLE " + t2.getFullName() + " DROP COLUMN " + c2.getNome() + ";"));
                        }
                    }

                    //COMPARA FOREIGNKEYS
                    for (ForeignKey fk : t1.getFKs()) {
                        if (!t2.getFKs().contains(fk)) {
                            String onUp = fk.getOnUpdate().getText();
                            String onDel = fk.getOnDelete().getText();

//                                    if (fk.getOnUpdate() == 0) {
//                                        onUp = "CASCADE";
//                                    }
//                                    if (fk.getOnDelete() == 0) {
//                                        onDel = "CASCADE";
//                                    }
                            alteracoes.add(new Alteracao(Operacao.CRIAR, fk, TipoObjeto.CHAVE, true, true,
                                    "ALTER TABLE " + t2.getFullName()
                                    + " ADD " + (fk.getNome() != null && !fk.getNome().trim().isEmpty() ? "CONSTRAINT " + fk.getNome() : "")
                                    + " FOREIGN KEY (" + fk.getFkCol().getNome() + ") REFERENCES "
                                    + t2.getSchema() + "." + fk.getPkTab().getNome()
                                    + "(" + fk.getPkCol().getNome() + ") ON UPDATE " + onUp + " ON DELETE " + onDel + ";"));
                        } else {
                            ForeignKey fk2 = t2.getFKs().get(t2.getFKs().indexOf(fk));
                            if (fk.getOnUpdate() != fk2.getOnUpdate() || fk.getOnDelete() != fk2.getOnDelete()) {
                                String onUp = fk.getOnUpdate().getText();
                                String onDel = fk.getOnDelete().getText();

//                                        if (fk.getOnUpdate() == 0) {
//                                            onUp = "CASCADE";
//                                        }
//                                        if (fk.getOnDelete() == 0) {
//                                            onDel = "CASCADE";
//                                        }
                                alteracoes.add(new Alteracao(Operacao.ALTERAR, fk, TipoObjeto.CHAVE, true, true,
                                        "ALTER TABLE " + t2.getFullName()
                                        + " DROP CONSTRAINT " + fk2.getNome() + ";\n"
                                        + "ALTER TABLE " + t2.getFullName()
                                        + " ADD " + (fk.getNome() != null && !fk.getNome().trim().isEmpty() ? "CONSTRAINT " + fk.getNome() : "")
                                        + " FOREIGN KEY (" + fk.getFkCol().getNome() + ") REFERENCES "
                                        + t2.getSchema() + "." + fk.getPkTab().getNome()
                                        + "(" + fk.getPkCol().getNome() + ") ON UPDATE " + onUp + " ON DELETE " + onDel + ";"));
                            }
                        }
                    }
                    for (ForeignKey c2 : t2.getFKs()) {
                        if (!t1.getFKs().contains(c2)) {
                            alteracoes.add(new Alteracao(Operacao.REMOVER, c2, TipoObjeto.CHAVE, true, true,
                                    "ALTER TABLE " + t2.getFullName()
                                    + " DROP CONSTRAINT " + c2.getNome() + ";"));
                        }
                    }
                }
            }

            for (Tabela t2 : tabelas2) {
                if (!tabNames1.contains(t2.getNome())) {
                    alteracoes.add(new Alteracao(Operacao.REMOVER, t2, TipoObjeto.TABELA, true, true,
                            "DROP TABLE " + t2.getFullName() + ";"));
                }
            }

            //COMPARA SEQUENCES
            lStatus.setText("Analisando Sequences...");
            List<Sequence> sequences1 = new ArrayList<>(schema1.getSequences());
            List<Sequence> sequences2 = new ArrayList<>(schema2.getSequences());
            s:
            for (Sequence s1 : sequences1) {
                if (!sequences2.contains(s1)) {
                    for (Tabela t : tabelas1) {
                        if (t.getPKs().get(0).getColuna().getTipo().equals("serial")
                                && s1.getNome().equals(t.getNome() + "_" + t.getPKs().get(0).getColuna().getNome() + "_seq")) {
                            continue s;
                        }
                    }
                    alteracoes.add(new Alteracao(Operacao.CRIAR, s1, TipoObjeto.SEQUENCE, true, true,
                            "CREATE SEQUENCE " + s1.getFullName() + ";\n"
                            + "ALTER SEQUENCE " + s1.getFullName() + " OWNER TO iconvergence;"));
                }
            }

            for (Sequence s2 : sequences2) {
                if (!sequences1.contains(s2)) {
                    alteracoes.add(new Alteracao(Operacao.REMOVER, s2, TipoObjeto.SEQUENCE, true, true,
                            "DROP SEQUENCE " + s2.getFullName() + ";"));
                }
            }

            tAlter.setItems(FXCollections.observableList(alteracoes));

            if (!alteracoes.isEmpty()) {
                bSql.setDisable(false);
                lStatus.setText("Concluído!");
            } else {
                lStatus.setText("Concluído! As bases de dados estão iguais.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            bComp.setDisable(false);
            try {
                conn1.close();
                conn2.close();
            } catch (SQLException e) {
            }
        }
    }

    @FXML
    private void invert(ActionEvent event) {
        Schema aux = schema1;
        schema1 = schema2;
        schema2 = aux;

        lDe.setText(schema1.getBanco().getServidor().getNome() + "." + schema1.getBanco().getNome() + "." + schema1.getNome());
        lPara.setText(schema2.getBanco().getServidor().getNome() + "." + schema2.getBanco().getNome() + "." + schema2.getNome());
    }

    @FXML
    private void showSql(ActionEvent event) {
        taSql.setText("");

        for (Alteracao alter : tAlter.getItems()) {
            if (alter.isAlter() && alter.isGerar()) {
                if (alter.getOperacao().equals(Operacao.REMOVER)) {
                    if (alter.getObjeto() instanceof Sequence) {
                        taSql.appendText(alter.getSQL() + "\n");
                    }
                }

            }
        }
        taSql.appendText("\n");

        for (Alteracao alter : tAlter.getItems()) {
            if (alter.isAlter() && alter.isGerar()) {
                if (alter.getOperacao().equals(Operacao.REMOVER)) {
                    if (alter.getObjeto() instanceof ForeignKey) {
                        taSql.appendText(alter.getSQL() + "\n");
                    }
                }
            }
        }
        taSql.appendText("\n");

        for (Alteracao alter : tAlter.getItems()) {
            if (alter.isAlter() && alter.isGerar()) {
                if (alter.getOperacao().equals(Operacao.REMOVER)) {
                    if (alter.getObjeto() instanceof Coluna) {
                        taSql.appendText(alter.getSQL() + "\n");
                    }
                }
            }
        }
        taSql.appendText("\n");

        for (Alteracao alter : tAlter.getItems()) {
            if (alter.isAlter() && alter.isGerar()) {
                if (alter.getOperacao().equals(Operacao.REMOVER)) {
                    if (alter.getObjeto() instanceof Tabela) {
                        taSql.appendText(alter.getSQL() + "\n");
                    }
                }

            }
        }
        taSql.appendText("\n");

        for (Alteracao alter : tAlter.getItems()) {
            if (alter.isAlter() && alter.isGerar()) {
                if (alter.getOperacao().equals(Operacao.ALTERAR)) {
                    if (alter.getObjeto() instanceof Tabela) {
                        taSql.appendText(alter.getSQL() + "\n");
                    }
                }
            }
        }
        taSql.appendText("\n");

        for (Alteracao alter : tAlter.getItems()) {
            if (alter.isAlter() && alter.isGerar()) {
                if (alter.getOperacao().equals(Operacao.CRIAR)) {
                    if (alter.getObjeto() instanceof Sequence) {
                        taSql.appendText(alter.getSQL() + "\n");
                    }
                }
            }
        }
        taSql.appendText("\n");

        for (Alteracao alter : tAlter.getItems()) {
            if (alter.isAlter() && alter.isGerar()) {
                if (alter.getOperacao().equals(Operacao.ALTERAR)) {
                    if (alter.getObjeto() instanceof Coluna) {
                        taSql.appendText(alter.getSQL() + "\n");
                    }
                }
            }
        }
        taSql.appendText("\n");

        for (Alteracao alter : tAlter.getItems()) {
            if (alter.isAlter() && alter.isGerar()) {
                if (alter.getOperacao().equals(Operacao.ALTERAR)) {
                    if (alter.getObjeto() instanceof ForeignKey) {
                        taSql.appendText(alter.getSQL() + "\n");
                    }
                }
            }
        }
        taSql.appendText("\n");

        for (Alteracao alter : tAlter.getItems()) {
            if (alter.isAlter() && alter.isGerar()) {
                if (alter.getOperacao().equals(Operacao.ALTERAR)) {
                    if (alter.getObjeto() instanceof Sequence) {
                        taSql.appendText(alter.getSQL() + "\n");
                    }
                }
            }
        }
        taSql.appendText("\n");

        for (Alteracao alter : tAlter.getItems()) {
            if (alter.isAlter() && alter.isGerar()) {
                if (alter.getOperacao().equals(Operacao.CRIAR)) {
                    if (alter.getObjeto() instanceof Coluna) {
                        taSql.appendText(alter.getSQL() + "\n");
                    }
                }
            }
        }
        taSql.appendText("\n");

        List<Alteracao> aux = new ArrayList<>();
        List<Alteracao> aux2 = new ArrayList<>();
        for (Alteracao alter : tAlter.getItems()) {
            if (alter.isAlter() && alter.isGerar()) {
                if (alter.getOperacao().equals(Operacao.CRIAR)) {
                    if (alter.getObjeto() instanceof Tabela) {
                        aux.add(alter);
                        aux2.add(alter);
                    }
                }
            }
        }

        boolean mudou;
        do {
            mudou = false;
            for (int i = 1; i < aux.size(); i++) {
                Tabela t = (Tabela) aux.get(i).getObjeto();
                for (int j = 0; j < i; j++) {
                    Tabela t2 = (Tabela) aux.get(j).getObjeto();
                    if (t2.dependeDe(t.getNome())) {
                        Alteracao a = aux2.remove(i);
                        aux2.add(j, a);
                        mudou = true;
                    }
                }
            }
            aux = new ArrayList<>(aux2);
        } while (mudou);

        for (Alteracao a : aux2) {
            taSql.appendText(a.getSQL() + "\n");
        }

        taSql.appendText("\n");

        for (Alteracao alter : tAlter.getItems()) {
            if (alter.isAlter() && alter.isGerar()) {
                if (alter.getOperacao().equals(Operacao.CRIAR)) {
                    if (alter.getObjeto() instanceof ForeignKey) {
                        taSql.appendText(alter.getSQL() + "\n");
                    }
                }
            }
        }
        taSql.appendText("\n");

        bExecDDL.setDisable(false);
    }

    @FXML
    private void execDDL(ActionEvent event) {
        try (Connection conn2 = schema2.getBanco().connect()) {
            lStatus.setText("Processando Alterações...");
            conn2.setSchema(schema2.getNome());
            conn2.setAutoCommit(false);
            Statement stm = conn2.createStatement();
            stm.executeUpdate(taSql.getText());
//                    SqlLog.append(taSql.getText());
            conn2.commit();
            lStatus.setText("Alterações efetuadas com Sucesso!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            MessageBox.errorMessage(ex);
            return;
        }
    }

    @FXML
    private void tableClick(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            if (contextMenu.isShowing()) {
                contextMenu.hide();
            }
        } else if (event.getButton().equals(MouseButton.SECONDARY)) {
            if (contextMenu.isShowing()) {
                contextMenu.hide();
            } else {
                contextMenu.show(anchor, event.getScreenX(), event.getScreenY());
            }
        }
    }

    @FXML
    private void select(ActionEvent event) {
        for (Alteracao alter : tAlter.getItems()) {
            alter.setGerar(ckSelect.isSelected());
        }
    }

}
