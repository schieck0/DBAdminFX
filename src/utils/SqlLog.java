package utils;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;

public class SqlLog {

    private static final StringBuilder LOG = new StringBuilder();
    private static List<ChangeListener<String>> listners = new ArrayList<>();

    private SqlLog() {
    }

    public static void append(String sql) {
        LOG.append(sql).append("\n");
        for (ChangeListener listn : listners) {
            listn.changed(null, null, getLog());
        }
    }

    public static String getLog() {
        return LOG.toString();
    }
    
    public static void addChangeListner(ChangeListener<String> listen) {
        listners.add(listen);
    }
}
