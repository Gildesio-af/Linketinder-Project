package zg.acelera.repository.recruiter

import groovy.sql.GroovyRowResult
import groovy.sql.Sql

class RecruitmentAreaRepositoryJDBC implements RecruitmentAreaRepository {
    final Sql sql

    RecruitmentAreaRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    List<Map<String, Object>> findAll() {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT id, name
            FROM recruitment_areas
            ORDER BY name
        """)

        rows.collect { row ->
            [
                id  : UUID.fromString(row.id.toString()),
                name: row.name.toString()
            ] as Map<String, Object>
        }
    }
}
