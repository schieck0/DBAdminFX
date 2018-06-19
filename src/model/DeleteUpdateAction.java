package model;

public enum DeleteUpdateAction {
    CASCADE("CASCADE"),
    RESTRICT("RESTRICT"),
    SET_NULL("SET NULL"),
    NO_ACTION("NO ACTION"),
    SET_DEFAULT("SET DEFAULT");

    private String valor;

    private DeleteUpdateAction(String valor) {
        this.valor = valor;
    }

    public static DeleteUpdateAction fromText(String s) {
        if (s.equals(NO_ACTION.getText())) {
            return NO_ACTION;
        }
        if (s.equals(CASCADE.getText())) {
            return CASCADE;
        }
        if (s.equals(SET_NULL.getText())) {
            return SET_NULL;
        }
        if (s.equals(SET_DEFAULT.getText())) {
            return SET_DEFAULT;
        }
        if (s.equals(RESTRICT.getText())) {
            return RESTRICT;
        } else {
            return null;
        }
    }

    public String getText() {
        return valor;
    }

    @Override
    public String toString() {
        return valor;
    }

}
