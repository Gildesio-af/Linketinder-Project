package service

import spock.lang.Specification
import zg.acelera.domain.SkillEnum
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.repository.ICandidateRepository
import zg.acelera.service.CandidateService

class CandidateServiceSpec extends Specification {

    def "registerCandidates should register a new candidate"() {
        given:
        CandidateDTO newCandidate = CandidateDTO.builder()
                .cpf("12345678900")
                .name("Zoro")
                .email("zorop@gmail.com")
                .age(20)
                .state("GL")
                .cep("12345678")
                .description("Member of the Straw Hat crew")
                .skills(Set.of(SkillEnum.JAVA, SkillEnum.GO, SkillEnum.CSHARP))
                .build()

        ICandidateRepository candidateRepository = Mock()
        CandidateService service = new CandidateService(candidateRepository)

        when:
        service.registerCandidate(newCandidate)

        then:
        1 * candidateRepository.save(newCandidate)
    }

    def "registerCandidate should do nothing when candidate is null"() {
        given:
        ICandidateRepository candidateRepository = Mock()
        CandidateService service = new CandidateService(candidateRepository)

        when:
        service.registerCandidate(null)

        then:
        notThrown(Exception)
    }
}
