package zg.acelera.dto.candidate

import groovy.transform.builder.Builder
import zg.acelera.domain.Candidate;
import zg.acelera.domain.SkillEnum

import java.time.LocalDate;

@Builder
record CandidateUpdateDTO (
        String cpf,
        String name,
        String email,
        String password,
        String description,
        String lastName,
        LocalDate birthDate
) {
    public CandidateUpdateDTO {
        if (name != null && name.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid name.")
        if (email != null && email.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid email.")
        if (password != null && password.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid password.")
        if (description != null && description.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid description.")
        if (lastName != null && lastName.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid last name.")
        if (birthDate != null && birthDate.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Please provide a valid birth date.")
    }

    Candidate toCandidate() {
        return Candidate.builder()
                .cpf(cpf ? cpf : null)
                .name(name ? name : null)
                .lastName(lastName ? lastName : null)
                .email(email ? email : null)
                .password(password ? password : null)
                .description(description ? description : null)
                .birthDate(birthDate ? birthDate : null)
                .build()
    }

    static CandidateUpdateDTO fromMap(Map<String, Object> map) {
        return new CandidateUpdateDTO(
                map.get("cpf") as String ?: null,
                map.get("name") as String ?: null,
                map.get("email") as String ?: null,
                map.get("password") as String ?: null,
                map.get("description") as String ?: null,
                map.get("lastName") as String ?: null,
                map.get("birthDate") ? LocalDate.parse(map.get("birthDate") as String) : null
        )
    }
}