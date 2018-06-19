package view.controller;

import model.Servidor;
import utils.MessageBox;
import utils.SqlLog;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class NewDBController implements Initializable {

    @FXML
    private Button bOk;
    @FXML
    private TextField tfNome;
    @FXML
    private ComboBox<String> cbOwner;
    @FXML
    private ComboBox<String> cbEncode;

    private Servidor servidor;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbEncode.getItems().add("UTF8");
        cbEncode.getItems().add("SQL_ASCII");
        cbEncode.getItems().add("WIN1252");
        cbEncode.getItems().add("LATIN1");
        cbEncode.getSelectionModel().select(0);
        
        this.servidor = PrincipalController.INSTANCE.getSelectedObject();
        try {
            List<String> roles = servidor.getLoginRolesNames();
            for (String role : roles) {
                cbOwner.getItems().add(role);
            }
            cbOwner.getSelectionModel().select(0);
        } catch (Exception ex) {
            MessageBox.errorMessage(ex);
        }
    }

    @FXML
    private void criarDB(ActionEvent event) {
        try (Connection conn = servidor.connect()) {
            String sql = "CREATE DATABASE " + tfNome.getText()
                    + "  WITH ENCODING='" + cbEncode.getSelectionModel().getSelectedItem() + "'"
                    + "       OWNER=" + cbOwner.getSelectionModel().getSelectedItem()
                    + "       CONNECTION LIMIT=-1;";
            conn.createStatement().executeUpdate(sql);
            SqlLog.append(sql);
            PrincipalController.INSTANCE.refresh(servidor);
            PrincipalController.INSTANCE.closeSelectedTab();
        } catch (SQLException ex) {
            MessageBox.errorMessage(ex);
        }
    }

}
