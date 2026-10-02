package zg.acelera.service

import zg.acelera.domain.Address
import zg.acelera.domain.Job
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.job.JobCreateDTO
import zg.acelera.dto.job.JobMatchResponseDTO
import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.dto.job.JobUpdateDTO
import zg.acelera.repository.job.JobRepository
import zg.acelera.utils.exception.EntityNotFoundException

import java.sql.SQLException

class JobService {
    JobRepository jobRepository
    AddressService addressService
    CompanyService companyService
    SkillService skillService

    JobService(JobRepository jobRepository, AddressService addressService, CompanyService companyService, SkillService skillService) {
        this.jobRepository = jobRepository
        this.addressService = addressService
        this.companyService = companyService
        this.skillService = skillService
    }

    JobMatchResponseDTO getJobByIdToMatch(UUID id) {
        Job searchedJob = jobRepository.findById(id)
        AddressResponseDTO address = addressService.getAddressByJobId(searchedJob.id)
        CompanyResponseDTO company = companyService.getCompanyByJobId(searchedJob.id)
        JobMatchResponseDTO jobResponse = JobMatchResponseDTO.fromDomain(searchedJob, address, company)

        return jobResponse
    }

    Set<JobResponseDTO> getAllJobs() {
        Set<Job> allJobsSearched = jobRepository.findAll()

        Set<JobResponseDTO> jobsResponse = allJobsSearched.collect { job ->
            AddressResponseDTO address = addressService.getAddressByJobId(job.id)
            JobResponseDTO.fromDomain(job, address)
        } as Set<JobResponseDTO>

        return jobsResponse
    }

    Set<JobResponseDTO> getJobsByPublisher(UUID publisherId) {
        Set<Job> jobsByPublisher = jobRepository.findByPublisherId(publisherId)

        Set<JobResponseDTO> jobsResponse = jobsByPublisher.collect { job ->
            AddressResponseDTO address = addressService.getAddressByJobId(job.id)
            JobResponseDTO.fromDomain(job, address)
        } as Set<JobResponseDTO>

        return jobsResponse
    }

    Set<JobResponseDTO> getJobsByName(String name) {
        Set<Job> jobsByName = jobRepository.findByName(name)

        Set<JobResponseDTO> jobsResponse = jobsByName.collect { job ->
            AddressResponseDTO address = addressService.getAddressByJobId(job.id)
            JobResponseDTO.fromDomain(job, address)
        } as Set<JobResponseDTO>

        return jobsResponse
    }

    Set<JobResponseDTO> getJobsBySkill(String skill) {
        Set<Job> jobsBySkill = jobRepository.findBySkill(skill)

        Set<JobResponseDTO> jobsResponse = jobsBySkill.collect { job ->
                AddressResponseDTO address = addressService.getAddressByJobId(job.id)
                JobResponseDTO.fromDomain(job, address)
            }

        return jobsResponse
    }


    JobResponseDTO createJob(JobCreateDTO newJob, AddressCreateDTO newAddress) {
        AddressResponseDTO addressResponse = addressService.createAddress(newAddress, newJob.publisherId())
        if (!addressResponse) throw new IllegalStateException("Failed to create address for the job.")

        Job jobDomain = newJob.toDomain()
        jobDomain.address = new Address(id: addressResponse.id())
        List<UUID> skillsIds = getSkillsIdByName(newJob.skills())

        Job createdJob = jobRepository.create(jobDomain, skillsIds)

        return JobResponseDTO.fromDomain(createdJob, addressResponse)
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
        jobRepository.delete(id)
    }

    private List<UUID> getSkillsIdByName(List<String> skillNames) {
        return skillNames.collect { skillName ->
            skillService.getSkillByName(skillName)
        } .collect { it.id() }
    }
}
