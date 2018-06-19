package view.controller;

import model.Banco;
import model.ObjetoBkp;
import model.Schema;
import model.Tabela;
import utils.DB;
import utils.MessageBox;
import utils.SystemRuntime;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class BackupController implements Initializable {

    private Banco banco;
    private File arquivo;
    @FXML
    private Label lBanco;
    @FXML
    private TextField tfLocal;
    @FXML
    private TextField tfParams;
    @FXML
    private TextArea taLog;
    @FXML
    private Button bBackup;
    @FXML
    private Label lPgDump;
    @FXML
    private CheckBox ckStruct;
    @FXML
    private CheckBox ckData;
    @FXML
    private TabPane tabPane;
    @FXML
    private TreeTableView<ObjetoBkp> tObjs;
    @FXML
    private ComboBox<String> cbFormat;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.banco = PrincipalController.INSTANCE.getSelectedObject();
        lBanco.setText(banco.getServidor().getNome() + "." + banco.getNome());

        cbFormat.setItems(FXCollections.observableArrayList("custom", "plain", "tar", "directory"));
        cbFormat.getSelectionModel().select(0);

        if (DB.getPgDump() == null || !DB.getPgDump().exists()) {
            lPgDump.setText("pgDump NÃO DEFINIDO");
        } else {
            String res = SystemRuntime.exec(DB.getPgDump().getAbsolutePath() + " --version");
            Matcher matcher = Pattern.compile("(\\d\\d?.?)+$").matcher(res.trim());
            if (matcher.find()) {
                lPgDump.setText("pgDump " + matcher.group());
            }
        }

        TreeTableColumn<ObjetoBkp, String> colTab = new TreeTableColumn<>("Objeto");
        colTab.setCellValueFactory((TreeTableColumn.CellDataFeatures<ObjetoBkp, String> p) -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return p.getValue().getValue().getDescricao();
            }
        });
        colTab.setMinWidth(200);
        colTab.setPrefWidth(300);

        TreeTableColumn<ObjetoBkp, Boolean> colStruct = new TreeTableColumn<>("Estrutura");
        colStruct.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(colStruct));
        colStruct.setCellValueFactory(param -> {
            return param.getValue().getValue().structProperty();
        });
        colStruct.setEditable(true);
        colStruct.setMaxWidth(100);

        TreeTableColumn<ObjetoBkp, Boolean> colDados = new TreeTableColumn<>("Dados");
        colDados.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(colDados));
        colDados.setCellValueFactory(param -> {
            return param.getValue().getValue().dadosProperty();
        });
        colDados.setEditable(true);
        colDados.setMaxWidth(100);

        tObjs.getColumns().addAll(colTab, colStruct, colDados);
        tObjs.setEditable(true);
//        tObjs.setTableMenuButtonVisible(true); 

        TreeItem<ObjetoBkp> root = new TreeItem<ObjetoBkp>(new ObjetoBkp("Schemas"));
        try {
            if (banco.getSchemas() == null) {
                banco.loadSchemas();
            }
            for (Schema s : banco.getSchemas()) {
                if (s.getTabelas() == null) {
                    s.loadTables();
                }

                ObjetoBkp objSch = new ObjetoBkp(s);
                TreeItem<ObjetoBkp> tiObjSch = new TreeItem<ObjetoBkp>(objSch);
                root.getChildren().add(tiObjSch);
                objSch.structProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        for (TreeItem<ObjetoBkp> tabItem : tiObjSch.getChildren()) {
                            if (tabItem.getValue().isTable()) {
                                tabItem.getValue().setStruct(newValue);
                            }
                        }
                    }
                });

                objSch.dadosProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        for (TreeItem<ObjetoBkp> tabItem : tiObjSch.getChildren()) {
                            if (tabItem.getValue().isTable()) {
                                tabItem.getValue().setDados(newValue);
                            }
                        }
                    }
                });

                for (Tabela t : s.getTabelas()) {
                    TreeItem<ObjetoBkp> tabItem = new TreeItem<ObjetoBkp>(new ObjetoBkp(t));
                    tiObjSch.getChildren().add(tabItem);

                    tabItem.getValue().structProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            if (!newValue) {
                                tabItem.getValue().setDados(false);
                            }
                        }
                    });

                    tabItem.getValue().dadosProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            if (newValue && !tabItem.getValue().structProperty().get()) {
                                tabItem.getValue().setStruct(true);
                            }
                        }
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        tObjs.setRoot(root);
    }

    @FXML
    private void openSaveDialog(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName(banco.getNome() + ".backup");
        arquivo = fc.showSaveDialog(null);
        tfLocal.setText(arquivo != null ? arquivo.getAbsolutePath() : "");
    }

    @FXML
    private void backup(ActionEvent event) {
        taLog.setText("");

        if (arquivo == null && !cbFormat.getSelectionModel().getSelectedItem().equals("plain")) {
            MessageBox.errorMessage("Selecine o arquivo destino do backup.");
            return;
        }

        if (arquivo == null && cbFormat.getSelectionModel().getSelectedItem().equals("plain")) {
            tfParams.setText(tfParams.getText().replace("--verbose", ""));
        }

        if (DB.getPgDump() == null || !DB.getPgDump().exists()) {
            MessageBox.message("A localização do pg_dump ainda não está definida. Selecione seu local.");
            selPgDumpFile();

            if (DB.getPgDump() == null) {
                return;
            }
        }

        final StringProperty s = new SimpleStringProperty("");
        if (!cbFormat.getSelectionModel().getSelectedItem().equals("plain")) {
            s.addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            taLog.appendText(newValue + "\n");
                        }
                    });
                }
            });
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String cmd = DB.getPgDump().getAbsolutePath();
                    cmd += " --host " + banco.getServidor().getEndereco();
                    cmd += " --port " + banco.getServidor().getPorta();
                    cmd += " --username " + banco.getServidor().getUser();
                    cmd += " --format " + cbFormat.getSelectionModel().getSelectedItem();
                    cmd += " " + tfParams.getText();
                    if (arquivo != null) {
                        cmd += " --file \"" + arquivo.getAbsolutePath() + "\"";
                    }

                    if (ckStruct.isSelected()) {
                        cmd += " --schema-only";
                    } else if (ckData.isSelected()) {
                        cmd += " --data-only";
                    }

                    for (TreeItem<ObjetoBkp> schemaItem : tObjs.getRoot().getChildren()) {
                        if (!schemaItem.getValue().isStruct()) {
                            boolean alguem = false;
                            for (TreeItem<ObjetoBkp> tabItem : schemaItem.getChildren()) {
                                if (tabItem.getValue().isStruct()) {
                                    alguem = true;
                                    break;
                                }
                            }
                            if (!alguem) {
                                cmd += " --exclude-schema " + schemaItem.getValue().getSchema().getNome();
                                continue;
                            }
                        }
                        for (TreeItem<ObjetoBkp> tabItem : schemaItem.getChildren()) {
                            if (!tabItem.getValue().isStruct()) {
                                cmd += " --exclude-table " + tabItem.getValue().getTabela().getFullName();
                            } else if (!tabItem.getValue().isDados()) {
                                cmd += " --exclude-table-data " + tabItem.getValue().getTabela().getFullName();
                            }
                        }
                    }

                    cmd += " --dbname " + banco.getNome();
                    taLog.appendText(cmd + "\n");
                    System.out.println(cmd);
                    bBackup.setDisable(true);

                    if (cbFormat.getSelectionModel().getSelectedItem().equals("plain")) {
                        StringBuilder sbOut = new StringBuilder();
                        SystemRuntime.exec(cmd, sbOut, cmd.toLowerCase().contains("utf8") ? StandardCharsets.UTF_8 : null, "PGPASSWORD=" + banco.getServidor().getPwd());
                        System.out.println(sbOut.toString());
                        taLog.setText(sbOut.toString());
                    } else {
                        SystemRuntime.exec(cmd, s, "PGPASSWORD=" + banco.getServidor().getPwd());
                        Thread.sleep(2000);
                        taLog.appendText("\nBackup Concluído!\n");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    s.set("Erro: \n" + ex.getMessage());
                } finally {
                    bBackup.setDisable(false);
                }
            }
        }).start();
    }

    @FXML
    private File selPgDumpFile() {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("pg_dump.exe");
        File pgDump = fc.showOpenDialog(null);
        if (pgDump != null) {
            DB.setPgDump(pgDump);
        }
        return pgDump;
    }

    private void selPgDumpFile(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            selPgDumpFile();
        }
    }

    @FXML
    private void ckStructAct(ActionEvent event) {
        if (ckStruct.isSelected()) {
            ckData.setSelected(false);
        }

        tabPane.getTabs().get(1).setDisable(ckStruct.isSelected() || ckData.isSelected());
    }

    @FXML
    private void ckDataAct(ActionEvent event) {
        if (ckData.isSelected()) {
            ckStruct.setSelected(false);
        }

        tabPane.getTabs().get(1).setDisable(ckStruct.isSelected() || ckData.isSelected());
    }

}
