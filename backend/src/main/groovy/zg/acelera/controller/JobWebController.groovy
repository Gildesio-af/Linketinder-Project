package zg.acelera.controller

import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.job.JobCreateDTO
import zg.acelera.dto.job.JobMatchResponseDTO
import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.dto.job.JobUpdateDTO
import zg.acelera.service.JobService

class JobWebController extends BaseWebController {
    private final JobService jobService

    JobWebController(JobService jobService, JsonSlurper jsonSlurper) {
        super(jsonSlurper)
        this.jobService = jobService
        mapRoutes()
    }

    // GET /jobs
    private void listAllJobs(HttpServletRequest req, HttpServletResponse resp) {
        Set<JobResponseDTO> jobs = jobService.getAllJobs()
        answerJson(resp, HttpServletResponse.SC_OK, jobs)
    }

    // GET /jobs/{uuid}
    private void getJobById(HttpServletRequest req, HttpServletResponse resp) {
        UUID id = UUID.fromString(req.pathInfo.split("/")[1])
        JobMatchResponseDTO job = jobService.getJobByIdToMatch(id)
        answerJson(resp, HttpServletResponse.SC_OK, job)
    }

    // GET /jobs/publishers/{publisherId}
    private void listByPublisherPath(HttpServletRequest req, HttpServletResponse resp) {
        UUID publisherId = UUID.fromString(req.pathInfo.split("/")[2])
        Set<JobResponseDTO> jobs = jobService.getJobsByPublisher(publisherId)
        answerJson(resp, HttpServletResponse.SC_OK, jobs)
    }

    // GET /jobs/names/{name}
    private void listByNamePath(HttpServletRequest req, HttpServletResponse resp) {
        String name = req.pathInfo.split("/")[2]
        Set<JobResponseDTO> jobs = jobService.getJobsByName(name)
        answerJson(resp, HttpServletResponse.SC_OK, jobs)
    }

    // GET /jobs/skills/{skillName}
    private void listBySkillPath(HttpServletRequest req, HttpServletResponse resp) {
        String skillName = req.pathInfo.split("/")[2]
        Set<JobResponseDTO> jobs = jobService.getJobsBySkill(skillName)
        answerJson(resp, HttpServletResponse.SC_OK, jobs)
    }

    // POST /jobs
    private void create(HttpServletRequest req, HttpServletResponse resp) {
        Map body = readBody(req)

        Map jobData = (body.job ?: body) as Map
        Map addressData = (body.address ?: body) as Map

        JobCreateDTO jobDTO = JobCreateDTO.fromMap(jobData)
        AddressCreateDTO addressDTO = AddressCreateDTO.fromMap(addressData)

        JobResponseDTO created = jobService.createJobWithSkillsId(jobDTO, addressDTO)
        answerJson(resp, HttpServletResponse.SC_CREATED, created)
    }

    // POST /jobs/{id}/skills
    private void addSkills(HttpServletRequest req, HttpServletResponse resp) {
        UUID jobId = UUID.fromString(req.pathInfo.split("/")[1])
        Map body = readBody(req)

        List<String> skills = (body.skills ?: []) as List<String>

        jobService.addJobSkills(jobId, skills)
        answerJson(resp, HttpServletResponse.SC_NO_CONTENT, null)
    }

    // PUT /jobs/{id}
    private void updateById(HttpServletRequest req, HttpServletResponse resp) {
        UUID jobId = UUID.fromString(req.pathInfo.substring(1))
        Map body = readBody(req)

        JobUpdateDTO updateDTO = JobUpdateDTO.fromMap(body)

        JobResponseDTO updated = jobService.updateJob(updateDTO, jobId)
        answerJson(resp, HttpServletResponse.SC_OK, updated)
    }

    // DELETE /jobs/{id}
    private void deleteById(HttpServletRequest req, HttpServletResponse resp) {
        UUID id = UUID.fromString(req.pathInfo.substring(1))

        jobService.deleteJob(id)
        answerJson(resp, HttpServletResponse.SC_NO_CONTENT, null)
    }

    private void mapRoutes() {
        get("/", this.&listAllJobs)                                 // GET /jobs
        get("/${UUID_REGEX}", this.&getJobById)                     // GET /jobs/{id}
        get("/publishers/${UUID_REGEX}", this.&listByPublisherPath) // GET /jobs/publishers/{publisherId}
        get("/names/[^/]+", this.&listByNamePath)                   // GET /jobs/names/{name}
        get("/skills/[^/]+", this.&listBySkillPath)                 // GET /jobs/skills/{skillName}

        post("/", this.&create)                                     // POST /jobs
        post("/${UUID_REGEX}/skills", this.&addSkills)              // POST /jobs/{id}/skills
        put("/${UUID_REGEX}", this.&updateById)                     // PUT /jobs/{id}
        delete("/${UUID_REGEX}", this.&deleteById)                  // DELETE /jobs/{id}
    }
}