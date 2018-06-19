package model;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Servidor implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nome;
    private String endereco;
    private int porta;
    private String user;
    private String pwd;
    private String versao;
    private String sgbd;
    private final List<Banco> bancos = new ArrayList<>();

    public Servidor() {
    }

    public Servidor(String nome, String endereco, int porta) {
        this.nome = nome;
        this.endereco = endereco;
        this.porta = porta;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public List<Banco> getBancos() {
        return bancos;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getVersao() {
        return versao;
    }

    public void setVersao(String versao) {
        this.versao = versao;
    }

    public String getSgbd() {
        return sgbd;
    }

    public void setSgbd(String sgbd) {
        this.sgbd = sgbd;
    }

    public List<String> getLoginRolesNames() throws SQLException {
        List<String> loginRoles = new ArrayList<>();
        try (Connection conn = connect()) {
            ResultSet rs = conn.prepareStatement("SELECT rolname FROM pg_roles where rolcanlogin=true").executeQuery();
            while (rs.next()) {
                loginRoles.add(rs.getString(1));
            }
        }
        return loginRoles;
    }

    public List<String> getLanguages() throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection conn = connect()) {
            ResultSet rs = conn.prepareStatement("SELECT lanname FROM pg_language").executeQuery();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        }
        return list;
    }

    public List<String> getTypes() throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection conn = connect()) {
            ResultSet rs = conn.prepareStatement("select typname from pg_type "
                    + " where typtype = 'b'"
                    + " and (typelem = 0 or typelem not in (select oid from pg_type where typtype <> 'b'))").executeQuery();
            while (rs.next()) {
                list.add(rs.getString(1).replaceAll("_(\\w+)", "$1[]"));
            }
        }

        list.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        return list;
    }

    public String getTypeByOID(String oid) throws SQLException {
        try (Connection conn = connect()) {
            ResultSet rs = conn.prepareStatement("select typname from pg_type "
                    + " where oid = " + oid).executeQuery();
            rs.next();
            return rs.getString(1).replaceAll("_(\\w+)", "$1[]");
        }
    }

    public Map<Integer, String> getLoginRoles() throws SQLException {
        Map<Integer, String> loginRoles = new HashMap<>();
        try (Connection conn = connect()) {
            ResultSet rs = conn.prepareStatement("SELECT oid, rolname FROM pg_roles where rolcanlogin=true").executeQuery();
            while (rs.next()) {
                loginRoles.put(rs.getInt(1), rs.getString(2));
            }
        }
        return loginRoles;
    }

    public Connection connect() throws SQLException {
        return connect((Banco) null);
    }

    public Connection connect(Banco banco) throws SQLException {
        String url = "jdbc:postgresql://" + endereco + ":" + porta + "/"
                + (banco != null ? banco.getNome() : "postgres") + "?ApplicationName=DBAdmin";
        Connection conn = DriverManager.getConnection(url, user, pwd);
        return conn;
    }

    public Connection connect(Schema schema) throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://" + endereco + ":" + porta + "/"
                + schema.getBanco().getNome()
                + "?ApplicationName=DBAdmin&currentSchema=" + schema.getNome(), user, pwd);
    }

    public void loadDatabases() throws SQLException {
        try (Connection conn = connect()) {
            ResultSet rs = conn.prepareStatement("select * from pg_database order by 1").executeQuery();
            bancos.clear();
            while (rs.next()) {
//                Banco db = new Banco(this, "<html><b>"+rs.getString(1)+"</b></html>");
                Banco db = new Banco(this, rs.getString(1));
//                if (!db.getNome().equals("postgres") && !db.getNome().equals("template0") && !db.getNome().equals("template1")) {
                if (!db.getNome().equals("template0") && !db.getNome().equals("template1")) {
                    bancos.add(db);
                }
            }

            DatabaseMetaData md = conn.getMetaData();
            versao = md.getDatabaseProductVersion();
            sgbd = md.getDatabaseProductName();
        }
    }

    @Override
    public String toString() {
        return nome + " (" + endereco + ":" + porta + ", " + sgbd + " " + versao + ")";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.nome);
        hash = 17 * hash + this.porta;
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
        final Servidor other = (Servidor) obj;
        if (this.porta != other.porta) {
            return false;
        }
        if (!Objects.equals(this.nome, other.nome)) {
            return false;
        }
        if (!Objects.equals(this.endereco, other.endereco)) {
            return false;
        }
        return true;
    }

}
