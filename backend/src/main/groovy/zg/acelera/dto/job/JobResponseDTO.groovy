package zg.acelera.dto.job

import groovy.transform.ImmutableOptions
import zg.acelera.domain.Job
import zg.acelera.domain.Skill
import zg.acelera.dto.address.AddressResponseDTO

@ImmutableOptions(knownImmutableClasses = [AddressResponseDTO])
record JobResponseDTO(
        UUID id,
        String name,
        String description,
        Set<Skill> skills,
        AddressResponseDTO addressResponseDTO
) {
    static JobResponseDTO fromDomain(Job job, AddressResponseDTO addressResponseDTO) {
        return new JobResponseDTO(
                id: job.id,
                name: job.name,
                description: job.description,
                skills: job.desiredSkills,
                addressResponseDTO: addressResponseDTO
        )
    }
}