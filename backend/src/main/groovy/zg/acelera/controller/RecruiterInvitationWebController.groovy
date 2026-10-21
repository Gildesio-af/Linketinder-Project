package zg.acelera.controller

import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import zg.acelera.service.RecruiterInvitationService

class RecruiterInvitationWebController extends BaseWebController {
    private final RecruiterInvitationService invitationService

    RecruiterInvitationWebController(RecruiterInvitationService invitationService, JsonSlurper jsonSlurper) {
        super(jsonSlurper)
        this.invitationService = invitationService
        mapRoutes()
    }

    private void create(HttpServletRequest req, HttpServletResponse resp) {
        Map body = readBody(req)
        UUID companyId = authenticatedUserId(req, "COMPANY")
        UUID recruiterId = UUID.fromString(body.recruiterId as String)
        UUID jobId = UUID.fromString(body.jobId as String)
        answerJson(resp, HttpServletResponse.SC_CREATED, invitationService.invite(companyId, recruiterId, jobId))
    }

    private void listForRecruiter(HttpServletRequest req, HttpServletResponse resp) {
        answerJson(resp, HttpServletResponse.SC_OK,
                invitationService.listForRecruiter(authenticatedUserId(req, "RECRUITER")))
    }

    private void listForCompany(HttpServletRequest req, HttpServletResponse resp) {
        answerJson(resp, HttpServletResponse.SC_OK,
                invitationService.listForCompany(authenticatedUserId(req, "COMPANY")))
    }

    private void accept(HttpServletRequest req, HttpServletResponse resp) {
        answerJson(resp, HttpServletResponse.SC_OK,
                invitationService.accept(authenticatedUserId(req, "RECRUITER"), pathUuid(req, 0)))
    }

    private void decline(HttpServletRequest req, HttpServletResponse resp) {
        answerJson(resp, HttpServletResponse.SC_OK,
                invitationService.decline(authenticatedUserId(req, "RECRUITER"), pathUuid(req, 0)))
    }

    private void cancel(HttpServletRequest req, HttpServletResponse resp) {
        answerJson(resp, HttpServletResponse.SC_OK,
                invitationService.cancel(authenticatedUserId(req, "COMPANY"), pathUuid(req, 0)))
    }

    private void revoke(HttpServletRequest req, HttpServletResponse resp) {
        answerJson(resp, HttpServletResponse.SC_OK,
                invitationService.revoke(authenticatedUserId(req, "COMPANY"), pathUuid(req, 0)))
    }

    private void mapRoutes() {
        post("/", this.&create)
        get("/recruiter", this.&listForRecruiter)
        get("/company", this.&listForCompany)
        post("/${UUID_REGEX}/accept", this.&accept)
        post("/${UUID_REGEX}/decline", this.&decline)
        post("/${UUID_REGEX}/cancel", this.&cancel)
        post("/${UUID_REGEX}/revoke", this.&revoke)
    }
}
