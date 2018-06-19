package view.controller;

import model.Schema;
import utils.MessageBox;
import utils.SqlLog;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
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
public class NewSequenceController implements Initializable {

    private Schema schema;
    @FXML
    private Button bOk;
    @FXML
    private ComboBox<String> cbOwner;
    @FXML
    private TextField tfNome;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.schema = PrincipalController.INSTANCE.getSelectedObject();
        try {
            List<String> roles = schema.getBanco().getServidor().getLoginRolesNames();
            for (String role : roles) {
                cbOwner.getItems().add(role);
            }
            cbOwner.getSelectionModel().select(0);
        } catch (Exception ex) {
            MessageBox.errorMessage(ex);
        }
    }

    @FXML
    private void criarSequence(ActionEvent event) {
        try (Connection conn = schema.connect()) {
            String sql = "CREATE SEQUENCE " + tfNome.getText() + ";\n"
                    + " ALTER SEQUENCE " + tfNome.getText()
                    + "  OWNER TO " + cbOwner.getSelectionModel().getSelectedItem() + ";";
            conn.createStatement().executeUpdate(sql);
            SqlLog.append(sql);
            PrincipalController.INSTANCE.refresh(schema);
            PrincipalController.INSTANCE.closeSelectedTab();
        } catch (SQLException ex) {
            MessageBox.errorMessage(ex);
        }
    }

}
