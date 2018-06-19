package view;

import utils.DB;
import view.controller.PrincipalController;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Rodrigo
 */
public class Start extends Application {

    public static List<String> params;

    @Override
    public void start(Stage stage) throws Exception {
        params = getParameters().getRaw();

        Parent root = FXMLLoader.load(getClass().getResource("fxml/principal.fxml"));
        Scene scene = new Scene(root);
        scene.setFill(Color.BLACK);
        stage.setScene(scene);
        stage.setTitle("DBAdmin FX");
        stage.getIcons().add(new Image("/images/database-px-png.png"));
        stage.setMaximized(true);
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                DB.store();
                PrincipalController.INSTANCE.tabbedPane.getTabs().clear();
                Platform.exit();
                System.exit(0);
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
