package view;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Test {

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://10.20.40.24:5433/postgres", "postgres", "senha")) {
            System.out.println("conn");

            Statement stm = conn.createStatement();
            System.out.println("INSERT");
//            stm.executeUpdate("insert into teste(descricao) values ('asd') RETURNING (id)");
            stm.executeUpdate("insert into teste(descricao) values ('asd')", Statement.RETURN_GENERATED_KEYS);
            
            ResultSet rs = stm.getGeneratedKeys();
            System.out.println("KEYS:");
            while (rs.next()) {
                System.out.println(rs.getInt(1));
            }
            
            
            System.out.println("END");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
