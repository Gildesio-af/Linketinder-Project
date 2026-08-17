package zg.acelera.dto.company

import zg.acelera.domain.Company
import zg.acelera.domain.Skill

record CompanyDTO(String cnpj, String name, String corporateEmail,
                  String state, String cep, String description, Set<String> skills) {

    public CompanyDTO() {
        if (cnpj == null || cnpj.trim().isEmpty() || cnpj.length() != 14)
            throw new IllegalArgumentException("Please provide a valid CPF with 14 digits.")
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid name.")
        if (corporateEmail == null || corporateEmail.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid email.")
        if (state == null || state.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid state.")
        if (cep == null || cep.trim().isEmpty() || cep.length() != 8)
            throw new IllegalArgumentException("Please provide a valid CEP.")
        if (description == null || description.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid description.")
        if (skills == null || skills.isEmpty())
            throw new IllegalArgumentException("Please provide a valid set of skills.")
    }

    Company toCompany() {
        Company.builder()
        .cnpj(cnpj)
        .name(name)
        .email(corporateEmail)
        .state(state)
        .cep(cep)
        .description(description)
        .skills(skills.collect { Skill.valueOf(it)} as Set<Skill>)
        .build()
    }

}