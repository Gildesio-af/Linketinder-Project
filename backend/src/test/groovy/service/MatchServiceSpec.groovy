package service

import spock.lang.Specification
import zg.acelera.domain.Match
import zg.acelera.dto.match.MatchCreateDTO
import zg.acelera.dto.match.MatchResponseDTO
import zg.acelera.repository.match.MatchRepository
import zg.acelera.service.MatchService

class MatchServiceSpec extends Specification {

    MatchRepository repositoryMock
    MatchService service

    UUID matchId = UUID.randomUUID()
    UUID candidateId = UUID.randomUUID()
    UUID companyId = UUID.randomUUID()
    UUID jobId = UUID.randomUUID()

    def setup() {
        repositoryMock = Mock(MatchRepository)
        service = new MatchService(repositoryMock)
    }

    private Match createMatch() {
        return new Match(id: matchId, candidateId: candidateId, companyId: companyId, jobId: jobId)
    }

    def "createMatch should persist and return the match when it does not exist"() {
        given:
        MatchCreateDTO dto = new MatchCreateDTO(candidateId, companyId, jobId, null)

        when:
        MatchResponseDTO result = service.createMatch(dto)

        then:
        1 * repositoryMock.existsMatch(candidateId, jobId) >> false
        1 * repositoryMock.createMatch({ Match m ->
            m.candidateId == candidateId && m.companyId == companyId && m.jobId == jobId
        }) >> createMatch()
        result.id() == matchId
        result.candidateId() == candidateId
        result.jobId() == jobId
    }

    def "createMatch should not persist when the match already exists"() {
        when:
        MatchResponseDTO result = service.createMatch(new MatchCreateDTO(candidateId, companyId, jobId, null))

        then:
        1 * repositoryMock.existsMatch(candidateId, jobId) >> true
        0 * repositoryMock.createMatch(_)
        result == null
    }

    def "getMatchesByCandidate should map matches to DTOs"() {
        given:
        repositoryMock.findMatchByCandidateId(candidateId) >> [createMatch()]

        when:
        List<MatchResponseDTO> result = service.getMatchesByCandidate(candidateId)

        then:
        result.size() == 1
        result[0].id() == matchId
    }

    def "getMatchesByCompany should map matches to DTOs"() {
        given:
        repositoryMock.findMatchByCompanyId(companyId) >> [createMatch()]

        when:
        List<MatchResponseDTO> result = service.getMatchesByCompany(companyId)

        then:
        result*.companyId() == [companyId]
    }

    def "getMatchesByJob should map matches to DTOs"() {
        given:
        repositoryMock.findMatchByJobId(jobId) >> [createMatch()]

        when:
        List<MatchResponseDTO> result = service.getMatchesByJob(jobId)

        then:
        result.size() == 1
        result[0].jobId() == jobId
        result[0].candidateId() == candidateId
    }

    def "getMatchesBy* should return empty list when repository has no matches"() {
        given:
        repositoryMock.findMatchByCandidateId(candidateId) >> []
        repositoryMock.findMatchByCompanyId(companyId) >> []
        repositoryMock.findMatchByJobId(jobId) >> []

        expect:
        service.getMatchesByCandidate(candidateId).isEmpty()
        service.getMatchesByCompany(companyId).isEmpty()
        service.getMatchesByJob(jobId).isEmpty()
    }

    def "createMatch should propagate createdAt returned by repository"() {
        given:
        def createdAt = java.time.LocalDateTime.of(2026, 1, 1, 10, 0)
        Match persisted = createMatch()
        persisted.createdAt = createdAt
        repositoryMock.existsMatch(candidateId, jobId) >> false
        repositoryMock.createMatch(_ as Match) >> persisted

        when:
        MatchResponseDTO result = service.createMatch(new MatchCreateDTO(candidateId, companyId, jobId, null))

        then:
        result.createdAt() == createdAt
        result.companyId() == companyId
    }

    def "deleteMatchByCandidateAndJob should delegate to repository"() {
        when:
        service.deleteMatchByCandidateAndJob(candidateId, jobId)

        then:
        1 * repositoryMock.deleteMatchByCandidateAndJob(candidateId, jobId)
    }

    def "deleteMatchesByCandidateAndCompany should delegate to repository"() {
        when:
        service.deleteMatchesByCandidateAndCompany(candidateId, companyId)

        then:
        1 * repositoryMock.deleteMatchesByCandidateAndCompany(candidateId, companyId)
    }
}
