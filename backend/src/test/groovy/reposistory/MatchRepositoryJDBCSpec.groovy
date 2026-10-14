package reposistory

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import spock.lang.Specification
import zg.acelera.domain.Match
import zg.acelera.repository.match.MatchRepositoryJDBC

import java.sql.Timestamp
import java.time.LocalDateTime

class MatchRepositoryJDBCSpec extends Specification {

    Sql sqlMock
    MatchRepositoryJDBC repository

    UUID matchId = UUID.randomUUID()
    UUID candidateId = UUID.randomUUID()
    UUID companyId = UUID.randomUUID()
    UUID jobId = UUID.randomUUID()
    LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 30)

    def setup() {
        sqlMock = Mock(Sql)
        repository = new MatchRepositoryJDBC(sqlMock)
    }

    private GroovyRowResult createMatchRow(Timestamp created = Timestamp.valueOf(createdAt)) {
        return [
                id          : matchId,
                candidate_id: candidateId,
                company_id  : companyId,
                job_id      : jobId,
                created_at  : created
        ] as GroovyRowResult
    }

    def "createMatch should insert and map the returned row"() {
        given:
        Match match = new Match(candidateId: candidateId, companyId: companyId, jobId: jobId)

        when:
        Match result = repository.createMatch(match)

        then:
        1 * sqlMock.firstRow({ it.toString().contains("INSERT INTO matches") }, [candidateId, companyId, jobId]) >> createMatchRow()
        result.id == matchId
        result.candidateId == candidateId
        result.companyId == companyId
        result.jobId == jobId
        result.createdAt == createdAt
    }

    def "createMatch should map null created_at to null"() {
        given:
        sqlMock.firstRow(_, _) >> createMatchRow(null)

        when:
        Match result = repository.createMatch(new Match(candidateId: candidateId, companyId: companyId, jobId: jobId))

        then:
        result.createdAt == null
    }

    def "findMatchByCandidateId should map all rows"() {
        when:
        List<Match> result = repository.findMatchByCandidateId(candidateId)

        then:
        1 * sqlMock.rows({ it.toString().contains("candidate_id = ?") }, [candidateId]) >> [createMatchRow(), createMatchRow()]
        result.size() == 2
        result.every { it.candidateId == candidateId }
    }

    def "findMatchByCompanyId should map all rows"() {
        when:
        List<Match> result = repository.findMatchByCompanyId(companyId)

        then:
        1 * sqlMock.rows({ it.toString().contains("company_id = ?") }, [companyId]) >> [createMatchRow()]
        result*.companyId == [companyId]
    }

    def "findMatchByJobId should map all rows"() {
        when:
        List<Match> result = repository.findMatchByJobId(jobId)

        then:
        1 * sqlMock.rows({ it.toString().contains("job_id = ?") }, [jobId]) >> [createMatchRow()]
        result*.jobId == [jobId]
    }

    def "find methods should return empty list when there are no rows"() {
        given:
        sqlMock.rows(_, _) >> []

        expect:
        repository.findMatchByCandidateId(candidateId).isEmpty()
        repository.findMatchByCompanyId(companyId).isEmpty()
        repository.findMatchByJobId(jobId).isEmpty()
    }

    def "existsMatch should return #expected when query returns #matched"() {
        when:
        boolean result = repository.existsMatch(candidateId, jobId)

        then:
        1 * sqlMock.firstRow(_, [candidateId, jobId]) >> ([matched: matched] as GroovyRowResult)
        result == expected

        where:
        matched || expected
        true    || true
        false   || false
    }

    def "deleteMatchByCandidateAndJob should execute delete with candidate and job ids"() {
        when:
        repository.deleteMatchByCandidateAndJob(candidateId, jobId)

        then:
        1 * sqlMock.executeUpdate('DELETE FROM matches WHERE candidate_id = ? AND job_id = ?', [candidateId, jobId])
    }

    def "deleteMatchesByCandidateAndCompany should execute delete with candidate and company ids"() {
        when:
        repository.deleteMatchesByCandidateAndCompany(candidateId, companyId)

        then:
        1 * sqlMock.executeUpdate('DELETE FROM matches WHERE candidate_id = ? AND company_id = ?', [candidateId, companyId])
    }
}
