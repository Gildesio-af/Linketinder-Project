package zg.acelera.service.auth

import zg.acelera.domain.User
import zg.acelera.dto.auth.AuthResponseDTO
import zg.acelera.repository.company.CompanyRepository

class CompanyAuthenticationStrategy implements AuthenticationStrategy {
    private final CompanyRepository repository

    CompanyAuthenticationStrategy(CompanyRepository repository) {
        this.repository = repository
    }

    @Override
    boolean supports(String username) {
        username ==~ /\d{14}/
    }

    @Override
    AuthResponseDTO authenticate(String username, String password) {
        User company = repository.findByCnpj(username)
        if (company != null && company.password == password)
            return new AuthResponseDTO(company.id, AuthResponseDTO.COMPANY_ROLE)
        throw new IllegalArgumentException('Invalid username or password')
    }
}
