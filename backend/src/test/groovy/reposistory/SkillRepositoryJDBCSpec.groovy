package reposistory

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import spock.lang.Specification
import zg.acelera.domain.Skill
import zg.acelera.repository.skill.SkillRepositoryJDBC
import zg.acelera.utils.exception.EntityNotFoundException

class SkillRepositoryJDBCSpec extends Specification {

    Sql sqlMock
    SkillRepositoryJDBC repository

    UUID skillId = UUID.randomUUID()

    def setup() {
        sqlMock = Mock(Sql)
        repository = new SkillRepositoryJDBC(sqlMock)
    }

    def "findById should return a skill when found"() {
        given:
        sqlMock.firstRow("SELECT * FROM skills WHERE id = ?", [skillId]) >> ([skill_id: skillId, skill_name: "Java"] as GroovyRowResult)

        when:
        Skill result = repository.findById(skillId)

        then:
        result.id == skillId
        result.name == "Java"
    }

    def "findById should throw EntityNotFoundException when skill not found"() {
        given:
        sqlMock.firstRow("SELECT * FROM skills WHERE id = ?", [skillId]) >> null

        when:
        repository.findById(skillId)

        then:
        thrown(EntityNotFoundException)
    }

    def "findByName should return a skill when found"() {
        given:
        sqlMock.firstRow("SELECT * FROM skills WHERE lower(name) = lower(?)", ["Java"]) >> ([skill_id: skillId, skill_name: "Java"] as GroovyRowResult)

        when:
        Skill result = repository.findByName("Java")

        then:
        result.name == "Java"
    }

    def "findByName should throw EntityNotFoundException when skill not found"() {
        given:
        sqlMock.firstRow("SELECT * FROM skills WHERE lower(name) = lower(?)", ["NonExistent"]) >> null

        when:
        repository.findByName("NonExistent")

        then:
        thrown(EntityNotFoundException)
    }

    def "findAll should return all skills"() {
        given:
        UUID id2 = UUID.randomUUID()
        sqlMock.rows(_) >> [
                [id: skillId, name: "Java"] as GroovyRowResult,
                [id: id2, name: "Python"] as GroovyRowResult
        ]

        when:
        Set<Skill> result = repository.findAll()

        then:
        result.size() == 2
    }

    def "findAll should return empty set when no skills exist"() {
        given:
        sqlMock.rows(_) >> []

        when:
        Set<Skill> result = repository.findAll()

        then:
        result.isEmpty()
    }

    def "save should insert a skill and return it"() {
        given:
        Skill skill = new Skill(name: "Groovy")
        UUID newId = UUID.randomUUID()
        sqlMock.firstRow("INSERT INTO skills (name) VALUES (?) RETURNING *", ["Groovy"]) >> ([skill_id: newId, skill_name: "Groovy"] as GroovyRowResult)

        when:
        Skill result = repository.save(skill)

        then:
        result.id == newId
        result.name == "Groovy"
    }

    def "save should return null when insert fails"() {
        given:
        Skill skill = new Skill(name: "Groovy")
        sqlMock.firstRow("INSERT INTO skills (name) VALUES (?) RETURNING *", ["Groovy"]) >> null

        when:
        Skill result = repository.save(skill)

        then:
        result == null
    }

    def "update should update a skill and return it"() {
        given:
        Skill skill = new Skill(name: "Java Updated")
        sqlMock.firstRow("UPDATE skills SET name = ? WHERE id = ? RETURNING *", ["Java Updated", skillId]) >> ([skill_id: skillId, skill_name: "Java Updated"] as GroovyRowResult)

        when:
        Skill result = repository.update(skill, skillId)

        then:
        result.name == "Java Updated"
    }

    def "update should throw EntityNotFoundException when skill not found"() {
        given:
        Skill skill = new Skill(name: "Java Updated")
        sqlMock.firstRow("UPDATE skills SET name = ? WHERE id = ? RETURNING *", ["Java Updated", skillId]) >> null

        when:
        repository.update(skill, skillId)

        then:
        thrown(EntityNotFoundException)
    }

    def "deleteById should delete a skill successfully"() {
        given:
        sqlMock.executeUpdate(_, [skillId]) >> 1
        sqlMock.withTransaction(_) >> { Closure closure -> closure.call() }

        when:
        repository.deleteById(skillId)

        then:
        notThrown(EntityNotFoundException)
    }

    def "findByUserId should return skills for a given user"() {
        given:
        UUID userId = UUID.randomUUID()
        sqlMock.rows(_, [userId]) >> [
                [skill_id: skillId, skill_name: "Java"] as GroovyRowResult
        ]

        when:
        Set<Skill> result = repository.findByUserId(userId)

        then:
        result.size() == 1
        result.first().name == "Java"
    }

    def "findByUserId should return empty set when no skills found"() {
        given:
        UUID userId = UUID.randomUUID()
        sqlMock.rows(_, [userId]) >> []

        when:
        Set<Skill> result = repository.findByUserId(userId)

        then:
        result.isEmpty()
    }
}
