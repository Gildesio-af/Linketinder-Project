package user_interface

import spock.lang.Specification
import zg.acelera.controller.CompanyController
import zg.acelera.controller.JobController
import zg.acelera.domain.Skill
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.dto.job.JobUpdateDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.user_interface.CompanyJobUI
import zg.acelera.utils.reader.AddressConsoleReader
import zg.acelera.utils.reader.InputReader
import zg.acelera.utils.reader.SkillConsoleReader

class CompanyJobUISpec extends Specification {

    JobController jobControllerMock
    CompanyController companyControllerMock
    InputReader inputMock
    AddressConsoleReader addressReaderMock
    SkillConsoleReader skillReaderMock
    CompanyJobUI ui

    UUID companyId = UUID.randomUUID()
    UUID jobId = UUID.randomUUID()
    UUID addressId = UUID.randomUUID()
    UUID countryId = UUID.randomUUID()
    UUID skillId = UUID.randomUUID()

    def setup() {
        jobControllerMock = Mock(JobController)
        companyControllerMock = Mock(CompanyController)
        inputMock = Mock(InputReader)
        addressReaderMock = Mock(AddressConsoleReader)
        skillReaderMock = Mock(SkillConsoleReader)
        ui = new CompanyJobUI(jobControllerMock, companyControllerMock, inputMock, addressReaderMock, skillReaderMock)
    }

    private CompanyResponseDTO createCompanyResponse() {
        return new CompanyResponseDTO(
                id: companyId, name: "Tech Corp", email: "tech@corp.com",
                description: "A tech company", cnpj: "12345678000199",
                addresses: [] as Set,
                skills: [new SkillResponseDTO(id: skillId, name: "Java")] as Set
        )
    }

    private JobResponseDTO createJobResponse() {
        return new JobResponseDTO(
                id: jobId, name: "Java Developer", description: "Develop Java apps",
                skills: [new Skill(id: skillId, name: "Java")] as Set,
                addressResponseDTO: new AddressResponseDTO(
                        id: addressId, cep: "12345678", street: "Main St", number: "100",
                        city: "São Paulo", state: "SP", complement: null, neighborhood: "Downtown",
                        country: new CountryDTO(id: countryId, name: "Brazil", code: "BR"),
                        userId: companyId
                )
        )
    }

    def "manageJobs should fail login when company not found"() {
        given:
        inputMock.readString("Enter your CNPJ to login: ") >> "12345678000199"
        companyControllerMock.findByCnpj("12345678000199") >> null

        when:
        ui.manageJobs()

        then:
        0 * jobControllerMock.getJobsByPublisher(_)
    }

    def "manageJobs should login and allow viewing jobs"() {
        given:
        inputMock.readString("Enter your CNPJ to login: ") >> "12345678000199"
        companyControllerMock.findByCnpj("12345678000199") >> createCompanyResponse()

        inputMock.readString("Choose an option: ") >>> ["1", "0"]
        jobControllerMock.getJobsByPublisher(companyId) >> ([createJobResponse()] as Set)

        when:
        ui.manageJobs()

        then:
        1 * jobControllerMock.getJobsByPublisher(companyId)
    }

    def "manageJobs should login and allow creating a job"() {
        given:
        inputMock.readString("Enter your CNPJ to login: ") >> "12345678000199"
        companyControllerMock.findByCnpj("12345678000199") >> createCompanyResponse()

        inputMock.readString("Choose an option: ") >>> ["2", "0"]
        inputMock.readString("Job Title: ") >> "Java Dev"
        inputMock.readString("Job Description: ") >> "Develop Java apps"

        skillReaderMock.getSkillsFromUser() >> (["Java"] as Set)

        AddressCreateDTO addressDTO = new AddressCreateDTO(
                cep: "12345678", street: "Main St", number: "100",
                city: "São Paulo", state: "SP", complement: null, neighborhood: "Downtown",
                countryId: countryId.toString()
        )
        addressReaderMock.readAddressData() >> addressDTO

        jobControllerMock.createJob(_, addressDTO) >> createJobResponse()

        when:
        ui.manageJobs()

        then:
        1 * jobControllerMock.createJob(_, addressDTO)
    }

    def "manageJobs should login and exit immediately when user selects 0"() {
        given:
        inputMock.readString("Enter your CNPJ to login: ") >> "12345678000199"
        companyControllerMock.findByCnpj("12345678000199") >> createCompanyResponse()
        inputMock.readString("Choose an option: ") >> "0"

        when:
        ui.manageJobs()

        then:
        0 * jobControllerMock._
    }

    def "getDataToUpdateJob should read new title and description"() {
        given:
        JobResponseDTO existingJob = createJobResponse()
        inputMock.readString("New Title: ", false) >> "Updated Title"
        inputMock.readString("New Description: ", false) >> "Updated Desc"

        when:
        JobUpdateDTO result = ui.getDataToUpdateJob(existingJob)

        then:
        result.title() == "Updated Title"
        result.description() == "Updated Desc"
    }

    def "getDataToUpdateJob should handle null inputs for keeping current values"() {
        given:
        JobResponseDTO existingJob = createJobResponse()
        inputMock.readString("New Title: ", false) >> null
        inputMock.readString("New Description: ", false) >> null

        when:
        JobUpdateDTO result = ui.getDataToUpdateJob(existingJob)

        then:
        result.title() == null
        result.description() == null
    }
}
