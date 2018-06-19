package view.controller;

import model.ForeignKey;
import model.Tabela;
import utils.MessageBox;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class TabVinculosController implements Initializable {

    private Tabela tabela;
    @FXML
    private ListView<Tabela> listDependencias;
    @FXML
    private ListView<Tabela> listDependentes;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.tabela = PrincipalController.INSTANCE.getSelectedObject();

        listDependencias.getItems().add(new Tabela(null, "Carregando..."));
        listDependentes.getItems().add(new Tabela(null, "Carregando..."));

        Task<List<Tabela>> taskDependencias = new Task<List<Tabela>>() {
            List<Tabela> dependencias = new ArrayList<>();

            @Override protected List<Tabela> call() throws Exception {
                try {
                    tabela.loadCols();
                    for (ForeignKey fk : tabela.getFKs()) {
                        if (!dependencias.contains(fk.getPkTab())) {
                            dependencias.add(fk.getPkTab());
                        }
                    }

                    return dependencias;
                } catch (SQLException ex) {
                    MessageBox.errorMessage(ex);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                listDependencias.setItems(FXCollections.observableList(dependencias));
            }
        };

        Task<List<Tabela>> taskDependentes = new Task<List<Tabela>>() {
            List<Tabela> dependentes = new ArrayList<>();

            @Override protected List<Tabela> call() throws Exception {
                try {
                    for (Tabela t : tabela.getSchema().getBanco().getTabelas()) {
                        t.loadCols();
                        for (ForeignKey fk : t.getFKs()) {
                            if (fk.getPkTab().equals(tabela) && !dependentes.contains(t)) {
                                dependentes.add(t);
                            }
                        }
                    }

                    return dependentes;
                } catch (SQLException ex) {
                    MessageBox.errorMessage(ex);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                listDependentes.setItems(FXCollections.observableList(dependentes));
            }

        };

        new Thread(taskDependencias).start();
        new Thread(taskDependentes).start();
    }

}
