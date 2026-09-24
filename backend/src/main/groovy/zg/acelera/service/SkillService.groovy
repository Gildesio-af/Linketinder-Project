package zg.acelera.service

import zg.acelera.domain.Skill
import zg.acelera.dto.skill.SkillRequestDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.repository.skill.SkillRepository

import java.sql.SQLException

class SkillService {
    final SkillRepository skillRepository

    SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository
    }

    SkillResponseDTO getSkillById(UUID id) {
        Skill skill = skillRepository.findById(id)

        return SkillResponseDTO.fromDomain(skill)
    }

    SkillResponseDTO getSkillByName(String name) {
        Skill skill = skillRepository.findByName(name)

        return SkillResponseDTO.fromDomain(skill)
    }

    Set<SkillResponseDTO> getAllSkills() {
        return  skillRepository.findAll().collect { skill -> SkillResponseDTO.fromDomain(skill) } as Set
    }

    SkillResponseDTO createSkill(SkillRequestDTO skillDTO) {
        Skill newSkill = skillDTO.toDomain()
        Skill createdSkill = skillRepository.save(newSkill)

        return SkillResponseDTO.fromDomain(createdSkill)
    }

    SkillResponseDTO updateSkill(SkillRequestDTO skillDTO) {
        Skill skill = skillDTO.toDomain()

        Skill updatedSkill = skillRepository.update(skill, skill.id)

        return SkillResponseDTO.fromDomain(updatedSkill)
    }

    void deleteSkill(UUID id) {
        skillRepository.deleteById(id)
    }
}
