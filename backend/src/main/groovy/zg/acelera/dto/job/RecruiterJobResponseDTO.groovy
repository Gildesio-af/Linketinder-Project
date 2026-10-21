package zg.acelera.dto.job

import groovy.transform.ImmutableOptions
import zg.acelera.dto.skill.SkillResponseDTO

@ImmutableOptions(knownImmutableClasses = [SkillResponseDTO])
record RecruiterJobResponseDTO(
        UUID id,
        String name,
        String description,
        Set<SkillResponseDTO> skills,
        String city,
        String state,
        String country
) {
    RecruiterJobResponseDTO {
        skills = skills ?: [] as Set<SkillResponseDTO>
    }
}
