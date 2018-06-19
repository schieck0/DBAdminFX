package model;

import java.util.List;

public class Compare {

    private Object chave;
    private List<Object> dados;
    private int tipo;

    public Compare(int tipo) {
        this.tipo = tipo;
    }

    public Object getChave() {
        return chave;
    }

    public void setChave(Object chave) {
        this.chave = chave;
    }

    public List<Object> getDados() {
        return dados;
    }

    public void setDados(List<Object> dados) {
        this.dados = dados;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public static final int EQ = 0;
    public static final int DIF = 1;
    public static final int BLANK = 2;
    public static final int EXTRA = 3;
    
}
