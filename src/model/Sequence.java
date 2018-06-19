package model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Sequence {

    public static final Icon ICONE = new ImageIcon(Servidor.class.getResource("/images/resultset-next-icon.png"));

    private String nome;
    private long valor;
    private Schema schema;

    public Sequence(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public String getFullName() {
        return getSchema().getNome() + "." + getNome();
    }

    public long getValor() throws SQLException {
        try (Connection conn = schema.connect()) {
            ResultSet rs = conn.createStatement().executeQuery("select last_value from " + nome);
            rs.next();

            this.valor = rs.getLong(1);
        }
        return valor;
    }

    public void setValor(long valor) throws SQLException {
        try (Connection conn = schema.connect()) {
            conn.createStatement().executeQuery("SELECT setval('" + nome + "', " + valor + ", true)");
            this.valor = valor;
        }
    }

    public String getAbsoluteName() {
        return "<html>" + getSchema().getBanco().getNome() + "." + getSchema().getNome() + ".<b>" + getNome() + "</b></html>";
    }

    @Override
    public String toString() {
        return nome;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.nome);
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
        final Sequence other = (Sequence) obj;
        if (!Objects.equals(this.getNome(), other.getNome())) {
            return false;
        }
        return true;
    }

}
