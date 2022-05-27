package hikaricp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSource {
    private static HikariConfig conf = new HikariConfig();
    private static HikariDataSource ds;

    static {
        conf.setJdbcUrl("JDBC_URL");
        conf.setUsername("USER_NAME");
        conf.setPassword("PASSWORD");
        conf.addDataSourceProperty("cachePrepStmts", "true");
        conf.addDataSourceProperty("prepStmtCacheSize", "250");
    }
}
