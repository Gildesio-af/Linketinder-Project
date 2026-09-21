package zg.acelera.dto.job

import zg.acelera.domain.Address
import zg.acelera.domain.Company
import zg.acelera.domain.Job
import zg.acelera.domain.Skill

record JobDTO (
        UUID id,
        String title,
        String description,
        List<String> skills,
        UUID addressId,
        UUID publisherId
 ) {
     JobDTO() {
        if (!title || title().trim().isEmpty())
            throw new IllegalArgumentException("Job title must be provided and cannot be empty")
        if (!description || description().trim().isEmpty())
            throw new IllegalArgumentException("Job description must be provided and cannot be empty")
        if (skills == null || skills.isEmpty())
            throw new IllegalArgumentException("Job skills must be provided and cannot be empty")
    }

    Job toDomain() {
        return new Job(
                id: id,
                name: title,
                description: description,
                desiredSkills: skills.collect { skillName -> new Skill(name: skillName) } as Set<Skill>,
                address: new Address(id: addressId),
                publisher: new Company(id: publisherId)
        )
    }
}
