package zg.acelera.dto.job

import zg.acelera.domain.Address
import zg.acelera.domain.Company
import zg.acelera.domain.Job
import zg.acelera.domain.Skill

record JobCreateDTO(
        String title,
        String description,
        List<String> skills,
        UUID publisherId
 ) {
     JobCreateDTO() {
        if (!title || title().trim().isEmpty())
            throw new IllegalArgumentException("Job title must be provided and cannot be empty")
        if (!description || description().trim().isEmpty())
            throw new IllegalArgumentException("Job description must be provided and cannot be empty")
        if (!skills)
            throw new IllegalArgumentException("Job skills must be provided and cannot be empty")
    }

    Job toDomain() {
        return new Job(
                name: title,
                description: description,
                desiredSkills: skills.collect { skillName -> new Skill(name: skillName) } as Set<Skill>,
                publisher: new Company(id: publisherId)
        )
    }

    static JobCreateDTO fromMap(Map<String, Object> map) {
        return new JobCreateDTO(
                map.get("title") as String,
                map.get("description") as String,
                (map.get("skills") as List<String>) ?: [] as List<String>,
                map.get("publisherId") ? UUID.fromString(map.get("publisherId") as String) : null
        )
    }
}
