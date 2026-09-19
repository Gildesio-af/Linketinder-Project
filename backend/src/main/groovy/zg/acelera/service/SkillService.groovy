package zg.acelera.service

import zg.acelera.domain.Skill
import zg.acelera.dto.skill.SkillDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.repository.ISkillRepository

import java.sql.SQLException

class SkillService {
    final ISkillRepository skillRepository

    SkillService(ISkillRepository skillRepository) {
        this.skillRepository = skillRepository
    }

    SkillResponseDTO getSkillById(UUID id) {
        SkillResponseDTO responseDTO
        try {
            Skill skill = skillRepository.findById(id)
            responseDTO = SkillResponseDTO.fromDomain(skill)
        } catch (Exception e) {
            e.printStackTrace()
            return  null
        }
        return  responseDTO
    }

    Set<Skill> getAllSkills() {
        try {
            return  skillRepository.findAll()
        } catch (SQLException e) {
            println("Error: ${e.getMessage()}")
            return []
        }
    }

    SkillResponseDTO createSkill(SkillDTO skillDTO) {
        Skill skill = skillDTO.toDomain()
        Skill createdSkill
        try {
            createdSkill = skillRepository.save(skill)
        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
        return SkillResponseDTO.fromDomain(createdSkill)
    }

    SkillResponseDTO updateSkill(SkillDTO skillDTO, UUID id) {
        Skill skill = skillDTO.toDomain()
        skill.setId(id)
        Skill updatedSkill
        try {
            updatedSkill = skillRepository.update(skill, id)
        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
        return SkillResponseDTO.fromDomain(updatedSkill)
    }

    void deleteSkill(UUID id) {
        try {
            skillRepository.deleteById(id)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }
}
