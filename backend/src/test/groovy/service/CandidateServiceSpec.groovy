package service

import spock.lang.Specification
import zg.acelera.domain.Candidate
import zg.acelera.domain.Skill
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.dto.candidate.CandidateResponseDTO
import zg.acelera.dto.candidate.CandidateUpdateDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.repository.candidate.CandidateRepository
import zg.acelera.service.AddressService
import zg.acelera.service.CandidateService
import zg.acelera.service.SkillService
import zg.acelera.utils.exception.EntityNotFoundException

import java.time.LocalDate

class CandidateServiceSpec extends Specification {

    CandidateRepository repositoryMock
    AddressService addressServiceMock
    SkillService skillServiceMock
    CandidateService service

    UUID candidateId = UUID.randomUUID()
    UUID skillId = UUID.randomUUID()
    UUID addressId = UUID.randomUUID()
    UUID countryId = UUID.randomUUID()

    def setup() {
        repositoryMock = Mock(CandidateRepository)
        addressServiceMock = Mock(AddressService)
        skillServiceMock = Mock(SkillService)
        service = new CandidateService(repositoryMock, addressServiceMock, skillServiceMock)
    }

    private Candidate createCandidate() {
        return new Candidate(
                id: candidateId, cpf: "12345678900", name: "John", lastName: "Doe",
                email: "john@email.com", password: "secret123", description: "Developer",
                birthDate: LocalDate.of(1995, 5, 15),
                skills: [new Skill(id: skillId, name: "Java")] as Set
        )
    }

    private AddressResponseDTO createAddressResponse() {
        return new AddressResponseDTO(
                id: addressId, cep: "12345678", street: "Main St", number: "100",
                city: "São Paulo", state: "SP", complement: null, neighborhood: "Downtown",
                country: new CountryDTO(id: countryId, name: "Brazil", code: "BR"),
                userId: candidateId
        )
    }

    def "listAllCandidates should return a list of candidate response DTOs"() {
        given:
        Candidate candidate = createCandidate()
        repositoryMock.findAll() >> [candidate]
        addressServiceMock.getAddressesByUserId(candidateId) >> ([createAddressResponse()] as Set)

        when:
        List<CandidateResponseDTO> result = service.listAllCandidates()

        then:
        result.size() == 1
        result[0].name() == "John"
        result[0].cpf() == "12345678900"
    }

    def "listAllCandidates should return empty list when no candidates exist"() {
        given:
        repositoryMock.findAll() >> []

        when:
        List<CandidateResponseDTO> result = service.listAllCandidates()

        then:
        result.isEmpty()
    }

    def "listCandidateByCpf should return a candidate response DTO"() {
        given:
        Candidate candidate = createCandidate()
        repositoryMock.findByCpf("12345678900") >> candidate
        addressServiceMock.getAddressesByUserId(candidateId) >> ([createAddressResponse()] as Set)

        when:
        CandidateResponseDTO result = service.listCandidateByCpf("12345678900")

        then:
        result.cpf() == "12345678900"
        result.name() == "John"
    }

    def "listCandidatesBySkill should return candidates matching the skill"() {
        given:
        Candidate candidate = createCandidate()
        repositoryMock.findBySkill("Java") >> [candidate]
        addressServiceMock.getAddressesByUserId(candidateId) >> ([createAddressResponse()] as Set)

        when:
        List<CandidateResponseDTO> result = service.listCandidatesBySkill("Java")

        then:
        result.size() == 1
        result[0].name() == "John"
    }

    def "registerCandidate should save candidate and return response DTO"() {
        given:
        CandidateDTO dto = new CandidateDTO(
                "12345678900", "John", "john@email.com", "secret123",
                "Developer", "Doe", LocalDate.of(1995, 5, 15),
                [skillId.toString()] as Set
        )
        AddressCreateDTO addressDTO = new AddressCreateDTO(null, "12345678", "Main St", "100", "São Paulo", "SP", null, "Downtown", countryId.toString(), null)
        Candidate savedCandidate = createCandidate()
        AddressResponseDTO addressResponse = createAddressResponse()

        repositoryMock.save(_) >> savedCandidate
        addressServiceMock.createAddress(addressDTO, candidateId) >> addressResponse

        when:
        CandidateResponseDTO result = service.registerCandidate(dto, addressDTO)

        then:
        result.name() == "John"
        result.cpf() == "12345678900"
    }

    def "resolveSkillNamesToIds should return set of skill IDs"() {
        given:
        skillServiceMock.getSkillByName("Java") >> new SkillResponseDTO(id: skillId, name: "Java")

        when:
        Set<String> result = service.resolveSkillNamesToIds(["Java"] as Set)

        then:
        result.size() == 1
        result.first() == skillId.toString()
    }

    def "resolveSkillNamesToIds should throw when skill not found"() {
        given:
        skillServiceMock.getSkillByName("NonExistent") >> null

        when:
        service.resolveSkillNamesToIds(["NonExistent"] as Set)

        then:
        thrown(EntityNotFoundException)
    }

    def "updateCandidate should update and return response DTO"() {
        given:
        Candidate existingCandidate = createCandidate()
        CandidateUpdateDTO updateDTO = new CandidateUpdateDTO(
                "12345678900", "John Updated", null, null, null, null, null
        )
        Candidate updatedCandidate = createCandidate()
        updatedCandidate.name = "John Updated"

        repositoryMock.findByCpf("12345678900") >> existingCandidate
        repositoryMock.update(_, candidateId) >> updatedCandidate
        addressServiceMock.getAddressesByUserId(candidateId) >> ([createAddressResponse()] as Set)

        when:
        CandidateResponseDTO result = service.updateCandidate(updateDTO)

        then:
        result.name() == "John Updated"
    }

    def "deleteCandidate should find by CPF and delete"() {
        given:
        Candidate candidate = createCandidate()
        repositoryMock.findByCpf("12345678900") >> candidate

        when:
        service.deleteCandidate("12345678900")

        then:
        1 * repositoryMock.delete(candidateId)
    }
}
