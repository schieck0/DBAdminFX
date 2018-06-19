package view.controller;

import model.Sequence;
import utils.MessageBox;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class SequenceValController implements Initializable {

    private Sequence sequence;
    
    @FXML
    private TextField tfVal;
    @FXML
    private Button bAlterar;
    @FXML
    private Label lMsg;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sequence = PrincipalController.INSTANCE.getSelectedObject();
        
        try {
            tfVal.setText(sequence.getValor() + "");
        } catch (SQLException ex) {
            MessageBox.errorMessage(ex);
        }
    }    

    @FXML
    private void alterar(ActionEvent event) {
        try {
            lMsg.setText("");
            sequence.setValor(Long.parseLong(tfVal.getText()));
            lMsg.setText("Alterado!");
        } catch (SQLException ex) {
            MessageBox.errorMessage(ex);
        }
    }
    
}
