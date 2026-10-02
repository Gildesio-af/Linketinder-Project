package zg.acelera.user_interface

import zg.acelera.controller.JobController
import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.utils.reader.AddressConsoleReader
import zg.acelera.utils.reader.InputReader
import zg.acelera.utils.reader.SkillConsoleReader

class CandidateJobUI extends JobUI {
    CandidateJobUI(JobController jobController, InputReader input, AddressConsoleReader addressReader, SkillConsoleReader skillReader) {
        super (jobController, input, addressReader, skillReader)
    }

    void candidateViewJobsMenu() {
        boolean running = true

        while (running) {
            printMenuToCandidate()
            String option = input.readString("Choose an option: ")

            switch (option) {
                case "1":
                    viewAllJobs()
                    break
                case "2":
                    searchJobsByName()
                    break
                case "3":
                    searchJobsBySkill()
                    break
                case "0":
                    running = false
                    break
                default:
                    println "Invalid option. Please try again."
            }
        }
    }

    private void printMenuToCandidate() {
        println """
            ========================================
                 AVAILABLE JOBS
            ========================================
            1. View all jobs
            2. Search jobs by name
            3. Search jobs by skill
            0. Go back
            ========================================
            """
    }

    private void viewAllJobs() {
        println "\n=== ALL JOBS ==="
        Set<JobResponseDTO> jobs = jobController.getAllJobs()
        displayJobs(jobs, "No jobs available at the moment.")
    }

    private void searchJobsByName() {
        println "\n=== SEARCH JOBS BY NAME ==="
        String name = input.readString("Enter the job name to search: ")
        Set<JobResponseDTO> jobsByName = jobController.getJobsByName(name)
        String emptyMessage = "No jobs found matching: ${name}"
        displayJobs(jobsByName, emptyMessage)
    }

    private void searchJobsBySkill() {
        println "\n=== SEARCH JOBS BY SKILL ==="
        String skill = input.readString("Enter the skill name to search (e.g. JAVA): ")
        Set<JobResponseDTO> jobs = jobController.getJobsBySkill(skill)
        String emptyMessage = "No jobs found with skill: ${skill}"
        displayJobs(jobs, emptyMessage)
    }
}
