package zg.acelera.repository

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.Candidate
import zg.acelera.domain.IPerson
import zg.acelera.utils.exception.EntityNotFoundException

import java.time.LocalDate

class CandidateRepositoryJDBC implements ICandidateRepository {
    final Sql sql

    CandidateRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    IPerson findById(UUID id) {
        GroovyRowResult row = sql.firstRow("SELECT * FROM users,  candidates WHERE users.id = candidates.user_id AND users.id = ?", [id.toString()])
        if (row) return getCandidateFromRow(row)

        throw new EntityNotFoundException("Candidate with ID ${id} not found")
    }

    @Override
    List<IPerson> findAll() {
        List<GroovyRowResult> rows = sql.rows("SELECT * FROM users,  candidates WHERE users.id = candidates.user_id")
        List<IPerson> candidates = rows.collect { row -> getCandidateFromRow(row) }
        return candidates
    }

    @Override
    IPerson findByCpf(String cpf) {
        GroovyRowResult row = sql.firstRow("SELECT * FROM users,  candidates WHERE users.id = candidates.user_id AND candidates.cpf = ?", [cpf])
        if (row) return getCandidateFromRow(row)

        throw new EntityNotFoundException("Candidate) with CPF ${cpf} not found")
    }

    @Override
    List<IPerson> findBySkill(String skill) {
        List<GroovyRowResult> rows = sql.rows("""
            SELECT us.*, ca.*
            FROM users us
            INNER JOIN candidates ca ON us.id = ca.user_id
            INNER JOIN users_skill usk ON us.id = usk.user_id
            INNER JOIN skills sk ON usk.skill_id = sk.id
            WHERE lower(sk.name) = lower(?)
        """, [skill])

        return rows.collect { row -> getCandidateFromRow(row) }
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

            savedCandidate = getCandidateFromUserRowAndCandidateRow(rowGenericUser, rowCandidate)
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
