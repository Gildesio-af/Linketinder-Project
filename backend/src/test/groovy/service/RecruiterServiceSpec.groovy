package service

import spock.lang.Specification
import groovy.json.JsonOutput
import zg.acelera.domain.Recruiter
import zg.acelera.domain.RecruiterStatus
import zg.acelera.dto.recruiter.RecruiterCreateDTO
import zg.acelera.dto.recruiter.RecruiterUpdateDTO
import zg.acelera.repository.recruiter.RecruitmentAreaRepository
import zg.acelera.repository.recruiter.RecruiterRepository
import zg.acelera.service.RecruiterService
import zg.acelera.utils.exception.EntityNotFoundException

import java.time.LocalDate

class RecruiterServiceSpec extends Specification {
    RecruiterRepository recruiterRepository = Mock()
    RecruitmentAreaRepository areaRepository = Mock()
    RecruiterService service
    UUID areaId = UUID.randomUUID()
    UUID recruiterId = UUID.randomUUID()

    def setup() {
        service = new RecruiterService(recruiterRepository, areaRepository)
    }

    def "registers an independent recruiter without exposing sensitive fields"() {
        given:
        def dto = createDTO()
        areaRepository.findAll() >> [[id: areaId]]
        recruiterRepository.findByEmail(dto.email()) >> { throw new EntityNotFoundException('not found') }
        recruiterRepository.save(_ as Recruiter) >> new Recruiter(
                id: recruiterId, name: dto.name(), email: dto.email(),
                recruitmentAreaId: areaId, description: dto.description(),
                cpf: dto.cpf(), birthDate: dto.birthDate(), status: RecruiterStatus.ACTIVE)

        when:
        def response = service.register(dto)

        then:
        response.id() == recruiterId
        response.email() == dto.email()
        !JsonOutput.toJson(response).contains('password')
        !JsonOutput.toJson(response).contains(dto.cpf())
        !JsonOutput.toJson(response).contains(dto.birthDate().toString())
    }

    def "rejects an already registered email"() {
        given:
        def dto = createDTO()
        areaRepository.findAll() >> [[id: areaId]]
        recruiterRepository.findByEmail(dto.email()) >> new Recruiter(id: UUID.randomUUID())

        when:
        service.register(dto)

        then:
        def error = thrown(IllegalArgumentException)
        error.message == 'Email is already in use.'
        0 * recruiterRepository.save(_)
    }

    def "updates only the authenticated recruiter's own profile"() {
        given:
        areaRepository.findAll() >> [[id: areaId]]

        when:
        service.update(recruiterId, UUID.randomUUID(), new RecruiterUpdateDTO('Changed', null, null, null, null, null, null))

        then:
        def error = thrown(SecurityException)
        error.message == 'Recruiters may only update their own profile.'
        0 * recruiterRepository._
    }

    def "rejects updates for an inactive account"() {
        given:
        recruiterRepository.findById(recruiterId) >> new Recruiter(id: recruiterId, status: RecruiterStatus.INACTIVE)

        when:
        service.updateProfile(recruiterId, new RecruiterUpdateDTO('Changed', null, null, null, null, null, null))

        then:
        thrown(SecurityException)
        0 * recruiterRepository.update(_, _)
    }

    private RecruiterCreateDTO createDTO() {
        new RecruiterCreateDTO(
                'Ana Recruiter', areaId, 'Talent acquisition', 'ANA@EXAMPLE.COM',
                '123.456.789-09', LocalDate.now().minusYears(30), 'secret')
    }
}
