package utils;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SQLType implements java.sql.SQLType {

    private final String name;
    private final Integer typeNumber;

    private SQLType(String name, Integer typeNumber) {
        this.name = name;
        this.typeNumber = typeNumber;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVendor() {
        return null;
    }

    @Override
    public Integer getVendorTypeNumber() {
        return typeNumber;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.typeNumber);
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
        final SQLType other = (SQLType) obj;
        if (!Objects.equals(this.typeNumber, other.typeNumber)) {
            return false;
        }
        return true;
    }

    public static final SQLType VARCHAR = new SQLType("VARCHAR", Types.VARCHAR);
    public static final SQLType INTEGER = new SQLType("INTEGER", Types.INTEGER);
    public static final SQLType DOUBLE = new SQLType("DOUBLE", Types.DOUBLE);
    public static final SQLType DATE = new SQLType("DATE", Types.DATE);
    public static final SQLType TIME = new SQLType("TIME", Types.TIME);
    public static final SQLType TIMESTAMP = new SQLType("TIMESTAMP", Types.TIMESTAMP);
    public static final SQLType BOOLEAN = new SQLType("BOOLEAN", Types.BIT);
    public static final SQLType ARRAY = new SQLType("ARRAY", Types.ARRAY);
    
    public static final List<SQLType> TYPES = new ArrayList<>(Arrays.asList(VARCHAR, INTEGER, DOUBLE, DATE, TIME, TIMESTAMP, BOOLEAN, ARRAY));

    public static Class<?> toClass(int type) {
        Class<?> result = Object.class;

        switch (type) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                result = String.class;
                break;

            case Types.NUMERIC:
            case Types.DECIMAL:
                result = java.math.BigDecimal.class;
                break;

            case Types.BIT:
                result = Boolean.class;
                break;

            case Types.TINYINT:
                result = Byte.class;
                break;

            case Types.SMALLINT:
                result = Short.class;
                break;

            case Types.INTEGER:
                result = Integer.class;
                break;

            case Types.BIGINT:
                result = Long.class;
                break;

            case Types.REAL:
            case Types.FLOAT:
                result = Float.class;
                break;

            case Types.DOUBLE:
                result = Double.class;
                break;

            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                result = Byte[].class;
                break;

            case Types.DATE:
                result = java.sql.Date.class;
                break;

            case Types.TIME:
                result = java.sql.Time.class;
                break;

            case Types.TIMESTAMP:
                result = java.sql.Timestamp.class;
                break;
        }

        return result;
    }
}
