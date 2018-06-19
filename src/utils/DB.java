package utils;

import model.Servidor;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;

public class DB {

    private static Data data;

    private static final String dbName = "dbadmin.ago";

    public static void store() {
        try {
            for (Servidor s : data.servers) {
                s.getBancos().clear();
            }
            File ago = new File(System.getProperty("user.home"), ".ago");
            if (!ago.exists()) {
                ago.mkdir();
            }
            FileOutputStream fos = new FileOutputStream(new File(ago, dbName));

            ObjectOutput out = new ObjectOutputStream(fos);
            out.writeObject(data);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao salvar dados: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        
    }

    public static void read() {
        try {
            File ago = new File(System.getProperty("user.home"), ".ago");
            if (!ago.exists()) {
                ago.mkdir();
            }
            File dados = new File(ago, dbName);

            if (dados.exists()) {
                byte[] buffer = null;
                try (InputStream is = new FileInputStream(dados)) {
                    buffer = new byte[is.available()];
                    is.read(buffer);
                }

                try (ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                        ObjectInput in = new ObjectInputStream(bis)) {
                    Data d = (Data) in.readObject();
                    data = d;
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            } else {
                data = new Data();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao ler dados: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            data = new Data();
        }
    }
    
    public static Set<Servidor> getServers() {
        if (data.servers == null) {
            data.servers = new HashSet<>();
        }
        return data.servers;
    }
    
    public static File getPgDump() {
        return data.pdDump;
    }
    
    public static void setPgDump(File f) {
        data.pdDump = f;
    }
    
    static class Data implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Set<Servidor> servers;
        private File pdDump;
    }

}
