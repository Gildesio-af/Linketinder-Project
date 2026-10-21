package service

import spock.lang.Specification
import zg.acelera.domain.Candidate
import zg.acelera.domain.Company
import zg.acelera.domain.Match
import zg.acelera.domain.Recruiter
import zg.acelera.dto.auth.AuthResponseDTO
import zg.acelera.dto.match.AuthorizedMatchResponseDTO
import zg.acelera.repository.candidate.CandidateRepository
import zg.acelera.repository.company.CompanyRepository
import zg.acelera.repository.match.MatchRepository
import zg.acelera.repository.recruiter.RecruiterJobLikeRepository
import zg.acelera.repository.recruiter.RecruiterRepository
import zg.acelera.service.MatchService

class AuthorizedMatchVisibilitySpec extends Specification {
    MatchRepository matchRepository = Mock()
    CandidateRepository candidateRepository = Mock()
    CompanyRepository companyRepository = Mock()
    RecruiterRepository recruiterRepository = Mock()
    RecruiterJobLikeRepository recruiterJobLikeRepository = Mock()
    MatchService service

    UUID candidateId = UUID.randomUUID()
    UUID companyId = UUID.randomUUID()
    UUID jobId = UUID.randomUUID()
    UUID recruiterId = UUID.randomUUID()

    def setup() {
        service = new MatchService(matchRepository, candidateRepository, companyRepository,
                recruiterRepository, recruiterJobLikeRepository)
    }

    def "candidate can access an owned match and receives only approved recruiter fields"() {
        given:
        matchRepository.findMatchByCandidateId(candidateId) >> [match()]
        candidateRepository.findById(candidateId) >> new Candidate(id: candidateId, name: 'Candidate',
                email: 'candidate@example.test', cpf: '11111111111',
                birthDate: java.time.LocalDate.of(1990, 1, 1), password: 'candidate-secret')
        companyRepository.findById(companyId) >> new Company(id: companyId, name: 'Acme',
                email: 'company@example.test')
        recruiterJobLikeRepository.existsActive(recruiterId, jobId, candidateId) >> true
        recruiterJobLikeRepository.hasAcceptedAssignment(recruiterId, jobId) >> true
        recruiterRepository.findById(recruiterId) >> new Recruiter(id: recruiterId,
                email: 'recruiter@example.test', password: 'recruiter-secret',
                cpf: '22222222222', birthDate: java.time.LocalDate.of(1988, 1, 1),
                recruitmentAreaId: UUID.randomUUID(), description: 'Technology hiring')

        when:
        AuthorizedMatchResponseDTO result = service.getAuthorizedMatch(candidateId, jobId, candidateId,
                AuthResponseDTO.CANDIDATE_ROLE, recruiterId)

        then:
        result.candidateName() == 'Candidate'
        result.companyName() == 'Acme'
        result.recruiterDescription() == 'Technology hiring'
        result.recruiterId() == recruiterId
        !result.properties.containsKey('candidateCpf')
        !result.properties.containsKey('recruiterEmail')
        !result.properties.containsKey('recruiterCpf')
        !result.properties.containsKey('recruiterBirthDate')
        !result.properties.containsKey('recruiterPassword')
    }

    def "unrelated candidate is rejected before sensitive records are loaded"() {
        given:
        matchRepository.findMatchByCandidateId(candidateId) >> [match()]

        when:
        service.getAuthorizedMatch(candidateId, jobId, UUID.randomUUID(), AuthResponseDTO.CANDIDATE_ROLE)

        then:
        thrown(SecurityException)
        0 * candidateRepository.findById(_)
        0 * companyRepository.findById(_)
        0 * recruiterRepository._
    }

    def "revoked recruiter cannot access a match"() {
        given:
        matchRepository.findMatchByCandidateId(candidateId) >> [match()]
        recruiterJobLikeRepository.existsActive(recruiterId, jobId, candidateId) >> false

        when:
        service.getAuthorizedMatch(candidateId, jobId, recruiterId, AuthResponseDTO.RECRUITER_ROLE, recruiterId)

        then:
        thrown(SecurityException)
        0 * candidateRepository.findById(_)
        0 * companyRepository.findById(_)
    }

    private Match match() {
        new Match(id: UUID.randomUUID(), candidateId: candidateId, companyId: companyId, jobId: jobId)
    }
}
