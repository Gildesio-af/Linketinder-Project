package zg.acelera.dto.candidate

import groovy.transform.ImmutableOptions
import zg.acelera.dto.skill.SkillResponseDTO

@ImmutableOptions(knownImmutableClasses = [SkillResponseDTO])
record AnonymousCandidateResponseDTO(
        String description,
        Set<SkillResponseDTO> skills
) {
    AnonymousCandidateResponseDTO {
        skills = skills ?: [] as Set<SkillResponseDTO>
    }
}
