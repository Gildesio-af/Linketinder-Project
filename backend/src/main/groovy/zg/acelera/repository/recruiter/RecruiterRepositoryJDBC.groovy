package zg.acelera.repository.recruiter

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.Recruiter
import zg.acelera.domain.RecruiterStatus
import zg.acelera.utils.exception.EntityNotFoundException
import zg.acelera.utils.mapper.RecruiterRowMapper

class RecruiterRepositoryJDBC implements RecruiterRepository {
    final Sql sql

    RecruiterRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    Recruiter findById(UUID id) {
        GroovyRowResult row = sql.firstRow("""
            SELECT u.id, u.name, u.email, u.description,
                   r.recruitment_area_id, r.cpf, r.birth_date, r.status
            FROM users u
            INNER JOIN recruiters r ON r.user_id = u.id
            WHERE u.id = ?
        """, [id])

        if (!row) throw new EntityNotFoundException("Recruiter with ID ${id} not found")

        RecruiterRowMapper.fromRow(row)
    }

    @Override
    Recruiter findByEmail(String email) {
        GroovyRowResult row = sql.firstRow("""
            SELECT u.id, u.name, u.email, u.description,
                   r.recruitment_area_id, r.cpf, r.birth_date, r.status
            FROM users u
            INNER JOIN recruiters r ON r.user_id = u.id
            WHERE lower(u.email) = lower(?)
        """, [email])

        if (!row) throw new EntityNotFoundException("Recruiter with email ${email} not found")

        RecruiterRowMapper.fromRow(row)
    }

    @Override
    Recruiter save(Recruiter recruiter) {
        Recruiter savedRecruiter = null

        sql.withTransaction {
            GroovyRowResult userRow = sql.firstRow("""
                INSERT INTO users (name, email, password, description)
                VALUES (?, ?, ?, ?)
                RETURNING id, name, email, description
            """, [recruiter.name, recruiter.email, recruiter.password, recruiter.description])

            GroovyRowResult recruiterRow = sql.firstRow("""
                INSERT INTO recruiters (user_id, recruitment_area_id, cpf, birth_date, status)
                VALUES (?, ?, ?, ?, ?)
                RETURNING recruitment_area_id, cpf, birth_date, status
            """, [
                userRow.id,
                recruiter.recruitmentAreaId,
                recruiter.cpf,
                recruiter.birthDate,
                (recruiter.status ?: RecruiterStatus.ACTIVE).name()
            ])

            savedRecruiter = RecruiterRowMapper.fromRow(userRow + recruiterRow as GroovyRowResult)
        }

        savedRecruiter
    }

    @Override
    Recruiter update(Recruiter recruiter, UUID userId) {
        Recruiter updatedRecruiter = null

        sql.withTransaction {
            GroovyRowResult userRow = sql.firstRow("""
                UPDATE users
                SET name = COALESCE(?, name),
                    email = COALESCE(?, email),
                    description = COALESCE(?, description),
                    password = COALESCE(?, password)
                WHERE id = ?
                RETURNING id, name, email, description
            """, [recruiter.name, recruiter.email, recruiter.description, recruiter.password, userId])

            if (!userRow) throw new EntityNotFoundException("Recruiter with ID ${userId} not found")

            GroovyRowResult recruiterRow = sql.firstRow("""
                UPDATE recruiters
                SET recruitment_area_id = COALESCE(?, recruitment_area_id),
                    cpf = COALESCE(?, cpf),
                    birth_date = COALESCE(?, birth_date)
                WHERE user_id = ?
                RETURNING recruitment_area_id, cpf, birth_date, status
            """, [recruiter.recruitmentAreaId, recruiter.cpf, recruiter.birthDate, userId])

            if (!recruiterRow) throw new EntityNotFoundException("Recruiter with ID ${userId} not found")

            updatedRecruiter = RecruiterRowMapper.fromRow(userRow + recruiterRow as GroovyRowResult)
        }

        updatedRecruiter
    }

    @Override
    Recruiter updateStatus(UUID userId, RecruiterStatus status) {
        if (status == null) throw new IllegalArgumentException('Recruiter status is required.')

        GroovyRowResult row = sql.firstRow("""
            UPDATE recruiters
            SET status = ?
            WHERE user_id = ?
            RETURNING user_id
        """, [status.name(), userId])

        if (!row) throw new EntityNotFoundException("Recruiter with ID ${userId} not found")

        findById(userId)
    }
}
