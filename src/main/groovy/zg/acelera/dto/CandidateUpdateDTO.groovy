package zg.acelera.dto

import zg.acelera.domain.Candidate;
import zg.acelera.domain.Skill;

record CandidateUpdateDTO (
        String cpf,
        String name,
        String email,
        Integer age,
        String state,
        String cep,
        String description,
        Set<String> skills
) {
    public CandidateUpdateDTO {
        if (name() != null && name().trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid name.")
        if (email() != null && email().trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid email.")
        if (age() != null && age() < 0)
            throw new IllegalArgumentException("Please provide a valid age.")
        if (state() != null && state().trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid state.")
        if (cep() != null && (cep().trim().isEmpty() || cep().length() != 8))
            throw new IllegalArgumentException("Please provide a valid CEP.")
        if (description() != null && description().trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid description.")
        if (skills() != null && skills().isEmpty())
            throw new IllegalArgumentException("Please provide a valid set of skills.")
    }

    Candidate updateCandidate(Candidate candidate) {
        if (name() != null) candidate.name = name()
        if (email() != null) candidate.email = email()
        if (age() != null) candidate.age = age()
        if (state() != null) candidate.state = state()
        if (cep() != null) candidate.cep = cep()
        if (description() != null) candidate.description = description()
        if (skills() != null) candidate.skills = skills().collect { Skill.valueOf(it) } as HashSet
        candidate
    }
}