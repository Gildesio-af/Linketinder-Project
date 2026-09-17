package zg.acelera.repository

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.Country
import zg.acelera.utils.exception.EntityNotFoundException

class CountryRepositoryJDBC implements ICountryRepository {
    final Sql sql

    CountryRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    Country findById(UUID id) {
        GroovyRowResult row = sql.firstRow("SELECT * FROM countries WHERE id = ?", [id])
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
