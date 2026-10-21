package reposistory

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import spock.lang.Specification
import zg.acelera.domain.RecruiterJobLike
import zg.acelera.repository.recruiter.RecruiterJobLikeRepositoryJDBC

import java.sql.Timestamp
import java.time.LocalDateTime

class RecruiterJobLikeRepositoryJDBCSpec extends Specification {
    Sql sqlMock
    RecruiterJobLikeRepositoryJDBC repository
    UUID recruiterId = UUID.randomUUID()
    UUID jobId = UUID.randomUUID()
    UUID candidateId = UUID.randomUUID()
    UUID companyId = UUID.randomUUID()
    LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 30)

    def setup() {
        sqlMock = Mock(Sql)
        repository = new RecruiterJobLikeRepositoryJDBC(sqlMock)
    }

    private GroovyRowResult row(Object removedAt = null) {
        [
                recruiter_id: recruiterId, job_id: jobId, candidate_id: candidateId,
                created_at: Timestamp.valueOf(createdAt), removed_at: removedAt
        ] as GroovyRowResult
    }

    def "like inserts or reactivates the same audit row"() {
        when:
        RecruiterJobLike result = repository.like(recruiterId, jobId, candidateId)

        then:
        1 * sqlMock.firstRow({ it.contains('ON CONFLICT') }, [recruiterId, jobId, candidateId]) >> row()
        result.status == RecruiterJobLike.Status.ACTIVE
        result.recruiterId == recruiterId
    }

    def "unlike records removed_at and preserves the row"() {
        given:
        Timestamp removedAt = Timestamp.valueOf(createdAt.plusDays(1))

        when:
        RecruiterJobLike result = repository.unlike(recruiterId, jobId, candidateId)

        then:
        1 * sqlMock.firstRow({ it.contains('SET removed_at') }, [recruiterId, jobId, candidateId]) >> row(removedAt)
        result.status == RecruiterJobLike.Status.REMOVED
        result.removedAt == removedAt.toLocalDateTime()
    }

    def "queries active assignments and likes without loading personal data"() {
        when:
        boolean assigned = repository.hasAcceptedAssignment(recruiterId, jobId)
        boolean active = repository.hasActiveLike(jobId, candidateId)

        then:
        1 * sqlMock.firstRow(_, [recruiterId, jobId]) >> ([value: 1] as GroovyRowResult)
        1 * sqlMock.firstRow(_, [jobId, candidateId]) >> ([value: 1] as GroovyRowResult)
        assigned
        active
    }

    def "finds the job publisher for match creation"() {
        when:
        UUID result = repository.findCompanyIdByJobId(jobId)

        then:
        1 * sqlMock.firstRow(_, [jobId]) >> ([publisher_id: companyId] as GroovyRowResult)
        result == companyId
    }
}
