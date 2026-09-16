package zg.acelera.utils.db

import groovy.sql.Sql
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

class DatabaseManager {
    private static final DatabaseManager instance = new DatabaseManager()
    private HikariDataSource dataSource

    private DatabaseManager() {
        def config = new HikariConfig()
        String dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5433/postgres"
        String dbUser = System.getenv("DB_USER") ?: "postgres"
        String dbPassword = System.getenv("DB_PASSWORD") ?: "123456"
        config.setJdbcUrl(dbUrl)
        config.setUsername(dbUser)
        config.setPassword(dbPassword)
        config.setDriverClassName("org.postgresql.Driver")

        config.setMaximumPoolSize(5)
        config.setConnectionTimeout(3000)

        this.dataSource = new HikariDataSource(config)
    }

    static Sql getSql() {
        return new Sql(instance.dataSource)
    }

    static void close() {
        if (instance.dataSource) {
            instance.dataSource.close()
        }
    }
}