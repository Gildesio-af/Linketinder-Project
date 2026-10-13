package zg.acelera.user_interface

import zg.acelera.controller.JobController
import zg.acelera.dto.candidate.CandidateResponseDTO
import zg.acelera.dto.job.JobMatchResponseDTO
import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.dto.like.LikeCreateDTO
import zg.acelera.dto.match.MatchResponseDTO
import zg.acelera.controller.LikeController
import zg.acelera.controller.MatchController
import zg.acelera.utils.reader.InputReader

class CandidateMatchUI {
    private final JobController jobController
    private final LikeController likeController
    private final MatchController matchController
    private final InputReader input

    CandidateMatchUI(JobController jobController,
                     LikeController likeController,
                     MatchController matchController,
                     InputReader input) {
        this.jobController = jobController
        this.likeController = likeController
        this.matchController = matchController
        this.input = input
    }

    void start(CandidateResponseDTO candidate) {
        println "\nWelcome, ${candidate.name()}!"
        candidateLoop(candidate)
    }

    private void candidateLoop(CandidateResponseDTO candidate) {
        boolean running = true

        while (running) {
            showOptions()
            String option = input.readString("Choose an option: ")

            try {
                switch (option) {
                    case "1" -> viewJobs()
                    case "2" -> likeJob(candidate)
                    case "3" -> unlikeJob(candidate)
                    case "4" -> showCandidateMatches(candidate)
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

    private void showOptions() {
        println """
        ========================================
                 LINKETINDER - CANDIDATE FEED
        ========================================
        1. View available jobs
        2. Like a job
        3. Unlike a job
        4. Show my matches
        0. Logout
        ========================================
        """
    }

    private void viewJobs() {
        println "\n--- AVAILABLE JOBS ---"
        Set<JobResponseDTO> jobs = jobController.getAllJobs()

        if (!jobs) {
            println "No jobs available at the moment."
            return
        }

        jobs.each { job ->
            String skillsStr = job.skills()*.name.join(', ')
            println "[ID: ${job.id()}] - ${job.name()} | Skills: ${skillsStr} | Description: ${job.description()}"
        }
    }

    private void likeJob(CandidateResponseDTO candidate) {
        UUID jobId = input.readUUID("Enter the job ID you want to like: ")
        if (!jobId) return

        MatchResponseDTO match = likeController.likeJob(new LikeCreateDTO(jobId, candidate.id()))
        println "Job liked successfully."
        if (match) {
            println "*** IT'S A MATCH! The company also liked you. ***"
        }
    }

    private void unlikeJob(CandidateResponseDTO candidate) {
        UUID jobId = input.readUUID("Enter the job ID you want to unlike: ")
        if (!jobId) return

        likeController.unlikeJob(new LikeCreateDTO(jobId, candidate.id()))
        println "Job unliked successfully."
    }

    private void showCandidateMatches(CandidateResponseDTO candidate) {
        println "\n--- MY MATCHES ---"
        List<MatchResponseDTO> matches = matchController.getMatchesByCandidate(candidate.id())

        if (!matches) {
            println "You don't have any matches yet."
            return
        }

        matches.each { match ->
            JobMatchResponseDTO job = jobController.getJobByIdToMatch(match.jobId())
            String companyName = job.companyResponseDTO().name()
            String companyEmail = job.companyResponseDTO()?.email()

            println "Job: ${job.name()} | Company: ${companyName} (${companyEmail}) | Matched at: ${match.createdAt()}"
        }
    }
}
