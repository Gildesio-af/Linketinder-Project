package zg.acelera.repository.skill

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.Skill
import zg.acelera.utils.exception.EntityNotFoundException
import zg.acelera.utils.mapper.SkillRowMapper

class SkillRepositoryJDBC implements SkillRepository {
    final Sql sql

    SkillRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    Skill findById(UUID id) {
        GroovyRowResult row = sql.firstRow("SELECT * FROM skills WHERE id = ?", [id])
        if (row) return SkillRowMapper.getDomainFromRowToOtherEntities(row)

        throw new EntityNotFoundException("Skill with id ${id} not found")
    }

    @Override
    Skill findByName(String name) {
        GroovyRowResult row = sql.firstRow("SELECT * FROM skills WHERE lower(name) = lower(?)", [name])
        if (row) return SkillRowMapper.getDomainFromRowToOtherEntities(row)

        throw new EntityNotFoundException("Skill with name ${name} not found")
    }

    @Override
    Set<Skill> findAll() {
        Set<GroovyRowResult> rows = sql.rows("SELECT * FROM skills")

        return rows.collect {row -> SkillRowMapper.getDomainFromRow(row)}
    }

    @Override
    Skill save(Skill skill) {
        GroovyRowResult row = sql.firstRow("INSERT INTO skills (name) VALUES (?) RETURNING *", [skill.name])
        if (row) return SkillRowMapper.getDomainFromRowToOtherEntities(row)

        return null
    }

    @Override
    Skill update(Skill skill, UUID id) {
        GroovyRowResult row = sql.firstRow("UPDATE skills SET name = ? WHERE id = ? RETURNING *", [skill.name, id])
        if (row) return SkillRowMapper.getDomainFromRowToOtherEntities(row)

        throw new EntityNotFoundException("Skill with id ${id} not found")
    }

    @Override
    void deleteById(UUID id) {
        int rowsAffected = sql.executeUpdate("DELETE FROM skills WHERE id = ?", [id])
        sql.withTransaction {
            sql.executeUpdate("DELETE FROM addresses WHERE id IN (SELECT address_id FROM jobs WHERE id = ?)", [id])
            rowsAffected = sql.executeUpdate("DELETE FROM skills WHERE id = ?", [id])
        }
        if (rowsAffected == 0) {
            throw new EntityNotFoundException("Skill with id ${id} not found")
        }
    }

    Set<Skill> findByUserId(UUID userId) {
        List<GroovyRowResult> skillRows = sql.rows("""
            SELECT sk.id AS skill_id, sk.name AS skill_name
            FROM users_skill usk
            INNER JOIN skills sk ON sk.id = usk.skill_id
            WHERE usk.user_id = ?
        """, [userId])

        if (!skillRows) return [] as Set<Skill>
        return skillRows.collect { SkillRowMapper.getDomainFromRowToOtherEntities(it) } as Set<Skill>
    }
}
