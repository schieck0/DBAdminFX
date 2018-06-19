package utils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NamedParameterStatement {

    ////////////////////////////////////////////////////////////////
    // ATRIBUTOS
    ////////////////////////////////////////////////////////////////
    //Cria um PreparedStatement interno para realizar a execucao do SQL
    private PreparedStatement statement;

    //Map para indicar as posicoes dos parametros
    private final Map<String, List<Integer>> mapPosicoesParametros = new HashMap<String, List<Integer>>();

    ////////////////////////////////////////////////////////////////
    // CONSTRUTORES
    ////////////////////////////////////////////////////////////////
    /**
     * Construtor que realiza a instanciacao de um prepareStatement a partir da
     * connection
     *
     * @param connection - A conexao com o banco de dados
     * @param query	- A query no formato namedQuery
     * @throws SQLException - Se o statement nao puder ser criado
     */
    public NamedParameterStatement(Connection connection, String namedQuery) throws SQLException {
        String parsedQuery = parse(namedQuery);
        statement = connection.prepareStatement(parsedQuery);
    }

    /**
     * Construtor que realiza a instanciacao de um prepareStatement a partir da
     * connection para inserts com auto generated keys
     *
     * @param connection	- A conexao com o banco de dados
     * @param query	- A query no formato namedQuery
     * @param RETURN_GENERATED_KEYS	- A constante definida em
     * java.sql.Statement.RETURN_GENERATED_KEYS
     * @throws SQLException - Se o statement nao puder ser criado
     *
     * Construtor que realiza a instanciacao de um prepareStatement a partir da
     * connection
     * @param connection the database connection
     * @param query	the parameterized query
     * @throws SQLException if the statement could not be created
     */
    public NamedParameterStatement(Connection connection, String namedQuery, Integer RETURN_GENERATED_KEYS) throws SQLException {
        String parsedQuery = parse(namedQuery);
        statement = connection.prepareStatement(parsedQuery, RETURN_GENERATED_KEYS);
    }

    ////////////////////////////////////////////////////////////////
    // PARSER
    ////////////////////////////////////////////////////////////////
    private String parse(String namedQuery) {
        int length = namedQuery.length();

        //Cria um String Buffer com o tamanho do SQL
        StringBuilder parsedQuery = new StringBuilder(length);

        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        int position = 1;
        //Percorre todo o SQL
        for (int i = 0; i < length; i++) {
            char c = namedQuery.charAt(i);

            //: Significa inicio de um rotulo de parametro
            //E nao esta em uma palavra com aspas simples ou duplas
            if (c == ':' && !inSingleQuote && !inDoubleQuote && i + 1 < length
                    && Character.isJavaIdentifierStart(namedQuery.charAt(i + 1))) {
                int j = i + 2;

                while (j < length && Character.isJavaIdentifierPart(namedQuery.charAt(j))) {
                    j++;
                }

                String name = namedQuery.substring(i + 1, j);
                c = '?'; // substitui o caracter c pelo parametro de index
                i += name.length(); // pula i ate o fim do nome do parametro

                List<Integer> indexList = mapPosicoesParametros.get(name);
                //Se o parametro ainda nao existir no map inicializa-o
                if (indexList == null) {
                    indexList = new LinkedList<Integer>();
                    mapPosicoesParametros.put(name, indexList);
                }
                indexList.add(position++);
            }

            //Adiciona o novo caractere a query passada pelo parser
            parsedQuery.append(c);

            if (c == '\'') {
                inSingleQuote = !inSingleQuote;
            } else if (c == '"') {
                inDoubleQuote = !inDoubleQuote;
            }
        }
        return parsedQuery.toString();
    }//Fim do metodo parser

    ////////////////////////////////////////////////////////////////
    // PARAMETERS
    ////////////////////////////////////////////////////////////////
    /**
     * Returns the indexes for a parameter.
     *
     * @param name parameter name
     * @return parameter indexes
     * @throws IllegalArgumentException if the parameter does not exist
     */
    private List<Integer> getIndexes(String name) {
        List<Integer> indexes = mapPosicoesParametros.get(name);
        if (indexes == null) {
            throw new IllegalArgumentException("Parameter not found: " + name);
        }
        return indexes;
    }

    /**
     * Sets a parameter.
     *
     * @param name parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setBoolean(int, boolean)
     */
    public void setBoolean(String name, boolean value) throws SQLException {
        for (Integer position : getIndexes(name)) {
            statement.setBoolean(position, value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param name parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setDate(int, java.sql.Date)
     */
    public void setDate(String name, Date value) throws SQLException {
        for (Integer position : getIndexes(name)) {
            statement.setDate(position, value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param name parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setDouble(int, double)
     */
    public void setDouble(String name, double value) throws SQLException {
        for (Integer position : getIndexes(name)) {
            statement.setDouble(position, value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param name parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setInt(int, int)
     */
    public void setInt(String name, int value) throws SQLException {
        for (Integer position : getIndexes(name)) {
            statement.setInt(position, value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param name parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setInt(int, int)
     */
    public void setLong(String name, long value) throws SQLException {
        for (Integer position : getIndexes(name)) {
            statement.setLong(position, value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param name parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setObject(int, java.lang.Object)
     */
    public void setObject(String name, Object value) throws SQLException {
        for (Integer position : getIndexes(name)) {
            statement.setObject(position, value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param name parameter name
     * @param value parameter value
     * @param sqlType parameter sqlType
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setObject(int, java.lang.Object)
     */
    public void setObject(String name, Object value, int sqlType) throws SQLException {
        for (Integer position : getIndexes(name)) {
            statement.setObject(position, value, sqlType);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param name parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setString(int, java.lang.String)
     */
    public void setString(String name, String value) throws SQLException {
        for (Integer position : getIndexes(name)) {
            statement.setString(position, value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param name parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setTimestamp(int, java.sql.Time)
     */
    public void setTime(String name, Time value) throws SQLException {
        for (Integer position : getIndexes(name)) {
            statement.setTime(position, value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param name parameter name
     * @param value parameter value
     * @throws SQLException if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setTimestamp(int, java.sql.Timestamp)
     */
    public void setTimestamp(String name, Timestamp value) throws SQLException {
        for (Integer position : getIndexes(name)) {
            statement.setTimestamp(position, value);
        }
    }

    ////////////////////////////////////////////////////////////////
    // EXECUCAO DE QUERIES
    ////////////////////////////////////////////////////////////////
    /**
     * Executes the statement.
     *
     * @return true if the first result is a {@link ResultSet}
     * @throws SQLException if an error occurred
     * @see PreparedStatement#execute()
     */
    public boolean execute() throws SQLException {
        return statement.execute();
    }

    /**
     * Executes the statement, which must be a query.
     *
     * @return the query results
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeQuery()
     */
    public ResultSet executeQuery() throws SQLException {
        return statement.executeQuery();
    }

    /**
     * Executes the statement, which must be an SQL INSERT, UPDATE or DELETE
     * statement; or an SQL statement that returns nothing, such as a DDL
     * statement.
     *
     * @return number of rows affected
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeUpdate()
     */
    public int executeUpdate() throws SQLException {
        return statement.executeUpdate();
    }

    ////////////////////////////////////////////////////////////////
    // RECUPERACAO DE CHAVES GERADAS
    ////////////////////////////////////////////////////////////////
    /**
     * Retrieves any auto-generated keys created as a result of executing this
     * Statement object. If this Statement object did not generate any keys, an
     * empty ResultSet object is returned.
     *
     * @return a ResultSet object containing the auto-generated key(s) generated
     * by the execution of this Statement object
     * @throws SQLException if an error occurred
     * @see PreparedStatement#getGeneratedKeys()
     */
    public ResultSet getGeneratedKeys() throws SQLException {
        return statement.getGeneratedKeys();
    }

    ////////////////////////////////////////////////////////////////
    // OUTROS
    ////////////////////////////////////////////////////////////////
    /**
     * Returns the underlying statement.
     *
     * @return the statement
     */
    public PreparedStatement getStatement() {
        return statement;
    }

    /**
     * Closes the statement.
     *
     * @throws SQLException if an error occurred
     * @see Statement#close()
     */
    public void close() throws SQLException {
        statement.close();
    }
}//Fim da classe<span class="pun">;</span>
