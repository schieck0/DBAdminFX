package model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ForeignKeyEdit {

    private final StringProperty nome = new SimpleStringProperty();
    private final ObjectProperty<ColumnEdit> fkCol = new SimpleObjectProperty<>();
    private final ObjectProperty<Tabela> pkTab = new SimpleObjectProperty<>();
    private final ObjectProperty<Coluna> pkCol = new SimpleObjectProperty<>();
    private final ObjectProperty<DeleteUpdateAction> onUpdate = new SimpleObjectProperty<>();
    private final ObjectProperty<DeleteUpdateAction> onDelete = new SimpleObjectProperty<>();

    public ForeignKeyEdit(ForeignKey fk) {
        nome.set(fk.getNome());
        fkCol.set(new ColumnEdit(new Coluna(fk.getFkCol().getNome())));
        pkTab.set(fk.getPkTab());
        pkCol.set(fk.getPkCol());
        onUpdate.set(fk.getOnUpdate());
        onDelete.set(fk.getOnDelete());
    }

    public ForeignKeyEdit() {
    }

    public Tabela getPkTab() {
        return pkTab.get();
    }

    public void setPkTab(Tabela pkTabNome) {
        this.pkTab.set(pkTabNome);
    }
    
    public String getNome() {
        return nome.get();
    }

    public void setNome(String nome) {
        this.nome.set(nome);
    }

    public Coluna getPkCol() {
        return pkCol.get();
    }

    public void setPkCol(Coluna pkCol) {
        this.pkCol.set(pkCol);
    }

    public ColumnEdit getFkCol() {
        return fkCol.get();
    }

    public void setFkCol(ColumnEdit fkCol) {
        this.fkCol.set(fkCol);
    }

    public DeleteUpdateAction getOnUpdate() {
        return onUpdate.get();
    }

    public void setOnUpdate(DeleteUpdateAction onUpdate) {
        this.onUpdate.set(onUpdate);
    }

    public DeleteUpdateAction getOnDelete() {
        return onDelete.get();
    }

    public void setOnDelete(DeleteUpdateAction onDelete) {
        this.onDelete.set(onDelete);
    }

    public ObjectProperty<Tabela> pkTabProperty() {
        return pkTab;
    }

    public ObjectProperty<Coluna> pkColProperty() {
        return pkCol;
    }

    public StringProperty nomeProperty() {
        return nome;
    }
    
    public ObjectProperty<ColumnEdit> fkColProperty() {
        return fkCol;
    }

    public ObjectProperty<DeleteUpdateAction> onUpdateProperty() {
        return onUpdate;
    }

    public ObjectProperty<DeleteUpdateAction> onDeleteProperty() {
        return onDelete;
    }
}
