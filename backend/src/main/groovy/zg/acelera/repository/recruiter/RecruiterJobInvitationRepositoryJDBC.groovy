package zg.acelera.repository.recruiter

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.RecruiterJobInvitation
import zg.acelera.utils.exception.EntityNotFoundException

import java.sql.Timestamp
import java.time.LocalDateTime

class RecruiterJobInvitationRepositoryJDBC implements RecruiterJobInvitationRepository {
    final Sql sql

    RecruiterJobInvitationRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    RecruiterJobInvitation create(RecruiterJobInvitation invitation) {
        RecruiterJobInvitation result = null
        sql.withTransaction {
            GroovyRowResult existing = sql.firstRow("""
                SELECT id, recruiter_id, company_id, job_id, status,
                       created_at, responded_at, revoked_at
                FROM recruiter_job_invitations
                WHERE recruiter_id = ? AND job_id = ?
                  AND status IN ('PENDING', 'ACCEPTED')
                FOR UPDATE
            """, [invitation.recruiterId, invitation.jobId])

            if (existing) {
                result = fromRow(existing)
            } else {
                GroovyRowResult row = sql.firstRow("""
                    INSERT INTO recruiter_job_invitations
                        (recruiter_id, company_id, job_id, status)
                    VALUES (?, ?, ?, 'PENDING')
                    RETURNING id, recruiter_id, company_id, job_id, status,
                              created_at, responded_at, revoked_at
                """, [invitation.recruiterId, invitation.companyId, invitation.jobId])
                if (!row)
                    throw new IllegalStateException('Invitation could not be created.')
                result = fromRow(row)
            }
        }
        result
    }

    @Override
    RecruiterJobInvitation findById(UUID invitationId) {
        GroovyRowResult row = sql.firstRow(selectSql() + ' WHERE i.id = ?', [invitationId])
        if (!row)
            throw new EntityNotFoundException("Invitation with ID ${invitationId} not found")
        fromRow(row)
    }

    @Override
    List<RecruiterJobInvitation> findByRecruiterId(UUID recruiterId) {
        rows(selectSql() + ' WHERE i.recruiter_id = ? ORDER BY i.created_at DESC', [recruiterId])
    }

    @Override
    List<RecruiterJobInvitation> findByCompanyId(UUID companyId) {
        rows(selectSql() + ' WHERE i.company_id = ? ORDER BY i.created_at DESC', [companyId])
    }

    @Override
    RecruiterJobInvitation accept(UUID invitationId, UUID recruiterId) {
        transition(invitationId, 'ACCEPTED', 'responded_at = CURRENT_TIMESTAMP',
                'id = ? AND recruiter_id = ? AND status = \'PENDING\'', [invitationId, recruiterId])
    }

    @Override
    RecruiterJobInvitation decline(UUID invitationId, UUID recruiterId) {
        transition(invitationId, 'DECLINED', 'responded_at = CURRENT_TIMESTAMP',
                'id = ? AND recruiter_id = ? AND status = \'PENDING\'', [invitationId, recruiterId])
    }

    @Override
    RecruiterJobInvitation cancel(UUID invitationId, UUID companyId) {
        transition(invitationId, 'CANCELLED', 'responded_at = CURRENT_TIMESTAMP',
                'id = ? AND company_id = ? AND status = \'PENDING\'', [invitationId, companyId])
    }

    @Override
    RecruiterJobInvitation revoke(UUID invitationId, UUID companyId) {
        transition(invitationId, 'REVOKED', 'revoked_at = CURRENT_TIMESTAMP',
                'id = ? AND company_id = ? AND status = \'ACCEPTED\'', [invitationId, companyId])
    }

    private RecruiterJobInvitation transition(UUID invitationId, String status, String timestampAssignment,
                                               String predicate, List<Object> predicateParams) {
        GroovyRowResult row
        sql.withTransaction {
            row = sql.firstRow("""
                UPDATE recruiter_job_invitations
                SET status = ?, ${timestampAssignment}
                WHERE ${predicate}
                RETURNING id, recruiter_id, company_id, job_id, status,
                          created_at, responded_at, revoked_at
            """, [status] + predicateParams)
        }
        if (!row)
            throw new IllegalStateException("Invitation ${invitationId} cannot transition to ${status}.")
        fromRow(row)
    }

    private List<RecruiterJobInvitation> rows(String query, List<Object> params) {
        sql.rows(query, params).collect { GroovyRowResult row -> fromRow(row) }
    }

    private String selectSql() {
        """
            SELECT i.id, i.recruiter_id, i.company_id, i.job_id, i.status,
                   i.created_at, i.responded_at, i.revoked_at
            FROM recruiter_job_invitations i
        """
    }

    private static RecruiterJobInvitation fromRow(GroovyRowResult row) {
        new RecruiterJobInvitation(
                id: row.id as UUID,
                recruiterId: row.recruiter_id as UUID,
                companyId: row.company_id as UUID,
                jobId: row.job_id as UUID,
                status: RecruiterJobInvitation.Status.valueOf(row.status.toString()),
                createdAt: toLocalDateTime(row.created_at),
                respondedAt: toLocalDateTime(row.responded_at),
                revokedAt: toLocalDateTime(row.revoked_at)
        )
    }

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null
        if (value instanceof LocalDateTime) return value
        if (value instanceof Timestamp) return value.toLocalDateTime()
        LocalDateTime.parse(value.toString())
    }
}
