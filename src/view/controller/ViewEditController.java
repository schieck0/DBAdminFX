package view.controller;

import model.Schema;
import model.View;
import utils.MessageBox;
import utils.SQLFormatter;
import utils.SqlLog;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class ViewEditController implements Initializable {

    private View view;
    private Schema schema;

    @FXML
    private AnchorPane anchor;
    @FXML
    private TextField tfNome;
    @FXML
    private Button bOK;
    @FXML
    private ComboBox<String> cbOwner;
    @FXML
    private CodeArea taSql;

    private ExecutorService executor;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        taSql.setParagraphGraphicFactory(LineNumberFactory.get(taSql));

        taSql.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .successionEnds(Duration.ofMillis(500))
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(taSql.richChanges())
                .filterMap(t -> {
                    if (t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        t.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                })
                .subscribe(this::applyHighlighting);

        taSql.getStylesheets().add(this.getClass().getResource("sql-keywords.css").toExternalForm());

        executor = Executors.newSingleThreadExecutor();

        Object o = PrincipalController.INSTANCE.getSelectedObject();
        try {
            if (o instanceof View) {
                view = (View) o;
                view.loadCols();
                schema = view.getSchema();
                tfNome.setText(view.getNome());
                List<String> roles = schema.getBanco().getServidor().getLoginRolesNames();
                cbOwner.setItems(FXCollections.observableArrayList(roles));
                cbOwner.getSelectionModel().select(view.getOwner());
                taSql.replaceText(view.getDefinition());
            } else if (o instanceof Schema) {
                schema = (Schema) o;
                List<String> roles = schema.getBanco().getServidor().getLoginRolesNames();
                cbOwner.setItems(FXCollections.observableArrayList(roles));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = taSql.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return computeHighlighting(text);
            }
        };
        executor.execute(task);
        return task;
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = SQLFormatter.PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass
                    = matcher.group("KEYWORD") != null ? "keyword"
                    : matcher.group("PAREN") != null ? "paren"
                    : matcher.group("PARENC") != null ? "parenc"
                    : matcher.group("BRACKET") != null ? "bracket"
                    : matcher.group("SEMICOLON") != null ? "semicolon"
                    : matcher.group("STRING") != null ? "string"
                    : matcher.group("COMMENT") != null ? "comment"
                    : null;
            /* never happens */ assert styleClass != null;

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        taSql.setStyleSpans(0, highlighting);
    }

    @FXML
    private void processar(ActionEvent event) {
        try (Connection conn = schema.connect()) {
            bOK.setDisable(true);
            bOK.setText("Processando...");

            String ddl = "";
            if (view != null) {
                if (!tfNome.getText().equals(view.getNome())) {
                    ddl += "ALTER VIEW " + view.getNome() + " RENAME TO " + tfNome.getText() + ";";
                }

                if (!cbOwner.getSelectionModel().getSelectedItem().equals(view.getOwner())) {
                    ddl += "\n\nALTER TABLE " + tfNome.getText() + " OWNER TO " + cbOwner.getSelectionModel().getSelectedItem() + ";";
                }

                if (!taSql.getText().trim().equals(view.getDefinition())) {
                    ddl += "\n\nCREATE OR REPLACE VIEW " + tfNome.getText() + " AS \n" + taSql.getText().trim() + ";";
                }
            } else {
                ddl += "CREATE OR REPLACE VIEW " + tfNome.getText() + " AS \n" + taSql.getText().trim() + ";"
                        + "\n\nALTER TABLE " + tfNome.getText() + " OWNER TO " + cbOwner.getSelectionModel().getSelectedItem() + ";";
            }

            if (ddl.isEmpty()) {
                MessageBox.warning("Nenhuma alteração a ser processada.");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Clique no botão abaixo para ver a sql gerada.", ButtonType.CANCEL, ButtonType.OK);

            Label label = new Label("Comando DDL:");
            TextArea textArea = new TextArea(ddl.trim());
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);
            alert.setHeaderText("Aplicar alterações?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                conn.createStatement().executeUpdate(ddl);
                SqlLog.append(ddl);
                PrincipalController.INSTANCE.refresh(schema);
                PrincipalController.INSTANCE.closeSelectedTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.errorMessage(ex);
        } finally {
            bOK.setText("OK");
            bOK.setDisable(false);
        }
    }
}
