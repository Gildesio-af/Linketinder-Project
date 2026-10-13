package zg.acelera.user_interface

import zg.acelera.controller.CandidateController
import zg.acelera.controller.JobController
import zg.acelera.controller.MatchController
import zg.acelera.dto.candidate.CandidateResponseDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.job.JobMatchResponseDTO
import zg.acelera.dto.like.LikeCreateDTO
import zg.acelera.dto.match.MatchResponseDTO
import zg.acelera.controller.LikeController
import zg.acelera.utils.reader.InputReader

class CompanyMatchUI {
    private final CandidateController candidateController
    private final JobController jobController
    private final LikeController likeController
    private final MatchController matchController
    private final InputReader input

    CompanyMatchUI(CandidateController candidateController,
                   JobController jobController,
                   LikeController likeController,
                   MatchController matchController,
                   InputReader input) {
        this.candidateController = candidateController
        this.jobController = jobController
        this.likeController = likeController
        this.matchController = matchController
        this.input = input
    }

    void start(CompanyResponseDTO company) {
        println "\nWelcome, ${company.name()}!"
        companyLoop(company)
    }

    private void companyLoop(CompanyResponseDTO company) {
        boolean running = true

        while (running) {
            showOptions()
            String option = input.readString("Choose an option: ")

            try {
                switch (option) {
                    case "1" -> viewCandidates()
                    case "2" -> likeCandidate(company)
                    case "3" -> unlikeCandidate(company)
                    case "4" -> showCompanyMatches(company)
                    case "0" -> {
                        println "Logging out..."
                        running = false
                    }
                    default -> println "Invalid option. Please try again."
                }
            } catch (IllegalArgumentException e) {
                println "Action failed: ${e.message}"
            }
        }
    }

    private static void showOptions() {
        println """
        ========================================
                  LINKETINDER - COMPANY FEED
        ========================================
        1. View candidates
        2. Like a candidate
        3. Unlike a candidate
        4. Show my matches
        0. Logout
        ========================================
        """
    }

    private void viewCandidates() {
        println "\n--- CANDIDATES ---"
        List<CandidateResponseDTO> candidates = candidateController.listAll()

        if (!candidates) {
            println "No candidates available at the moment."
            return
        }

        candidates.each { candidate ->
            String skillsStr = candidate.skills()*.name().join(', ')
            println "[CPF: ${candidate.cpf()}] - Skills: ${skillsStr} | Description: ${candidate.description()}"
        }
    }

    private void likeCandidate(CompanyResponseDTO company) {
        String cpf = input.readString("Enter the CPF of the candidate you want to like: ")
        CandidateResponseDTO candidate = candidateController.findByCpf(cpf)

        List<MatchResponseDTO> matches = likeController.likeCandidate(new LikeCreateDTO(candidate.id(), company.id()))
        println "Candidate liked successfully."

        if (matches && !matches.isEmpty()) {
            println "*** IT'S A MATCH! ${matches.size()} new match(es) with your jobs. ***"
        }
    }

    private void unlikeCandidate(CompanyResponseDTO company) {
        String cpf = input.readString("Enter the CPF of the candidate you want to unlike: ")
        CandidateResponseDTO candidate = candidateController.findByCpf(cpf)

        likeController.unlikeCandidate(new LikeCreateDTO(candidate.id(), company.id()))
        println "Candidate unliked successfully."
    }

    private void showCompanyMatches(CompanyResponseDTO company) {
        println "\n--- MY MATCHES ---"
        List<MatchResponseDTO> matches = matchController.getMatchesByCompany(company.id())

        if (!matches) {
            println "You don't have any matches yet."
            return
        }

        matches.each { match ->
            CandidateResponseDTO candidate = candidateController.findById(match.candidateId())
            JobMatchResponseDTO job = jobController.getJobByIdToMatch(match.jobId())

            println "Candidate: ${candidate.name()} ${candidate.lastName()} (${candidate.email()}) | " +
                    "Job: ${job.name()} | Matched at: ${match.createdAt()}"
        }
    }
}