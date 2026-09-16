package zg.acelera.repository

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.Country
import zg.acelera.utils.db.DatabaseManager
import zg.acelera.utils.exception.EntityNotFoundException

class CountryRepositoryJDBC implements ICountryRepository {
    Sql sql

    CountryRepositoryJDBC(DatabaseManager databaseManager) {
        this.sql = databaseManager.getSql()
    }

    @Override
    Country findById(UUID id) {
        GroovyRowResult row = sql.firstRow("SELECT * FROM countries WHERE id = ?", [id.toString()])
        if (row) {
            return  new Country(
                id: UUID.fromString(row.id.toString()),
                name: row.name,
                code: row.code
            )
        } else {
            throw new EntityNotFoundException("Country with id ${id} not found")
        }
    }

    @Override
    List<Country> findAll() {
        List<GroovyRowResult> rows = sql.rows("SELECT * FROM countries")
        return rows.collect { row ->
            new Country(
                id: UUID.fromString(row.id.toString()),
                name: row.name,
                code: row.code
            )
        }
    }
}
