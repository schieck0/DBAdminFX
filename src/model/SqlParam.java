package model;

import utils.SQLType;
import java.util.Objects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SqlParam {

    private StringProperty name = new SimpleStringProperty();
    private StringProperty value = new SimpleStringProperty();
    private final ObjectProperty<SQLType> type = new SimpleObjectProperty<>();

    public SqlParam() {
    }

    public SqlParam(String name, String value) {
        this.name.set(name);
        this.value.set(value);
    }

    public SqlParam(String name, String value, SQLType type) {
        this.name.set(name);
        this.value.set(value);
        this.type.set(type);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public SQLType getType() {
        return type.get();
    }

    public void setType(SQLType type) {
        this.type.set(type);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty valueProperty() {
        return value;
    }

    public ObjectProperty typeProperty() {
        return type;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.name.get());
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
        final SqlParam other = (SqlParam) obj;
        if (!Objects.equals(this.name.get(), other.name.get())) {
            return false;
        }
        return true;
    }

}
