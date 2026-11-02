package controller

import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import spock.lang.Specification
import zg.acelera.controller.RecruiterLikeWebController
import zg.acelera.domain.RecruiterJobLike
import zg.acelera.dto.recruiter.RecruiterLikeResultDTO
import zg.acelera.service.MatchService
import zg.acelera.service.RecruiterJobLikeService

import java.io.PrintWriter
import java.io.StringWriter

class RecruiterLikeWebControllerSpec extends Specification {
    RecruiterJobLikeService likeService = Mock()
    MatchService matchService = Mock()
    RecruiterLikeWebController controller

    UUID recruiterId = UUID.randomUUID()
    UUID jobId = UUID.randomUUID()
    UUID candidateId = UUID.randomUUID()

    def setup() {
        controller = new RecruiterLikeWebController(likeService, matchService, new JsonSlurper())
    }

    def "reads job and candidate IDs from recruiter like route"() {
        given:
        HttpServletRequest request = request("POST", "/jobs/${jobId}/candidates/${candidateId}")
        HttpServletResponse response = response()
        RecruiterLikeResultDTO result = new RecruiterLikeResultDTO(
                recruiterId, jobId, candidateId, RecruiterJobLike.Status.ACTIVE, null, null)
        likeService.like(recruiterId, jobId, candidateId) >> result

        when:
        controller.service(request, response)

        then:
        1 * likeService.like(recruiterId, jobId, candidateId)
        1 * response.setStatus(HttpServletResponse.SC_OK)
    }

    def "reads candidate and job IDs from recruiter match route"() {
        given:
        HttpServletRequest request = request("GET", "/matches/${candidateId}/${jobId}")
        HttpServletResponse response = response()

        when:
        controller.service(request, response)

        then:
        1 * matchService.getAuthorizedMatch(
                candidateId, jobId, recruiterId, "RECRUITER", recruiterId)
        1 * response.setStatus(HttpServletResponse.SC_OK)
    }

    private HttpServletRequest request(String method, String path) {
        HttpServletRequest request = Mock()
        request.method >> method
        request.pathInfo >> path
        request.getHeader("X-User-Id") >> recruiterId.toString()
        request.getHeader("X-User-Role") >> "RECRUITER"
        request
    }

    private HttpServletResponse response() {
        HttpServletResponse response = Mock()
        response.writer >> new PrintWriter(new StringWriter())
        response
    }
}
