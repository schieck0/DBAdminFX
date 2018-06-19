package view.controller;

import model.Banco;
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
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class NewSchemaController implements Initializable {

    private Banco banco;
    private Schema schema;
    @FXML
    private AnchorPane anchor;
    @FXML
    private TextField tfNome;
    @FXML
    private ComboBox<String> cbOwner;
    @FXML
    private Button bOk;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Object o = PrincipalController.INSTANCE.getSelectedObject();
        if (o instanceof Banco) {
            this.banco = (Banco) o;
        } else if (o instanceof Schema) {
            this.schema = (Schema) o;
        }

        try {
            List<String> roles = (banco != null ? banco : schema.getBanco()).getServidor().getLoginRolesNames();
            for (String role : roles) {
                cbOwner.getItems().add(role);
            }
            cbOwner.getSelectionModel().select(0);
        } catch (Exception ex) {
            MessageBox.errorMessage(ex);
        }

        if (schema != null) {
            tfNome.setText(schema.getNome());
            cbOwner.getSelectionModel().select(schema.getOwner());
        }
    }

    @FXML
    private void criarSchema(ActionEvent event) {
        if (banco != null) {
            try (Connection conn = banco.connect()) {
                String sql = "CREATE SCHEMA " + tfNome.getText()
                        + " AUTHORIZATION " + cbOwner.getSelectionModel().getSelectedItem() + ";";
                conn.createStatement().executeUpdate(sql);
                SqlLog.append(sql);
                PrincipalController.INSTANCE.refresh(banco);
                PrincipalController.INSTANCE.closeSelectedTab();
            } catch (SQLException ex) {
                MessageBox.errorMessage(ex);
            }
        } else if (schema != null) {
            String sql = "";

            if (!schema.getNome().equals(tfNome.getText())) {
                sql += "ALTER SCHEMA " + schema.getNome() + "  RENAME TO " + tfNome.getText() + ";\n";
            }

            if (!schema.getOwner().equals(cbOwner.getSelectionModel().getSelectedItem())) {
                sql += "ALTER SCHEMA " + schema.getNome() + " OWNER TO " + cbOwner.getSelectionModel().getSelectedItem() + ";";
            }

            if (!sql.isEmpty()) {
                try (Connection conn = schema.getBanco().connect()) {
                    conn.createStatement().executeUpdate(sql);
                    SqlLog.append(sql);
                    PrincipalController.INSTANCE.refresh(schema.getBanco());
                    PrincipalController.INSTANCE.closeSelectedTab();
                } catch (SQLException ex) {
                    MessageBox.errorMessage(ex);
                }
            }
        }
    }

}
