package rest.servlet.dbconnect;

import org.apache.commons.dbcp2.BasicDataSource;
import rest.servlet.util.PropertiesUtil;

import java.sql.Connection;
import java.sql.SQLException;

public final class DataSource {
    private static BasicDataSource ds = new BasicDataSource();

    static {
        ds.setUrl(PropertiesUtil.getProperty("db.url"));
        ds.setUsername(PropertiesUtil.getProperty("db.username"));
        ds.setPassword(PropertiesUtil.getProperty("db.password"));
        ds.setMinIdle(5);
        ds.setMaxIdle(10);
        ds.setMaxOpenPreparedStatements(100);
    }

    public static Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private DataSource() {}
}
