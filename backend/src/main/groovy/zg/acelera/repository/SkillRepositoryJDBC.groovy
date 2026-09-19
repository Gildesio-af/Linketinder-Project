package zg.acelera.repository

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.Skill
import zg.acelera.utils.exception.EntityNotFoundException

class SkillRepositoryJDBC implements ISkillRepository {
    final Sql sql

    SkillRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    Skill findById(UUID id) {
        GroovyRowResult row = sql.firstRow("SELECT * FROM skills WHERE id = ?", [id])
        if (row) return skillFromRow(row)

        throw new EntityNotFoundException("Skill with id ${id} not found")
    }

    @Override
    Set<Skill> findAll() {
        Set<GroovyRowResult> rows = sql.rows("SELECT * FROM skills")

        return rows.collect {row -> skillFromRow(row)}
    }

    @Override
    Skill save(Skill skill) {
        GroovyRowResult row = sql.firstRow("INSERT INTO skills (name) VALUES (?) RETURNING *", [skill.name])
        if (row) return skillFromRow(row)

        return null
    }

    @Override
    Skill update(Skill skill, UUID id) {
        GroovyRowResult row = sql.firstRow("UPDATE skills SET name = ? WHERE id = ? RETURNING *", [skill.name, id])
        if (row) return skillFromRow(row)

        throw new EntityNotFoundException("Skill with id ${id} not found")
    }

    @Override
    void deleteById(UUID id) {
        int rowsAffected = sql.executeUpdate("DELETE FROM skills WHERE id = ?", [id])
        if (rowsAffected == 0) {
            throw new EntityNotFoundException("Skill with id ${id} not found")
        }
    }

    private static Skill skillFromRow(GroovyRowResult row) {
        return new Skill(
            id: UUID.fromString(row.id.toString()),
            name: row.name
        )
    }
}
