package model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Banco {

    private static final long serialVersionUID = 2L;

    private String nome;
    private List<Schema> schemas;
    private final Servidor servidor;

    public Banco(Servidor servidor, String nome) {
        this.servidor = servidor;
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Schema> getSchemas() {
        return schemas;
    }

    public Servidor getServidor() {
        return servidor;
    }

    @Override
    public String toString() {
        return nome;
    }

    public Connection connect() throws SQLException {
        return servidor.connect(this);
    }

    public Connection connect(Schema schema) throws SQLException {
        return servidor.connect(schema);
    }

    public void loadSchemas() throws SQLException {
        try (Connection conn = connect()) {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getSchemas();
            schemas = new ArrayList<>();
            while (rs.next()) {
                Schema sch = new Schema(this, rs.getString(1));

                ResultSet rs2 = conn.createStatement().executeQuery("select r.rolname from pg_catalog.pg_namespace s"
                        + " join pg_roles r on (r.oid = s.nspowner) where s.nspname = '" + sch.getNome() + "'");
                rs2.next();
                sch.setOwner(rs2.getString(1));
                if (!sch.getNome().equals("information_schema") && !sch.getNome().equals("pg_catalog")) {
                    schemas.add(sch);
                }
            }
        }
    }

    public List<Tabela> getTabelas() {
        List<Tabela> tabs = new ArrayList<>();

        try {
            if (schemas == null) {
                loadSchemas();
            }

            for (Schema s : schemas) {
                if (s.getTabelas() == null) {
                    s.loadTables();
                }
                tabs.addAll(s.getTabelas());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return tabs;
    }
}
