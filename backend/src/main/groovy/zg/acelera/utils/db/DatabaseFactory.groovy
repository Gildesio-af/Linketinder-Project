package zg.acelera.utils.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import groovy.sql.Sql

class DatabaseFactory implements DatabaseConnectionFactory {
    private HikariDataSource dataSource

    DatabaseFactory(DatabaseConfig config) {
        HikariConfig hikariConfig = new HikariConfig()
        hikariConfig.setJdbcUrl(config.url)
        hikariConfig.setUsername(config.user)
        hikariConfig.setPassword(config.password)
        hikariConfig.setDriverClassName(config.driver)
        hikariConfig.setMaximumPoolSize(config.maxPoolSize)
        hikariConfig.setConnectionTimeout(config.connectionTimeout)
        this.dataSource = new HikariDataSource(hikariConfig)
    }

    @Override
    Sql createSql() {
        return new Sql(dataSource)
    }

    @Override
    void close() {
        if (dataSource) dataSource.close()
    }
}
