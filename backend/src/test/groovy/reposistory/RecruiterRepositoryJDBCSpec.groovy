package reposistory

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import spock.lang.Specification
import zg.acelera.domain.Recruiter
import zg.acelera.domain.RecruiterStatus
import zg.acelera.repository.recruiter.RecruiterRepositoryJDBC
import zg.acelera.utils.exception.EntityNotFoundException

import java.time.LocalDate

class RecruiterRepositoryJDBCSpec extends Specification {
    Sql sqlMock
    RecruiterRepositoryJDBC repository

    UUID recruiterId = UUID.randomUUID()
    UUID areaId = UUID.randomUUID()

    def setup() {
        sqlMock = Mock(Sql)
        repository = new RecruiterRepositoryJDBC(sqlMock)
    }

    private GroovyRowResult recruiterRow(UUID id = recruiterId, String status = 'ACTIVE') {
        [
                id                  : id,
                name                : 'Ana Recruiter',
                email               : 'ana@example.com',
                description         : 'Recruitment profile',
                recruitment_area_id : areaId,
                cpf                 : '12345678901',
                birth_date          : '1990-01-01',
                status              : status
        ] as GroovyRowResult
    }

    def "findById returns a recruiter without exposing password"() {
        given:
        sqlMock.firstRow(_, [recruiterId]) >> recruiterRow()

        when:
        Recruiter result = repository.findById(recruiterId)

        then:
        result.id == recruiterId
        result.recruitmentAreaId == areaId
        result.password == null
        result.status == RecruiterStatus.ACTIVE
    }

    def "findByEmail uses a parameterized case-insensitive lookup"() {
        given:
        sqlMock.firstRow(_, ['ANA@EXAMPLE.COM']) >> recruiterRow()

        when:
        Recruiter result = repository.findByEmail('ANA@EXAMPLE.COM')

        then:
        result.email == 'ana@example.com'
    }

    def "findById throws when recruiter does not exist"() {
        given:
        sqlMock.firstRow(_, [recruiterId]) >> null

        when:
        repository.findById(recruiterId)

        then:
        thrown(EntityNotFoundException)
    }

    def "save creates the global user and recruiter profile in one transaction"() {
        given:
        Recruiter recruiter = new Recruiter(
                name: 'Ana Recruiter',
                email: 'ana@example.com',
                password: 'secret123',
                description: 'Recruitment profile',
                recruitmentAreaId: areaId,
                cpf: '12345678901',
                birthDate: LocalDate.of(1990, 1, 1)
        )
        GroovyRowResult userRow = [
                id: recruiterId, name: recruiter.name, email: recruiter.email,
                description: recruiter.description
        ] as GroovyRowResult
        GroovyRowResult profileRow = [
                recruitment_area_id: areaId, cpf: recruiter.cpf,
                birth_date: '1990-01-01', status: 'ACTIVE'
        ] as GroovyRowResult

        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }
        sqlMock.firstRow({ it.contains('INSERT INTO users') }, _) >> userRow
        sqlMock.firstRow({ it.contains('INSERT INTO recruiters') }, _) >> profileRow

        when:
        Recruiter result = repository.save(recruiter)

        then:
        result.id == recruiterId
        result.password == null
        result.status == RecruiterStatus.ACTIVE
    }

    def "updateStatus rejects null status"() {
        when:
        repository.updateStatus(recruiterId, null)

        then:
        thrown(IllegalArgumentException)
    }

    def "findByEmail throws when recruiter does not exist"() {
        given:
        sqlMock.firstRow(_, ['missing@example.com']) >> null

        when:
        repository.findByEmail('missing@example.com')

        then:
        thrown(EntityNotFoundException)
    }
}
