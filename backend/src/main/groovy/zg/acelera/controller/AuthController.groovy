package zg.acelera.controller

import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import zg.acelera.dto.auth.AuthRequestDTO
import zg.acelera.service.AuthService

class AuthController extends BaseWebController {
    private final AuthService authService

    AuthController(AuthService authService, JsonSlurper jsonSlurper) {
        super(jsonSlurper)
        this.authService = authService
        post("/", this.&authenticate)
    }

    private void authenticate(HttpServletRequest req, HttpServletResponse resp) {
        Map body = readBody(req)
        answerJson(resp, HttpServletResponse.SC_OK,
                authService.authenticate(new AuthRequestDTO(body.username as String, body.password as String)))
    }
}
