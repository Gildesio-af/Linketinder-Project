package reposistory

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import spock.lang.Specification
import zg.acelera.domain.Candidate
import zg.acelera.domain.Person
import zg.acelera.domain.Skill
import zg.acelera.repository.candidate.CandidateRepositoryJDBC
import zg.acelera.repository.skill.SkillRepository
import zg.acelera.user_interface.CandidateJobUI
import zg.acelera.utils.exception.EntityNotFoundException

import java.time.LocalDate

class CandidateRepositoryJDBCSpec extends Specification {

    Sql sqlMock
    SkillRepository skillRepositoryMock
    CandidateRepositoryJDBC repository

    UUID candidateId = UUID.randomUUID()
    UUID skillId = UUID.randomUUID()

    def setup() {
        sqlMock = Mock(Sql)
        skillRepositoryMock = Mock(SkillRepository)
        repository = new CandidateRepositoryJDBC(sqlMock, skillRepositoryMock)
    }

    private GroovyRowResult createCandidateRow(UUID id = candidateId) {
        return [
                id         : id,
                name       : "User",
                email      : "user@email.com",
                password   : "senha123",
                description: "A developer",
                cpf        : "12345678900",
                last_name  : "Kamado",
                birth_date : LocalDate.of(2008, 5, 15),
                skill_id   : skillId,
                skill_name : "Java"
        ] as GroovyRowResult
    }

    def "findById should return a candidate when found"() {
        given:
        sqlMock.rows(_, [candidateId]) >> [createCandidateRow()]

        when:
        Person result = repository.findById(candidateId) as Candidate

        then:
        result.id == candidateId
        result.name == "User"
        result.cpf == "12345678900"
        result.lastName == "Kamado"
        result.skills.size() == 1
        result.skills.first().name == "Java"
    }

    def "findById should throw EntityNotFoundException when candidate not found"() {
        given:
        sqlMock.rows(_, [candidateId]) >> []

        when:
        repository.findById(candidateId)

        then:
        thrown(EntityNotFoundException)
    }

    def "findAll should return a list of candidates"() {
        given:
        UUID id1 = UUID.randomUUID()
        UUID id2 = UUID.randomUUID()
        GroovyRowResult row1 = createCandidateRow(id1)
        GroovyRowResult row2 = [
                id: id2, name: "Jane", email: "jane@email.com", password: "pwd",
                description: "Designer", cpf: "98765432100", last_name: "Smith",
                birth_date: LocalDate.of(1990, 1, 1), skill_id: null, skill_name: null
        ] as GroovyRowResult

        sqlMock.rows(_) >> [row1, row2]

        when:
        List<Person> result = repository.findAll()

        then:
        result.size() == 2
    }

    def "findAll should return empty list when no candidates exist"() {
        given:
        sqlMock.rows(_) >> []

        when:
        List<Person> result = repository.findAll()

        then:
        result.isEmpty()
    }

    def "findByCpf should return a candidate when CPF matches"() {
        given:
        String cpf = "12345678900"
        sqlMock.rows(_, [cpf]) >> [createCandidateRow()]

        when:
        Person result = repository.findByCpf(cpf) as Candidate

        then:
        result.cpf == cpf
        result.name == "User"
    }

    def "findByCpf should throw EntityNotFoundException when CPF not found"() {
        given:
        sqlMock.rows(_, ["99999999999"]) >> []

        when:
        repository.findByCpf("99999999999")

        then:
        thrown(EntityNotFoundException)
    }

    def "findBySkill should return candidates that have the given skill"() {
        given:
        sqlMock.rows(_, ["Java"]) >> [createCandidateRow()]

        when:
        List<Person> result = repository.findBySkill("Java")

        then:
        result.size() == 1
    }

    def "findBySkill should return empty list when no candidates match"() {
        given:
        sqlMock.rows(_, ["Rust"]) >> []

        when:
        List<Person> result = repository.findBySkill("Rust")

        then:
        result.isEmpty()
    }

    def "save should insert user and candidate and return saved candidate"() {
        given:
        Candidate candidate = new Candidate(
                name: "John", email: "user@email.com", password: "senha123",
                description: "Dev", cpf: "12345678900", lastName: "Kamado",
                birthDate: LocalDate.of(1995, 5, 15),
                skills: [new Skill(id: skillId, name: "Java")] as Set
        )

        GroovyRowResult userRow = [id: candidateId, name: "John", email: "user@email.com",
                                   password: "senha123", description: "Dev"] as GroovyRowResult
        GroovyRowResult candidateRow = [cpf: "12345678900", last_name: "Kamado",
                                        birth_date: LocalDate.of(2008, 5, 15)] as GroovyRowResult

        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }
        sqlMock.firstRow(_, { it[0] == "John" }) >> userRow
        sqlMock.firstRow(_, { it[0] == candidateId }) >> candidateRow

        skillRepositoryMock.findByUserId(candidateId) >> ([new Skill(id: skillId, name: "Java")] as Set)

        when:
        Candidate result = repository.save(candidate)

        then:
        result != null
        result.name == "John"
        result.cpf == "12345678900"
    }

    def "delete should call findById and execute delete queries"() {
        given:
        sqlMock.rows(_, [candidateId]) >> [createCandidateRow()]
        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }

        when:
        repository.delete(candidateId)

        then:
        1 * sqlMock.execute("DELETE FROM candidates WHERE user_id = ?", [candidateId])
        1 * sqlMock.execute("DELETE FROM users WHERE id = ?", [candidateId])
    }

    def "delete should throw EntityNotFoundException when candidate does not exist"() {
        given:
        sqlMock.rows(_, [candidateId]) >> []

        when:
        repository.delete(candidateId)

        then:
        thrown(EntityNotFoundException)
    }

    def "update should update user and candidate and return updated candidate"() {
        given:
        Candidate candidateToUpdate = new Candidate(
                name: "John Updated", email: "user_new@email.com",
                description: "Senior Dev", lastName: "Kamado Updated",
                birthDate: LocalDate.of(2009, 6, 20)
        )

        GroovyRowResult userRow = [id: candidateId, name: "John Updated", email: "user_new@email.com",
                                   password: "senha123", description: "Senior Dev"] as GroovyRowResult
        GroovyRowResult candidateRow = [cpf: "12345678900", last_name: "Kamado Updated",
                                        birth_date: LocalDate.of(2009, 6, 20)] as GroovyRowResult

        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }
        sqlMock.firstRow({ it.contains("UPDATE users") }, _) >> userRow
        sqlMock.firstRow({ it.contains("UPDATE candidates") }, _) >> candidateRow

        skillRepositoryMock.findByUserId(candidateId) >> ([] as Set)

        when:
        Candidate result = repository.update(candidateToUpdate, candidateId)

        then:
        result != null
        result.name == "John Updated"
        result.lastName == "Kamado Updated"
        result.birthDate == LocalDate.of(2009, 6, 20)
    }
}
