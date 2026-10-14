package zg.acelera.controller

import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.dto.candidate.CandidateResponseDTO
import zg.acelera.dto.candidate.CandidateUpdateDTO
import zg.acelera.service.CandidateService

class CandidateWebController extends BaseWebController {
    private final CandidateService candidateService

    CandidateWebController(CandidateService candidateService, JsonSlurper jsonSlurper) {
        super(jsonSlurper)
        this.candidateService = candidateService
        mapRoutes()
    }

    // GET /candidates/{uuid}
    private void findById(HttpServletRequest req, HttpServletResponse resp) {
        UUID id = UUID.fromString(req.pathInfo.substring(1))
        CandidateResponseDTO candidate = candidateService.findCandidateById(id)
        answerJson(resp, HttpServletResponse.SC_OK, candidate)
    }

    // GET /candidates/cpf/{cpf}
    private void findByCpfPath(HttpServletRequest req, HttpServletResponse resp) {
        String cpf = req.pathInfo.split("/")[2]
        CandidateResponseDTO candidate = candidateService.listCandidateByCpf(cpf)
        answerJson(resp, HttpServletResponse.SC_OK, candidate)
    }

    // GET /candidates/skills/{skillName}
    private void listBySkillPath(HttpServletRequest req, HttpServletResponse resp) {
        String skillName = req.pathInfo.split("/")[2]
        List<CandidateResponseDTO> candidates = candidateService.listCandidatesBySkill(skillName)
        answerJson(resp, HttpServletResponse.SC_OK, candidates)
    }

    // POST /candidates
    private void register(HttpServletRequest req, HttpServletResponse resp) {
        Map body = readBody(req)

        Map candidateData = (body.candidate ?: body) as Map
        Map addressData = (body.address ?: body) as Map

        CandidateDTO candidateDTO = CandidateDTO.fromMap(candidateData)
        AddressCreateDTO addressDTO = AddressCreateDTO.fromMap(addressData)

        CandidateResponseDTO created = candidateService.registerCandidate(candidateDTO, addressDTO)
        answerJson(resp, HttpServletResponse.SC_CREATED, created)
    }

    // PUT /candidates/{uuid}
    private void update(HttpServletRequest req, HttpServletResponse resp) {
        UUID candidateId = UUID.fromString(req.pathInfo.substring(1))
        CandidateUpdateDTO updateDTO = getUpdateDTOFromRequest(req)
        CandidateResponseDTO updated = candidateService.updateCandidate(updateDTO, candidateId)
        answerJson(resp, HttpServletResponse.SC_OK, updated)
    }

    // DELETE /candidates/{uuid}
    private void deleteById(HttpServletRequest req, HttpServletResponse resp) {
        UUID candidateId = UUID.fromString(req.pathInfo.substring(1))
        candidateService.deleteCandidateById(candidateId)
        answerJson(resp, HttpServletResponse.SC_NO_CONTENT, null)
    }

    private CandidateUpdateDTO getUpdateDTOFromRequest(HttpServletRequest req) {
        Map body = readBody(req)
        return CandidateUpdateDTO.fromMap(body)
    }

    private void mapRoutes() {
        get("/${UUID_REGEX}", this.&findById)           // GET /candidates/{uuid}
        get("/cpf/[^/]+", this.&findByCpfPath)          // GET /candidates/cpf/{cpf}
        get("/skills/[^/]+", this.&listBySkillPath)      // GET /candidates/skill/{skillName}

        post("/", this.&register)                       // POST /candidates
        put("/${UUID_REGEX}", this.&update)             // PUT /candidates/{uuid}
        delete("/${UUID_REGEX}", this.&deleteById)      // DELETE /candidates/{uuid}
    }
}