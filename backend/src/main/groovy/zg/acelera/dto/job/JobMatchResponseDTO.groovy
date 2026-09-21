package zg.acelera.dto.job

import groovy.transform.ImmutableOptions
import zg.acelera.domain.Job
import zg.acelera.domain.Skill
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.company.CompanyResponseDTO

@ImmutableOptions(knownImmutableClasses = [AddressResponseDTO, CompanyResponseDTO])
record JobMatchResponseDTO(
        UUID id,
        String name,
        String description,
        Set<Skill> skills,
        AddressResponseDTO addressResponseDTO,
        CompanyResponseDTO companyResponseDTO
) {
    static JobMatchResponseDTO fromDomain(Job job, AddressResponseDTO addressResponseDTO, CompanyResponseDTO companyResponseDTO) {
        return new JobMatchResponseDTO(
                id: job.id,
                name: job.name,
                description: job.description,
                skills: job.desiredSkills,
                addressResponseDTO: addressResponseDTO,
                companyResponseDTO: companyResponseDTO
        )
    }
}
