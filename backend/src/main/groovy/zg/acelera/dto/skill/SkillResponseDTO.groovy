package zg.acelera.dto.skill

import zg.acelera.domain.Skill

record SkillResponseDTO(
        UUID id,
        String name
) {
    static SkillResponseDTO fromDomain(Skill skill) {
        return new SkillResponseDTO(
                id: skill.id,
                name: skill.name
        )
    }
}
