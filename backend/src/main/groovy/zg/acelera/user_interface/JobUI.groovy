package zg.acelera.user_interface

import zg.acelera.dto.address.AddressDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.dto.job.JobDTO
import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.dto.job.JobUpdateDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.service.CompanyService
import zg.acelera.service.CountryService
import zg.acelera.service.JobService
import zg.acelera.service.SkillService
import zg.acelera.utils.DataManager

class JobUI {
    private final JobService jobService
    private final CompanyService companyService
    private final SkillService skillService
    private final CountryService countryService
    private final InputReader input

    JobUI(JobService jobService, CompanyService companyService, SkillService skillService, CountryService countryService, InputReader inputReader) {
        this.jobService = jobService
        this.companyService = companyService
        this.skillService = skillService
        this.countryService = countryService
        this.input = inputReader
    }

    void manageJobs() {
        println "\n=== COMPANY LOGIN ==="
        String cnpj = input.readString("Enter your CNPJ to login: ")

        CompanyResponseDTO company = companyService.listCompanyByCnpj(cnpj)
        if (!company) {
            println "Error: No company found with CNPJ: ${cnpj}. Login failed."
            return
        }

        println "\nLogin successful! Welcome, ${company.name()}."
        jobManagementMenu(company)
    }

    private void jobManagementMenu(CompanyResponseDTO company) {
        boolean running = true

        while (running) {
            println """
            ========================================
              JOB MANAGEMENT - ${company.name()}
            ========================================
            1. View my jobs
            2. Create a new job
            3. Update a job
            4. Delete a job
            0. Go back to Company Menu
            ========================================
            """
            String option = input.readString("Choose an option: ")

            switch (option) {
                case "1": viewMyJobs(company); break
                case "2": createJob(company); break
                case "3": updateJob(company); break
                case "4": deleteJob(company); break
                case "0":
                    println "Going back to the Company Menu..."
                    running = false
                    break
                default:
                    println "Invalid option. Please try again."
            }
        }
    }

    private void viewMyJobs(CompanyResponseDTO company) {
        println "\n=== MY JOBS ==="
        Set<JobResponseDTO> jobs = jobService.getJobsByPublisher(company.id())
        if (!jobs || jobs.isEmpty()) {
            println "You don't have any jobs registered yet."
            return
        }

        jobs.each { job -> printJobResponse(job) }
    }

    private void createJob(CompanyResponseDTO company) {
        println "\n=== CREATE NEW JOB ==="
        try {
            String title = input.readString("Job Title: ")
            String description = input.readString("Job Description: ")

            List<String> skillNames = DataManager.readSkillsFromDatabase() as List<String>
            if (!skillNames || skillNames.isEmpty()) {
                println "Error: No skills selected. Job creation canceled."
                return
            }

            println "\n--- JOB LOCATION ---"
            AddressDTO addressDTO = DataManager.readAddressData()

            JobResponseDTO result = jobService.createJobWithAddress(title, description, skillNames, addressDTO, company.id())

            if (result) {
                println "\nJob created successfully!"
                printJobResponse(result)
            }

        } catch (IllegalArgumentException e) {
            println "Validation error: ${e.message}"
        } catch (Exception e) {
            println "Error: ${e.message}"
        }
    }

    private void updateJob(CompanyResponseDTO company) {
        println "\n=== UPDATE JOB ==="

        Set<JobResponseDTO> jobs = jobService.getJobsByPublisher(company.id())
        if (!jobs || jobs.isEmpty()) {
            println "You don't have any jobs to update."
            return
        }

        println "Your jobs:"
        List<JobResponseDTO> jobList = jobs.toList()
        jobList.eachWithIndex { job, index ->
            println "  ${index + 1}. ${job.name()} - ${job.description()}"
        }

        Integer jobChoice = input.readInt("Select the job number to update: ")
        if (jobChoice == null || jobChoice < 1 || jobChoice > jobList.size()) {
            println "Invalid selection."
            return
        }

        JobResponseDTO selectedJob = jobList[jobChoice - 1]
        println "\n--- Enter the new values or press ENTER to keep the current value ---"
        println "Current Title: ${selectedJob.name()}"
        String newTitle = input.readString("New Title: ", false)

        println "Current Description: ${selectedJob.description()}"
        String newDescription = input.readString("New Description: ", false)

        try {
            JobUpdateDTO updateDTO = new JobUpdateDTO(newTitle, newDescription, null)
            JobResponseDTO result = jobService.updateJob(updateDTO, selectedJob.id())

            if (result) {
                println "\nJob updated successfully!"
                printJobResponse(result)
            }
        } catch (Exception e) {
            println "Error updating job: ${e.message}"
        }
    }

    private void deleteJob(CompanyResponseDTO company) {
        println "\n=== DELETE JOB ==="

        Set<JobResponseDTO> jobs = jobService.getJobsByPublisher(company.id())
        if (!jobs || jobs.isEmpty()) {
            println "You don't have any jobs to delete."
            return
        }

        println "Your jobs:"
        List<JobResponseDTO> jobList = jobs.toList()
        jobList.eachWithIndex { job, index ->
            println "  ${index + 1}. ${job.name()} - ${job.description()}"
        }

        Integer jobChoice = input.readInt("Select the job number to delete: ")
        if (jobChoice == null || jobChoice < 1 || jobChoice > jobList.size()) {
            println "Invalid selection."
            return
        }

        JobResponseDTO selectedJob = jobList[jobChoice - 1]
        String confirm = input.readString("Are you sure you want to delete '${selectedJob.name()}'? (yes/no): ")
        if (confirm?.toLowerCase() == "yes") {
            jobService.deleteJob(selectedJob.id())
            println "Job deleted successfully."
        } else {
            println "Deletion canceled."
        }
    }

    void candidateViewJobsMenu() {
        boolean running = true

        while (running) {
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

    private void viewAllJobs() {
        println "\n=== ALL JOBS ==="
        Set<JobResponseDTO> jobs = jobService.getAllJobs()
        if (!jobs || jobs.isEmpty()) {
            println "No jobs available at the moment."
            return
        }
        jobs.each { printJobResponse(it) }
    }

    private void searchJobsByName() {
        println "\n=== SEARCH JOBS BY NAME ==="
        String name = input.readString("Enter the job name to search: ")
        Set<JobResponseDTO> jobs = jobService.getJobsByName(name)
        if (!jobs || jobs.isEmpty()) {
            println "No jobs found matching: ${name}"
            return
        }
        jobs.each { printJobResponse(it) }
    }

    private void searchJobsBySkill() {
        println "\n=== SEARCH JOBS BY SKILL ==="
        String skill = input.readString("Enter the skill name to search (e.g. JAVA): ")
        Set<JobResponseDTO> jobs = jobService.getJobsBySkill(skill)
        if (!jobs || jobs.isEmpty()) {
            println "No jobs found with skill: ${skill}"
            return
        }
        jobs.each { printJobResponse(it) }
    }

    private static void printJobResponse(JobResponseDTO job) {
        println "------------------------------"
        println "  ID: ${job.id()}"
        println "  Title: ${job.name()}"
        println "  Description: ${job.description()}"
        if (job.skills()) {
            println "  Skills: ${job.skills().collect { it.name }.join(', ')}"
        }
        if (job.addressResponseDTO()) {
            def addr = job.addressResponseDTO()
            println "  Location: ${addr.street()}, ${addr.number()} - ${addr.neighborhood()}, ${addr.city()} - ${addr.state()}, ${addr.cep()}"
            if (addr.country()) {
                println "  Country: ${addr.country().name()} (${addr.country().code()})"
            }
        }
        println "------------------------------"
    }
}
