package zg.acelera.user_interface


import zg.acelera.controller.JobController
import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.utils.reader.AddressConsoleReader
import zg.acelera.utils.reader.InputReader
import zg.acelera.utils.reader.SkillConsoleReader

abstract class JobUI {
    protected final JobController jobController
    protected final InputReader input
    protected final AddressConsoleReader addressReader
    protected final SkillConsoleReader skillReader

    JobUI(JobController jobController, InputReader input, AddressConsoleReader addressReader, SkillConsoleReader skillReader) {
        this.jobController = jobController
        this.input = input
        this.addressReader = addressReader
        this.skillReader = skillReader
    }

    protected static void printJobResponse(JobResponseDTO job) {
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

    protected void displayJobs(Set<JobResponseDTO> jobs, String messageToEmptyCase) {
        if (!jobs) {
            println messageToEmptyCase
            return
        }
        jobs.each { printJobResponse(it) }
    }
}
