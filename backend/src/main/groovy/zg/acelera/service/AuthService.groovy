package zg.acelera.service

import zg.acelera.dto.auth.AuthRequestDTO
import zg.acelera.dto.auth.AuthResponseDTO
import zg.acelera.repository.candidate.CandidateRepository
import zg.acelera.repository.company.CompanyRepository
import zg.acelera.repository.recruiter.RecruiterRepository
import zg.acelera.service.auth.AuthenticationStrategy
import zg.acelera.service.auth.CandidateAuthenticationStrategy
import zg.acelera.service.auth.CompanyAuthenticationStrategy
import zg.acelera.service.auth.RecruiterAuthenticationStrategy

class AuthService {
    private final List<AuthenticationStrategy> authenticationStrategies

    AuthService(CandidateRepository candidateRepository, CompanyRepository companyRepository) {
        this(candidateRepository, companyRepository, null)
    }

    AuthService(CandidateRepository candidateRepository, CompanyRepository companyRepository,
                RecruiterRepository recruiterRepository) {
        authenticationStrategies = [
                new CandidateAuthenticationStrategy(candidateRepository),
                new CompanyAuthenticationStrategy(companyRepository)
        ]
        if (recruiterRepository != null)
            authenticationStrategies << new RecruiterAuthenticationStrategy(recruiterRepository)
    }

    AuthResponseDTO authenticate(AuthRequestDTO authDTO) {
        String username = authDTO.username()?.trim()
        if (!username)
            throw new IllegalArgumentException("Invalid username format")
        if (!username || !authDTO.password())
            throw new IllegalArgumentException("Invalid username or password")

        String compactUsername = username.replaceAll('[.\\-\\s]', '')
        String normalizedUsername = compactUsername ==~ /\d+/ ?
                compactUsername : username.toLowerCase(Locale.ROOT)
        AuthenticationStrategy strategy = authenticationStrategies.find { it.supports(normalizedUsername) }
        if (strategy == null)
            throw new IllegalArgumentException("Invalid username format")
        strategy.authenticate(normalizedUsername.toLowerCase(Locale.ROOT), authDTO.password())
    }
}
