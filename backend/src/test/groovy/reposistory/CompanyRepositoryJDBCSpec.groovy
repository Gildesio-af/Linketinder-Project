package reposistory

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import spock.lang.Specification
import zg.acelera.domain.Company
import zg.acelera.domain.Person
import zg.acelera.domain.Skill
import zg.acelera.repository.company.CompanyRepositoryJDBC
import zg.acelera.repository.skill.SkillRepository
import zg.acelera.utils.exception.EntityNotFoundException

class CompanyRepositoryJDBCSpec extends Specification {

    Sql sqlMock
    SkillRepository skillRepositoryMock
    CompanyRepositoryJDBC repository

    UUID companyId = UUID.randomUUID()
    UUID skillId = UUID.randomUUID()

    def setup() {
        sqlMock = Mock(Sql)
        skillRepositoryMock = Mock(SkillRepository)
        repository = new CompanyRepositoryJDBC(sqlMock, skillRepositoryMock)
    }

    private GroovyRowResult createCompanyRow(UUID id = companyId) {
        return [
                id         : id,
                name       : "Umbrella Corp",
                email      : "umbrella@corp.com",
                password   : "secret123",
                description: "A tech company",
                cnpj       : "12345678000199",
                skill_id   : skillId,
                skill_name : "Java"
        ] as GroovyRowResult
    }

    def "findById should return a company when found"() {
        given:
        sqlMock.rows(_, [companyId]) >> [createCompanyRow()]

        when:
        Person result = repository.findById(companyId) as Company

        then:
        result.id == companyId
        result.name == "Umbrella Corp"
        result.cnpj == "12345678000199"
        result.skills.size() == 1
    }

    def "findById should throw EntityNotFoundException when company not found"() {
        given:
        sqlMock.rows(_, [companyId]) >> []

        when:
        repository.findById(companyId)

        then:
        thrown(EntityNotFoundException)
    }

    def "findAll should return a list of companies"() {
        given:
        sqlMock.rows(_) >> [createCompanyRow()]

        when:
        def result = repository.findAll()

        then:
        result.size() == 1
    }

    def "findAll should return empty list when no companies exist"() {
        given:
        sqlMock.rows(_) >> []

        when:
        List<Person> result = repository.findAll()

        then:
        result.isEmpty()
    }

    def "findByCnpj should return a company when CNPJ matches"() {
        given:
        String cnpj = "12345678000199"
        sqlMock.rows(_, [cnpj]) >> [createCompanyRow()]

        when:
        Company result = repository.findByCnpj(cnpj) as Company

        then:
        result.cnpj == cnpj
        result.name == "Umbrella Corp"
    }

    def "findByCnpj should throw EntityNotFoundException when CNPJ not found"() {
        given:
        sqlMock.rows(_, ["99999999999999"]) >> []

        when:
        repository.findByCnpj("99999999999999")

        then:
        thrown(EntityNotFoundException)
    }

    def "findByJobId should return a company when job ID matches"() {
        given:
        UUID jobId = UUID.randomUUID()
        sqlMock.rows(_, [jobId]) >> [createCompanyRow()]

        when:
        Company result = repository.findByJobId(jobId) as Company

        then:
        result.name == "Umbrella Corp"
    }

    def "findByJobId should throw EntityNotFoundException when no company found for job"() {
        given:
        UUID jobId = UUID.randomUUID()
        sqlMock.rows(_, [jobId]) >> []

        when:
        repository.findByJobId(jobId)

        then:
        thrown(EntityNotFoundException)
    }

    def "findBySkill should return companies that have the given skill"() {
        given:
        sqlMock.rows(_, ["Java"]) >> [createCompanyRow()]

        when:
        List<Person> result = repository.findBySkill("Java")

        then:
        result.size() == 1
    }

    def "findBySkill should return empty list when no companies match"() {
        given:
        sqlMock.rows(_, ["Rust"]) >> []

        when:
        List<Person> result = repository.findBySkill("Rust")

        then:
        result.isEmpty()
    }

    def "save should insert user and company and return saved company"() {
        given:
        Company company = new Company(
                name: "Umbrella Corp", email: "umbrella@corp.com", password: "secret123",
                description: "A tech company", cnpj: "12345678000199",
                skills: [new Skill(id: skillId, name: "Java")] as Set
        )

        GroovyRowResult userRow = [id: companyId, name: "Umbrella Corp", email: "umbrella@corp.com",
                                   password: "secret123", description: "A tech company"] as GroovyRowResult
        GroovyRowResult companyRow = [cnpj: "12345678000199"] as GroovyRowResult

        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }
        sqlMock.firstRow(_, { it[0] == "Umbrella Corp" }) >> userRow
        sqlMock.firstRow(_, { it[0] == companyId }) >> companyRow

        skillRepositoryMock.findByUserId(companyId) >> ([new Skill(id: skillId, name: "Java")] as Set)

        when:
        Company result = repository.save(company)

        then:
        result != null
        result.name == "Umbrella Corp"
        result.cnpj == "12345678000199"
    }

    def "delete should execute delete queries within transaction"() {
        given:
        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }

        when:
        repository.delete(companyId)

        then:
        1 * sqlMock.execute("DELETE FROM companies WHERE user_id = ?", [companyId])
        1 * sqlMock.execute("DELETE FROM users WHERE id = ?", [companyId])
    }

    def "update should update user and company and return updated company"() {
        given:
        Company companyToUpdate = new Company(
                name: "Umbrella Corp Updated", email: "new@corp.com",
                password: "123456", description: "Updated desc", cnpj: "12345678000199"
        )

        GroovyRowResult userRow = [id: companyId, name: "Umbrella Corp Updated", email: "new@corp.com",
                                   password: "123456", description: "Updated desc"] as GroovyRowResult
        GroovyRowResult companyRow = [cnpj: "12345678000199"] as GroovyRowResult

        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }
        sqlMock.firstRow({ it.contains("UPDATE users") }, _) >> userRow
        sqlMock.firstRow({ it.contains("UPDATE companies") }, _) >> companyRow

        skillRepositoryMock.findByUserId(companyId) >> ([] as Set)

        when:
        Company result = repository.update(companyToUpdate, companyId)

        then:
        result != null
        result.name == "Umbrella Corp Updated"
    }
}
