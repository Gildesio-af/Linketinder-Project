package service

import spock.lang.Specification
import zg.acelera.domain.Address
import zg.acelera.domain.Company
import zg.acelera.domain.Job
import zg.acelera.domain.Skill
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.dto.job.JobCreateDTO
import zg.acelera.dto.job.JobMatchResponseDTO
import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.dto.job.JobUpdateDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.repository.job.JobRepository
import zg.acelera.service.AddressService
import zg.acelera.service.CompanyService
import zg.acelera.service.JobService
import zg.acelera.service.SkillService

class JobServiceSpec extends Specification {

    JobRepository repositoryMock
    AddressService addressServiceMock
    CompanyService companyServiceMock
    SkillService skillServiceMock
    JobService service

    UUID jobId = UUID.randomUUID()
    UUID addressId = UUID.randomUUID()
    UUID publisherId = UUID.randomUUID()
    UUID skillId = UUID.randomUUID()
    UUID countryId = UUID.randomUUID()

    def setup() {
        repositoryMock = Mock(JobRepository)
        addressServiceMock = Mock(AddressService)
        companyServiceMock = Mock(CompanyService)
        skillServiceMock = Mock(SkillService)
        service = new JobService(repositoryMock, addressServiceMock, companyServiceMock, skillServiceMock)
    }

    private Job createJob() {
        return new Job(
                id: jobId, name: "Java Developer", description: "Develop Java apps",
                address: new Address(id: addressId),
                publisher: new Company(id: publisherId),
                desiredSkills: [new Skill(id: skillId, name: "Java")] as Set
        )
    }

    private AddressResponseDTO createAddressResponse() {
        return new AddressResponseDTO(
                id: addressId, cep: "12345678", street: "Main St", number: "100",
                city: "São Paulo", state: "SP", complement: null, neighborhood: "Downtown",
                country: new CountryDTO(id: countryId, name: "Brazil", code: "BR"),
                userId: publisherId
        )
    }

    private CompanyResponseDTO createCompanyResponse() {
        return new CompanyResponseDTO(
                id: publisherId, name: "Tech Corp", email: "tech@corp.com",
                description: "A tech company", cnpj: "12345678000199",
                addresses: [] as Set, skills: [] as Set
        )
    }

    def "getJobByIdToMatch should return JobMatchResponseDTO"() {
        given:
        Job job = createJob()
        repositoryMock.findById(jobId) >> job
        addressServiceMock.getAddressByJobId(jobId) >> createAddressResponse()
        companyServiceMock.getCompanyByJobId(jobId) >> createCompanyResponse()

        when:
        JobMatchResponseDTO result = service.getJobByIdToMatch(jobId)

        then:
        result.name() == "Java Developer"
        result.companyResponseDTO().name() == "Tech Corp"
    }

    def "getAllJobs should return a set of JobResponseDTOs"() {
        given:
        Job job = createJob()
        repositoryMock.findAll() >> ([job] as Set)
        addressServiceMock.getAddressByJobId(jobId) >> createAddressResponse()

        when:
        Set<JobResponseDTO> result = service.getAllJobs()

        then:
        result.size() == 1
        result.first().name() == "Java Developer"
    }

    def "getAllJobs should return empty set when no jobs exist"() {
        given:
        repositoryMock.findAll() >> ([] as Set)

        when:
        Set<JobResponseDTO> result = service.getAllJobs()

        then:
        result.isEmpty()
    }

    def "getJobsByPublisher should return jobs for the given publisher"() {
        given:
        Job job = createJob()
        repositoryMock.findByPublisherId(publisherId) >> ([job] as Set)
        addressServiceMock.getAddressByJobId(jobId) >> createAddressResponse()

        when:
        Set<JobResponseDTO> result = service.getJobsByPublisher(publisherId)

        then:
        result.size() == 1
    }

    def "getJobsByName should return jobs matching the name"() {
        given:
        Job job = createJob()
        repositoryMock.findByName("Java") >> ([job] as Set)
        addressServiceMock.getAddressByJobId(jobId) >> createAddressResponse()

        when:
        Set<JobResponseDTO> result = service.getJobsByName("Java")

        then:
        result.size() == 1
    }

    def "getJobsBySkill should return jobs with the given skill"() {
        given:
        Job job = createJob()
        repositoryMock.findBySkill("Java") >> ([job] as Set)
        addressServiceMock.getAddressByJobId(jobId) >> createAddressResponse()

        when:
        Set<JobResponseDTO> result = service.getJobsBySkill("Java")

        then:
        result.size() == 1
    }

    def "createJob should create address, job and return JobResponseDTO"() {
        given:
        JobCreateDTO dto = new JobCreateDTO("Java Dev", "Develop Java apps", ["Java"], publisherId)
        AddressCreateDTO addressDTO = new AddressCreateDTO(null, "12345678", "Main St", "100", "São Paulo", "SP", null, "Downtown", countryId.toString(), null)
        AddressResponseDTO addressResponse = createAddressResponse()

        addressServiceMock.createAddress(addressDTO, publisherId) >> addressResponse

        skillServiceMock.getSkillByName("Java") >> new SkillResponseDTO(id: skillId, name: "Java")

        Job createdJob = createJob()
        repositoryMock.create(_, [skillId]) >> createdJob

        when:
        JobResponseDTO result = service.createJob(dto, addressDTO)

        then:
        result.name() == "Java Developer"
    }

    def "createJob should throw when address creation fails"() {
        given:
        JobCreateDTO dto = new JobCreateDTO("Java Dev", "Develop Java apps", ["Java"], publisherId)
        AddressCreateDTO addressDTO = new AddressCreateDTO(null, "12345678", "Main St", "100", "São Paulo", "SP", null, "Downtown", countryId.toString(), null)

        addressServiceMock.createAddress(addressDTO, publisherId) >> null

        when:
        service.createJob(dto, addressDTO)

        then:
        thrown(IllegalStateException)
    }

    def "updateJob should update and return JobResponseDTO"() {
        given:
        Job domainJob = new Job(name: "Updated Job", description: "Updated desc")
        JobUpdateDTO updateDTO = new JobUpdateDTO("Updated Job", "Updated desc", null)
        updateDTO.toDomain() >> domainJob

        Job updatedJob = createJob()
        updatedJob.name = "Updated Job"

        repositoryMock.update(_) >> updatedJob
        addressServiceMock.getAddressByJobId(jobId) >> createAddressResponse()

        when:
        JobResponseDTO result = service.updateJob(updateDTO, jobId)

        then:
        result.name() == "Updated Job"
    }

    def "deleteJob should call repository delete"() {
        when:
        service.deleteJob(jobId)

        then:
        1 * repositoryMock.delete(jobId)
    }

    def "addJobSkills should resolve skill names and add to job"() {
        given:
        skillServiceMock.getSkillByName("Java") >> new SkillResponseDTO(id: skillId, name: "Java")

        when:
        service.addJobSkills(jobId, ["Java"])

        then:
        1 * repositoryMock.addSkillToJob(jobId, { it.contains(skillId) })
    }
}
