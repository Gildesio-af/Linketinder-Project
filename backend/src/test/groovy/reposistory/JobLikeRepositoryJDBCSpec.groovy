package reposistory

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import spock.lang.Specification
import zg.acelera.repository.like.JobLikeRepositoryJDBC

class JobLikeRepositoryJDBCSpec extends Specification {

    Sql sqlMock
    JobLikeRepositoryJDBC repository

    UUID jobId = UUID.randomUUID()
    UUID candidateId = UUID.randomUUID()

    def setup() {
        sqlMock = Mock(Sql)
        repository = new JobLikeRepositoryJDBC(sqlMock)
    }

    def "like should insert job id and candidate id in this order"() {
        when:
        repository.like(jobId, candidateId)

        then:
        1 * sqlMock.executeInsert('INSERT INTO jobs_like (job_id, candidate_id) VALUES (?, ?)', [jobId, candidateId])
    }

    def "unlike should delete by job id and candidate id"() {
        when:
        repository.unlike(jobId, candidateId)

        then:
        1 * sqlMock.executeUpdate('DELETE FROM jobs_like WHERE job_id = ? AND candidate_id = ?', [jobId, candidateId])
    }

    def "existsLike should return #expected when query returns #liked"() {
        when:
        boolean result = repository.existsLike(jobId, candidateId)

        then:
        1 * sqlMock.firstRow({ it.toString().contains("FROM jobs_like") }, [jobId, candidateId]) >> ([liked: liked] as GroovyRowResult)
        result == expected

        where:
        liked || expected
        true  || true
        false || false
    }
}
