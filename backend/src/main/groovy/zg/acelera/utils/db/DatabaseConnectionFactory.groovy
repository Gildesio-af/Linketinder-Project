package zg.acelera.utils.db

import groovy.sql.Sql

interface DatabaseConnectionFactory {
    Sql createSql()
    void close()
}

