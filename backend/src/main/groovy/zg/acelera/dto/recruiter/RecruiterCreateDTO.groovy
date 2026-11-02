package zg.acelera.dto.recruiter

import groovy.transform.builder.Builder
import zg.acelera.domain.Recruiter
import zg.acelera.domain.RecruiterStatus

import java.time.LocalDate
import java.util.regex.Pattern

@Builder
record RecruiterCreateDTO(
        String name,
        UUID recruitmentAreaId,
        String description,
        String email,
        String cpf,
        LocalDate birthDate,
        String password
) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile('^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$')

    public RecruiterCreateDTO {
        name = requiredText(name, 'name')
        description = requiredText(description, 'description')
        email = normalizeEmail(email)
        cpf = normalizeCpf(cpf)
        if (recruitmentAreaId == null)
            throw new IllegalArgumentException('Please provide a valid recruitment area.')
        if (birthDate == null || !isAdult(birthDate))
            throw new IllegalArgumentException('Recruiter must be at least 18 years old.')
        if (!password || password.length() < 6)
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
                .status(RecruiterStatus.ACTIVE)
                .build()
    }

    static RecruiterCreateDTO fromMap(Map<String, Object> map) {
        new RecruiterCreateDTO(
                map.get('name') as String,
                map.get('recruitmentAreaId') ? UUID.fromString(map.get('recruitmentAreaId') as String) : null,
                map.get('description') as String,
                map.get('email') as String,
                map.get('cpf') as String,
                map.get('birthDate') ? LocalDate.parse(map.get('birthDate') as String) : null,
                map.get('password') as String
        )
    }

    static String normalizeEmail(String value) {
        String normalized = value?.trim()?.toLowerCase(Locale.ROOT)
        if (!normalized || !EMAIL_PATTERN.matcher(normalized).matches())
            throw new IllegalArgumentException('Please provide a valid email.')
        normalized
    }

    static String normalizeCpf(String value) {
        String normalized = value?.trim()?.replaceAll('[.\\-\\s]', '')
        if (!normalized || !normalized.matches('\\d{11}'))
            throw new IllegalArgumentException('Please provide a valid CPF with 11 digits.')
        normalized
    }

    private static String requiredText(String value, String field) {
        if (!value || value.trim().isEmpty())
            throw new IllegalArgumentException("Please provide a valid ${field}.")
        value.trim()
    }

    private static boolean isAdult(LocalDate date) {
        !date.isAfter(LocalDate.now().minusYears(18))
    }
}
