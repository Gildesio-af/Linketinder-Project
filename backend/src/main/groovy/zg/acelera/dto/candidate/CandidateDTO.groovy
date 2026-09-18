package zg.acelera.dto.candidate

import groovy.transform.builder.Builder
import zg.acelera.domain.Candidate
import zg.acelera.domain.Skill

import java.time.LocalDate

@Builder
record CandidateDTO(String cpf,
                    String name,
                    String email,
                    String password,
                    String description,
                    String lastName,
                    LocalDate birthDate,
                    Set<String> skillsId) {
    public CandidateDTO {
        if (!cpf || cpf.trim().isEmpty() || cpf.length() != 11)
            throw new IllegalArgumentException("Please provide a valid CPF with 11 digits.")
        if (!name || name.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid name.")
        if (!email|| email.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid email.")
        if (!password || password.trim().length() < 6)
            throw new IllegalArgumentException("Please provide a valid password with at least 6 characters.")
        if (!description || description.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid description.")
        if (!lastName || lastName.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid last name.")
        if (!birthDate)
            throw new IllegalArgumentException("Please provide a valid birth date.")
        if (!skillsId || skillsId.isEmpty())
            throw new IllegalArgumentException("Please provide a valid set of skills.")
    }

    Candidate toDomain() {
        return Candidate.builder()
                .cpf(cpf)
                .name(name)
                .lastName(lastName)
                .email(email)
                .password(password)
                .description(description)
                .birthDate(birthDate)
                .skills(skillsId.collect {skillId -> new Skill(id: UUID.fromString(skillId))} as Set)
                .build()
    }
}
