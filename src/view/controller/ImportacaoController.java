package view.controller;

import model.Tabela;
import utils.MessageBox;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class ImportacaoController implements Initializable {

    private Tabela t1, t2;
    @FXML
    private TextField tfDe;
    @FXML
    private TextField tfPara;
    @FXML
    private CheckBox ckAtSeq;
    @FXML
    private CheckBox ckClean;
    @FXML
    private Button bImport;
    @FXML
    private Button bInvert;
    @FXML
    private ProgressBar progress;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bInvert.setTooltip(new Tooltip("Inverter"));
        List sel = PrincipalController.INSTANCE.getSelectedObjects();
        t1 = (Tabela) sel.get(0);
        t2 = (Tabela) sel.get(1);

        tfDe.setText(t1.getSchema().getBanco().getServidor().getNome() + "." + t1.getSchema().getBanco().getNome() + "." + t1.getSchema().getNome() + "." + t1.getNome());
        tfPara.setText(t2.getSchema().getBanco().getServidor().getNome() + "." + t2.getSchema().getBanco().getNome() + "." + t2.getSchema().getNome() + "." + t2.getNome());
    }

    @FXML
    private void importar(ActionEvent event) {
        bImport.setDisable(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (Connection conn1 = t1.getSchema().getBanco().connect(); Connection conn2 = t2.getSchema().getBanco().connect()) {
                    conn2.setAutoCommit(false);

                    if (ckClean.isSelected()) {
                        Statement stm2 = conn2.createStatement();
                        stm2.executeUpdate("DELETE FROM " + t2.getSchema().getNome() + "." + t2.getNome());
                        conn2.commit();
                    }

                    Statement stm1 = conn1.createStatement();
                    ResultSet rs = stm1.executeQuery("select count(*) from " + t1.getSchema().getNome() + "." + t1.getNome());
                    rs.next();

                    final int count = rs.getInt(1);
                    int i = 1;
                    rs = stm1.executeQuery("select * from " + t1.getSchema().getNome() + "." + t1.getNome());
                    int cols = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        List<Object> linha = new ArrayList<>(cols);
                        for (int col = 1; col <= cols; col++) {
                            linha.add(rs.getObject(col));
                        }

                        String insert = "INSERT INTO " + t2.getSchema().getNome() + "." + t2.getNome() + " VALUES (?";
                        for (int col = 2; col <= cols; col++) {
                            insert += ",?";
                        }
                        insert += ");";

                        PreparedStatement pstm = conn2.prepareStatement(insert);
                        for (int col = 1; col <= cols; col++) {
                            pstm.setObject(col, linha.get(col - 1));
                        }

                        pstm.executeUpdate();

                        final double ii = i;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                progress.setProgress(ii / count);
                            }
                        });
                        i++;
                    }

                    if (ckAtSeq.isSelected()) {
                        try {
                            long value = 1;
                            rs = stm1.executeQuery("select last_value from " + t1.getSchema().getNome() + ".seq_id_" + t1.getNome());
                            if (rs.next()) {
                                value = rs.getLong(1);
                            }

                            Statement stm2 = conn2.createStatement();
                            stm2.execute("SELECT setval('" + t2.getSchema().getNome() + ".seq_id_" + t2.getNome() + "', " + value + ", true);");
                        } catch (SQLException e) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    MessageBox.errorMessage("Não foi possível atualizar a sequence!",
                                            "Sequence '" + t2.getSchema().getNome() + ".seq_id_" + t2.getNome() + "' não encontrada.");
                                }
                            });
                        }
                    }
                    conn2.commit();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            MessageBox.message("Importação concluída com sucesso!");
                        }
                    });
                } catch (SQLException ex) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            MessageBox.errorMessage(ex);
                        }
                    });
                } finally {
                    bImport.setDisable(false);
                }
            }
        }).start();
    }

    @FXML
    private void inverter(ActionEvent event) {
        Tabela aux = t1;
        t1 = t2;
        t2 = aux;

        tfDe.setText(t1.getSchema().getBanco().getServidor().getNome() + "." + t1.getSchema().getBanco().getNome() + "." + t1.getSchema().getNome() + "." + t1.getNome());
        tfPara.setText(t2.getSchema().getBanco().getServidor().getNome() + "." + t2.getSchema().getBanco().getNome() + "." + t2.getSchema().getNome() + "." + t2.getNome());
    }

}
