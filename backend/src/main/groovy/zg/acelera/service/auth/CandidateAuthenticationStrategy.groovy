package zg.acelera.service.auth

import zg.acelera.domain.User
import zg.acelera.dto.auth.AuthResponseDTO
import zg.acelera.repository.candidate.CandidateRepository

class CandidateAuthenticationStrategy implements AuthenticationStrategy {
    private final CandidateRepository repository

    CandidateAuthenticationStrategy(CandidateRepository repository) {
        this.repository = repository
    }

    @Override
    boolean supports(String username) {
        username ==~ /\d{11}/
    }

    @Override
    AuthResponseDTO authenticate(String username, String password) {
        User candidate = repository.findByCpf(username)
        if (candidate != null && candidate.password == password)
            return new AuthResponseDTO(candidate.id, AuthResponseDTO.CANDIDATE_ROLE)
        throw new IllegalArgumentException('Invalid username or password')
    }
}
