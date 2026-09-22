package zg.acelera.dto.company

import groovy.transform.ImmutableOptions
import zg.acelera.domain.Company
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.skill.SkillResponseDTO

@ImmutableOptions(knownImmutableClasses = [AddressResponseDTO, SkillResponseDTO])
record CompanyResponseDTO(
        UUID id,
        String name,
        String email,
        String description,
        String cnpj,
        Set<AddressResponseDTO> addresses,
        Set<SkillResponseDTO> skills
) {
    static CompanyResponseDTO fromDomain(Company company, Set<AddressResponseDTO> addresses) {
        Set<SkillResponseDTO> skillDTOs = company.skills?.collect { skill ->
            SkillResponseDTO.fromDomain(skill)
        } as Set<SkillResponseDTO> ?: [] as Set<SkillResponseDTO>

        return new CompanyResponseDTO(
                id: company.id,
                name: company.name,
                email: company.email,
                description: company.description,
                cnpj: company.cnpj,
                addresses: addresses,
                skills: skillDTOs
        )
    }
}
