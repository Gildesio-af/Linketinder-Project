package zg.acelera.dto.candidate

import groovy.transform.ImmutableOptions
import zg.acelera.domain.Candidate
import zg.acelera.dto.address.AddressResponseDTO

import java.time.LocalDate

@ImmutableOptions(knownImmutableClasses = [AddressResponseDTO])
record CandidateResponseDTO(
        UUID id,
        String name,
        String lastName,
        String  email,
        String description,
        String cpf,
        LocalDate birthDate,
        Set<AddressResponseDTO> address
) {
    static CandidateResponseDTO fromDomain(Candidate candidate, Set<AddressResponseDTO> addresses) {
        return new CandidateResponseDTO(
                id: candidate.id,
                name: candidate.name,
                lastName: candidate.lastName,
                email: candidate.email,
                description: candidate.description,
                cpf: candidate.cpf,
                birthDate: candidate.birthDate,
                address: addresses
        )
    }
}