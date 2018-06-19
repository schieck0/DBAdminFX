package view.controller;

import model.Banco;
import model.Function;
import model.Schema;
import model.Sequence;
import model.Servidor;
import model.Tabela;
import model.View;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import utils.DB;
import utils.MessageBox;
import utils.SqlLog;
import view.Start;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.w3c.dom.Element;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class PrincipalController implements Initializable {

    @FXML
    private AnchorPane anchor;
    @FXML
    public TreeView<Object> tree;
    @FXML
    public TabPane tabbedPane;

    private TreeItem<Object> rootNode;

    public static PrincipalController INSTANCE;

    public static final Image imgServer = new Image(Servidor.class.getResourceAsStream("/images/Places-network-server-icon.png"));
    public static final Image imgDB = new Image(Servidor.class.getResourceAsStream("/images/Misc-Database-3-icon.png"));
    public static final Image imgSchema = new Image(Servidor.class.getResourceAsStream("/images/diamond12.png"));
    public static final Image imgTable = new Image(Servidor.class.getResourceAsStream("/images/Table-icon.png"));
    public static final Image imgView = new Image(Servidor.class.getResourceAsStream("/images/view_table.png"));
    public static final Image imgFunction = new Image(Servidor.class.getResourceAsStream("/images/arrow-right-icon.png"));
    public static final Image imgSequence = new Image(Servidor.class.getResourceAsStream("/images/resultset-next-icon.png"));
    public static final Image imgTables = new Image(Servidor.class.getResourceAsStream("/images/table-multiple-icon.png"));
    public static final Image imgSeqs = new Image(Servidor.class.getResourceAsStream("/images/double_right.png"));

    private final ContextMenu cmServer = new ContextMenu();
    private final ContextMenu cmBanco = new ContextMenu();
    private final ContextMenu cmSchema = new ContextMenu();
    private final ContextMenu cmTab = new ContextMenu();
    private final ContextMenu cmSeq = new ContextMenu();
    private final ContextMenu cmView = new ContextMenu();
    private final ContextMenu cmFunc = new ContextMenu();
    @FXML
    private Button bAddServer;
    @FXML
    private Button bSqlLog;
    @FXML
    private Button bRefreshItem;
    @FXML
    private Button bCompareStruct;
    @FXML
    private Button bCompareData;
    @FXML
    private Button bImportData;
    @FXML
    private Button bSql;
    @FXML
    private Button bModeler;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            INSTANCE = this;
            Node rootIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/images/folder-close-icon.png"))
            );
            rootNode = new TreeItem<>("Servidores", rootIcon);
            rootNode.setExpanded(true);
            tree.setRoot(rootNode);
            tree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            try {
                DB.read();

                loadTree();

                loadMenus();
            } catch (Exception e) {
                e.printStackTrace();
            }

            bAddServer.setTooltip(new Tooltip("Nova conexão"));
            bSqlLog.setTooltip(new Tooltip("SQL Log"));
            bRefreshItem.setTooltip(new Tooltip("Atualizar"));
            bCompareStruct.setTooltip(new Tooltip("Comparar Schemas"));//(selecione 2 schemas)
            bCompareData.setTooltip(new Tooltip("Comparar Dados"));//(selecione 2 tabelas)
            bImportData.setTooltip(new Tooltip("Importar Dados"));//(selecione 2 tabelas)
            bSql.setTooltip(new Tooltip("SQL"));
            bModeler.setTooltip(new Tooltip("Modeler"));

            addTab("dsf", "sqlLog");
            tabbedPane.getTabs().clear();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    public void loadTree() {
        rootNode.getChildren().clear();
        for (Servidor s : DB.getServers()) {
            TreeItem<Object> servNode = new TreeItem<>(s, new ImageView(imgServer));
            for (Banco db : s.getBancos()) {
                TreeItem<Object> dbNode = new TreeItem<>(db, new ImageView(imgDB));
                if (db.getSchemas() != null) {
                    for (Schema sc : db.getSchemas()) {
                        TreeItem<Object> scNode = new TreeItem<>(sc, new ImageView(imgSchema));
                        if (sc.getSequences() != null) {
                            TreeItem<Object> seqsNode = new TreeItem<>("Sequences (" + sc.getSequences().size() + ")", new ImageView(imgSeqs));
                            for (Sequence seq : sc.getSequences()) {
                                seqsNode.getChildren().add(new TreeItem<>(seq, new ImageView(imgSequence)));
                            }
                            scNode.getChildren().add(seqsNode);
                        }
                        if (sc.getTabelas() != null) {
                            TreeItem<Object> tabsNode = new TreeItem<>("Tabelas (" + sc.getTabelas().size() + ")", new ImageView(imgTables));
                            for (Tabela tab : sc.getTabelas()) {
                                tabsNode.getChildren().add(new TreeItem<>(tab, new ImageView(imgTable)));
                            }
                            scNode.getChildren().add(tabsNode);
                        }
                        if (sc.getViews() != null && !sc.getViews().isEmpty()) {
                            TreeItem<Object> tabsNode = new TreeItem<>("Views (" + sc.getViews().size() + ")", new ImageView(imgTables));
                            for (View vw : sc.getViews()) {
                                tabsNode.getChildren().add(new TreeItem<>(vw, new ImageView(imgView)));
                            }
                            scNode.getChildren().add(tabsNode);
                        }
                        if (sc.getFunctions() != null && !sc.getFunctions().isEmpty()) {
                            TreeItem<Object> tabsNode = new TreeItem<>("Functions (" + sc.getFunctions().size() + ")", new ImageView(imgFunction));
                            for (Function fu : sc.getFunctions()) {
                                tabsNode.getChildren().add(new TreeItem<>(fu, new ImageView(imgFunction)));
                            }
                            scNode.getChildren().add(tabsNode);
                        }

                        dbNode.getChildren().add(scNode);
                    }
                }
                servNode.getChildren().add(dbNode);
            }
            rootNode.getChildren().add(servNode);
        }
    }

    @FXML
    private void treeClick(MouseEvent event) {
        closeMenus();
        if (tree.getSelectionModel().getSelectedIndex() >= 0) {
            TreeItem<Object> node = tree.getSelectionModel().getSelectedItem();
            Object nodeObj = node.getValue();
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                if (nodeObj instanceof Servidor) {
                    Servidor server = (Servidor) nodeObj;
                    if (server.getBancos().isEmpty()) {
                        try {
                            server.loadDatabases();
                            for (Banco db : server.getBancos()) {
                                node.getChildren().add(new TreeItem<>(db, new ImageView(imgDB)));
                            }
                            node.setExpanded(true);
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex.getMessage());
                        }
                    }
                } else if (nodeObj instanceof Banco) {
                    Banco banco = (Banco) nodeObj;
                    if (banco.getSchemas() == null) {
                        try {
                            banco.loadSchemas();
                            for (Schema sc : banco.getSchemas()) {
                                node.getChildren().add(new TreeItem<>(sc, new ImageView(imgSchema)));
                            }
                            node.setExpanded(true);
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex.getMessage());
                        }
                    } else if (node.getChildren().isEmpty()) {
                        for (Schema sc : banco.getSchemas()) {
                            node.getChildren().add(new TreeItem<>(sc, new ImageView(imgSchema)));
                        }
                        node.setExpanded(true);
                    }
                } else if (nodeObj instanceof Schema) {
                    Schema sch = (Schema) nodeObj;
                    if (sch.getTabelas() == null || sch.getTabelas().isEmpty() || node.getChildren().isEmpty()) {
                        try {
                            if (sch.getTabelas() == null) {
                                sch.loadTables();
                            }
                            if (sch.getSequences() == null) {
                                sch.loadSequences();
                            }
                            if (sch.getViews() == null) {
                                sch.loadViews();
                            }
                            if (sch.getFunctions() == null) {
                                sch.loadFunctions();
                            }

                            if (sch.getSequences() != null) {
                                TreeItem<Object> seqsNode;
                                if (!node.getChildren().isEmpty()) {
                                    seqsNode = node.getChildren().get(0);
                                } else {
                                    seqsNode = new TreeItem<>("Sequences (" + sch.getSequences().size() + ")", new ImageView(imgSeqs));
                                    node.getChildren().add(seqsNode);
                                }
                                for (Sequence seq : sch.getSequences()) {
                                    seqsNode.getChildren().add(new TreeItem<>(seq, new ImageView(imgSequence)));
                                }
                            }
                            if (sch.getTabelas() != null) {
                                TreeItem<Object> tabsNode;
                                if (node.getChildren().size() == 2) {
                                    tabsNode = node.getChildren().get(1);
                                } else {
                                    tabsNode = new TreeItem<>("Tabelas (" + sch.getTabelas().size() + ")", new ImageView(imgTables));
                                    node.getChildren().add(tabsNode);
                                }
                                for (Tabela tab : sch.getTabelas()) {
                                    tabsNode.getChildren().add(new TreeItem<>(tab, new ImageView(imgTable)));
                                }
                            }
                            if (sch.getViews() != null && !sch.getViews().isEmpty()) {
                                TreeItem<Object> vwsNode;
                                if (node.getChildren().size() == 3) {
                                    vwsNode = node.getChildren().get(2);
                                } else {
                                    vwsNode = new TreeItem<>("Views (" + sch.getViews().size() + ")", new ImageView(imgView));
                                    node.getChildren().add(vwsNode);
                                }
                                for (View vw : sch.getViews()) {
                                    vwsNode.getChildren().add(new TreeItem<>(vw, new ImageView(imgView)));
                                }
                            }
                            if (sch.getFunctions() != null && !sch.getFunctions().isEmpty()) {
                                TreeItem<Object> funsNode;
                                if (node.getChildren().size() == 4) {
                                    funsNode = node.getChildren().get(3);
                                } else {
                                    funsNode = new TreeItem<>("Functions (" + sch.getFunctions().size() + ")", new ImageView(imgFunction));
                                    node.getChildren().add(funsNode);
                                }
                                for (Function fu : sch.getFunctions()) {
                                    funsNode.getChildren().add(new TreeItem<>(fu, new ImageView(imgFunction)));
                                }
                            }
                            node.setExpanded(true);
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex.getMessage());
                        }
                    }
                } else if (nodeObj instanceof View) {
                    View vw = (View) nodeObj;
                    addTab(vw.getSchema().getBanco().getNome() + "." + vw.getSchema().getNome() + "." + vw.getNome(), "tableData");
                } else if (nodeObj instanceof Tabela) {
                    Tabela tabela = (Tabela) nodeObj;
                    addTab(tabela.getSchema().getBanco().getNome() + "." + tabela.getSchema().getNome() + "." + tabela.getNome(), "tableData");
                } else if (nodeObj instanceof Sequence) {
                    Sequence seq = (Sequence) nodeObj;
                    addTab(seq.getSchema().getBanco().getNome() + "." + seq.getSchema().getNome() + "." + seq.getNome(), "sequenceVal");
                }
            } else if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1) {
                if (tree.getSelectionModel().getSelectedItems().size() == 1) {
                    bCompareData.setDisable(true);
                    bCompareStruct.setDisable(true);
                    bImportData.setDisable(true);
                    if (nodeObj instanceof Banco || nodeObj instanceof Schema || nodeObj instanceof Tabela || nodeObj instanceof View || nodeObj instanceof Function) {
                        bSql.setDisable(false);
                    } else {
                        bSql.setDisable(true);
                    }
                } else if (tree.getSelectionModel().getSelectedItems().size() == 2) {
                    bSql.setDisable(true);
                    if (tree.getSelectionModel().getSelectedItems().get(0).getValue() instanceof Schema
                            && tree.getSelectionModel().getSelectedItems().get(1).getValue() instanceof Schema) {
                        bCompareStruct.setDisable(false);
                        bCompareData.setDisable(true);
                        bImportData.setDisable(true);
                    } else if (tree.getSelectionModel().getSelectedItems().get(0).getValue() instanceof Tabela
                            && tree.getSelectionModel().getSelectedItems().get(1).getValue() instanceof Tabela) {
                        bCompareData.setDisable(false);
                        bImportData.setDisable(false);
                        bCompareStruct.setDisable(true);
                    } else {
                        bCompareStruct.setDisable(true);
                        bCompareData.setDisable(true);
                        bImportData.setDisable(true);
                    }
                } else {
                    bSql.setDisable(true);
                    bCompareStruct.setDisable(true);
                    bCompareData.setDisable(true);
                    bImportData.setDisable(true);
                }
            } else if (event.getButton().equals(MouseButton.SECONDARY)) {
                if (nodeObj instanceof Servidor) {
                    cmServer.show(anchor, event.getScreenX(), event.getScreenY());
                } else if (nodeObj instanceof Banco) {
                    cmBanco.show(anchor, event.getScreenX(), event.getScreenY());
                } else if (nodeObj instanceof Schema) {
                    cmSchema.show(anchor, event.getScreenX(), event.getScreenY());
                } else if (nodeObj instanceof View) {
                    cmView.show(anchor, event.getScreenX(), event.getScreenY());
                } else if (nodeObj instanceof Tabela) {
                    cmTab.show(anchor, event.getScreenX(), event.getScreenY());
                } else if (nodeObj instanceof Sequence) {
                    cmSeq.show(anchor, event.getScreenX(), event.getScreenY());
                } else if (nodeObj instanceof Function) {
                    cmFunc.show(anchor, event.getScreenX(), event.getScreenY());
                }
            }
        }
    }

    private void loadMenus() {
        {//SERVER
            MenuItem miEdit = new MenuItem("Editar");
            cmServer.getItems().add(miEdit);
            miEdit.setOnAction(new EventHandler() {
                public void handle(Event e) {
                    Servidor servidor = (Servidor) tree.getSelectionModel().getSelectedItem().getValue();
                    String tit = "Editar " + servidor.getNome();
                    for (Tab t : tabbedPane.getTabs()) {
                        if (t.getText().equals(tit)) {
                            tabbedPane.getSelectionModel().select(t);
                            return;
                        }
                    }

                    final Tab tab = new Tab(tit);

                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/addServer.fxml"));
                        AnchorPane anchorPane = (AnchorPane) loader.load();
                        AddServerController controller = loader.getController();
                        controller.setServidor(servidor);
                        tab.setContent(anchorPane);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    tabbedPane.getTabs().add(tab);
                }
            });

            MenuItem miRemove = new MenuItem("Remover");
            cmServer.getItems().add(miRemove);
            miRemove.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    if (MessageBox.confirm("Excluír o servidor da lista?")) {
                        TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                        Servidor servidor = (Servidor) item.getValue();
                        DB.getServers().remove(servidor);
                        rootNode.getChildren().remove(item);
                    }
                }
            });

            MenuItem miConns = new MenuItem("Ver Conexões");
            cmServer.getItems().add(miConns);
            miConns.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Servidor servidor = (Servidor) item.getValue();

                    addTab("Conexões " + servidor.getNome(), "connections");
                }
            });

            MenuItem miNewDB = new MenuItem("Novo Banco");
            cmServer.getItems().add(miNewDB);
            miNewDB.setOnAction(new EventHandler() {
                public void handle(Event e) {
                    addTab("Novo Banco", "newDB");
                }
            });

            MenuItem miBkp = new MenuItem("Backup");
            cmServer.getItems().add(miBkp);
            miBkp.setOnAction(new EventHandler() {
                public void handle(Event e) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Servidor servidor = (Servidor) item.getValue();
                    addTab("Backup " + servidor.getNome(), "backupServer");
                }
            });

            MenuItem miRestore = new MenuItem("Restore");
            cmServer.getItems().add(miRestore);
            miRestore.setOnAction(new EventHandler() {
                public void handle(Event e) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Servidor servidor = (Servidor) item.getValue();
                    addTab("Restore " + servidor.getNome(), "restoreServer");
                }
            });
        }
        {//DB
            MenuItem miBkp = new MenuItem("Backup");
            cmBanco.getItems().add(miBkp);
            miBkp.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Banco banco = (Banco) item.getValue();
                    addTab("Backup " + banco.getNome(), "backup");
                }
            });

            MenuItem miRestore = new MenuItem("Restore");
            cmBanco.getItems().add(miRestore);
            miRestore.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Banco banco = (Banco) item.getValue();

                    addTab("Restore " + banco.getNome(), "restore");
                }
            });

            MenuItem miConns = new MenuItem("Ver Conexões");
            cmBanco.getItems().add(miConns);
            miConns.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Banco banco = (Banco) item.getValue();
                    addTab("Conexões " + banco.getServidor().getNome() + "." + banco.getNome(), "connections");
                }
            });

            MenuItem miNewSc = new MenuItem("Novo Schema");
            cmBanco.getItems().add(miNewSc);
            miNewSc.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    addTab("Novo Schema", "newSchema");
                }
            });

            MenuItem miRename = new MenuItem("Renomear");
            cmBanco.getItems().add(miRename);
            miRename.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Banco banco = (Banco) item.getValue();
                    String newName = MessageBox.input("Novo nome:", banco.getNome());
                    if (newName != null) {
                        try (Connection conn = banco.getServidor().connect()) {
                            String sql = "ALTER DATABASE " + banco.getNome() + "  RENAME TO " + newName + ";";
                            conn.createStatement().executeUpdate(sql);
                            SqlLog.append(sql);
                            banco.setNome(newName);
                            item.getParent().getChildren().set(item.getParent().getChildren().indexOf(item), item);
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex);
                        }
                    }
                }
            });

            MenuItem miRemove = new MenuItem("DROP");
            cmBanco.getItems().add(miRemove);
            miRemove.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    if (MessageBox.confirm("Excluír o banco?")) {
                        TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                        Banco banco = (Banco) item.getValue();

                        try (Connection conn = banco.getServidor().connect()) {
                            //verifica conexoes
                            ResultSet rs = conn.createStatement().executeQuery("select " + (banco.getServidor().getVersao().startsWith("9.1") ? "procpid" : "pid")
                                    + " from pg_stat_activity where datname='" + banco.getNome() + "'");
                            List<Integer> pids = new ArrayList<>();
                            while (rs.next()) {
                                pids.add(rs.getInt(1));
                            }

                            if (!pids.isEmpty()) {
                                if (!MessageBox.confirm("Fechar conexões e prosseguir, ou cancelar o DROP?")) {
                                    return;
                                }
                                for (Integer pid : pids) {
                                    conn.createStatement().executeQuery("SELECT pg_terminate_backend(" + pid + ")");
                                }
                            }

                            String sql = "DROP DATABASE " + banco.getNome() + ";";
                            conn.createStatement().executeUpdate(sql);
                            SqlLog.append(sql);
                            banco.getServidor().getBancos().remove(banco);
                            item.getParent().getChildren().remove(item);
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex.getMessage());
                        }
                    }
                }
            });

            MenuItem miDropCascade = new MenuItem("DROP CASCADE");
            cmBanco.getItems().add(miDropCascade);
            miDropCascade.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    if (MessageBox.confirm("Excluír o banco?")) {
                        TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                        Banco banco = (Banco) item.getValue();

                        try (Connection conn = banco.getServidor().connect()) {
                            //verifica conexoes
                            ResultSet rs = conn.createStatement().executeQuery("select " + (banco.getServidor().getVersao().startsWith("9.1") ? "procpid" : "pid")
                                    + " from pg_stat_activity where datname='" + banco.getNome() + "'");
                            List<Integer> pids = new ArrayList<>();
                            while (rs.next()) {
                                pids.add(rs.getInt(1));
                            }

                            if (!pids.isEmpty()) {
                                if (!MessageBox.confirm("Fechar conexões e prosseguir, ou cancelar o DROP?")) {
                                    return;
                                }
                                for (Integer pid : pids) {
                                    conn.createStatement().executeQuery("SELECT pg_terminate_backend(" + pid + ")");
                                }
                            }

                            String sql = "DROP DATABASE " + banco.getNome() + " CASCADE;";
                            conn.createStatement().executeUpdate(sql);
                            SqlLog.append(sql);
                            banco.getServidor().getBancos().remove(banco);
                            item.getParent().getChildren().remove(item);
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex.getMessage());
                        }
                    }
                }
            });
        }
        {//SCHEMA
            MenuItem miNewTab = new MenuItem("Nova Tabela");
            cmSchema.getItems().add(miNewTab);
            miNewTab.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Schema schema = (Schema) item.getValue();
                    addTab("Nova Tabela", "tableEdit");
                }
            });

            MenuItem miNewSeq = new MenuItem("Nova Sequence");
            cmSchema.getItems().add(miNewSeq);
            miNewSeq.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    addTab("Nova Sequence", "newSequence");
                }
            });

            MenuItem miNewView = new MenuItem("Nova View");
            cmSchema.getItems().add(miNewView);
            miNewView.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Schema schema = (Schema) item.getValue();
                    addTab("Nova View", "viewEdit");
                }
            });

            MenuItem miNewFunction = new MenuItem("Nova Function");
            cmSchema.getItems().add(miNewFunction);
            miNewFunction.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Schema schema = (Schema) item.getValue();
                    addTab("Nova Function", "functionEdit");
                }
            });

            MenuItem miEdit = new MenuItem("Editar");
            cmSchema.getItems().add(miEdit);
            miEdit.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Schema schema = (Schema) item.getValue();
                    addTab("Editar Schema - " + schema.getNome(), "newSchema");
                }
            });

            MenuItem miRename = new MenuItem("Renomear");
            cmSchema.getItems().add(miRename);
            miRename.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Schema schema = (Schema) item.getValue();
                    String newName = MessageBox.input("Novo nome:", schema.getNome());
                    if (newName != null) {
                        try (Connection conn = schema.connect()) {
                            String sql = "ALTER SCHEMA " + schema.getNome() + "  RENAME TO " + newName + ";";
                            conn.createStatement().executeUpdate(sql);
                            SqlLog.append(sql);
                            schema.setNome(newName);
                            item.getParent().getChildren().set(item.getParent().getChildren().indexOf(item), item);
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex);
                        }
                    }
                }
            });

            MenuItem miRemove = new MenuItem("DROP");
            cmSchema.getItems().add(miRemove);
            miRemove.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Schema schema = (Schema) item.getValue();
                    if (MessageBox.confirm("Remover SCHEMA?")) {
                        try (Connection conn = schema.getBanco().connect()) {
                            String sql = "DROP SCHEMA " + schema.getNome() + ";";
                            conn.createStatement().executeUpdate(sql);
                            SqlLog.append(sql);
                            schema.getBanco().loadSchemas();
                            item.getParent().getChildren().remove(item);
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex.getMessage());
                        }
                    }
                }
            });
            MenuItem miDropCascade = new MenuItem("DROP CASCADE");
            cmSchema.getItems().add(miDropCascade);
            miDropCascade.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Schema schema = (Schema) item.getValue();
                    if (MessageBox.confirm("Remover SCHEMA?")) {
                        try (Connection conn = schema.getBanco().connect()) {
                            String sql = "DROP SCHEMA " + schema.getNome() + " CASCADE;";
                            conn.createStatement().executeUpdate(sql);
                            SqlLog.append(sql);
                            schema.getBanco().loadSchemas();
                            item.getParent().getChildren().remove(item);
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex.getMessage());
                        }
                    }
                }
            });
        }
        {//TABELA
            MenuItem miEdit = new MenuItem("Editar");
            cmTab.getItems().add(miEdit);
            miEdit.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Tabela tabela = (Tabela) item.getValue();
                    addTab("Editar Tabela '" + tabela.getSchema().getBanco().getNome() + "." + tabela.getSchema().getNome() + "." + tabela.getNome() + "'", "tableEdit");
                }
            });

            MenuItem miVinculos = new MenuItem("Ver Vinculos");
            cmTab.getItems().add(miVinculos);
            miVinculos.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Tabela tabela = (Tabela) item.getValue();
                    addTab("Vínculos " + tabela.getSchema().getBanco().getNome() + "." + tabela.getSchema().getNome() + "." + tabela.getNome(), "tabVinculos");
                }
            });

            MenuItem miRemove = new MenuItem("DROP");
            cmTab.getItems().add(miRemove);
            miRemove.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    if (MessageBox.confirm("Excluír a tabela do banco?")) {
                        TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                        Tabela tabela = (Tabela) item.getValue();
                        try (Connection conn = tabela.getSchema().connect()) {
                            String sql = "DROP TABLE " + tabela.getNome() + ";";
                            conn.createStatement().executeUpdate(sql);
                            SqlLog.append(sql);
                            refresh(item.getParent().getParent());
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex.getMessage());
                        }
                    }
                }
            });

            MenuItem miDropCascade = new MenuItem("DROP CASCADE");
            cmTab.getItems().add(miDropCascade);
            miDropCascade.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    if (MessageBox.confirm("Excluír a tabela do banco?")) {
                        TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                        Tabela tabela = (Tabela) item.getValue();
                        try (Connection conn = tabela.getSchema().connect()) {
                            String sql = "DROP TABLE " + tabela.getNome() + " CASCADE;";
                            conn.createStatement().executeUpdate(sql);
                            SqlLog.append(sql);
                            refresh(item.getParent().getParent());
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex.getMessage());
                        }
                    }
                }
            });
        }
        {//SEQUENCE
            MenuItem miRename = new MenuItem("Renomear");
            cmSeq.getItems().add(miRename);
            miRename.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Sequence seq = (Sequence) item.getValue();
                    String newName = MessageBox.input("Novo nome:", seq.getNome());
                    if (newName != null) {
                        try (Connection conn = seq.getSchema().connect()) {
                            String sql = "ALTER SEQUENCE " + seq.getNome() + "  RENAME TO " + newName + ";";
                            conn.createStatement().executeUpdate(sql);
                            SqlLog.append(sql);
                            seq.setNome(newName);
                            item.getParent().getChildren().set(item.getParent().getChildren().indexOf(item), item);
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex);
                        }
                    }
                }
            });

            MenuItem miRemove = new MenuItem("DROP");
            cmSeq.getItems().add(miRemove);
            miRemove.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Sequence seq = (Sequence) item.getValue();
                    if (MessageBox.confirm("Remover Sequence?")) {
                        try (Connection conn = seq.getSchema().connect()) {
                            String sql = "DROP SEQUENCE " + seq.getNome() + ";";
                            conn.createStatement().executeUpdate(sql);
                            SqlLog.append(sql);
                            refresh(item.getParent().getParent());
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex.getMessage());
                        }
                    }
                }
            });

            MenuItem miDropCascade = new MenuItem("DROP CASCADE");
            cmSeq.getItems().add(miDropCascade);
            miDropCascade.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Sequence seq = (Sequence) item.getValue();
                    if (MessageBox.confirm("Remover Sequence?")) {
                        try (Connection conn = seq.getSchema().connect()) {
                            String sql = "DROP SEQUENCE " + seq.getNome() + " CASCADE;";
                            conn.createStatement().executeUpdate(sql);
                            SqlLog.append(sql);
                            refresh(item.getParent().getParent());
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex.getMessage());
                        }
                    }
                }
            });
        }
        {//VIEW
            MenuItem miEdit = new MenuItem("Editar");
            cmView.getItems().add(miEdit);
            miEdit.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    View v = (View) item.getValue();
                    addTab("Editar View '" + v.getSchema().getBanco().getNome() + "." + v.getSchema().getNome() + "." + v.getNome() + "'", "viewEdit");
                }
            });

            MenuItem miRemove = new MenuItem("DROP");
            cmView.getItems().add(miRemove);
            miRemove.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    View vw = (View) item.getValue();
                    if (MessageBox.confirm("Remover View?")) {
                        try (Connection conn = vw.getSchema().connect()) {
                            String sql = "DROP VIEW " + vw.getNome() + ";";
                            conn.createStatement().executeUpdate(sql);
                            SqlLog.append(sql);
                            refresh(item.getParent().getParent());
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex.getMessage());
                        }
                    }
                }
            });
        }
        {//FUNCTION
            MenuItem miEdit = new MenuItem("Editar");
            cmFunc.getItems().add(miEdit);
            miEdit.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Function f = (Function) item.getValue();
                    addTab("Editar Function '" + f.getSchema().getBanco().getNome() + "." + f.getSchema().getNome() + "." + f.getNome() + "'", "functionEdit");
                }
            });

            MenuItem miRemove = new MenuItem("DROP");
            cmFunc.getItems().add(miRemove);
            miRemove.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
                    Function fu = (Function) item.getValue();
                    if (MessageBox.confirm("Remover Function?")) {
                        try (Connection conn = fu.getSchema().connect()) {
                            fu.load();
                            String ps = "";
                            int i = 0;
                            for (String p : fu.getParams().values()) {
                                ps += p;
                                i++;
                                if (i < fu.getParams().values().size()) {
                                    ps += ", ";
                                }
                            }
                            String sql = "DROP FUNCTION " + fu.getNome() + "(" + ps + ");";
                            conn.createStatement().executeUpdate(sql);
                            SqlLog.append(sql);
                            refresh(item.getParent().getParent());
                        } catch (SQLException ex) {
                            MessageBox.errorMessage(ex.getMessage());
                        }
                    }
                }
            });
        }
    }

    private void closeMenus() {
        cmServer.hide();
        cmBanco.hide();
        cmSchema.hide();
        cmTab.hide();
        cmSeq.hide();
        cmView.hide();
        cmFunc.hide();
    }

    private Tab addTab(String titulo, String fxml) {
        return addTab(titulo, fxml, true);
    }

    private Tab addTab(String titulo, String fxml, boolean unique) {
        if (unique) {
            for (Tab t : tabbedPane.getTabs()) {
                if (t.getText().equals(titulo)) {
                    tabbedPane.getSelectionModel().select(t);
                    return t;
                }
            }
        }

        final Tab tab = new Tab(titulo);

        try {
            AnchorPane anchorPane = (AnchorPane) FXMLLoader.load(getClass().getResource("/view/fxml/" + fxml + ".fxml"));
            tab.setContent(anchorPane);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        tabbedPane.getTabs().add(tab);
        tabbedPane.getSelectionModel().select(tab);
        return tab;
    }

    public void closeSelectedTab() {
        tabbedPane.getTabs().remove(tabbedPane.getSelectionModel().getSelectedIndex());
    }

    public void refresh(TreeItem node) {
        node.getChildren().clear();
        Object nodeObj = node.getValue();
        try {
            if (nodeObj instanceof Servidor) {
                Servidor servidor = (Servidor) nodeObj;

                servidor.loadDatabases();
                for (Banco b : servidor.getBancos()) {
                    node.getChildren().add(new TreeItem<>(b, new ImageView(imgDB)));
                }
            } else if (nodeObj instanceof Banco) {
                Banco banco = (Banco) nodeObj;
                banco.loadSchemas();
                for (Schema s : banco.getSchemas()) {
                    node.getChildren().add(new TreeItem<>(s, new ImageView(imgSchema)));
                }
            } else if (nodeObj instanceof Schema) {
                Schema schema = (Schema) nodeObj;
                schema.loadSequences();

                TreeItem<Object> seqsNode = new TreeItem<>("Sequences (" + schema.getSequences().size() + ")", new ImageView(imgSeqs));
                for (Sequence s : schema.getSequences()) {
                    seqsNode.getChildren().add(new TreeItem<>(s, new ImageView(imgSequence)));
                }
                node.getChildren().add(seqsNode);

                schema.loadTables();
                TreeItem<Object> tabsNode = new TreeItem<>("Tabelas (" + schema.getTabelas().size() + ")", new ImageView(imgTables));
                for (Tabela t : schema.getTabelas()) {
                    tabsNode.getChildren().add(new TreeItem<>(t, new ImageView(imgTable)));
                }
                node.getChildren().add(tabsNode);

                schema.loadViews();
                TreeItem<Object> viewsNode = new TreeItem<>("Views (" + schema.getViews().size() + ")", new ImageView(imgView));
                for (View v : schema.getViews()) {
                    viewsNode.getChildren().add(new TreeItem<>(v, new ImageView(imgView)));
                }
                node.getChildren().add(viewsNode);

                schema.loadFunctions();
                TreeItem<Object> functsNode = new TreeItem<>("Functions (" + schema.getFunctions().size() + ")", new ImageView(imgFunction));
                for (Function f : schema.getFunctions()) {
                    functsNode.getChildren().add(new TreeItem<>(f, new ImageView(imgFunction)));
                }
                node.getChildren().add(functsNode);

//            } else if (nodeObj instanceof Tabela) {
//                Tabela tabela = (Tabela) nodeObj;
//            } else if (nodeObj instanceof Sequence) {
//                Sequence seq = (Sequence) nodeObj;
            }
        } catch (SQLException ex) {
        }
    }

    public void refresh(Object treeObj) {
        TreeItem node = getTreeItem(treeObj, null);
        if (node != null) {
            refresh(node);
        }
    }

    public TreeItem getTreeItem(Object value, TreeItem<Object> nodeStart) {
        if (nodeStart == null) {
            nodeStart = rootNode;
        }

        if (nodeStart.getValue().equals(value)) {
            return nodeStart;
        }

        for (TreeItem item : nodeStart.getChildren()) {
            if (item.getValue().equals(value)) {
                return item;
            }
            if (!item.getChildren().isEmpty()) {
                TreeItem ti = getTreeItem(value, item);
                if (ti != null) {
                    return ti;
                }
            }
        }
        return null;
    }

    public <T> T getSelectedObject() {
        return (T) tree.getSelectionModel().getSelectedItem().getValue();
    }

    public List getSelectedObjects() {
        List l = new ArrayList();
        for (TreeItem t : tree.getSelectionModel().getSelectedItems()) {
            l.add(t.getValue());
        }
        return l;
    }

    @FXML
    private void addServer(ActionEvent event) {
        Tab tab = addTab("Adicionar Servidor", "addServer");
    }

    @FXML
    private void showSqlLog(ActionEvent event) {
        addTab("SQL Log", "sqlLog");
    }

    @FXML
    private void refreshTreeItem(ActionEvent event) {
        refresh(tree.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void compareStruct(ActionEvent event) {
        addTab("Comparar Schemas", "compStruct");
    }

    @FXML
    private void compareData(ActionEvent event) {
        addTab("Comparar Dados", "compData");
    }

    @FXML
    private void importData(ActionEvent event) {
        addTab("Importar Dados", "importacao");
    }

    @FXML
    private void openSqlEditor(ActionEvent event) {
        TreeItem<Object> item = tree.getSelectionModel().getSelectedItem();
        Object nodeVal = item.getValue();

        String titulo = "SQL Editor";
        if (nodeVal instanceof Banco) {
            Banco b = (Banco) nodeVal;
            titulo = b.getNome();
        } else if (nodeVal instanceof Schema) {
            Schema sc = (Schema) nodeVal;
            titulo = sc.getBanco() + "." + sc.getNome();
        } else if (nodeVal instanceof Tabela) {
            Tabela t = (Tabela) nodeVal;
            titulo = t.getSchema().getBanco() + "." + t.getSchema().getNome();
        }

        addTab(titulo, "sql", false);
    }

    @FXML
    private void openModeler(ActionEvent event) {
        final Tab tab = new Tab("Modeler");

        WebView webView = new WebView();
        tab.setContent(webView);

        webView.getEngine().getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {

            @Override
            public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) webView.getEngine().executeScript("window");
                    window.setMember("app", new JavaHelper());

                    Element el = (Element) webView.getEngine().executeScript("document.getElementById('teste')");
                    el.setTextContent("Em breve...");
                }
            }
        });

        webView.getEngine().load(Start.class.getResource("modeler/index.html").toExternalForm());

        tabbedPane.getTabs().add(tab);
        tabbedPane.getSelectionModel().select(tab);
    }

    public class JavaHelper {

        public void msg(String msg) {
            MessageBox.message(msg);
        }
    }

}
