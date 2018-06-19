package model;

import java.util.Objects;

public class ForeignKey {
 
    private String nome;
    private Tabela pkTab;
    private Tabela fkTab;
    private Coluna pkCol;
    private Coluna fkCol;
    private DeleteUpdateAction onUpdate;
    private DeleteUpdateAction onDelete;

    private Tabela tabela;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Tabela getPkTab() {
        return pkTab;
    }

    public void setPkTab(Tabela pkTab) {
        this.pkTab = pkTab;
    }

    public Tabela getFkTab() {
        return fkTab;
    }

    public void setFkTab(Tabela fkTab) {
        this.fkTab = fkTab;
    }

    public Coluna getPkCol() {
        return pkCol;
    }

    public void setPkCol(Coluna pkCol) {
        this.pkCol = pkCol;
    }

    public Coluna getFkCol() {
        return fkCol;
    }

    public void setFkCol(Coluna fkCol) {
        this.fkCol = fkCol;
    }

    public DeleteUpdateAction getOnUpdate() {
        if (onUpdate == null) {
            return DeleteUpdateAction.NO_ACTION;
        }
        return onUpdate;
    }

    public void setOnUpdate(DeleteUpdateAction onUpdate) {
        this.onUpdate = onUpdate;
    }

    public DeleteUpdateAction getOnDelete() {
        if (onDelete == null) {
            return DeleteUpdateAction.NO_ACTION;
        }
        return onDelete;
    }

    public void setOnDelete(DeleteUpdateAction onDelete) {
        this.onDelete = onDelete;
    }

    public Tabela getTabela() {
        return tabela;
    }

    public void setTabela(Tabela tabela) {
        this.tabela = tabela;
    }

    @Override
    public String toString() {
        return nome;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.nome);
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
        final ForeignKey other = (ForeignKey) obj;
        if (!Objects.equals(this.nome, other.getNome())) {
            return false;
        }
        if (!Objects.equals(this.pkTab.getNome(), other.pkTab.getNome())) {
            return false;
        }
        if (!Objects.equals(this.fkTab.getNome(), other.fkTab.getNome())) {
            return false;
        }
        if (!Objects.equals(this.pkCol.getNome(), other.pkCol.getNome())) {
            return false;
        }
        if (!Objects.equals(this.fkCol.getNome(), other.fkCol.getNome())) {
            return false;
        }
        return true;
    }

}
