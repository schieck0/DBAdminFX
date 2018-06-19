package model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ColumnEdit {

    private final StringProperty nome = new SimpleStringProperty();
    private String nomeAnt;
    private final StringProperty tipo = new SimpleStringProperty();
    private final StringProperty tamanho = new SimpleStringProperty();
    private final StringProperty defaultVal = new SimpleStringProperty();
    private final BooleanProperty notNull = new SimpleBooleanProperty();
    private Integer tipoSql;
    private final BooleanProperty pk = new SimpleBooleanProperty();
    private boolean fk;

    public ColumnEdit(Coluna col) {
        nome.set(col.getNome());
        nomeAnt = col.getNome();
        tipo.set(col.getTipo());
        tamanho.set(col.getTamanho() + "");
        defaultVal.set(col.getDefaultVal());
        notNull.set(col.isNotNull());
        pk.set(col.isPk());
    }

    public ColumnEdit() {
    }

    public String getNome() {
        return nome.get();
    }

    public void setNome(String nome) {
        this.nome.set(nome);
    }

    public String getNomeAnt() {
        return nomeAnt;
    }

    public void setNomeAnt(String nomeAnt) {
        this.nomeAnt = nomeAnt;
    }

    public String getTipo() {
        return tipo.get();
    }

    public void setTipo(String tipo) {
        this.tipo.set(tipo);
    }

    public String getTamanho() {
        return tamanho.get();
    }

    public void setTamanho(String tamanho) {
        this.tamanho.set(tamanho);
    }
    
    public String getDefaultVal() {
        return defaultVal.get();
    }

    public void setDefaultVal(String valor) {
        this.defaultVal.set(valor);
    }

    public boolean getNotNull() {
        return notNull.get();
    }

    public void setNotNull(boolean nn) {
        this.notNull.set(nn);
    }

    public Integer getTipoSql() {
        return tipoSql;
    }

    public void setTipoSql(Integer tipoSql) {
        this.tipoSql = tipoSql;
    }

    public boolean isPk() {
        return pk.get();
    }

    public void setPk(boolean pk) {
        this.pk.set(pk);
    }

    public boolean isFk() {
        return fk;
    }

    public void setFk(boolean fk) {
        this.fk = fk;
    }

    public StringProperty nomeProperty() {
        return nome;
    }

    public StringProperty tipoProperty() {
        return tipo;
    }

    public StringProperty tamanhoProperty() {
        return tamanho;
    }
    
    public StringProperty defaultValProperty() {
        return defaultVal;
    }

    public BooleanProperty notNullProperty() {
        return notNull;
    }

    public BooleanProperty pkProperty() {
        return pk;
    }

    @Override
    public String toString() {
        return getNome();
    }

}
