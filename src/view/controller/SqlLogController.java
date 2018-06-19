package view.controller;

import utils.SqlLog;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class SqlLogController implements Initializable {

    @FXML
    private TextArea taSql;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        taSql.setText(SqlLog.getLog());

        SqlLog.addChangeListner(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                taSql.setText(newValue);
            }
        });
    }

}
