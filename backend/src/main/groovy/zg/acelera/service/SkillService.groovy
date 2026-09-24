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

    SkillResponseDTO getSkillByName(String name) {
        SkillResponseDTO responseDTO
        try {
            Skill skill = skillRepository.findByName(name)
            responseDTO = SkillResponseDTO.fromDomain(skill)
        } catch (Exception e) {
            e.printStackTrace()
            return  null
        }
        return  responseDTO
    }

    Set<SkillResponseDTO> getAllSkills() {
        try {
            return  skillRepository.findAll().collect { skill -> SkillResponseDTO.fromDomain(skill) } as Set
        } catch (SQLException e) {
            println("Error: ${e.getMessage()}")
            return []
        }
    }

    SkillResponseDTO createSkill(SkillRequestDTO skillDTO) {
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

    SkillResponseDTO updateSkill(SkillRequestDTO skillDTO, UUID id) {
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
