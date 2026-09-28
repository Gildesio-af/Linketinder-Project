package service

import spock.lang.Specification
import zg.acelera.domain.Skill
import zg.acelera.dto.skill.SkillRequestDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.repository.skill.SkillRepository
import zg.acelera.service.SkillService

class SkillServiceSpec extends Specification {

    SkillRepository repositoryMock
    SkillService service

    UUID skillId = UUID.randomUUID()

    def setup() {
        repositoryMock = Mock(SkillRepository)
        service = new SkillService(repositoryMock)
    }

    def "getSkillById should return a SkillResponseDTO"() {
        given:
        repositoryMock.findById(skillId) >> new Skill(id: skillId, name: "Java")

        when:
        SkillResponseDTO result = service.getSkillById(skillId)

        then:
        result.id() == skillId
        result.name() == "Java"
    }

    def "getSkillByName should return a SkillResponseDTO"() {
        given:
        repositoryMock.findByName("Java") >> new Skill(id: skillId, name: "Java")

        when:
        SkillResponseDTO result = service.getSkillByName("Java")

        then:
        result.name() == "Java"
    }

    def "getAllSkills should return a set of SkillResponseDTOs"() {
        given:
        UUID id2 = UUID.randomUUID()
        repositoryMock.findAll() >> ([
                new Skill(id: skillId, name: "Java"),
                new Skill(id: id2, name: "Python")
        ] as Set)

        when:
        Set<SkillResponseDTO> result = service.getAllSkills()

        then:
        result.size() == 2
    }

    def "getAllSkills should return empty set when no skills exist"() {
        given:
        repositoryMock.findAll() >> ([] as Set)

        when:
        Set<SkillResponseDTO> result = service.getAllSkills()

        then:
        result.isEmpty()
    }

    def "createSkill should save and return SkillResponseDTO"() {
        given:
        SkillRequestDTO dto = new SkillRequestDTO("Groovy")
        repositoryMock.save(_) >> new Skill(id: skillId, name: "Groovy")

        when:
        SkillResponseDTO result = service.createSkill(dto)

        then:
        result.name() == "Groovy"
    }

    def "updateSkill should update and return SkillResponseDTO"() {
        given:
        Skill skill = new Skill(id: skillId, name: "Java Updated")
        SkillRequestDTO dto = new SkillRequestDTO(
                name: "Java Updated")

        repositoryMock.update(_, skillId) >> skill

        when:
        SkillResponseDTO result = service.updateSkill(dto, skillId)

        then:
        result.name() == "Java Updated"
    }

    def "deleteSkill should call repository deleteById"() {
        when:
        service.deleteSkill(skillId)

        then:
        1 * repositoryMock.deleteById(skillId)
    }
}
