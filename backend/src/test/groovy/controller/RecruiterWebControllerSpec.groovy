package controller

import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import spock.lang.Specification
import zg.acelera.controller.RecruiterWebController
import zg.acelera.dto.recruiter.RecruiterResponseDTO
import zg.acelera.domain.RecruiterStatus
import zg.acelera.service.CandidateService
import zg.acelera.service.RecruiterService

import java.io.PrintWriter
import java.io.StringWriter

class RecruiterWebControllerSpec extends Specification {
    RecruiterService recruiterService = Mock()
    CandidateService candidateService = Mock()
    RecruiterWebController controller
    UUID recruiterId = UUID.randomUUID()

    def setup() {
        controller = new RecruiterWebController(recruiterService, candidateService, new JsonSlurper())
    }

    def "returns only the authenticated recruiter's own profile"() {
        given:
        HttpServletRequest request = Mock()
        HttpServletResponse response = response()
        request.method >> "GET"
        request.pathInfo >> "/${recruiterId}"
        request.getHeader("X-User-Id") >> recruiterId.toString()
        request.getHeader("X-User-Role") >> "RECRUITER"
        recruiterService.findOwnProfile(recruiterId) >> new RecruiterResponseDTO(
                recruiterId, "Recruiter", UUID.randomUUID(), "Hiring", "recruiter@example.test",
                RecruiterStatus.ACTIVE)

        when:
        controller.service(request, response)

        then:
        1 * response.setStatus(HttpServletResponse.SC_OK)
        response.body().contains('"name":"Recruiter"')
        !response.body().contains("password")
        !response.body().contains("cpf")
        !response.body().contains("birthDate")
    }

    def "rejects profile access without recruiter authentication"() {
        given:
        HttpServletRequest request = Mock()
        HttpServletResponse response = response()
        request.method >> "GET"
        request.pathInfo >> "/${recruiterId}"
        request.getHeader("X-User-Id") >> null
        request.getHeader("X-User-Role") >> null

        when:
        controller.service(request, response)

        then:
        1 * response.setStatus(HttpServletResponse.SC_FORBIDDEN)
        0 * recruiterService._
    }

    private HttpServletResponse response() {
        StringWriter output = new StringWriter()
        HttpServletResponse response = Mock()
        response.writer >> new PrintWriter(output)
        response.metaClass.body = { -> output.toString() }
        response
    }
}
