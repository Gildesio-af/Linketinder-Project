package zg.acelera.dto.candidate

import groovy.transform.builder.Builder
import zg.acelera.domain.Candidate
import zg.acelera.domain.SkillEnum

@Builder
record CandidateDTO(String cpf, String name, String email, Integer age,
                    String state, String cep, String description, Set<String> skills) {
    public CandidateDTO {
        if (cpf == null || cpf.trim().isEmpty() || cpf.length() != 11)
            throw new IllegalArgumentException("Please provide a valid CPF with 11 digits.")
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid name.")
        if (email == null || email.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid email.")
        if (age == null || age < 0)
            throw new IllegalArgumentException("Please provide a valid age.")
        if (state == null || state.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid state.")
        if (cep == null || cep.trim().isEmpty() || cep.length() != 8)
            throw new IllegalArgumentException("Please provide a valid CEP.")
        if (description == null || description.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid description.")
        if (skills == null || skills.isEmpty())
            throw new IllegalArgumentException("Please provide a valid set of skills.")
    }

    Candidate toCandidate() {
        return Candidate.builder()
                .cpf(cpf)
                .name(name)
                .email(email)
                .age(age)
                .state(state)
                .cep(cep)
                .description(description)
                .skills(skills.collect { SkillEnum.valueOf(it) } as HashSet)
                .build()
    }
}