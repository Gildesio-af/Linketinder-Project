package zg.acelera.service.auth

import zg.acelera.domain.RecruiterStatus
import zg.acelera.dto.auth.AuthResponseDTO
import zg.acelera.repository.recruiter.RecruiterRepository
import zg.acelera.utils.exception.EntityNotFoundException

class RecruiterAuthenticationStrategy implements AuthenticationStrategy {
    private final RecruiterRepository repository

    RecruiterAuthenticationStrategy(RecruiterRepository repository) {
        this.repository = repository
    }

    @Override
    boolean supports(String username) {
        username.contains('@')
    }

    @Override
    AuthResponseDTO authenticate(String username, String password) {
        try {
            def recruiter = repository.findByEmail(username)
            if (recruiter?.status == RecruiterStatus.ACTIVE && recruiter.password == password)
                return new AuthResponseDTO(recruiter.id, AuthResponseDTO.RECRUITER_ROLE)
        } catch (EntityNotFoundException ignored) {
            // Invalid credentials have the same response regardless of account existence.
        }
        throw new IllegalArgumentException('Invalid username or password')
    }
}
