package zg.acelera.user_interface

import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.job.JobCreateDTO
import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.dto.job.JobUpdateDTO
import zg.acelera.service.CompanyService
import zg.acelera.service.JobService
import zg.acelera.utils.reader.AddressConsoleReader
import zg.acelera.utils.reader.InputReader
import zg.acelera.utils.reader.SkillConsoleReader

import java.sql.SQLException

class CompanyJobUI extends JobUI {
    private final CompanyService companyService

    CompanyJobUI(JobService jobService, CompanyService companyService, InputReader input, AddressConsoleReader addressReader, SkillConsoleReader skillReader) {
        super (jobService, input, addressReader, skillReader)
        this.companyService = companyService
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
            printJobMenuToCompany(company)
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
        Set<JobResponseDTO> jobsByPublisher = jobService.getJobsByPublisher(company.id())
        String emptyMessage = "You don't have any jobs posted yet."
        displayJobs(jobsByPublisher, emptyMessage)
    }

    private void createJob(CompanyResponseDTO company) {
        println "\n=== CREATE NEW JOB ==="
        try {
            String title = input.readString("Job Title: ")
            String description = input.readString("Job Description: ")

            List<String> skillNames = skillReader.getSkillsFromUser()
            if (!skillNames) {
                println "Error: No skills selected. Job creation canceled."
                return
            }

            JobCreateDTO newJob = new JobCreateDTO(title, description, skillNames, company.id())

            println "\n--- JOB LOCATION ---"
            AddressCreateDTO addressDTO = addressReader.readAddressData()

            JobResponseDTO result = jobService.createJob(newJob, addressDTO)

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

        try {
            JobResponseDTO selectedJob = selectJobFromCompany(company)
            JobUpdateDTO jobToUpdateDTO = getDataToUpdateJob(selectedJob)
            JobResponseDTO jobUpdated = jobService.updateJob(jobToUpdateDTO, selectedJob.id())

            if (jobUpdated) {
                println "\nJob updated successfully!"
                printJobResponse(jobUpdated)
            }
        } catch (IllegalArgumentException e) {
            println "Validation error: ${e.message}"
        } catch (SQLException e) {
            println "Database error: ${e.message}"
        } catch (Exception e) {
            println "Error updating job: ${e.message}"
        }
    }

    private void deleteJob(CompanyResponseDTO company) {
        println "\n=== DELETE JOB ==="

        JobResponseDTO selectedJob = selectJobFromCompany(company)
        String confirm = input.readString("Are you sure you want to delete '${selectedJob.name()}'? (yes/no): ")
        if (confirm?.toLowerCase() == "yes") {
            jobService.deleteJob(selectedJob.id())
            println "Job deleted successfully."
        } else {
            println "Deletion canceled."
        }
    }

    private void printJobMenuToCompany(CompanyResponseDTO company) {
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
    }

    private JobResponseDTO selectJobFromCompany(CompanyResponseDTO company) {
        Set<JobResponseDTO> jobs = jobService.getJobsByPublisher(company.id())

        if (!jobs) {
            println "You don't have any jobs to select."
            return null
        }

        return promptUserToSelectJob(jobs.toList())
    }

    private JobResponseDTO promptUserToSelectJob(List<JobResponseDTO> jobList) {
        println "Your jobs:"
        jobList.eachWithIndex { job, index ->
            println "  ${index + 1}. ${job.name()} - ${job.description()}"
        }

        while (true) {
            Integer jobChoice = input.readInt("Select the job number (or 0 to cancel): ")

            if (jobChoice == 0) {
                println "Operation canceled."
                return null
            }

            if (jobChoice != null && jobChoice >= 1 && jobChoice <= jobList.size()) {
                return jobList[jobChoice - 1]
            }

            println "Invalid selection. Please choose a number between 1 and ${jobList.size()}."
        }
    }

    JobUpdateDTO getDataToUpdateJob(JobResponseDTO job) {
        println "\n--- Enter the new values or press ENTER to keep the current value ---"
        println "Current Title: ${job.name()}"
        String newTitle = input.readString("New Title: ", false)

        println "Current Description: ${job.description()}"
        String newDescription = input.readString("New Description: ", false)

        return new JobUpdateDTO(newTitle, newDescription, null)
    }
}
