package zg.acelera.dto.skill

import zg.acelera.domain.Skill

record SkillDTO(
        String name
) {
    SkillDTO() {
        if (!name || name().trim().isEmpty())
            throw new IllegalArgumentException("Skill name must be provided and cannot be empty")
    }

    Skill toDomain() {
        return new Skill(name: name)
    }
}