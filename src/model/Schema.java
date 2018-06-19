package model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Schema {

    private static final long serialVersionUID = 3L;

    private String nome;
    private String owner;
    private List<Tabela> tabelas;
    private List<View> views;
    private List<Function> functions;
    private List<Sequence> sequences;
    private final Banco banco;

    public Schema(Banco banco, String nome) {
        this.banco = banco;
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<Tabela> getTabelas() {
        return tabelas;
    }

    public List<Sequence> getSequences() {
        return sequences;
    }

    public Banco getBanco() {
        return banco;
    }

    public List<View> getViews() {
        return views;
    }

    public void setViews(List<View> views) {
        this.views = views;
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public void setFunctions(List<Function> functions) {
        this.functions = functions;
    }

    @Override
    public String toString() {
//        return nome + (tabelas != null ? " (" + tabelas.size() + ")" : "");
        return nome;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.nome);
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
        final Schema other = (Schema) obj;
        if (!Objects.equals(this.nome, other.nome)) {
            return false;
        }
        return true;
    }

    public Connection connect() throws SQLException {
        return banco.getServidor().connect(this);
    }

    public boolean loadTables() throws SQLException {
        try (Connection conn = banco.connect()) {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, nome, null, new String[]{"TABLE"});
            tabelas = new ArrayList<>();
            while (rs.next()) {
                Tabela t = new Tabela(this, rs.getString(3));
                tabelas.add(t);
            }
        }
        return !tabelas.isEmpty();
    }
    
    public boolean loadViews() throws SQLException {
        try (Connection conn = banco.connect()) {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, nome, null, new String[]{"VIEW"});
            views = new ArrayList<>();
            while (rs.next()) {
                View v = new View(this, rs.getString(3));
                views.add(v);
            }
        }
        return !tabelas.isEmpty();
    }
    
    public boolean loadFunctions() throws SQLException {
        try (Connection conn = banco.connect()) {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getFunctions(null, nome, null);
            functions = new ArrayList<>();
            while (rs.next()) {
                Function v = new Function(this, rs.getString(3));
                functions.add(v);
            }
        }
        return !tabelas.isEmpty();
    }

    public void loadSequences() throws SQLException {
        try (Connection conn = banco.connect()) {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, nome, null, new String[]{"SEQUENCE"});
            sequences = new ArrayList<>();
            while (rs.next()) {
                Sequence s = new Sequence(rs.getString(3));
                s.setSchema(this);
                sequences.add(s);
            }
        }
    }
}
