package model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Function {

    private String nome;
    private String owner;
    private Schema schema;
    private String code;
    private String linguagem;
    private String retorno;
    private Map<String, String> params = new HashMap<>();

    public Function(Schema schema, String nome) {
        this.nome = nome;
        this.schema = schema;
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

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLinguagem() {
        return linguagem;
    }

    public void setLinguagem(String linguagem) {
        this.linguagem = linguagem;
    }

    public String getRetorno() {
        return retorno;
    }

    public void setRetorno(String retorno) {
        this.retorno = retorno;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return nome;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.nome);
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
        final Function other = (Function) obj;
        if (!Objects.equals(this.nome, other.nome)) {
            return false;
        }
        if (!Objects.equals(this.schema, other.schema)) {
            return false;
        }
        return true;
    }

    public void load() throws SQLException {
        try (Connection conn = schema.connect()) {
//            ResultSet rs = conn.createStatement().executeQuery("select pg_get_functiondef('" + nome + "'::regproc)");
            ResultSet rs = conn.createStatement().executeQuery("select p.*, t.typname as retorno, l.lanname as linguagem, r.rolname as owner from pg_proc p"
                    + " join pg_type t on (p.prorettype = t.oid)"
                    + " join pg_language l on (l.oid = p.prolang)"
                    + " join pg_roles r on (r.oid = p.proowner)"
                    + " join pg_namespace n on (n.oid = p.pronamespace)"
                    + " where p.proname='" + nome + "' and n.nspname = '" + schema.getNome() + "'");
            rs.next();
            code = rs.getString("prosrc").trim();

            owner = rs.getString("owner");
            retorno = rs.getString("retorno");
            linguagem = rs.getString("linguagem");

            String args = rs.getString("proargnames");
            if (args != null) {
                String[] argNames = args.replace("{", "").replace("}", "").split(",");
                String[] argTypes = rs.getString("proargtypes").split(" ");
                params.clear();
                for (int i = 0; i < argNames.length; i++) {
                    params.put(argNames[i], schema.getBanco().getServidor().getTypeByOID(argTypes[i]));
                }
            }
        }
    }

}
