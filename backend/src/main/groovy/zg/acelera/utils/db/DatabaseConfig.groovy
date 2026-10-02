package zg.acelera.utils.db

class DatabaseConfig {
    String url
    String user
    String password
    String driver
    int maxPoolSize = 5
    long connectionTimeout = 3000

    static DatabaseConfig fromEnvironment() {
        return new DatabaseConfig(
                url: System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5433/postgres",
                user: System.getenv("DB_USER") ?: "postgres",
                password: System.getenv("DB_PASSWORD") ?: "123456",
                driver: System.getenv("DB_DRIVER") ?: "org.postgresql.Driver"
        )
    }
}