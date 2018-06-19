package view.controller;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import model.Coluna;
import model.Compare;
import model.Tabela;
import utils.MessageBox;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValueBase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;
import javax.swing.JOptionPane;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class CompDataController implements Initializable {

    private Tabela t1, t2;

    @FXML
    private Label lDe;
    @FXML
    private Label lPara;
    @FXML
    private CheckBox ckDif;
    @FXML
    private Button bComp;
    @FXML
    private Button bInvert;
    @FXML
    private TableView<Compare> tOrigem;
    @FXML
    private TableView<Compare> tDestino;
    @FXML
    private Button bToDest;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bInvert.setTooltip(new Tooltip("Inverter"));
        List sel = PrincipalController.INSTANCE.getSelectedObjects();
        t1 = (Tabela) sel.get(0);
        t2 = (Tabela) sel.get(1);

        lDe.setText(t1.getSchema().getBanco().getServidor().getNome() + "." + t1.getSchema().getBanco().getNome() + "." + t1.getSchema().getNome() + "." + t1.getNome());
        lPara.setText(t2.getSchema().getBanco().getServidor().getNome() + "." + t2.getSchema().getBanco().getNome() + "." + t2.getSchema().getNome() + "." + t2.getNome());
        tDestino.setPlaceholder(new Label(""));
        tOrigem.setPlaceholder(new Label(""));

        tDestino.setSelectionModel(tOrigem.getSelectionModel());
    }

    @FXML
    private void switchTables(ActionEvent event) {
        Tabela aux = t1;
        t1 = t2;
        t2 = aux;

        lDe.setText(t1.getSchema().getBanco().getServidor().getNome() + "." + t1.getSchema().getBanco().getNome() + "." + t1.getSchema().getNome() + "." + t1.getNome());
        lPara.setText(t2.getSchema().getBanco().getServidor().getNome() + "." + t2.getSchema().getBanco().getNome() + "." + t2.getSchema().getNome() + "." + t2.getNome());
    }

    @FXML
    private void comparar(ActionEvent event) {
        tOrigem.getItems().clear();
        tDestino.getItems().clear();
        tOrigem.getColumns().clear();
        tDestino.getColumns().clear();

        bComp.setDisable(true);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        try (Connection conn1 = t1.getSchema().connect(); Connection conn2 = t2.getSchema().connect()) {
            t1.loadCols();

            for (int i = 0; i < t1.getColunas().size(); i++) {
                Coluna c = t1.getColunas().get(i);
                final int idx = i;

                TableColumn<Compare, String> col = new TableColumn<>(c.getNome());
                col.setCellValueFactory((TableColumn.CellDataFeatures<Compare, String> p) -> new ObservableValueBase<String>() {
                    @Override
                    public String getValue() {
                        if (idx == 0) {
                            return p.getValue().getChave() != null ? p.getValue().getChave().toString() : "";
                        } else {
                            return p.getValue().getDados() != null && p.getValue().getDados().get(idx - 1) != null ? p.getValue().getDados().get(idx - 1).toString() : "";
                        }
                    }
                });

                tOrigem.getColumns().add(col);
            }

            t2.loadCols();

            for (int i = 0; i < t2.getColunas().size(); i++) {
                Coluna c = t2.getColunas().get(i);
                final int idx = i;

                TableColumn<Compare, String> col = new TableColumn<>(c.getNome());
                col.setCellValueFactory((TableColumn.CellDataFeatures<Compare, String> p) -> new ObservableValueBase<String>() {
                    @Override
                    public String getValue() {
                        if (idx == 0) {
                            return p.getValue().getChave() != null ? p.getValue().getChave().toString() : "";
                        } else {
                            return p.getValue().getDados() != null && p.getValue().getDados().get(idx - 1) != null ? p.getValue().getDados().get(idx - 1).toString() : "";
                        }
                    }
                });

                col.setCellFactory(new Callback<TableColumn<Compare, String>, TableCell<Compare, String>>() {
                    @Override
                    public TableCell<Compare, String> call(TableColumn<Compare, String> personStringTableColumn) {
                        return new TableCell<Compare, String>() {
                            @Override
                            protected void updateItem(String name, boolean empty) {
                                super.updateItem(name, empty);

                                if (!isEmpty()) {
                                    TableRow<Compare> currentRow = getTableRow();
                                    if (currentRow != null) {
                                        Compare c = currentRow.getItem();

                                        if (c.getTipo() == Compare.DIF && idx > 0) {
                                            Compare c2 = tOrigem.getItems().get(currentRow.getIndex());
                                            if (!Objects.equals(c.getDados().get(idx - 1), c2.getDados().get(idx - 1))) {
                                                setStyle("-fx-background-color:lightsalmon");
                                            }
                                        } else if (c.getTipo() == Compare.EXTRA) {
//                                            currentRow.setStyle("-fx-background-color:lightcoral");
                                        } else if (c.getTipo() == Compare.EQ) {
//                                            currentRow.setStyle("-fx-background-color:lightgreen");
                                        } else {
                                        }
                                        this.setText(getItem());
                                    }
                                }
                            }
                        };
                    }
                });

                tDestino.getColumns().add(col);
            }

            //ORIGEM
            Statement stm1 = conn1.createStatement();
            ResultSet rs1 = stm1.executeQuery("select * from " + t1.getNome());

            Map<Object, Object[]> dados1 = new HashMap<>();

            Object[] linha;
            while (rs1.next()) {
                linha = new Object[t1.getColunas().size() - 1];
                for (int i = 0; i < t1.getColunas().size() - 1; i++) {
                    linha[i] = rs1.getObject(t1.getColunas().get(i + 1).getNome());
                }

                dados1.put(rs1.getObject(t1.getColunas().get(0).getNome()), linha);//chave
            }

            //DESTINO
            Statement stm2 = conn2.createStatement();
            ResultSet rs2 = stm2.executeQuery("select * from " + t2.getNome());

            Map<Object, Object[]> dados2 = new HashMap<>();

            while (rs2.next()) {
                linha = new Object[t2.getColunas().size() - 1];
                for (int i = 0; i < t2.getColunas().size() - 1; i++) {
                    linha[i] = rs2.getObject(t2.getColunas().get(i + 1).getNome());
                }

                dados2.put(rs2.getObject(t2.getColunas().get(0).getNome()), linha);//chave
            }

            //COMPARE
            //DIFERENTES
            for (Object ch : dados1.keySet()) {
                if (dados2.containsKey(ch)) {
                    if (!Arrays.equals(dados1.get(ch), dados2.get(ch))) {
                        Compare c1 = new Compare(Compare.DIF);
                        c1.setChave(ch);
                        c1.setDados(Arrays.asList(dados1.get(ch)));
                        tOrigem.getItems().add(c1);

                        Compare c2 = new Compare(Compare.DIF);
                        c2.setChave(ch);
                        c2.setDados(Arrays.asList(dados2.get(ch)));
                        tDestino.getItems().add(c2);
                    } else if (!ckDif.isSelected()) {
                        Compare c1 = new Compare(Compare.EQ);
                        c1.setChave(ch);
                        c1.setDados(Arrays.asList(dados1.get(ch)));
                        tOrigem.getItems().add(c1);

                        Compare c2 = new Compare(Compare.EQ);
                        c2.setChave(ch);
                        c2.setDados(Arrays.asList(dados2.get(ch)));
                        tDestino.getItems().add(c2);
                    }
                }
            }

            //FALTANTES
            for (Object ch : dados1.keySet()) {
                if (!dados2.containsKey(ch)) {
                    Compare c1 = new Compare(Compare.EXTRA);
                    c1.setChave(ch);
                    c1.setDados(Arrays.asList(dados1.get(ch)));
                    tOrigem.getItems().add(c1);

                    Compare c2 = new Compare(Compare.BLANK);
                    tDestino.getItems().add(c2);
                }
            }
            for (Object ch : dados2.keySet()) {
                if (!dados1.containsKey(ch)) {
                    Compare c1 = new Compare(Compare.BLANK);
                    tOrigem.getItems().add(c1);

                    Compare c2 = new Compare(Compare.EXTRA);
                    c2.setChave(ch);
                    c2.setDados(Arrays.asList(dados2.get(ch)));
                    tDestino.getItems().add(c2);
                }
            }

        } catch (Exception ex) {
            MessageBox.errorMessage(ex);
        }
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
        bComp.setDisable(false);
//                    }
//                });
//            }
//        }).start();
        bindScrollBars(tOrigem, tDestino);
    }

    @FXML
    private void enviarParaDestino(ActionEvent event) {
        bToDest.setDisable(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (Connection conn2 = t2.getSchema().getBanco().connect()) {
                    conn2.setAutoCommit(false);
                    for (int r : tOrigem.getSelectionModel().getSelectedIndices()) {
                        Compare c1 = tOrigem.getItems().get(r);
                        Compare c2 = tDestino.getItems().get(r);

                        if (c1.getTipo() == Compare.EXTRA) {
                            String sql = "INSERT INTO " + t2.getSchema().getNome() + "." + t2.getNome() + "(";
                            for (int i = 1; i <= t2.getColunas().size(); i++) {
                                sql += t2.getColunas().get(i - 1).getNome();
                                if (i < t2.getColunas().size()) {
                                    sql += ", ";
                                }
                            }
                            sql += ") VALUES (?";
                            for (int i = 0; i < c1.getDados().size(); i++) {
                                sql += ", ?";
                            }
                            sql += ");";
                            PreparedStatement pstm = conn2.prepareStatement(sql);

                            pstm.setObject(1, c1.getChave());
                            for (int i = 0; i < c1.getDados().size(); i++) {
                                pstm.setObject(i + 2, c1.getDados().get(i));
                            }
                            pstm.executeUpdate();
                        } else if (c1.getTipo() == Compare.DIF) {
                            String sql = "UPDATE " + t2.getSchema().getNome() + "." + t2.getNome() + " SET ";
                            for (int i = 1; i < t2.getColunas().size(); i++) {
                                sql += t2.getColunas().get(i).getNome() + "=?";
                                if (i < t2.getColunas().size() - 1) {
                                    sql += ", ";
                                }
                            }
                            sql += " WHERE " + t2.getColunas().get(0).getNome() + "=?;";
                            PreparedStatement pstm = conn2.prepareStatement(sql);
                            for (int i = 0; i < c1.getDados().size(); i++) {
                                pstm.setObject(i + 1, c1.getDados().get(i));
                            }
                            pstm.setObject(c1.getDados().size() + 1, c1.getChave());
                            pstm.executeUpdate();
                        } else {
                            continue;
                        }

                        c2.setChave(c1.getChave());
                        c2.setTipo(Compare.EQ);
                        c1.setTipo(Compare.EQ);
                        c2.setDados(c1.getDados());
                        tDestino.refresh();
//                        tmDest.refresh(r);
                    }

                    Statement stm2 = conn2.createStatement();
                    try {
                        stm2.execute("SELECT setval('" + t2.getSchema().getNome() + ".seq_id_" + t2.getNome()
                                + "', (SELECT MAX(" + t2.getColunas().get(0).getNome() + ") FROM " + t2.getSchema().getNome() + "." + t2.getNome() + "), true);");
                    } catch (Exception e) {
                    }
                    conn2.commit();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "ERRO: " + ex.getMessage());
                }
                bToDest.setDisable(false);
            }
        }).start();
    }

    private void bindScrollBars(TableView<?> tableView1, TableView<?> tableView2) {
        // Get the scrollbar of first table
        VirtualFlow vf = (VirtualFlow) tableView1.getChildrenUnmodifiable().get(1);
        ScrollBar scrollBarV1 = null;
        ScrollBar scrollBarH1 = null;
        for (final Node subNode : vf.getChildrenUnmodifiable()) {
            if (subNode instanceof ScrollBar
                    && ((ScrollBar) subNode).getOrientation() == Orientation.VERTICAL) {
                scrollBarV1 = (ScrollBar) subNode;
            }
            if (subNode instanceof ScrollBar
                    && ((ScrollBar) subNode).getOrientation() == Orientation.HORIZONTAL) {
                scrollBarH1 = (ScrollBar) subNode;
            }
        }

        // Get the scrollbar of second table
        vf = (VirtualFlow) tableView2.getChildrenUnmodifiable().get(1);
        ScrollBar scrollBarV2 = null;
        ScrollBar scrollBarH2 = null;
        for (final Node subNode : vf.getChildrenUnmodifiable()) {
            if (subNode instanceof ScrollBar
                    && ((ScrollBar) subNode).getOrientation() == Orientation.VERTICAL) {
                scrollBarV2 = (ScrollBar) subNode;
            }
            if (subNode instanceof ScrollBar
                    && ((ScrollBar) subNode).getOrientation() == Orientation.HORIZONTAL) {
                scrollBarH2 = (ScrollBar) subNode;
            }
        }

        // Set min/max of visible scrollbar to min/max of a table scrollbar
        scrollBarV2.setMin(scrollBarV1.getMin());
        scrollBarV2.setMax(scrollBarV1.getMax());
        scrollBarH2.setMin(scrollBarH1.getMin());
        scrollBarH2.setMax(scrollBarH1.getMax());

        // bind the hidden scrollbar valueProterty the visible scrollbar
        scrollBarV2.valueProperty().bindBidirectional(scrollBarV1.valueProperty());
        scrollBarH2.valueProperty().bindBidirectional(scrollBarH1.valueProperty());
    }
}
