package view.controller;

import model.Servidor;
import utils.DB;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class AddServerController implements Initializable {

    @FXML
    private TextField tfNome;
    @FXML
    private TextField tfPass;
    @FXML
    private TextField tfHost;
    @FXML
    private TextField tfUser;
    @FXML
    private TextField tfPort;
    @FXML
    private Button bOK;
    @FXML
    private AnchorPane anchor;

    private Servidor servidor;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }

    @FXML
    private void addServer(ActionEvent event) {
        bOK.setDisable(true);

        try {
            Servidor s = new Servidor();
            s.setNome(tfNome.getText());
            s.setEndereco(tfHost.getText());
            s.setPorta(Integer.parseInt(tfPort.getText()));
            s.setUser(tfUser.getText());
            s.setPwd(new String(tfPass.getText()));

            Connection conn = s.connect();
            DatabaseMetaData md = conn.getMetaData();
            s.setVersao(md.getDatabaseProductVersion());
            s.setSgbd(md.getDatabaseProductName());
            conn.close();

            if (servidor != null) {
                DB.getServers().remove(servidor);
            }
            DB.getServers().add(s);
            PrincipalController.INSTANCE.loadTree();
            PrincipalController.INSTANCE.closeSelectedTab();
        } catch (SQLException ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).show();
            bOK.setDisable(false);
        }
    }

    public void setServidor(Servidor servidor) {
        this.servidor = servidor;
        tfNome.setText(servidor.getNome());
        tfHost.setText(servidor.getEndereco());
        tfPort.setText(servidor.getPorta() + "");
        tfUser.setText(servidor.getUser());
        tfPass.setText(servidor.getPwd());
    }
}
