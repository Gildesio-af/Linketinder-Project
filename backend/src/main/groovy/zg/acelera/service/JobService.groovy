package zg.acelera.service

import zg.acelera.domain.Job
import zg.acelera.dto.address.AddressDTO
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.job.JobDTO
import zg.acelera.dto.job.JobMatchResponseDTO
import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.dto.job.JobUpdateDTO
import zg.acelera.repository.IJobRepository
import zg.acelera.utils.exception.EntityNotFoundException

import java.sql.SQLException

class JobService {
    IJobRepository jobRepository
    AddressService addressService
    CompanyService companyService
    SkillService skillService

    JobService(IJobRepository jobRepository, AddressService addressService, CompanyService companyService, SkillService skillService) {
        this.jobRepository = jobRepository
        this.addressService = addressService
        this.companyService = companyService
        this.skillService = skillService
    }

    JobMatchResponseDTO getJobByIdToMatch(UUID id) {
        JobMatchResponseDTO jobResponse
        try {
            Job job = jobRepository.findById(id)
            AddressResponseDTO address = addressService.getAddressByJobId(job.id)
            CompanyResponseDTO company = companyService.getCompanyByJobId(job.id)
            jobResponse = JobMatchResponseDTO.fromDomain(job, address, company)
        } catch (EntityNotFoundException e) {
            println("Job not found: ${e.message}")
            return null
        }

        return jobResponse
    }

    Set<JobResponseDTO> getAllJobs() {
        Set<JobResponseDTO> jobsResponse = new HashSet<>()
        try {
            Set<Job> jobs = jobRepository.findAll()
            jobs.each { job ->
                AddressResponseDTO address = addressService.getAddressByJobId(job.id)
                jobsResponse += JobResponseDTO.fromDomain(job, address)
            }
        } catch (Exception e) {
            println("Error fetching all jobs: ${e.message}")
            return []
        }

        return jobsResponse
    }

    Set<JobResponseDTO> getJobsByPublisher(UUID publisherId) {
        Set<JobResponseDTO> jobsResponse = new HashSet<>()
        try {
            Set<Job> jobs = jobRepository.findByPublisherId(publisherId)
            jobs.each { job ->
                AddressResponseDTO address = addressService.getAddressByJobId(job.id)
                jobsResponse += JobResponseDTO.fromDomain(job, address)
            }
        } catch (Exception e) {
            println("Error fetching jobs by publisher: ${e.message}")
            return []
        }

        return jobsResponse
    }

    Set<JobResponseDTO> getJobsByName(String name) {
        Set<JobResponseDTO> jobsResponse = new HashSet<>()
        try {
            Set<Job> jobs = jobRepository.findByName(name)
            jobs.each { job ->
                AddressResponseDTO address = addressService.getAddressByJobId(job.id)
                jobsResponse += JobResponseDTO.fromDomain(job, address)
            }
        } catch (SQLException e) {
            println("Error fetching jobs by name: ${e.message}")
            return []
        }

        return jobsResponse
    }

    Set<JobResponseDTO> getJobsBySkill(String skill) {
        Set<JobResponseDTO> jobsResponse = new HashSet<>()
        try {
            Set<Job> jobs = jobRepository.findBySkill(skill)
            jobs.each { job ->
                AddressResponseDTO address = addressService.getAddressByJobId(job.id)
                jobsResponse += JobResponseDTO.fromDomain(job, address)
            }
        } catch (SQLException e) {
            println("Error fetching jobs by skill: ${e.message}")
            return []
        }

        return jobsResponse
    }

    JobResponseDTO createJob(JobDTO jobDTO, List<String> skills) {
        Job job = jobDTO.toDomain()
        List<UUID> skillsIds = getSkillsIdByName(skills)

        Job createdJob = jobRepository.create(job, skillsIds)

        AddressResponseDTO address = addressService.getAddressByJobId(createdJob.id)
        return JobResponseDTO.fromDomain(createdJob, address)
    }

    JobResponseDTO createJobWithAddress(String title, String description, List<String> skillNames,
                                         AddressDTO addressDTO, UUID publisherId) {
        try {
            AddressResponseDTO addressResponse = addressService.createAddress(addressDTO, publisherId)
            if (!addressResponse) {
                println "Error: Could not create job address."
                return null
            }

            JobDTO jobDTO = new JobDTO(null, title, description, skillNames, addressResponse.id(), publisherId)
            return createJob(jobDTO, skillNames)
        } catch (Exception e) {
            println "Error creating job: ${e.message}"
            return null
        }
    }

    JobResponseDTO updateJob(JobUpdateDTO jobUpdateDTO, UUID jobId) {
        Job job = jobUpdateDTO.toDomain()
        job.id = jobId

        Job updatedJob = jobRepository.update(job)
        AddressResponseDTO address = addressService.getAddressByJobId(updatedJob.id)
        return JobResponseDTO.fromDomain(updatedJob, address)
    }

    void addJobSkills(UUID jobId, List<String> skills) {
        List<UUID> skillsIds = getSkillsIdByName(skills)
        jobRepository.addSkillToJob(jobId, skillsIds as Set<UUID>)
    }

    void deleteJob(UUID id) {
        try {
            jobRepository.delete(id)
        } catch (SQLException e) {
            println("Error deleting job: ${e.message}")
        }
    }

    private List<UUID> getSkillsIdByName(List<String> skillNames) {
        return skillNames.collect { skillName ->
            skillService.getSkillByName(skillName)
        } .collect { it.id() }
    }

}
