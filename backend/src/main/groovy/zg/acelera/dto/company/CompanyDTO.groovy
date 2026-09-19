package zg.acelera.dto.company

import groovy.transform.builder.Builder
import zg.acelera.domain.Company
import zg.acelera.domain.Skill

@Builder
record CompanyDTO(
        String cnpj,
        String name,
        String email,
        String password,
        String description,
        Set<String> skillsId
) {
    CompanyDTO(String cnpj, String name, String corporateEmail, String state, String cep, String description, Set<String> skills) {
        this(cnpj, name, corporateEmail, null, description, skills)
    }

    CompanyDTO {
        if (!cnpj || cnpj.trim().isEmpty() || cnpj.length() != 14)
            throw new IllegalArgumentException("Please provide a valid CNPJ with 14 digits.")
        if (!name || name.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid name.")
        if (!email || email.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid email.")
        if (password != null && password.trim().length() < 6)
            throw new IllegalArgumentException("Please provide a valid password with at least 6 characters.")
        if (!description || description.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid description.")
        if (!skillsId || skillsId.isEmpty())
            throw new IllegalArgumentException("Please provide a valid set of skills.")
    }

    Company toDomain() {
        return Company.builder()
                .cnpj(cnpj)
                .name(name)
                .email(email)
                .password(password)
                .description(description)
                .skills(skillsId.collect { skillId -> new Skill(id: UUID.fromString(skillId)) } as Set)
                .build()
    }
}