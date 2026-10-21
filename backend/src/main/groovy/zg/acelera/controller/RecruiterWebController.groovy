package zg.acelera.controller

import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import zg.acelera.dto.recruiter.RecruiterCreateDTO
import zg.acelera.dto.recruiter.RecruiterUpdateDTO
import zg.acelera.service.CandidateService
import zg.acelera.service.RecruiterService

class RecruiterWebController extends BaseWebController {
    private final RecruiterService recruiterService
    private final CandidateService candidateService

    RecruiterWebController(RecruiterService recruiterService, CandidateService candidateService,
                           JsonSlurper jsonSlurper) {
        super(jsonSlurper)
        this.recruiterService = recruiterService
        this.candidateService = candidateService
        mapRoutes()
    }

    private void register(HttpServletRequest req, HttpServletResponse resp) {
        answerJson(resp, HttpServletResponse.SC_CREATED,
                recruiterService.register(RecruiterCreateDTO.fromMap(readBody(req))))
    }

    private void profile(HttpServletRequest req, HttpServletResponse resp) {
        UUID requestedId = pathUuid(req, 0)
        if (authenticatedUserId(req, "RECRUITER") != requestedId)
            throw new SecurityException("Recruiters may only access their own profile.")
        answerJson(resp, HttpServletResponse.SC_OK, recruiterService.findOwnProfile(requestedId))
    }

    private void update(HttpServletRequest req, HttpServletResponse resp) {
        UUID authenticatedId = authenticatedUserId(req, "RECRUITER")
        UUID requestedId = pathUuid(req, 0)
        if (authenticatedId != requestedId)
            throw new SecurityException("Recruiters may only update their own profile.")
        answerJson(resp, HttpServletResponse.SC_OK,
                recruiterService.updateProfile(authenticatedId, RecruiterUpdateDTO.fromMap(readBody(req))))
    }

    private void acceptedJobs(HttpServletRequest req, HttpServletResponse resp) {
        UUID requestedId = pathUuid(req, 0)
        if (authenticatedUserId(req, "RECRUITER") != requestedId)
            throw new SecurityException("Recruiters may only access their own jobs.")
        answerJson(resp, HttpServletResponse.SC_OK, candidateService.listAcceptedJobsForRecruiter(requestedId))
    }

    private void anonymousCandidates(HttpServletRequest req, HttpServletResponse resp) {
        UUID recruiterId = authenticatedUserId(req, "RECRUITER")
        UUID jobId = pathUuid(req, 1)
        answerJson(resp, HttpServletResponse.SC_OK,
                candidateService.listAnonymousCandidatesForRecruiterJob(recruiterId, jobId))
    }

    private void mapRoutes() {
        post("/", this.&register)
        get("/${UUID_REGEX}", this.&profile)
        patch("/${UUID_REGEX}", this.&update)
        put("/${UUID_REGEX}", this.&update)
        get("/${UUID_REGEX}/jobs", this.&acceptedJobs)
        get("/jobs/${UUID_REGEX}/candidates", this.&anonymousCandidates)
    }
}
