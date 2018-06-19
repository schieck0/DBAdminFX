package model;

import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Alteracao {

    private boolean alter;
    private TipoObjeto tipo;
    private Operacao operacao;
    private BooleanProperty gerar;
    private Object objeto;
    private String SQL;

    public Alteracao(Operacao operacao, Object objeto, TipoObjeto tipo, boolean alter, boolean gerar, String sql) {
        this.alter = alter;
        this.tipo = tipo;
        this.operacao = operacao;
        this.gerar = new SimpleBooleanProperty(gerar);
        this.objeto = objeto;
        this.SQL = sql;
    }

    public boolean isAlter() {
        return alter;
    }

    public void setAlter(boolean alter) {
        this.alter = alter;
    }

    public TipoObjeto getTipo() {
        return tipo;
    }

    public void setTipo(TipoObjeto tipo) {
        this.tipo = tipo;
    }

    public Operacao getOperacao() {
        return operacao;
    }

    public void setOperacao(Operacao operacao) {
        this.operacao = operacao;
    }

    public boolean isGerar() {
        return gerar.get();
    }

    public void setGerar(boolean gerar) {
        this.gerar.set(gerar);
    }
    
    public BooleanProperty gerarProperty() {
        return gerar;
    }

    public Object getObjeto() {
        return objeto;
    }

    public void setObjeto(Object objeto) {
        this.objeto = objeto;
    }

    public String getSQL() {
        return SQL;
    }

    public void setSQL(String SQL) {
        this.SQL = SQL;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.objeto);
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
        final Alteracao other = (Alteracao) obj;
        if (!Objects.equals(this.objeto, other.objeto)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return objeto.toString();
    }
    
    
}
