package model;

public enum Operacao {

    CRIAR("Criar"), REMOVER("Remover"), ALTERAR("Alterar"), INFO("Info");

    private String op;

    Operacao(String op) {
        this.op = op;
    }

    public String getValue() {
        return this.op;
    }

    public String getOp(Integer op) {
        return values()[op].getValue();
    }
}
