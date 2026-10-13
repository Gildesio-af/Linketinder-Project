package zg.acelera.service


import zg.acelera.domain.User
import zg.acelera.dto.auth.AuthRequestDTO
import zg.acelera.dto.auth.AuthResponseDTO
import zg.acelera.repository.candidate.CandidateRepository
import zg.acelera.repository.company.CompanyRepository

class AuthService {
    private final CandidateRepository candidateRepository
    private final CompanyRepository companyRepository

    AuthService(CandidateRepository candidateRepository, CompanyRepository companyRepository) {
        this.candidateRepository = candidateRepository
        this.companyRepository = companyRepository
    }

    AuthResponseDTO authenticate(AuthRequestDTO authDTO) {
        String username = authDTO.username()

        if (username.length() == 11) {
            User candidate = candidateRepository.findByCpf(username)
            if (candidate != null && candidate.password == authDTO.password()) {
                return new AuthResponseDTO(candidate.id, "CANDIDATE")
            } else {
                throw new IllegalArgumentException("Invalid username or password")
            }
        } else if (username.length() == 14) {
            User company = companyRepository.findByCnpj(username)
            if (company != null && company.password == authDTO.password()) {
                return new AuthResponseDTO(company.id, "COMPANY")
            } else {
                throw new IllegalArgumentException("Invalid username or password")
            }
        } else {
            throw new IllegalArgumentException("Invalid username format")
        }
    }
}
