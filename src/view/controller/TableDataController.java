package view.controller;

import model.Coluna;
import model.Tabela;
import utils.MessageBox;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class TableDataController implements Initializable {

    private Tabela tabela;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @FXML
    private Button bRefresh;
    @FXML
    private Button bOrder;
    @FXML
    private Button bFilter;
    @FXML
    private TableView<Object[]> table;
    @FXML
    private Label lRes;
    @FXML
    private Label lOrder;
    @FXML
    private Label lWhere;
    @FXML
    private Button bInsertCmd;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tabela = PrincipalController.INSTANCE.getSelectedObject();

        lWhere.setText(tabela.getFilter());
        lOrder.setText(tabela.getOrder());

        try {
            tabela.loadCols();
            for (int i = 0; i < tabela.getColunas().size(); i++) {
                Coluna c = tabela.getColunas().get(i);
                final int idx = i;

                TableColumn<Object[], String> col = new TableColumn<>(c.getNome());
//                col.setCellValueFactory(new PropertyValueFactory<>("struct"));
                col.setCellValueFactory((TableColumn.CellDataFeatures<Object[], String> p) -> new ObservableValueBase<String>() {
                    @Override
                    public String getValue() {
                        return p.getValue()[idx] != null ? p.getValue()[idx].toString() : "";
                    }
                });

//                if (!c.isPk()) {
                col.setCellFactory(TextFieldTableCell.forTableColumn());
                col.setOnEditCommit(new EventHandler<CellEditEvent<Object[], String>>() {
                    @Override
                    public void handle(CellEditEvent<Object[], String> t) {
                        Object[] row = t.getRowValue();

                        try (Connection conn = tabela.getSchema().connect()) {
                            if (t.getNewValue().isEmpty()) {
                                row[idx] = null;
                            } else if (c.getSqlClass() == java.sql.Date.class) {
                                row[idx] = new java.sql.Date(DATE_FORMAT.parse(t.getNewValue()).getTime());
                            } else if (c.getSqlClass() == java.sql.Time.class) {
                                row[idx] = new java.sql.Time(TIME_FORMAT.parse(t.getNewValue()).getTime());
                            } else if (c.getSqlClass() == java.sql.Timestamp.class) {
                                row[idx] = new java.sql.Timestamp(TIMESTAMP_FORMAT.parse(t.getNewValue()).getTime());
                            } else if (c.getSqlClass() == Integer.class) {
                                row[idx] = Integer.parseInt(t.getNewValue());
                            } else if (c.getSqlClass() == Double.class) {
                                row[idx] = Double.parseDouble(t.getNewValue());
                            } else if (c.getSqlClass() == Long.class) {
                                row[idx] = Long.parseLong(t.getNewValue());
                            } else if (c.getSqlClass() == Boolean.class) {
                                row[idx] = Boolean.valueOf(t.getNewValue()).booleanValue();
                            } else {
                                row[idx] = t.getNewValue();
                            }

                            if (!c.isPk() && row[0] != null) {
                                PreparedStatement pstm = conn.prepareStatement("UPDATE " + tabela.getNome()
                                        + " SET " + c.getNome() + "=?"
                                        + " WHERE " + tabela.getColunas().get(0).getNome() + "=?;");
                                pstm.setObject(1, row[idx]);
                                pstm.setObject(2, row[0]);
                                pstm.executeUpdate();
                            } else if (idx == 0) {
                                String cols = tabela.getColunas().get(0).getNome();
                                String vals = "?";

                                for (int i = 1; i < row.length; i++) {
                                    if (row[i] != null) {
                                        cols += "," + tabela.getColunas().get(i).getNome();
                                        vals += ",?";
                                    }
                                }

                                String insert = "INSERT INTO " + tabela.getNome() + "(" + cols + ") values (" + vals + ");";
                                PreparedStatement pstm = conn.prepareStatement(insert);

                                int v = 1;
                                for (int i = 0; i < row.length; i++) {
                                    if (row[i] != null) {
                                        pstm.setObject(v, row[i]);
                                        v++;
                                    }
                                }
                                pstm.executeUpdate();
                                table.getItems().add(new Object[tabela.getColunas().size()]);
                            }
                        } catch (Exception ex) {
//                            row[idx] = t.getOldValue();
                            t.consume();
                            MessageBox.errorMessage(ex);
                        }
                    }
                }
                );

                col.setEditable(true);
//                }

                table.getColumns().add(col);
            }
            table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            refresh();
        } catch (SQLException ex) {
            MessageBox.errorMessage(ex);
        }
    }

    private void refresh() {
        try {
            table.getItems().clear();
            List<Object[]> lines = new ArrayList<Object[]>(tabela.readData());
            lRes.setText(lines.size() + " registros");
            table.setItems(FXCollections.observableList(lines));
            table.getItems().add(new Object[tabela.getColunas().size()]);
        } catch (SQLException ex) {
            MessageBox.errorMessage(ex);
        }
    }

    @FXML
    private void atualizar(ActionEvent event) {
        refresh();
    }

    @FXML
    private void ordenar(ActionEvent event) {
        while (true) {
            String order = MessageBox.input("ORDER BY:", tabela.getOrder());
            if (order != null) {
                tabela.setOrder(order);
                lOrder.setText("ORDER BY " + order);
                refresh();
                break;
            } else {
                break;
            }
        }
    }

    @FXML
    private void filtrar(ActionEvent event) {
        while (true) {
            String filter = MessageBox.input("WHERE:", tabela.getFilter());
            if (filter != null) {
                tabela.setFilter(filter);
                lWhere.setText("WHERE " + filter);
                refresh();
                break;
            } else {
                break;
            }
        }
    }

    @FXML
    private void tableKeyPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.F5)) {
            refresh();
        } else if (event.getCode().equals(KeyCode.DELETE)) {
            List<Object[]> rows = table.getSelectionModel().getSelectedItems();
            if (!rows.isEmpty() && MessageBox.confirm("Excluír os registros selecionados???")) {
                try (Connection conn = tabela.getSchema().connect()) {
                    conn.setAutoCommit(false);
                    for (Object[] row : rows) {
                        if (row[0] != null) {
                            conn.createStatement().executeUpdate("DELETE from " + tabela.getNome()
                                    + " where " + tabela.getColunas().get(0).getNome() + "=" + row[0]);
//                            table.getItems().remove(row);
                        }
                    }
                    conn.commit();
                } catch (SQLException ex) {
                    MessageBox.errorMessage(ex);
                }
                refresh();
            }
        }
    }

    @FXML
    private void genInsertCmd(ActionEvent event) {
        List<Object[]> rows = table.getSelectionModel().getSelectedItems();

        String cols = tabela.getColunas().get(0).getNome();

        for (int i = 1; i < tabela.getColunas().size(); i++) {
            cols += ", " + tabela.getColunas().get(i).getNome();
        }

        String cmd = "INSERT INTO " + tabela.getNome() + "(" + cols + ") values ";
        for (int j = 0; j < rows.size(); j++) {
            String vals = "('" + rows.get(j)[0].toString() + "'";
            for (int i = 1; i < rows.get(j).length; i++) {
                if (rows.get(j)[i] != null) {
                    vals += ", '" + rows.get(j)[i].toString() + "'";
                } else {
                    vals += ", null";
                }
            }
            cmd += vals + ")" + (j < rows.size() - 1 ? "," : "") + "\n";
        }
        cmd = cmd.trim() + ";";

        if (!cmd.isEmpty()) {
            MessageBox.detailMessage("Inserts gerados.", "SQL:", cmd);
        }
    }

}
