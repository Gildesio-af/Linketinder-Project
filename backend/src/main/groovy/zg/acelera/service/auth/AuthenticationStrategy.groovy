package zg.acelera.service.auth

import zg.acelera.dto.auth.AuthResponseDTO

interface AuthenticationStrategy {
    boolean supports(String username)

    AuthResponseDTO authenticate(String username, String password)
}
