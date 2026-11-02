package zg.acelera.controller

import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.company.CompanyCreateDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.company.CompanyUpdateDTO
import zg.acelera.service.CompanyService

class CompanyWebController extends BaseWebController {
    private final CompanyService companyService

    CompanyWebController(CompanyService companyService, JsonSlurper jsonSlurper) {
        super(jsonSlurper)
        this.companyService = companyService
        mapRoutes()
    }

    // GET /companies/{uuid}
    private void listCompanyById(HttpServletRequest req, HttpServletResponse resp) {
        String uuid = req.pathInfo.split("/")[1]
        CompanyResponseDTO company = companyService.listCompanyById(UUID.fromString(uuid))
        answerJson(resp, HttpServletResponse.SC_OK, company)
    }

    // GET /companies/skills/{skillName}
    private void listBySkillPath(HttpServletRequest req, HttpServletResponse resp) {
        String skillName = req.pathInfo.split("/")[2]
        List<CompanyResponseDTO> companies = companyService.listCompaniesBySkill(skillName)
        answerJson(resp, HttpServletResponse.SC_OK, companies)
    }

    // GET /companies/jobs/{jobId}
    private void findByJobIdPath(HttpServletRequest req, HttpServletResponse resp) {
        UUID jobId = UUID.fromString(req.pathInfo.split("/")[2])
        CompanyResponseDTO company = companyService.getCompanyByJobId(jobId)
        answerJson(resp, HttpServletResponse.SC_OK, company)
    }


    // POST /companies
    private void register(HttpServletRequest req, HttpServletResponse resp) {
        Map body = readBody(req)

        Map companyData = (body.company ?: body) as Map
        Map addressData = body.address as Map

        CompanyCreateDTO companyDTO = CompanyCreateDTO.fromMap(companyData)

        AddressCreateDTO addressDTO = addressData ? AddressCreateDTO.fromMap(addressData) : null

        CompanyResponseDTO created = companyService.registerCompany(companyDTO, addressDTO)
        answerJson(resp, HttpServletResponse.SC_CREATED, created)
    }


    // PUT /companies/{id}
    private void updateById(HttpServletRequest req, HttpServletResponse resp) {
        UUID userId = UUID.fromString(req.pathInfo.substring(1))
        Map body = readBody(req)

        CompanyUpdateDTO updateDTO = CompanyUpdateDTO.fromMap(body)

        CompanyResponseDTO updated = companyService.updateCompany(updateDTO, userId)
        answerJson(resp, HttpServletResponse.SC_OK, updated)
    }

    // DELETE /companies/{id}
    private void deleteById(HttpServletRequest req, HttpServletResponse resp) {
        UUID id = UUID.fromString(req.pathInfo.substring(1))

        companyService.deleteCompany(id)
        answerJson(resp, HttpServletResponse.SC_NO_CONTENT, null)
    }

    private void mapRoutes() {
        get("/${UUID_REGEX}", this.&listCompanyById)       // GET /companies/{id}
        get("/skills/[^/]+", this.&listBySkillPath)        // GET /companies/skill/{skillName}
        get("/jobs/${UUID_REGEX}", this.&findByJobIdPath)  // GET /companies/job/{jobId}

        post("/", this.&register)                         // POST /companies
        put("/${UUID_REGEX}", this.&updateById)           // PUT /companies/{id}
        delete("/${UUID_REGEX}", this.&deleteById)        // DELETE /companies/{id}
    }
}
