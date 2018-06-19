package model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Tabela {

    private static final long serialVersionUID = 4L;

    private String nome;
    private String owner;
    private final List<Coluna> colunas = new ArrayList<>();
    private final List<ForeignKey> fks = new ArrayList<>();
    private final List<PrimaryKey> pks = new ArrayList<>();
    private Schema schema;
    private String order;
    private String filter;

    public Tabela(Schema schema, String nome) {
        this.schema = schema;
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Coluna> getColunas() {
        return colunas;
    }

    public Coluna getColuna(String nome) {
        Coluna c = new Coluna(nome);
        if (!colunas.contains(c)) {
            return null;
        }

        return colunas.get(colunas.indexOf(c));
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public List<ForeignKey> getFKs() {
        return fks;
    }

    public List<PrimaryKey> getPKs() {
        if (!pks.isEmpty()) {
            return pks;
        }

        for (Coluna c : colunas) {
            if (c.isPk()) {
                pks.add(new PrimaryKey(c));
            }
        }
        return pks;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return (schema != null ? schema.getNome() + "." : "") + nome;
    }

    @Override
    public int hashCode() {
        int hash = 5;
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
        final Tabela other = (Tabela) obj;
        if (!Objects.equals(this.schema, other.schema) || !Objects.equals(this.nome, other.nome)) {
            return false;
        }
        return true;
    }

    public String getFullName() {
        return (schema != null ? schema.getNome() + "." : "") + getNome();
    }

    public String getAbsoluteName() {
        return "<html>" + getSchema().getBanco().getNome() + "." + getSchema().getNome() + ".<b>" + getNome() + "</b></html>";
    }

    public void loadCols() throws SQLException {
        try (Connection conn = schema.getBanco().connect()) {
            DatabaseMetaData md = conn.getMetaData();
            colunas.clear();
            //COLUNAS
            ResultSet rs = md.getColumns(null, schema.getNome(), nome, null);
            while (rs.next()) {
                Coluna c = new Coluna(rs.getString(4));
                c.setTipoSql(rs.getInt(5));
                c.setTipo(rs.getString(6));
                c.setTamanho(rs.getInt(7));
                c.setNullable(rs.getInt(11));
                c.setDefaultVal(rs.getString(13));
                c.setTabela(this);
                colunas.add(c);
            }

            //FKs
            rs = md.getImportedKeys(null, schema.getNome(), nome);
            fks.clear();
            while (rs.next()) {
                ForeignKey fk = new ForeignKey();
                fk.setNome(rs.getString(12));
                Tabela pkTab = new Tabela(new Schema(null, rs.getString(2)), rs.getString(3));
                fk.setPkTab(pkTab);
                Coluna pkCol = new Coluna(rs.getString(4));
                pkCol.setTabela(pkTab);
                fk.setPkCol(pkCol);
//                fk.setFkTabNome(rs.getString(7));
                fk.setFkTab(this);
                Coluna colLocal = getColuna(rs.getString(8));
                colLocal.setFk(true);
                fk.setFkCol(colLocal);
                fk.setOnUpdate(DeleteUpdateAction.values()[rs.getInt(10)]);
                fk.setOnDelete(DeleteUpdateAction.values()[rs.getInt(11)]);
                fk.setTabela(this);
                fks.add(fk);

//                for (Coluna col : colunas) {
//                    if (col.equals(fk.getFkCol())) {
//                        col.setFk(true);
//                    }
//                }
            }

            //PKs
            rs = md.getPrimaryKeys(null, schema.getNome(), nome);
            pks.clear();
            while (rs.next()) {
                String nomeCol = rs.getString(4);
                for (Coluna col : colunas) {
                    if (col.getNome().equals(nomeCol)) {
                        PrimaryKey pk = new PrimaryKey(col);
                        pk.setNome(rs.getString(6));
                        pks.add(pk);
                        col.setPk(true);
                        break;
                    }
                }
            }

            //ORDENA primeiro PK
            colunas.sort(new Comparator<Coluna>() {
                @Override
                public int compare(Coluna o1, Coluna o2) {
                    if (o1.isPk() == o2.isPk()) {
                        return 0;
                    } else {
                        return o1.isPk() ? -1 : 1;
                    }
                }
            });

            if (this instanceof View) {
                rs = conn.createStatement().executeQuery("select viewowner, definition from pg_views"
                        + " where schemaname = '" + schema.getNome() + "' and viewname = '" + nome + "'");
                rs.next();
                owner = rs.getString(1);
                ((View)this).setDefinition(rs.getString(2));
            } else {
                rs = conn.createStatement().executeQuery("select tableowner from pg_tables"
                        + " where schemaname = '" + schema.getNome() + "' and tablename = '" + nome + "'");
                rs.next();
                owner = rs.getString(1);
            }
        }
    }

    public List<Object[]> readData() throws SQLException {
        try (Connection conn = schema.getBanco().connect()) {
            Statement stm = conn.createStatement();
            String colNames = "";
            String orderByKey = "";
            for (int i = 1; i <= colunas.size(); i++) {
                colNames += colunas.get(i - 1).getNome();
                if (i < colunas.size()) {
                    colNames += ",";
                }

                if (colunas.get(i - 1).isPk()) {
                    orderByKey += colunas.get(i - 1).getNome() + ",";
                }
            }
            if (orderByKey.endsWith(",")) {
                orderByKey = orderByKey.substring(0, orderByKey.length() - 1);
            }

            if (order == null || order.isEmpty()) {
                order = orderByKey;
            }

            ResultSet rs = stm.executeQuery("select " + colNames + " from " + schema.getNome() + "." + nome
                    + (filter != null && !filter.isEmpty() ? " where " + filter : "")
                    + (order != null && !order.isEmpty() ? " order by " + order : ""));
            List<Object[]> dados = new ArrayList<>();
            Object[] linha;
            while (rs.next()) {
                linha = new Object[colunas.size()];
                for (int i = 1; i <= colunas.size(); i++) {
                    linha[i - 1] = rs.getObject(i);
                }

                dados.add(linha);
            }
            return dados;
        }
    }

    public boolean dependeDe(String tabela) {
        for (ForeignKey fk : fks) {
            if (fk.getPkTab().getNome().equals(tabela)) {
                return true;
            }
        }

        return false;
    }

}
