package view.controller;

import model.Banco;
import model.Schema;
import model.SqlParam;
import model.Tabela;
import utils.MessageBox;
import utils.SQLFormatter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;
import schieck.utils.FileUtils;

/**
 * FXML Controller class
 *
 * @author Rodrigo
 */
public class SqlController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private Banco banco;
    private Schema schema;
    private File arquivo;
    private Tab thisTab;

    @FXML
    private Button bOpen;
    @FXML
    private Button bSave;
    @FXML
    private Button bExec;
    @FXML
    private Button bFormat;
    @FXML
    private TableView<Object[]> table;
    @FXML
    private CodeArea taSql;
    @FXML
    private Label lTime;
    @FXML
    private AnchorPane anchor;
    @FXML
    private Label lSelect;
    @FXML
    private TableColumn<SqlParam, String> colParamName;
    @FXML
    private TableColumn<SqlParam, String> colParamVal;
//    @FXML
//    private TableColumn<SqlParam, SQLType> colParamType;
    @FXML
    private TableView<SqlParam> tParams;
    @FXML
    private SplitPane splitSqlParam;

    private ExecutorService executor;
    private ObservableList<SqlParam> params = FXCollections.observableArrayList();
    @FXML
    private Button bComment;
    @FXML
    private Button bExport;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        params.clear();
        splitSqlParam.setDividerPosition(0, 1);

        params.addListener(new ListChangeListener<SqlParam>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends SqlParam> c) {
                if (params.isEmpty()) {
                    splitSqlParam.setDividerPosition(0, 1);
                } else {
                    splitSqlParam.setDividerPosition(0, 0.7);
                }
            }
        });

        executor = Executors.newSingleThreadExecutor();
        bOpen.setTooltip(new Tooltip("Abrir"));
        bSave.setTooltip(new Tooltip("Salvar SQL"));
        bExec.setTooltip(new Tooltip("Executar"));
        bFormat.setTooltip(new Tooltip("Formatar"));
        bComment.setTooltip(new Tooltip("Comentar"));
        bExport.setTooltip(new Tooltip("Exportar CSV"));

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

        taSql.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            int charSel = taSql.getSelection().getLength();
            String status = "Ln: " + (taSql.getCurrentParagraph() + 1) + "   Col: " + (taSql.getCaretColumn() + 1)
                    + "  Sel: " + charSel;

//            if (charSel > 0) {
//                int selectedLines = 0;
//                int pStart = -1;
////                int pEnd = taSql.getParagraphs().size();
//                int pEnd = -1;
//                for (int i = 0; i < taSql.getParagraphs().size(); i++) {
//                    if (taSql.getParagraphSelection(i).getLength() > 0) {
//                        selectedLines++;
//                        if (pStart == -1) {
//                            pStart = i + 1;
//                        } else {
//                            pEnd = i + 1;
//                        }
//                    }
//                }
//                if (selectedLines > 0) {
//                    status += (charSel > 0 ? ("|" + (pEnd - pStart)) : "");
//                }
//            }
            lSelect.setText(status);

        });

        Object sel = PrincipalController.INSTANCE.getSelectedObject();

        if (sel instanceof Banco) {
            banco = (Banco) sel;
        } else if (sel instanceof Schema) {
            schema = (Schema) sel;
            banco = schema.getBanco();
        } else if (sel instanceof Tabela) {
            schema = (Schema) ((Tabela) sel).getSchema();
            banco = schema.getBanco();
        }

        colParamName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colParamVal.setCellValueFactory(new PropertyValueFactory<>("value"));
        colParamVal.setCellFactory(TextFieldTableCell.forTableColumn());

//        colParamType.setCellValueFactory(new PropertyValueFactory<>("type"));
//        colParamType.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(SQLType.TYPES)));
        tParams.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colParamName.setMaxWidth(1f * Integer.MAX_VALUE * 50); // 50% width
        colParamVal.setMaxWidth(1f * Integer.MAX_VALUE * 50); // 50% width
//        colParamType.setMaxWidth(1f * Integer.MAX_VALUE * 33); // 33% width
        tParams.setItems(params);

        Platform.runLater(() -> {
            anchor.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F5), () -> {
                executar();
            });
        });
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

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = SQLFormatter.PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        List<SqlParam> parametros = new ArrayList<>();
        while (matcher.find()) {
            String styleClass
                    = matcher.group("KEYWORD") != null ? "keyword"
                    : matcher.group("PAREN") != null ? "paren"
                    : matcher.group("PARENC") != null ? "parenc"
                    : matcher.group("BRACKET") != null ? "bracket"
                    : matcher.group("SEMICOLON") != null ? "semicolon"
                    : matcher.group("STRING") != null ? "string"
                    : matcher.group("PARAM") != null ? "param"
                    : matcher.group("COMMENT") != null ? "comment"
                    : null;
            /* never happens */ assert styleClass != null;
            if (styleClass == "param") {
                parametros.add(new SqlParam(matcher.group("PARAM").replace(":", ""), null));
            }

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }

        for (SqlParam parametro : parametros) {
            if (!params.contains(parametro)) {
                params.add(parametro);
            }
        }

        params.retainAll(parametros);

        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        taSql.setStyleSpans(0, highlighting);
    }

    private void executar() {
        bExec.setDisable(true);
        table.getColumns().clear();
        lTime.setText("Aguarde...");

        thisTab = PrincipalController.INSTANCE.tabbedPane.getSelectionModel().getSelectedItem();
        Image i = new Image("/images/balls24.gif");
        ImageView loader = new ImageView(i);
        loader.setFitHeight(24);
        loader.setFitWidth(24);
        thisTab.setGraphic(loader);

        String sql = taSql.getSelectedText().isEmpty() ? taSql.getText().trim() : taSql.getSelectedText();

        Task<RetornoExecucao> task = new Task<RetornoExecucao>() {
            @Override
            public RetornoExecucao call() throws Exception {

                RetornoExecucao retorno = new RetornoExecucao();
                try (Connection conn = schema != null ? schema.connect() : banco.connect()) {
                    conn.setAutoCommit(true);
                    final Statement stm = conn.createStatement();
                    long ini = System.currentTimeMillis();
                    retorno.resultSet = stm.execute(replaceParameters(sql));
                    retorno.duration = System.currentTimeMillis() - ini;

                    if (retorno.resultSet) {
                        ResultSet rs = stm.getResultSet();
                        for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                            final int x = i;
                            TableColumn<Object[], String> col = new TableColumn<>(rs.getMetaData().getColumnName(i + 1));
                            col.setEditable(true);
                            col.setCellFactory(TextFieldTableCell.forTableColumn());
                            col.setCellValueFactory((TableColumn.CellDataFeatures<Object[], String> p) -> new ObservableValueBase<String>() {
                                @Override
                                public String getValue() {
                                    return p.getValue()[x] != null ? p.getValue()[x].toString() : "";
                                }
                            });
                            retorno.cols.add(col);
                        }

                        Object[] linha;
                        while (rs.next()) {
                            linha = new Object[retorno.cols.size()];
                            for (int i = 1; i <= retorno.cols.size(); i++) {
                                linha[i - 1] = rs.getObject(i);
                            }

                            retorno.dados.add(linha);
                        }
                    } else {
                        int regs = stm.getUpdateCount();
                        TableColumn<Object[], String> col = new TableColumn<>("Resultado");
                        col.setEditable(true);
                        col.setCellFactory(TextFieldTableCell.forTableColumn());
                        col.setCellValueFactory((TableColumn.CellDataFeatures<Object[], String> p) -> new ObservableValueBase<String>() {
                            @Override
                            public String getValue() {
                                return p.getValue()[0].toString();
                            }
                        });
                        retorno.cols.add(col);
                        retorno.dados.add(new Object[]{regs + " registros afetados."});
                    }
                } catch (SQLException ex) {
                    TableColumn<Object[], String> col = new TableColumn<>("ERRO");
                    col.setCellValueFactory((TableColumn.CellDataFeatures<Object[], String> p) -> new ObservableValueBase<String>() {
                        @Override
                        public String getValue() {
                            return p.getValue()[0].toString();
                        }
                    });
                    retorno.cols.add(col);
                    retorno.dados.add(new Object[]{ex.getMessage()});
                    retorno.error = true;
                }

                return retorno;
            }
        };

        task.setOnSucceeded(event -> {
            try {
                RetornoExecucao retorno = task.get();

                table.getColumns().addAll(retorno.cols);
                table.setItems(FXCollections.observableArrayList(retorno.dados));
                if (retorno.resultSet) {
                    lTime.setText(retorno.dados.size() + " linhas em " + retorno.duration + " ms");
                } else {
                    lTime.setText(retorno.duration + " ms");
                    table.getColumns().get(0).setPrefWidth(table.getWidth() - 2);
                }

                if (retorno.error) {
                    Matcher matcher = Pattern.compile("\\d+$").matcher(retorno.dados.get(0)[0].toString());
                    if (matcher.find()) {
                        int pos = Integer.parseInt(matcher.group());
                        taSql.selectRange(pos - 1, pos - 1);
                        taSql.requestFocus();
                    }
                }
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
            bExec.setDisable(false);
            thisTab.setGraphic(null);
        });

        new Thread(task).start();
    }

    private String replaceParameters(String sql) {
        for (SqlParam param : params) {
            if (param.getValue() != null) {
                sql = sql.replaceAll(":" + param.getName() + "\\b", param.getValue());
            }
        }
        return sql;
    }

    private void formatar() {
        taSql.replaceText(SQLFormatter.format(taSql.getText()).trim());
    }

    @FXML
    private void open(ActionEvent event) {
        FileChooser fc = new FileChooser();
        arquivo = fc.showOpenDialog(null);

        if (arquivo != null) {
            try {
                taSql.replaceText(FileUtils.toText(arquivo));
            } catch (IOException ex) {
                MessageBox.errorMessage(ex);
            }
        }
    }

    @FXML
    private void save(ActionEvent event) {
        if (arquivo == null) {
            FileChooser fc = new FileChooser();
            arquivo = fc.showSaveDialog(null);
        }

        if (arquivo != null) {
            try {
                FileUtils.saveToFile(taSql.getText(), arquivo);
            } catch (IOException ex) {
                MessageBox.errorMessage(ex);
            }
        }
    }

    @FXML
    private void exec(ActionEvent event) {
        executar();
    }

    @FXML
    private void format(ActionEvent event) {
        formatar();
    }

    @FXML
    private void taSqlKeyPress(KeyEvent event) {
//        if (event.getCode().equals(KeyCode.F5)) {
//            executar();
//        }
    }

    @FXML
    private void comment(ActionEvent event) {
        int start = taSql.getSelection().getStart();
        int end = taSql.getSelection().getEnd();

//        int caret = taSql.getCaretPosition();
        String text = taSql.getText();

        Set<Integer> linesNum = new HashSet<>();

        int line = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((i >= start && i <= end) || (start == end && start == text.length() && i == start - 1)) {
                linesNum.add(line);
            }
            if (c == '\n') {
                line++;
            }
        }
        String[] lines = taSql.getText().split("\n");

        text = "";
        for (int i = 0; i < lines.length; i++) {
            text += (linesNum.contains(i) ? (lines[i].startsWith("--") ? lines[i].replaceFirst("--", "") : "--" + lines[i]) : lines[i]) + (i + 1 < lines.length ? "\n" : "");
        }

        taSql.replaceText(text);
//        taSql.positionCaret(caret);
    }

    @FXML
    private void export(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("dados.csv");
        File file = fc.showSaveDialog(null);

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
            for (TableColumn col : table.getColumns()) {
                writer.write(col.getText() + ";");
            }
            writer.write("\n");
            writer.flush();

            for (Object[] row : table.getItems()) {
                for (Object col : row) {
                    writer.write("'" + col + "';");
                }
                writer.write("\n");
                writer.flush();
            }

            MessageBox.message("Exportação Concluída!");
        } catch (IOException ex) {
            ex.printStackTrace();
            MessageBox.errorMessage(ex);
        }
    }
}

class RetornoExecucao {

    List<Object[]> dados = new ArrayList<>();
    List<TableColumn<Object[], String>> cols = new ArrayList<>();
    long duration;
    boolean resultSet;
    boolean error;
}
