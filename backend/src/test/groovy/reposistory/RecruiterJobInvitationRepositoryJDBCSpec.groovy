package reposistory

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import spock.lang.Specification
import zg.acelera.domain.RecruiterJobInvitation
import zg.acelera.repository.recruiter.RecruiterJobInvitationRepositoryJDBC
import zg.acelera.utils.exception.EntityNotFoundException

import java.time.LocalDateTime

class RecruiterJobInvitationRepositoryJDBCSpec extends Specification {
    Sql sqlMock = Mock()
    RecruiterJobInvitationRepositoryJDBC repository = new RecruiterJobInvitationRepositoryJDBC(sqlMock)

    UUID invitationId = UUID.randomUUID()
    UUID recruiterId = UUID.randomUUID()
    UUID companyId = UUID.randomUUID()
    UUID jobId = UUID.randomUUID()

    private GroovyRowResult row(String status = 'PENDING') {
        [
                id           : invitationId,
                recruiter_id : recruiterId,
                company_id   : companyId,
                job_id       : jobId,
                status       : status,
                created_at   : LocalDateTime.of(2026, 1, 1, 0, 0),
                responded_at : status == 'PENDING' ? null : LocalDateTime.of(2026, 1, 2, 0, 0),
                revoked_at   : status == 'REVOKED' ? LocalDateTime.of(2026, 1, 3, 0, 0) : null
        ] as GroovyRowResult
    }

    def "creates a pending invitation atomically"() {
        given:
        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }
        sqlMock.firstRow({ it.contains('status IN') }, [recruiterId, jobId]) >> null
        sqlMock.firstRow({ it.contains('INSERT INTO recruiter_job_invitations') }, [recruiterId, companyId, jobId]) >> row()

        when:
        def result = repository.create(new RecruiterJobInvitation(
                recruiterId: recruiterId, companyId: companyId, jobId: jobId))

        then:
        result.status == RecruiterJobInvitation.Status.PENDING
        result.recruiterId == recruiterId
    }

    def "returns the existing active invitation instead of duplicating it"() {
        given:
        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }
        sqlMock.firstRow({ it.contains('status IN') }, [recruiterId, jobId]) >> row('ACCEPTED')

        when:
        def result = repository.create(new RecruiterJobInvitation(
                recruiterId: recruiterId, companyId: companyId, jobId: jobId))

        then:
        result.status == RecruiterJobInvitation.Status.ACCEPTED
        0 * sqlMock.firstRow({ it.contains('INSERT INTO recruiter_job_invitations') }, _)
    }

    def "accepts only a pending invitation for the specified recruiter"() {
        given:
        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }
        sqlMock.firstRow({ it.contains('status = ?') && it.contains('recruiter_id') }, ['ACCEPTED', invitationId, recruiterId]) >> row('ACCEPTED')

        when:
        def result = repository.accept(invitationId, recruiterId)

        then:
        result.status == RecruiterJobInvitation.Status.ACCEPTED
    }

    def "rejects a transition that is not authorized or no longer valid"() {
        given:
        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }
        sqlMock.firstRow(_, _) >> null

        when:
        repository.revoke(invitationId, UUID.randomUUID())

        then:
        thrown(IllegalStateException)
    }

    def "findById reports a missing invitation"() {
        given:
        sqlMock.firstRow(_, [invitationId]) >> null

        when:
        repository.findById(invitationId)

        then:
        thrown(EntityNotFoundException)
    }
}
