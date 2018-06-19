package view.controller;

import model.Banco;
import utils.DB;
import utils.MessageBox;
import utils.SystemRuntime;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class RestoreController implements Initializable {

    private Banco banco;
    private File arquivo;
    @FXML
    private Label lBanco;
    @FXML
    private TextField tfArquivo;
    @FXML
    private Button bRestore;
    @FXML
    private TextArea taLog;
    @FXML
    private CheckBox ckData;
    @FXML
    private CheckBox ckStruct;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.banco = PrincipalController.INSTANCE.getSelectedObject();
        lBanco.setText(banco.getServidor().getNome() + "." + banco.getNome());

        taLog.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                taLog.setScrollTop(Double.MAX_VALUE);
            }
        });
    }

    @FXML
    private void selectFile(ActionEvent event) {
        FileChooser fc = new FileChooser();
        arquivo = fc.showOpenDialog(null);

        if (arquivo != null) {
            tfArquivo.setText(arquivo.getAbsolutePath());
        }
    }

    @FXML
    private void restore(ActionEvent event) {
        taLog.setText("");
        if (arquivo == null) {
            MessageBox.errorMessage("Selecine o arquivo origem do backup.");
            return;
        }

        if (DB.getPgDump() == null) {
            MessageBox.message("A localização do pg_dump ainda não está definida. Selecione seu local.");
            FileChooser fc = new FileChooser();
            fc.setInitialFileName("pg_dump.exe");
            DB.setPgDump(fc.showOpenDialog(null));

            if (DB.getPgDump() == null) {
                return;
            }
        }

        File pgRestore = new File(DB.getPgDump().getParentFile(), "pg_restore.exe");

        StringProperty sp = new SimpleStringProperty("");
        sp.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        taLog.appendText(newValue + "\n");
                    }
                });
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bRestore.setDisable(true);
                    String cmd = pgRestore.getAbsolutePath();
                    cmd += " --host " + banco.getServidor().getEndereco();
                    cmd += " --port " + banco.getServidor().getPorta();
//                    cmd += " --no-password --username " + banco.getServidor().getUser();
                    cmd += " --username " + banco.getServidor().getUser();
                    cmd += " --dbname " + banco.getNome();

                    if (ckStruct.isSelected()) {
                        cmd += " --schema-only";
                    } else if (ckData.isSelected()) {
                        cmd += " --data-only";
                    }

                    cmd += " --verbose \"" + arquivo.getAbsolutePath() + "\"";

                    sp.set(cmd + "\n");
                    SystemRuntime.exec(cmd, sp, "PGPASSWORD=" + banco.getServidor().getPwd());
                    Thread.sleep(2000);
                    sp.set("\nRestore Concluído!\n");
                    banco.loadSchemas();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    sp.set("Erro: \n" + ex.getMessage());
                } finally {
                    bRestore.setDisable(false);
                }
            }
        }).start();
    }

    @FXML
    private void ckDataAct(ActionEvent event) {
        if (ckData.isSelected()) {
            ckStruct.setSelected(false);
        }
    }

    @FXML
    private void ckStructAct(ActionEvent event) {
        if (ckStruct.isSelected()) {
            ckData.setSelected(false);
        }
    }

}
