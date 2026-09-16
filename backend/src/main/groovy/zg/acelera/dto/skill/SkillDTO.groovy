package zg.acelera.dto.skill

record SkillDTO(
        UUID id = null,
        String name
) {
    SkillDTO() {
        if (!name || name().trim().isEmpty())
            throw new IllegalArgumentException("Skill name must be provided and cannot be empty")
    }
}
