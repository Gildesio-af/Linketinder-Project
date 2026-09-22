package zg.acelera.dto.candidate

import groovy.transform.ImmutableOptions
import zg.acelera.domain.Candidate
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.skill.SkillResponseDTO

import java.time.LocalDate

@ImmutableOptions(knownImmutableClasses = [AddressResponseDTO, SkillResponseDTO])
record CandidateResponseDTO(
        UUID id,
        String name,
        String lastName,
        String  email,
        String description,
        String cpf,
        LocalDate birthDate,
        Set<AddressResponseDTO> address,
        Set<SkillResponseDTO> skills
) {
    static CandidateResponseDTO fromDomain(Candidate candidate, Set<AddressResponseDTO> addresses) {
        Set<SkillResponseDTO> skillDTOs = candidate.skills?.collect { skill ->
            SkillResponseDTO.fromDomain(skill)
        } as Set<SkillResponseDTO> ?: [] as Set<SkillResponseDTO>

        return new CandidateResponseDTO(
                id: candidate.id,
                name: candidate.name,
                lastName: candidate.lastName,
                email: candidate.email,
                description: candidate.description,
                cpf: candidate.cpf,
                birthDate: candidate.birthDate,
                address: addresses,
                skills: skillDTOs
        )
    }
}