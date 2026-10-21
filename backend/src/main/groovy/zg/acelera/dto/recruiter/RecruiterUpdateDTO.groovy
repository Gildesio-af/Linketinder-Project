package zg.acelera.dto.recruiter

import groovy.transform.builder.Builder
import zg.acelera.domain.Recruiter

import java.time.LocalDate

@Builder
record RecruiterUpdateDTO(
        String name,
        UUID recruitmentAreaId,
        String description,
        String email,
        String cpf,
        LocalDate birthDate,
        String password
) {
    public RecruiterUpdateDTO {
        if (name != null) name = requiredText(name, 'name')
        if (description != null) description = requiredText(description, 'description')
        if (email != null) email = RecruiterCreateDTO.normalizeEmail(email)
        if (cpf != null) cpf = RecruiterCreateDTO.normalizeCpf(cpf)
        if (birthDate != null && birthDate.isAfter(LocalDate.now().minusYears(18)))
            throw new IllegalArgumentException('Recruiter must be at least 18 years old.')
        if (password != null && password.length() < 6)
            throw new IllegalArgumentException('Please provide a password with at least 6 characters.')
    }

    Recruiter toDomain() {
        Recruiter.builder()
                .name(name)
                .recruitmentAreaId(recruitmentAreaId)
                .description(description)
                .email(email)
                .cpf(cpf)
                .birthDate(birthDate)
                .password(password)
                .build()
    }

    static RecruiterUpdateDTO fromMap(Map<String, Object> map) {
        new RecruiterUpdateDTO(
                map.get('name') as String,
                map.get('recruitmentAreaId') ? UUID.fromString(map.get('recruitmentAreaId') as String) : null,
                map.get('description') as String,
                map.get('email') as String,
                map.get('cpf') as String,
                map.get('birthDate') ? LocalDate.parse(map.get('birthDate') as String) : null,
                map.get('password') as String
        )
    }

    private static String requiredText(String value, String field) {
        if (value.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid ${field}.")
        value.trim()
    }
}
