package service

import groovy.json.JsonOutput
import spock.lang.Specification
import zg.acelera.domain.Recruiter
import zg.acelera.dto.recruiter.RecruiterAuthResponseDTO
import zg.acelera.dto.recruiter.RecruiterCreateDTO
import zg.acelera.dto.recruiter.RecruiterResponseDTO
import zg.acelera.dto.recruiter.RecruiterUpdateDTO

import java.time.LocalDate

class RecruiterValidationSpec extends Specification {
    private static final UUID AREA_ID = UUID.randomUUID()

    def 'normalizes email and cpf before creating the domain'() {
        given:
        def dto = new RecruiterCreateDTO(
                ' Ana Recruiter ',
                AREA_ID,
                ' Talent acquisition ',
                ' ANA@EXAMPLE.COM ',
                '123.456.789-09',
                LocalDate.now().minusYears(30),
                'secret'
        )

        expect:
        dto.email() == 'ana@example.com'
        dto.cpf() == '12345678909'
        dto.toDomain().status.name() == 'ACTIVE'
    }

    def 'rejects invalid recruiter registration data'() {
        when:
        new RecruiterCreateDTO(name, areaId, 'Description', email, cpf, birthDate, password)

        then:
        thrown(IllegalArgumentException)

        where:
        name  | areaId  | email             | cpf           | birthDate                    | password
        null  | AREA_ID | 'ana@example.com' | '12345678909' | LocalDate.now().minusYears(30) | 'secret'
        'Ana' | null    | 'ana@example.com' | '12345678909' | LocalDate.now().minusYears(30) | 'secret'
        'Ana' | AREA_ID | 'invalid-email'   | '12345678909' | LocalDate.now().minusYears(30) | 'secret'
        'Ana' | AREA_ID | 'ana@example.com' | '123'         | LocalDate.now().minusYears(30) | 'secret'
        'Ana' | AREA_ID | 'ana@example.com' | '12345678909' | LocalDate.now().minusYears(17) | 'secret'
        'Ana' | AREA_ID | 'ana@example.com' | '12345678909' | LocalDate.now().minusYears(30) | '12345'
    }

    def 'allows partial updates while validating supplied sensitive fields'() {
        when:
        def dto = new RecruiterUpdateDTO(null, null, null, ' ANA@EXAMPLE.COM ', '123.456.789-09', null, null)

        then:
        dto.email() == 'ana@example.com'
        dto.cpf() == '12345678909'

        when:
        new RecruiterUpdateDTO(null, null, null, null, null, LocalDate.now().minusYears(17), null)

        then:
        thrown(IllegalArgumentException)
    }

    def 'serializable recruiter responses never contain credentials or personal identifiers'() {
        given:
        Recruiter recruiter = new Recruiter(
                id: UUID.randomUUID(),
                name: 'Ana Recruiter',
                email: 'ana@example.com',
                password: 'secret',
                cpf: '12345678909',
                birthDate: LocalDate.of(1990, 1, 1),
                recruitmentAreaId: AREA_ID,
                description: 'Recruiting',
                status: zg.acelera.domain.RecruiterStatus.ACTIVE
        )
        def response = RecruiterResponseDTO.fromDomain(recruiter)
        def authResponse = RecruiterAuthResponseDTO.fromDomain(recruiter)

        expect:
        !JsonOutput.toJson(response).contains('secret')
        !JsonOutput.toJson(response).contains('12345678909')
        !JsonOutput.toJson(response).contains('1990-01-01')
        !JsonOutput.toJson(authResponse).contains('secret')
        !JsonOutput.toJson(authResponse).contains('12345678909')
        !JsonOutput.toJson(authResponse).contains('1990-01-01')
    }

}
