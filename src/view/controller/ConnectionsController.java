package view.controller;

import model.Banco;
import model.Servidor;
import utils.MessageBox;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class ConnectionsController implements Initializable {

    private int timeout = 1;
    private Servidor servidor;
    private Banco banco;
    private Tab thisTab;

    @FXML
    private AnchorPane anchor;
    @FXML
    private Button bRefresh;
    @FXML
    private Button bTerminate;
    @FXML
    private CheckBox ckAuto;
    @FXML
    private ComboBox<String> cbDelay;
    @FXML
    private TableView<Object[]> table;
    @FXML
    private Label lCount;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbDelay.setItems(FXCollections.observableArrayList(new String[]{
            "1 segundo", "5 segundos", "10 segundos", "30 segundos", "1 minuto", "5 minutos", "10 minutos", "30 minutos", "1 hora"}));

        cbDelay.getSelectionModel().select(0);
        Object o = PrincipalController.INSTANCE.getSelectedObject();
        if (o instanceof Servidor) {
            servidor = (Servidor) o;
        } else if (o instanceof Banco) {
            banco = (Banco) o;
            servidor = banco.getServidor();
        }

        TableColumn<Object[], String> colPid = new TableColumn<>("PID");
        colPid.setCellValueFactory((TableColumn.CellDataFeatures<Object[], String> p) -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return p.getValue()[0] != null ? p.getValue()[0].toString() : "";
            }
        });
        table.getColumns().add(colPid);

        TableColumn<Object[], String> colDatname = new TableColumn<>("Database");
        colDatname.setCellValueFactory((TableColumn.CellDataFeatures<Object[], String> p) -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return p.getValue()[1] != null ? p.getValue()[1].toString() : "";
            }
        });
        table.getColumns().add(colDatname);

        TableColumn<Object[], String> colAppName = new TableColumn<>("Application");
        colAppName.setCellValueFactory((TableColumn.CellDataFeatures<Object[], String> p) -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return p.getValue()[2] != null ? p.getValue()[2].toString() : "";
            }
        });
        table.getColumns().add(colAppName);

        TableColumn<Object[], String> colUser = new TableColumn<>("User");
        colUser.setCellValueFactory((TableColumn.CellDataFeatures<Object[], String> p) -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return p.getValue()[3] != null ? p.getValue()[3].toString() : "";
            }
        });
        table.getColumns().add(colUser);

        TableColumn<Object[], String> colCliAdd = new TableColumn<>("Client");
        colCliAdd.setCellValueFactory((TableColumn.CellDataFeatures<Object[], String> p) -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return p.getValue()[4] != null ? p.getValue()[4].toString() : "";
            }
        });
        table.getColumns().add(colCliAdd);

        TableColumn<Object[], String> colConnStart = new TableColumn<>("Connection Start");
        colConnStart.setCellValueFactory((TableColumn.CellDataFeatures<Object[], String> p) -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return p.getValue()[5] != null ? p.getValue()[5].toString() : "";
            }
        });
        table.getColumns().add(colConnStart);

        TableColumn<Object[], String> colSQLStart = new TableColumn<>("SQL Start");
        colSQLStart.setCellValueFactory((TableColumn.CellDataFeatures<Object[], String> p) -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return p.getValue()[6] != null ? p.getValue()[6].toString() : "";
            }
        });
        table.getColumns().add(colSQLStart);

        TableColumn<Object[], String> colBlock = new TableColumn<>("Blocked by");
        colBlock.setCellValueFactory((TableColumn.CellDataFeatures<Object[], String> p) -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return p.getValue()[7] != null ? p.getValue()[7].toString() : "";
            }
        });
        table.getColumns().add(colBlock);

        TableColumn<Object[], String> colSql = new TableColumn<>("SQL");
        colSql.setCellValueFactory((TableColumn.CellDataFeatures<Object[], String> p) -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return p.getValue()[8] != null ? p.getValue()[8].toString() : "";
            }
        });

        colSql.setCellFactory(new Callback<TableColumn<Object[], String>, TableCell<Object[], String>>() {
            @Override
            public TableCell<Object[], String> call(TableColumn<Object[], String> personStringTableColumn) {
                return new TableCell<Object[], String>() {
                    @Override
                    protected void updateItem(String name, boolean empty) {
                        super.updateItem(name, empty);

                        if (!isEmpty()) {
                            TableRow<Object[]> currentRow = getTableRow();
                            currentRow.setStyle("");
                            if (currentRow.getItem() != null && currentRow.getItem()[8] != null && currentRow.getItem()[8].toString().contains("in transaction")) {
                                currentRow.setStyle("-fx-background-color:lightsalmon");
                            } else if (currentRow.getItem()[7] != null && !currentRow.getItem()[7].toString().isEmpty()) {
                                currentRow.setStyle("-fx-background-color:lightcoral");
                            }
                            this.setText(currentRow.getItem()[8].toString());
                        }
                    }
                };
            }
        });
        table.getColumns().add(colSql);

        TableColumn<Object[], String> colSlow = new TableColumn<>("slow sql");
        colSlow.setCellValueFactory((TableColumn.CellDataFeatures<Object[], String> p) -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return p.getValue()[9] != null ? p.getValue()[9].toString() : "";
            }
        });
        table.getColumns().add(colSlow);

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                refresh(null);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
                thisTab = PrincipalController.INSTANCE.tabbedPane.getSelectionModel().getSelectedItem();
                int count = 1;
                while (PrincipalController.INSTANCE.tabbedPane.getTabs().contains(thisTab)) {
                    if (count >= timeout && ckAuto.isSelected()) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                refresh(null);
                            }
                        });
                        count = 0;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                    count++;
                }
            }
        }).start();
    }

    @FXML
    private void refresh(ActionEvent event) {
        bRefresh.setDisable(true);
        try (Connection conn = servidor.connect()) {
            
            String sql91 = "SELECt p.procpid AS pid, datname, application_name, usename,\n"
                    + " CASE WHEN client_port=-1 THEN 'local pipe' WHEN length(client_hostname)>0 THEN client_hostname||':'||client_port ELSE textin(inet_out(client_addr))||':'||client_port END AS client,\n"
                    + " date_trunc('second', backend_start) AS backend_start, CASE WHEN current_query='' OR current_query='<IDLE>' THEN '' ELSE date_trunc('second', query_start)::text END AS query_start,\n"
                    + " (SELECT min(l1.pid) FROM pg_locks l1 WHERE GRANTED AND (relation IN (SELECT relation FROM pg_locks l2 WHERE l2.pid=p.procpid AND NOT granted) OR transactionid IN (SELECT transactionid FROM pg_locks l3 WHERE l3.pid=p.procpid AND NOT granted))) AS blockedby,\n"
                    + " current_query AS query,\n"
                    + " CASE WHEN query_start IS NULL OR current_query LIKE '<IDLE>%' THEN false ELSE query_start < now() - '10 seconds'::interval END AS slowquery\n"
                    + " FROM pg_stat_activity p " + (banco != null ? " where p.datname='" + banco.getNome() + "'" : "") + " ORDER BY 1 ASC";

            String sql92 = "SELECt p.pid, datname, application_name, usename,\n"
                    + " CASE WHEN client_port=-1 THEN 'local pipe' WHEN length(client_hostname)>0 THEN client_hostname||':'||client_port ELSE textin(inet_out(client_addr))||':'||client_port END AS client,\n"
                    + " date_trunc('second', backend_start) AS backend_start, CASE WHEN query='' OR query='<IDLE>' THEN ''      ELSE date_trunc('second', query_start)::text END AS query_start,\n"
                    + " (SELECT min(l1.pid) FROM pg_locks l1 WHERE GRANTED AND (relation IN (SELECT relation FROM pg_locks l2 WHERE l2.pid=p.pid AND NOT granted) OR transactionid IN (SELECT transactionid FROM pg_locks l3 WHERE l3.pid=p.pid AND NOT granted))) AS blockedby,\n"
                    + " query,\n"
                    + " CASE WHEN query_start IS NULL OR query LIKE '<IDLE>%' THEN false ELSE query_start < now() - '10 seconds'::interval END AS slowquery\n"
                    + " FROM pg_stat_activity p " + (banco != null ? " where p.datname='" + banco.getNome() + "'" : "") + " ORDER BY 1 ASC";

            ResultSet rs = conn.createStatement().executeQuery(servidor.getVersao().startsWith("9.1") ? sql91 : sql92);
            List<Object[]> dados = new ArrayList<>();
            int cols = rs.getMetaData().getColumnCount();
            Object[] linha;
            while (rs.next()) {
                if (!rs.getString("query").startsWith("SELECt p.p")) {
                    linha = new Object[cols];
                    for (int i = 1; i <= cols; i++) {
                        linha[i - 1] = rs.getString(i);
                    }
                    if (linha[7] != null) {
                        Stage stage = (Stage) anchor.getScene().getWindow();
                        stage.requestFocus();
                    }
                    dados.add(linha);
                }
            }

            String pidSel = null;
            if (table.getSelectionModel().getSelectedIndex() >= 0) {
                pidSel = table.getSelectionModel().getSelectedItem()[0].toString();
            }
            table.setItems(FXCollections.observableList(dados));

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    lCount.setText(dados.size() + " conexões");
                }
            });

            if (pidSel != null) {
                for (int i = 0; i < dados.size(); i++) {
                    if (dados.get(i)[0].equals(pidSel)) {
                        table.getSelectionModel().select(i);
                        break;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            MessageBox.errorMessage(ex);
        } finally {
            bRefresh.setDisable(false);
        }
    }

    @FXML
    private void terminate(ActionEvent event) {
        if (!table.getSelectionModel().getSelectedItems().isEmpty()) {
            for (Object[] row : table.getSelectionModel().getSelectedItems()) {
                String pid = row[0].toString();

                try (Connection conn = servidor.connect()) {
                    ResultSet rs = conn.createStatement().executeQuery("SELECT pg_terminate_backend(" + pid + ")");
                    if (rs.next() && rs.getBoolean(1)) {
                        refresh(null);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    MessageBox.errorMessage(ex);
                }
            }
        }
    }

    @FXML
    private void changeDelay(ActionEvent event) {
        switch (cbDelay.getSelectionModel().getSelectedIndex()) {
            case (0):
                timeout = 1;
                break;
            case (1):
                timeout = 5;
                break;
            case (2):
                timeout = 10;
                break;
            case (3):
                timeout = 30;
                break;
            case (4):
                timeout = 60;
                break;
            case (5):
                timeout = 300;
                break;
            case (6):
                timeout = 600;
                break;
            case (7):
                timeout = 1800;
                break;
            case (8):
                timeout = 3600;
                break;
        }
    }

    @FXML
    private void ckAutoAction(ActionEvent event) {
        cbDelay.setDisable(!ckAuto.isSelected());
    }

}
