package zg.acelera.repository.recruiter

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.RecruiterJobLike
import zg.acelera.utils.exception.EntityNotFoundException

import java.sql.Timestamp
import java.time.LocalDateTime

class RecruiterJobLikeRepositoryJDBC implements RecruiterJobLikeRepository {
    private final Sql sql

    RecruiterJobLikeRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    RecruiterJobLike like(UUID recruiterId, UUID jobId, UUID candidateId) {
        GroovyRowResult row = sql.firstRow("""
            INSERT INTO recruiter_job_likes (recruiter_id, job_id, candidate_id, removed_at)
            VALUES (?, ?, ?, NULL)
            ON CONFLICT (recruiter_id, job_id, candidate_id)
            DO UPDATE SET removed_at = NULL
            RETURNING recruiter_id, job_id, candidate_id, created_at, removed_at
        """, [recruiterId, jobId, candidateId])
        fromRow(row)
    }

    @Override
    RecruiterJobLike unlike(UUID recruiterId, UUID jobId, UUID candidateId) {
        GroovyRowResult row = sql.firstRow("""
            UPDATE recruiter_job_likes
            SET removed_at = CURRENT_TIMESTAMP
            WHERE recruiter_id = ? AND job_id = ? AND candidate_id = ?
              AND removed_at IS NULL
            RETURNING recruiter_id, job_id, candidate_id, created_at, removed_at
        """, [recruiterId, jobId, candidateId])
        if (!row) {
            row = sql.firstRow("""
                SELECT recruiter_id, job_id, candidate_id, created_at, removed_at
                FROM recruiter_job_likes
                WHERE recruiter_id = ? AND job_id = ? AND candidate_id = ?
            """, [recruiterId, jobId, candidateId])
        }
        if (!row) throw new EntityNotFoundException('Recruiter like not found')
        fromRow(row)
    }

    @Override
    boolean existsActive(UUID recruiterId, UUID jobId, UUID candidateId) {
        exists("""
            SELECT 1 FROM recruiter_job_likes
            WHERE recruiter_id = ? AND job_id = ? AND candidate_id = ?
              AND removed_at IS NULL
        """, [recruiterId, jobId, candidateId])
    }

    @Override
    boolean hasActiveLike(UUID jobId, UUID candidateId) {
        exists("""
            SELECT 1 FROM recruiter_job_likes
            WHERE job_id = ? AND candidate_id = ? AND removed_at IS NULL
        """, [jobId, candidateId])
    }

    @Override
    boolean hasAcceptedAssignment(UUID recruiterId, UUID jobId) {
        exists("""
            SELECT 1 FROM recruiter_job_invitations
            WHERE recruiter_id = ? AND job_id = ? AND status = 'ACCEPTED'
        """, [recruiterId, jobId])
    }

    @Override
    UUID findCompanyIdByJobId(UUID jobId) {
        GroovyRowResult row = sql.firstRow('SELECT publisher_id FROM jobs WHERE id = ?', [jobId])
        if (!row) throw new EntityNotFoundException("Job with ID ${jobId} not found")
        row.publisher_id as UUID
    }

    private boolean exists(String query, List<Object> params) {
        sql.firstRow(query, params) != null
    }

    private static RecruiterJobLike fromRow(GroovyRowResult row) {
        new RecruiterJobLike(
                recruiterId: row.recruiter_id as UUID,
                jobId: row.job_id as UUID,
                candidateId: row.candidate_id as UUID,
                createdAt: toLocalDateTime(row.created_at),
                removedAt: toLocalDateTime(row.removed_at)
        )
    }

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null
        if (value instanceof LocalDateTime) return value
        if (value instanceof Timestamp) return value.toLocalDateTime()
        LocalDateTime.parse(value.toString())
    }
}
