package zg.acelera.controller

import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import zg.acelera.dto.auth.AuthResponseDTO
import zg.acelera.service.MatchService
import zg.acelera.service.RecruiterJobLikeService

class RecruiterLikeWebController extends BaseWebController {
    private final RecruiterJobLikeService likeService
    private final MatchService matchService

    RecruiterLikeWebController(RecruiterJobLikeService likeService, MatchService matchService,
                               JsonSlurper jsonSlurper) {
        super(jsonSlurper)
        this.likeService = likeService
        this.matchService = matchService
        mapRoutes()
    }

    private void like(HttpServletRequest req, HttpServletResponse resp) {
        UUID recruiterId = authenticatedUserId(req, "RECRUITER")
        answerJson(resp, HttpServletResponse.SC_OK,
                likeService.like(recruiterId, pathUuid(req, 1), pathUuid(req, 3)))
    }

    private void unlike(HttpServletRequest req, HttpServletResponse resp) {
        UUID recruiterId = authenticatedUserId(req, "RECRUITER")
        answerJson(resp, HttpServletResponse.SC_OK,
                likeService.unlike(recruiterId, pathUuid(req, 1), pathUuid(req, 3)))
    }

    private void match(HttpServletRequest req, HttpServletResponse resp) {
        UUID recruiterId = authenticatedUserId(req, "RECRUITER")
        UUID candidateId = pathUuid(req, 1)
        UUID jobId = pathUuid(req, 2)
        answerJson(resp, HttpServletResponse.SC_OK,
                matchService.getAuthorizedMatch(candidateId, jobId, recruiterId,
                        AuthResponseDTO.RECRUITER_ROLE, recruiterId))
    }

    private void mapRoutes() {
        post("/jobs/${UUID_REGEX}/candidates/${UUID_REGEX}", this.&like)
        delete("/jobs/${UUID_REGEX}/candidates/${UUID_REGEX}", this.&unlike)
        get("/matches/${UUID_REGEX}/${UUID_REGEX}", this.&match)
    }
}
