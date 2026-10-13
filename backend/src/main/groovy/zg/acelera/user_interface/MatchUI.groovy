package zg.acelera.user_interface

import zg.acelera.controller.CandidateController
import zg.acelera.controller.CompanyController
import zg.acelera.controller.JobController
import zg.acelera.controller.LikeController
import zg.acelera.controller.MatchController
import zg.acelera.dto.auth.AuthRequestDTO
import zg.acelera.dto.auth.AuthResponseDTO
import zg.acelera.dto.candidate.CandidateResponseDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.job.JobMatchResponseDTO
import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.dto.like.LikeCreateDTO
import zg.acelera.dto.match.MatchResponseDTO
import zg.acelera.service.AuthService
import zg.acelera.utils.reader.InputReader

class MatchUI {
    private final AuthService authService
    private final CandidateController candidateController
    private final CompanyController companyController
    private final CandidateMatchUI candidateUI
    private final CompanyMatchUI companyUI
    private final InputReader input

    MatchUI(AuthService authService,
            CandidateController candidateController,
            CompanyController companyController,
            CandidateMatchUI candidateUI,
            CompanyMatchUI companyUI,
            InputReader input) {
        this.authService = authService
        this.candidateController = candidateController
        this.companyController = companyController
        this.candidateUI = candidateUI
        this.companyUI = companyUI
        this.input = input
    }

    void showMenu() {
        println "\n=== LINKETINDER - MATCH SYSTEM ==="

        try {
            AuthRequestDTO authDTO = input.readAuthDTO()
            AuthResponseDTO authResponse = authService.authenticate(authDTO)

            if (authResponse.role() == "CANDIDATE") {
                CandidateResponseDTO candidate = candidateController.findByCpf(authDTO.username())
                candidateUI.start(candidate)

            } else if (authResponse.role() == "COMPANY") {
                CompanyResponseDTO company = companyController.findByCnpj(authDTO.username())
                companyUI.start(company)
            } else {
                println "Invalid format or role."
            }
        } catch (IllegalArgumentException e) {
            println "Validation Error: ${e.message}"
        } catch (Exception e) {
            println "Unexpected Error occurred:"
            e.printStackTrace()
        }
    }
}
