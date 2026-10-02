package zg.acelera.dto.company

import groovy.transform.builder.Builder
import zg.acelera.domain.Company

@Builder
record CompanyUpdateDTO(
        String cnpj,
        String name,
        String email,
        String password,
        String description
) {
    public CompanyUpdateDTO {
        if (cnpj != null && (cnpj.trim().isEmpty() || cnpj.length() != 14))
            throw new IllegalArgumentException("Please provide a valid CNPJ with 14 digits.")
        if (name != null && name.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid name.")
        if (email != null && email.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid email.")
        if (password != null && password.trim().length() < 6)
            throw new IllegalArgumentException("Please provide a valid password with at least 6 characters.")
        if (description != null && description.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid description.")
    }

    Company toCompany() {
        return Company.builder()
                .cnpj(cnpj)
                .name(name)
                .email(email)
                .password(password)
                .description(description)
                .build()
    }
}
