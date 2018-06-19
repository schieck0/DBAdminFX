package model;

public enum TipoObjeto {

    TABELA("Tabela"), COLUNA("Coluna"), SEQUENCE("Sequence"), CHAVE("Chave");
    
    private String obj;

    TipoObjeto(String obj) {
        this.obj = obj;
    }

    public String getValue() {
        return this.obj;
    }
    
    public String getTipo(Integer t) {
        return values()[t].getValue();
    }
}