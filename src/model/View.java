package model;

public class View extends Tabela {

    private static final long serialVersionUID = 5L;

    private String definition;
    
    public View(Schema schema, String nome) {
        super(schema, nome);
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

}
