package model;

import utils.SQLType;
import java.util.Objects;

public class Coluna {

    private String nome;
    private String nomeAnt;
    private String tipo;
    private String defaultVal;
    private int tamanho;
    private int nullable = 1;
    private Tabela tabela;
    private Integer tipoSql;
    private boolean pk;
    private boolean fk;

    public Coluna(String nome) {
        this.nome = nome;
        this.nomeAnt = nome;
    }

    public Coluna(String nome, String tipo) {
        this.nome = nome;
        this.nomeAnt = nome;
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNomeAnt() {
        return nomeAnt;
    }

    public void setNomeAnt(String nomeAnt) {
        this.nomeAnt = nomeAnt;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void setTamanho(int tamanho) {
        this.tamanho = tamanho;
    }

    public int getNullable() {
        return nullable;
    }

    public void setNullable(int nullable) {
        this.nullable = nullable;
    }

    public boolean isNotNull() {
        return nullable == 0;
    }

    public Tabela getTabela() {
        return tabela;
    }

    public void setTabela(Tabela tabela) {
        this.tabela = tabela;
    }

    public Integer getTipoSql() {
        return tipoSql;
    }

    public void setTipoSql(int tipoSql) {
        this.tipoSql = tipoSql;
    }

    public Class getSqlClass() {
        return SQLType.toClass(tipoSql);
    }

    public String getDefaultVal() {
        return defaultVal;
    }

    public void setDefaultVal(String defaultVal) {
        this.defaultVal = defaultVal;
    }

    @Override
    public String toString() {
        return nome;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.nome);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Coluna other = (Coluna) obj;
        if (!Objects.equals(this.nome, other.nome)) {
            return false;
        }
        return true;
    }

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }

    public boolean isFk() {
        return fk;
    }

    public void setFk(boolean fk) {
        this.fk = fk;
    }

}
