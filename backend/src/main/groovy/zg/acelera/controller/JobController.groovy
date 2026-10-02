package zg.acelera.controller

import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.job.JobCreateDTO
import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.dto.job.JobUpdateDTO
import zg.acelera.service.JobService

class JobController {
    private final JobService jobService

    JobController(JobService jobService) {
        this.jobService = jobService
    }

    Set<JobResponseDTO> getAllJobs() {
        return jobService.getAllJobs()
    }

    Set<JobResponseDTO> getJobsByPublisher(UUID publisherId) {
        return jobService.getJobsByPublisher(publisherId)
    }

    Set<JobResponseDTO> getJobsByName(String name) {
        return jobService.getJobsByName(name)
    }

    Set<JobResponseDTO> getJobsBySkill(String skill) {
        return jobService.getJobsBySkill(skill)
    }

    JobResponseDTO createJob(JobCreateDTO jobDTO, AddressCreateDTO addressDTO) {
        return jobService.createJob(jobDTO, addressDTO)
    }

    JobResponseDTO updateJob(JobUpdateDTO jobUpdateDTO, UUID jobId) {
        return jobService.updateJob(jobUpdateDTO, jobId)
    }

    void deleteJob(UUID id) {
        jobService.deleteJob(id)
    }
}
