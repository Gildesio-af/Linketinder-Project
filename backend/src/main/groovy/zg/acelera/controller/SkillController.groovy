package zg.acelera.controller

import zg.acelera.dto.skill.SkillRequestDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.service.SkillService

class SkillController {
    private final SkillService skillService

    SkillController(SkillService skillService) {
        this.skillService = skillService
    }

    SkillResponseDTO getSkillById(UUID id) {
        return skillService.getSkillById(id)
    }

    SkillResponseDTO getSkillByName(String name) {
        return skillService.getSkillByName(name)
    }

    Set<SkillResponseDTO> getAllSkills() {
        return skillService.getAllSkills()
    }

    SkillResponseDTO createSkill(SkillRequestDTO skillDTO) {
        return skillService.createSkill(skillDTO)
    }

    SkillResponseDTO updateSkill(SkillRequestDTO skillDTO, UUID id) {
        return skillService.updateSkill(skillDTO, id)
    }

    void deleteSkill(UUID id) {
        skillService.deleteSkill(id)
    }
}
