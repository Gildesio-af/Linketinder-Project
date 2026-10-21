package service

import spock.lang.Specification
import zg.acelera.domain.Candidate
import zg.acelera.domain.Company
import zg.acelera.domain.Recruiter
import zg.acelera.domain.RecruiterStatus
import zg.acelera.dto.auth.AuthRequestDTO
import zg.acelera.dto.auth.AuthResponseDTO
import zg.acelera.repository.candidate.CandidateRepository
import zg.acelera.repository.company.CompanyRepository
import zg.acelera.repository.recruiter.RecruiterRepository
import zg.acelera.service.AuthService

class AuthServiceSpec extends Specification {

    CandidateRepository candidateRepositoryMock
    CompanyRepository companyRepositoryMock
    RecruiterRepository recruiterRepositoryMock
    AuthService service

    UUID candidateId = UUID.randomUUID()
    UUID companyId = UUID.randomUUID()
    String cpf = "12345678901"
    String cnpj = "12345678000199"

    def setup() {
        candidateRepositoryMock = Mock(CandidateRepository)
        companyRepositoryMock = Mock(CompanyRepository)
        recruiterRepositoryMock = Mock(RecruiterRepository)
        service = new AuthService(candidateRepositoryMock, companyRepositoryMock)
    }

    def "authenticate should return CANDIDATE role for valid CPF and password"() {
        when:
        AuthResponseDTO result = service.authenticate(new AuthRequestDTO(cpf, "secret"))

        then:
        1 * candidateRepositoryMock.findByCpf(cpf) >> new Candidate(id: candidateId, cpf: cpf, password: "secret")
        0 * companyRepositoryMock._
        result.id() == candidateId
        result.role() == "CANDIDATE"
    }

    def "authenticate should return COMPANY role for valid CNPJ and password"() {
        when:
        AuthResponseDTO result = service.authenticate(new AuthRequestDTO(cnpj, "secret"))

        then:
        1 * companyRepositoryMock.findByCnpj(cnpj) >> new Company(id: companyId, cnpj: cnpj, password: "secret")
        0 * candidateRepositoryMock._
        result.id() == companyId
        result.role() == "COMPANY"
    }

    def "authenticate should throw when candidate password is wrong"() {
        given:
        candidateRepositoryMock.findByCpf(cpf) >> new Candidate(id: candidateId, cpf: cpf, password: "secret")

        when:
        service.authenticate(new AuthRequestDTO(cpf, "wrong"))

        then:
        IllegalArgumentException e = thrown()
        e.message == "Invalid username or password"
    }

    def "authenticate should throw when candidate is not found"() {
        given:
        candidateRepositoryMock.findByCpf(cpf) >> null

        when:
        service.authenticate(new AuthRequestDTO(cpf, "secret"))

        then:
        IllegalArgumentException e = thrown()
        e.message == "Invalid username or password"
    }

    def "authenticate should throw when company password is wrong"() {
        given:
        companyRepositoryMock.findByCnpj(cnpj) >> new Company(id: companyId, cnpj: cnpj, password: "secret")

        when:
        service.authenticate(new AuthRequestDTO(cnpj, "wrong"))

        then:
        IllegalArgumentException e = thrown()
        e.message == "Invalid username or password"
    }

    def "authenticate should throw when company is not found"() {
        given:
        companyRepositoryMock.findByCnpj(cnpj) >> null

        when:
        service.authenticate(new AuthRequestDTO(cnpj, "secret"))

        then:
        IllegalArgumentException e = thrown()
        e.message == "Invalid username or password"
    }

    def "authenticate should throw for usernames that are neither CPF nor CNPJ length: '#username'"() {
        when:
        service.authenticate(new AuthRequestDTO(username, "secret"))

        then:
        IllegalArgumentException e = thrown()
        e.message == "Invalid username format"
        0 * candidateRepositoryMock._
        0 * companyRepositoryMock._

        where:
        username << ["", "123", "1234567890", "123456789012", "123456789012345"]
    }

    def "authenticate should return RECRUITER role for an active recruiter email"() {
        given:
        def recruiter = new Recruiter(id: candidateId, email: 'ana@example.com',
                password: 'secret', status: RecruiterStatus.ACTIVE)
        service = new AuthService(candidateRepositoryMock, companyRepositoryMock, recruiterRepositoryMock)
        recruiterRepositoryMock.findByEmail('ana@example.com') >> recruiter

        when:
        def result = service.authenticate(new AuthRequestDTO(' ANA@EXAMPLE.COM ', 'secret'))

        then:
        result.id() == candidateId
        result.role() == 'RECRUITER'
    }

    def "authenticate should reject an inactive recruiter"() {
        given:
        service = new AuthService(candidateRepositoryMock, companyRepositoryMock, recruiterRepositoryMock)
        recruiterRepositoryMock.findByEmail('ana@example.com') >> new Recruiter(
                id: candidateId, email: 'ana@example.com', password: 'secret', status: RecruiterStatus.INACTIVE)

        when:
        service.authenticate(new AuthRequestDTO('ana@example.com', 'secret'))

        then:
        def error = thrown(IllegalArgumentException)
        error.message == 'Invalid username or password'
    }
}
