package view.controller;

import model.Servidor;
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
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class BackupServerController implements Initializable {

    private Servidor server;
    private File arquivo;
    @FXML
    private TextField tfLocal;
    @FXML
    private TextField tfParams;
    @FXML
    private TextArea taLog;
    @FXML
    private Button bBackup;
    @FXML
    private Label lServ;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.server = PrincipalController.INSTANCE.getSelectedObject();
        lServ.setText(server.getNome());
    }

    @FXML
    private void openSaveDialog(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName(server.getNome().replace(" ", "_") + ".backup");
        arquivo = fc.showSaveDialog(null);

        if (arquivo != null) {
            tfLocal.setText(arquivo.getAbsolutePath());
        }
    }

    @FXML
    private void backup(ActionEvent event) {
        taLog.setText("");
        if (arquivo == null) {
            MessageBox.errorMessage("Selecine o arquivo destino do backup.");
            return;
        }

        if (DB.getPgDump() == null || !DB.getPgDump().exists()) {
            MessageBox.message("A localização do pg_dump ainda não está definida. Selecione seu local.");
            FileChooser fc = new FileChooser();
            fc.setInitialFileName("pg_dump.exe");
            DB.setPgDump(fc.showOpenDialog(null));

            if (DB.getPgDump() == null) {
                return;
            }
        }

        File pgDumpALL = new File(DB.getPgDump().getParentFile(), "pg_dumpall.exe");

        final StringProperty s = new SimpleStringProperty("");
        s.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        taLog.appendText(newValue);
                    }
                });
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    pg_dumpall.exe --host 10.20.40.35 --port 5432 --username postgres -f C:/home/teste.sql
                    String cmd = (SystemRuntime.isWindows() ? "cmd /c " : "") + pgDumpALL.getAbsolutePath();
                    cmd += " --host " + server.getEndereco();
                    cmd += " --port " + server.getPorta();
                    cmd += " --username " + server.getUser();
                    cmd += " " + tfParams.getText();
                    cmd += " -f \"" + arquivo.getAbsolutePath() + "\"";

                    s.set(cmd + "\n");

                    bBackup.setDisable(true);
                    SystemRuntime.exec(cmd, s, "PGPASSWORD=" + server.getPwd());
                    Thread.sleep(2000);
                    s.set("\nBackup Concluído!\n");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    s.set("Erro: \n" + ex.getMessage());
                } finally {
                    bBackup.setDisable(false);
                }
            }
        }).start();
    }

}
