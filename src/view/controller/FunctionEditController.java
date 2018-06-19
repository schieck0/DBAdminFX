package view.controller;

import model.Function;
import model.Schema;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
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
public class FunctionEditController implements Initializable {

    private Function function;
    private Schema schema;
    private ExecutorService executor;
    private ObservableList<FuncParam> params = FXCollections.observableArrayList();

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
    @FXML
    private ComboBox<String> cbRet;
    @FXML
    private ComboBox<String> cbLing;
    @FXML
    private Button bAdd;
    @FXML
    private Button bRemove;
    @FXML
    private TableView<FuncParam> tParams;
    @FXML
    private TableColumn<FuncParam, String> colParamName;
    @FXML
    private TableColumn<FuncParam, String> colParamTip;

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

        tParams.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colParamName.setMaxWidth(1f * Integer.MAX_VALUE * 50); // 50% width
        colParamTip.setMaxWidth(1f * Integer.MAX_VALUE * 50); // 50% width

        Object o = PrincipalController.INSTANCE.getSelectedObject();
        try {
            if (o instanceof Function) {
                function = (Function) o;
                function.load();
                schema = function.getSchema();
            } else if (o instanceof Schema) {
                schema = (Schema) o;
            }

            ObservableList<String> types = FXCollections.observableList(schema.getBanco().getServidor().getTypes());

            colParamName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colParamName.setCellFactory(TextFieldTableCell.forTableColumn());

            colParamTip.setCellValueFactory(new PropertyValueFactory<>("type"));
            colParamTip.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(types)));

            cbRet.setItems(types);
            cbOwner.setItems(FXCollections.observableList(schema.getBanco().getServidor().getLoginRolesNames()));
            cbLing.setItems(FXCollections.observableList(schema.getBanco().getServidor().getLanguages()));

            List<String> roles = schema.getBanco().getServidor().getLoginRolesNames();
            cbOwner.setItems(FXCollections.observableList(roles));

            if (function != null) {
                tfNome.setText(function.getNome());
                cbOwner.getSelectionModel().select(function.getOwner());

                cbLing.getSelectionModel().select(function.getLinguagem());

                cbRet.getSelectionModel().select(function.getRetorno());

                taSql.replaceText(function.getCode());

                for (String key : function.getParams().keySet()) {
                    params.add(new FuncParam(key, function.getParams().get(key)));
                }
            }
            tParams.setItems(params);
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
            if (function != null) {
                if (!tfNome.getText().equals(function.getNome())) {
                    String p = "";
                    int i = 0;
                    for (String t : function.getParams().values()) {
                        p += t;
                        i++;
                        if (i < function.getParams().values().size()) {
                            p += ", ";
                        }
                    }
                    ddl += "ALTER FUNCTION " + function.getNome() + "(" + p + ") RENAME TO " + tfNome.getText() + ";";
                }

                if (!cbOwner.getSelectionModel().getSelectedItem().equals(function.getOwner())) {
                    String p = "";
                    int i = 0;
                    for (String t : function.getParams().values()) {
                        p += t;
                        i++;
                        if (i < function.getParams().values().size()) {
                            p += ", ";
                        }
                    }
                    ddl += "\n\nALTER FUNCTION " + tfNome.getText() + "(" + p + ") OWNER TO " + cbOwner.getSelectionModel().getSelectedItem() + ";";
                }

                boolean paramsChanged = params.size() != function.getParams().size();
                if (!paramsChanged) {
                    for (FuncParam fp : params) {
                        if (function.getParams().containsKey(fp.getName())) {
                            if (!fp.getType().equals(function.getParams().get(fp.getName()))) {
                                paramsChanged = true;
                                break;
                            }
                        } else {
                            paramsChanged = true;
                            break;
                        }
                    }
                }

                if (!cbLing.getSelectionModel().getSelectedItem().equals(function.getLinguagem())
                        || !cbRet.getSelectionModel().getSelectedItem().equals(function.getRetorno())
                        || !taSql.getText().trim().equals(function.getCode().trim().replace("\r\n", "\n"))
                        || paramsChanged) {
                    String p = "";
                    int i = 0;
                    for (FuncParam fp : params) {
                        p += fp.getName() + " " + fp.getType();
                        i++;
                        if (i < params.size()) {
                            p += ", ";
                        }
                    }

                    ddl += "CREATE OR REPLACE FUNCTION " + tfNome.getText() + "(" + p + ") RETURNS " + cbRet.getSelectionModel().getSelectedItem()
                            + " AS \n$$"
                            + taSql.getText().trim()
                            + "\n$$"
                            + "\n\nLANGUAGE " + cbLing.getSelectionModel().getSelectedItem() + " STABLE NOT LEAKPROOF;";
                }

            } else {
                String p = "";
                int i = 0;
                for (FuncParam fp : params) {
                    p += fp.getName() + " " + fp.getType();
                    i++;
                    if (i < params.size()) {
                        p += ", ";
                    }
                }
                ddl += "CREATE OR REPLACE FUNCTION " + tfNome.getText() + "(" + p + ") RETURNS " + cbRet.getSelectionModel().getSelectedItem()
                        + " AS \n$$\n"
                        + taSql.getText().trim()
                        + "\n$$"
                        + "\n\nLANGUAGE " + cbLing.getSelectionModel().getSelectedItem() + " STABLE NOT LEAKPROOF;";
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

    @FXML
    private void addParam(ActionEvent event) {
        tParams.getItems().add(new FuncParam("", ""));
    }

    @FXML
    private void removeParam(ActionEvent event) {
        FuncParam p = tParams.getSelectionModel().getSelectedItem();
        tParams.getItems().remove(p);
    }

    public class FuncParam {

        StringProperty name = new SimpleStringProperty();
        StringProperty type = new SimpleStringProperty();

        public FuncParam(String name, String type) {
            this.name.set(name);
            this.type.set(type);
        }

        public String getName() {
            return name.get();
        }

        public String getType() {
            return type.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public void setType(String type) {
            this.type.set(type);
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty typeProperty() {
            return type;
        }
    }
}
