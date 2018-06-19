package model;

import java.util.Objects;

public class PrimaryKey {

    private String nome;
    private Coluna coluna;

    public PrimaryKey(Coluna coluna) {
        this.coluna = coluna;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Coluna getColuna() {
        return coluna;
    }

    public void setColuna(Coluna coluna) {
        this.coluna = coluna;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.coluna.getNome());
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
        final PrimaryKey other = (PrimaryKey) obj;
        if (!Objects.equals(this.coluna.getNome(), other.coluna.getNome())) {
            return false;
        }
        return true;
    }

}
