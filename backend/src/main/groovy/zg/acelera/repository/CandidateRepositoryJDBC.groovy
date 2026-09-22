package zg.acelera.repository

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.Candidate
import zg.acelera.domain.IPerson
import zg.acelera.domain.Skill
import zg.acelera.utils.exception.EntityNotFoundException

import java.time.LocalDate

class CandidateRepositoryJDBC implements ICandidateRepository {
    final Sql sql

    CandidateRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    IPerson findById(UUID id) {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT us.*, ca.*, sk.id AS skill_id, sk.name AS skill_name
            FROM users us
            INNER JOIN candidates ca ON us.id = ca.user_id
            LEFT JOIN users_skill usk ON us.id = usk.user_id
            LEFT JOIN skills sk ON sk.id = usk.skill_id
            WHERE us.id = ?
        """, [id])

        if (!rows) {
            throw new EntityNotFoundException("Candidate with ID ${id} not found")
        }

        return getCandidatesWithSkillsFromRows(rows).first()
    }

    @Override
    List<IPerson> findAll() {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT us.*, ca.*, sk.id AS skill_id, sk.name AS skill_name
            FROM users us
            INNER JOIN candidates ca ON us.id = ca.user_id
            LEFT JOIN users_skill usk ON us.id = usk.user_id
            LEFT JOIN skills sk ON sk.id = usk.skill_id
        """)

        return getCandidatesWithSkillsFromRows(rows).toList()
    }

    @Override
    IPerson findByCpf(String cpf) {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT us.*, ca.*, sk.id AS skill_id, sk.name AS skill_name
            FROM users us
            INNER JOIN candidates ca ON us.id = ca.user_id
            LEFT JOIN users_skill usk ON us.id = usk.user_id
            LEFT JOIN skills sk ON sk.id = usk.skill_id
            WHERE ca.cpf = ?
        """, [cpf])

        if (!rows) {
            throw new EntityNotFoundException("Candidate) with CPF ${cpf} not found")
        }

        return getCandidatesWithSkillsFromRows(rows).first()
    }

    @Override
    List<IPerson> findBySkill(String skill) {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT us.*, ca.*, sk.id AS skill_id, sk.name AS skill_name
            FROM users us
            INNER JOIN candidates ca ON us.id = ca.user_id
            LEFT JOIN users_skill usk ON us.id = usk.user_id
            LEFT JOIN skills sk ON sk.id = usk.skill_id
            WHERE us.id IN (
                SELECT DISTINCT usk2.user_id
                FROM users_skill usk2
                INNER JOIN skills sk2 ON sk2.id = usk2.skill_id
                WHERE lower(sk2.name) = lower(?)
            )
        """, [skill])

        return getCandidatesWithSkillsFromRows(rows).toList()
    }

    @Override
    Candidate save(Candidate user) {
        Candidate savedCandidate = null

        sql.withTransaction {
            GroovyRowResult rowGenericUser = sql.firstRow("""
                INSERT INTO users (name, email, password, description)
                VALUES (?, ?, ?, ?)
                RETURNING *
            """, [user.name, user.email, user.password, user.description])

            UUID generatedUserId = UUID.fromString(rowGenericUser.id.toString())

            GroovyRowResult rowCandidate = sql.firstRow("""
                INSERT INTO candidates (user_id, cpf, last_name, birth_date)
                VALUES (?, ?, ?, ?)
                RETURNING *
            """, [generatedUserId, user.cpf, user.lastName, user.birthDate])

            if (user.skills) {
                user.skills.each { skill ->
                    if (skill.id) {
                        sql.executeInsert("""
                            INSERT INTO users_skill (user_id, skill_id)
                            VALUES (?, ?)
                        """, [generatedUserId, skill.id])
                    }
                }
            }

            savedCandidate = getCandidateFromUserRowAndCandidateRow(rowGenericUser, rowCandidate)

            Set<Skill> fullSkills = [] as Set<Skill>
            if (user.skills) {
                List<GroovyRowResult> skillRows = sql.rows("""
                    SELECT sk.id AS skill_id, sk.name AS skill_name
                    FROM users_skill usk
                    INNER JOIN skills sk ON sk.id = usk.skill_id
                    WHERE usk.user_id = ?
                """, [generatedUserId])
                fullSkills = skillRows.collect { row ->
                    new Skill(id: UUID.fromString(row.skill_id.toString()), name: row.skill_name.toString())
                } as Set<Skill>
            }
            savedCandidate.skills = fullSkills
        }

        return savedCandidate
    }

    @Override
    Candidate update(Candidate user, UUID userId) {
        Candidate updatedCandidate = null

        sql.withTransaction {
            GroovyRowResult rowGenericUser = sql.firstRow("""
                UPDATE users
                SET name = COALESCE(?, name), email = COALESCE(?, email),description = COALESCE(?, description)
                WHERE id = ?
                RETURNING *
            """, [user.name, user.email, user.description, userId])

            GroovyRowResult rowCandidate = sql.firstRow("""
                UPDATE candidates
                SET last_name = COALESCE(?, last_name), birth_date = COALESCE(?, birth_date)
                WHERE user_id = ?
                RETURNING *
            """, [user.lastName, user.birthDate, userId])

            updatedCandidate = getCandidateFromUserRowAndCandidateRow(rowGenericUser, rowCandidate)
            updatedCandidate.skills = user.skills ?: [] as Set<Skill>
        }

        return updatedCandidate
    }

    @Override
    void delete(UUID userId) {
        sql.withTransaction {
            sql.execute("DELETE FROM candidates WHERE user_id = ?", [userId])
            sql.execute("DELETE FROM users WHERE id = ?", [userId])
        }
    }

    private static Set<Candidate> getCandidatesWithSkillsFromRows(List<GroovyRowResult> rows) {
        if (!rows) return [] as Set<Candidate>

        return rows.groupBy { it.id }.collect { userId, userRows ->
            getCandidateWithSkillsFromRows(userRows)
        } as Set<Candidate>
    }

    private static Candidate getCandidateWithSkillsFromRows(List<GroovyRowResult> rows) {
        if (!rows) return null

        Candidate candidate = getCandidateFromRow(rows.first())

        Set<Skill> skills = rows.findResults { row ->
            if (row.skill_id) {
                return new Skill(
                    id: UUID.fromString(row.skill_id.toString()),
                    name: row.skill_name.toString()
                )
            }
            return null
        } as Set<Skill>

        candidate.skills = skills
        return candidate
    }

    private static Candidate getCandidateFromRow(GroovyRowResult row) {
        return new Candidate(
            id: UUID.fromString(row.id.toString()),
            name: row.name,
            email: row.email,
            password: row.password,
            description: row.description,
            cpf: row.cpf,
            lastName: row.last_name,
            birthDate: row.birth_date ? LocalDate.parse(row.birth_date.toString()) : null
        )
    }

    private static Candidate getCandidateFromUserRowAndCandidateRow(GroovyRowResult userRow, GroovyRowResult candidateRow) {
        return getCandidateFromRow(userRow + candidateRow as GroovyRowResult)
    }
}
