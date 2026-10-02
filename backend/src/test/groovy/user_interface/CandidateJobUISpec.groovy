package user_interface

import spock.lang.Specification
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.domain.Skill
import zg.acelera.service.JobService
import zg.acelera.user_interface.CandidateJobUI
import zg.acelera.utils.reader.AddressConsoleReader
import zg.acelera.utils.reader.InputReader
import zg.acelera.utils.reader.SkillConsoleReader

class CandidateJobUISpec extends Specification {

    JobService jobServiceMock
    InputReader inputMock
    AddressConsoleReader addressReaderMock
    SkillConsoleReader skillReaderMock
    CandidateJobUI ui

    UUID jobId = UUID.randomUUID()
    UUID addressId = UUID.randomUUID()
    UUID countryId = UUID.randomUUID()
    UUID skillId = UUID.randomUUID()

    def setup() {
        jobServiceMock = Mock(JobService)
        inputMock = Mock(InputReader)
        addressReaderMock = Mock(AddressConsoleReader)
        skillReaderMock = Mock(SkillConsoleReader)
        ui = new CandidateJobUI(jobServiceMock, inputMock, addressReaderMock, skillReaderMock)
    }

    private JobResponseDTO createJobResponse() {
        return new JobResponseDTO(
                id: jobId, name: "Java Developer", description: "Develop Java apps",
                skills: [new Skill(id: skillId, name: "Java")] as Set,
                addressResponseDTO: new AddressResponseDTO(
                        id: addressId, cep: "12345678", street: "Main St", number: "100",
                        city: "São Paulo", state: "SP", complement: null, neighborhood: "Downtown",
                        country: new CountryDTO(id: countryId, name: "Brazil", code: "BR"),
                        userId: UUID.randomUUID()
                )
        )
    }

    def "candidateViewJobsMenu should exit when user selects option 0"() {
        given:
        inputMock.readString("Choose an option: ") >> "0"

        when:
        ui.candidateViewJobsMenu()

        then:
        0 * jobServiceMock.getAllJobs()
    }

    def "candidateViewJobsMenu should call getAllJobs when user selects option 1"() {
        given:
        inputMock.readString("Choose an option: ") >>> ["1", "0"]
        jobServiceMock.getAllJobs() >> ([createJobResponse()] as Set)

        when:
        ui.candidateViewJobsMenu()

        then:
        1 * jobServiceMock.getAllJobs()
    }

    def "candidateViewJobsMenu should call getJobsByName when user selects option 2"() {
        given:
        inputMock.readString("Choose an option: ") >>> ["2", "0"]
        inputMock.readString("Enter the job name to search: ") >> "Java"
        jobServiceMock.getJobsByName("Java") >> ([createJobResponse()] as Set)

        when:
        ui.candidateViewJobsMenu()

        then:
        1 * jobServiceMock.getJobsByName("Java")
    }

    def "candidateViewJobsMenu should call getJobsBySkill when user selects option 3"() {
        given:
        inputMock.readString("Choose an option: ") >>> ["3", "0"]
        inputMock.readString("Enter the skill name to search (e.g. JAVA): ") >> "Java"
        jobServiceMock.getJobsBySkill("Java") >> ([createJobResponse()] as Set)

        when:
        ui.candidateViewJobsMenu()

        then:
        1 * jobServiceMock.getJobsBySkill("Java")
    }

    def "candidateViewJobsMenu should handle empty results gracefully"() {
        given:
        inputMock.readString("Choose an option: ") >>> ["1", "0"]
        jobServiceMock.getAllJobs() >> ([] as Set)

        when:
        ui.candidateViewJobsMenu()

        then:
        notThrown(Exception)
    }
}
