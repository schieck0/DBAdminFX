package model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class ObjetoBkp {

    private BooleanProperty struct;
    private BooleanProperty dados;
    private Tabela tabela;
    private Schema schema;
    private String descricao;

    public ObjetoBkp(String descricao) {
        this.descricao = descricao;
        init();
    }

    public ObjetoBkp(Schema schema) {
        this.schema = schema;
        descricao = schema.getNome();
        init();
    }

    public ObjetoBkp(Tabela tabela) {
        this.tabela = tabela;
//        descricao = tabela.getFullName();
        descricao = tabela.getNome();
        init();
    }

    private void init() {
        struct = new SimpleBooleanProperty(true);
        dados = new SimpleBooleanProperty(true);
    }

    public boolean isStruct() {
        return struct.get();
    }

    public void setStruct(boolean struct) {
        this.struct.set(struct);
    }

    public Tabela getTabela() {
        return tabela;
    }

    public void setTabela(Tabela tabela) {
        this.tabela = tabela;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isDados() {
        return dados.get();
    }

    public void setDados(boolean dados) {
        this.dados.set(dados);
    }

    public BooleanProperty dadosProperty() {
        return dados;
    }

    public BooleanProperty structProperty() {
        return struct;
    }

    public boolean isTable() {
        return tabela != null;
    }

    public boolean isSchema() {
        return schema != null;
    }
}
