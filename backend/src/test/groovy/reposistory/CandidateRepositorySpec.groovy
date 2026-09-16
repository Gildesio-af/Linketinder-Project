package reposistory

import spock.lang.Specification
import spock.lang.TempDir
import zg.acelera.domain.SkillEnum
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.dto.candidate.CandidateUpdateDTO
import zg.acelera.repository.CandidateRepository
import zg.acelera.domain.Candidate

import java.nio.file.Path

class CandidateRepositorySpec extends Specification{

    @TempDir
    Path tempDir

    def "findAll should return a list of candidates when the file contains data"() {
        given:
        File tempFile = tempDir.resolve("candidates_test.json").toFile()
        tempFile.text = """
        [
            {
                "cpf": "12345678900",
                "name": "Mary Jane",
                "email": "jane@email.com",
                "age": 28,
                "state": "NY",
                "cep": "12345-678",
                "description": "Software Engineer",
                "skills": ["JAVA"],
                "liked": []
            }
        ]
        """

        def repository = new CandidateRepository(tempFile.getAbsolutePath())

        when:
        def result = repository.findAll() as List<Candidate>

        then:
        result.size() == 1
        result[0].cpf == "12345678900"
        result[0].name == "Mary Jane"
        result[0].email == "jane@email.com"
        result[0].age == 28
        result[0].state == "NY"
        result[0].cep == "12345-678"
        result[0].description == "Software Engineer"
        result[0].skills[0] == SkillEnum.JAVA
        result[0].liked.isEmpty()
    }

    def "findAll should return an empty list when the file is empty"() {
        given:
        File tempFile = tempDir.resolve("candidates_test.json").toFile()
        def candidateRepository = new CandidateRepository(tempFile.getAbsolutePath())

        when:
        def result = candidateRepository.findAll()

        then:
        result.isEmpty()
    }

    def "findByCpf should return a candidate when cpf match"() {
        given:
        File tempFile = tempDir.resolve("candidates_test.json").toFile()
        tempFile.text = """
        [
            {
                "cpf": "12345678900",
                "name": "Mary Jane",
                "email": "jane@email.com",
                "age": 28,
                "state": "NY",
                "cep": "12345-678",
                "description": "Software Engineer",
                "skills": ["JAVA"],
                "liked": []
            }
        ]
        """

        def repository = new CandidateRepository(tempFile.getAbsolutePath())
        String searchedCpf = "12345678900"

        when:
        def candidate = repository.findByCpf(searchedCpf) as Candidate

        then:
        candidate.cpf == searchedCpf
    }

    def "findByCpf should return null when the file is empty"() {
        given:
        File tempFile = tempDir.resolve("candidates_test.json").toFile()
        def candidateRepository = new CandidateRepository(tempFile.getAbsolutePath())

        when:
        def result = candidateRepository.findByCpf("12345678900")

        then:
        result == null
    }

    def "findBySkill should return candidate when skills match"() {
        given:
        File tempFile = tempDir.resolve("candidates_test.json").toFile()
        tempFile.text = """
        [
            {
                "cpf": "12345678900",
                "name": "Mary Jane",
                "email": "jane@email.com",
                "age": 28,
                "state": "NY",
                "cep": "12345-678",
                "description": "Software Engineer",
                "skills": ["JAVA"],
                "liked": []
            }
        ]
        """

        def repository = new CandidateRepository(tempFile.getAbsolutePath())
        SkillEnum searchedSkill = SkillEnum.JAVA

        when:
        def result = repository.findBySkill(searchedSkill)

        then:
        result[0].skills[0] == searchedSkill
    }

    def "findBySkill should return null when the file is empty"() {
        given:
        File tempFile = tempDir.resolve("candidates_test.json").toFile()
        def candidateRepository = new CandidateRepository(tempFile.getAbsolutePath())

        when:
        def result = candidateRepository.findBySkill(SkillEnum.JAVA)

        then:
        result == []
    }

    def "save should throw IllegalArgumentException when already exists a candidate with the same cpf provided"() {
        given:
        File tempFile = tempDir.resolve("candidates_test.json").toFile()
        tempFile.text = """
        [
            {
                "cpf": "12345678900",
                "name": "Mary Jane",
                "email": "jane@email.com",
                "age": 28,
                "state": "NY",
                "cep": "12345-678",
                "description": "Software Engineer",
                "skills": ["JAVA"],
                "liked": []
            }
        ]
        """

        CandidateDTO newCandidate = CandidateDTO.builder()
                            .cpf("12345678900")
                            .name("a")
                            .email("a")
                            .age(1)
                            .state("a")
                            .cep("12345678")
                            .description("a")
                            .skills(Set.of(SkillEnum.JAVA))
                            .build()


        def repository = new CandidateRepository(tempFile.getAbsolutePath())

        when:
        repository.save(newCandidate)

        then:
        thrown(IllegalArgumentException)
    }

    def "save should return a Candidate when save a new candidate" () {
        given:
        File tempFile = tempDir.resolve("candidates_test.json").toFile()
        def candidateRepository = new CandidateRepository(tempFile.getAbsolutePath())

        CandidateDTO newCandidate = CandidateDTO.builder()
                .cpf("12345678900")
                .name("a")
                .email("a")
                .age(1)
                .state("a")
                .cep("12345678")
                .description("a")
                .skills(Set.of(SkillEnum.JAVA))
                .build()
        when:
        def result = candidateRepository.save(newCandidate)
        then:
        result.cpf == newCandidate.cpf()
        result.name == newCandidate.name()
        result.email == newCandidate.email()
        result.skills[0] == SkillEnum.JAVA
    }

    def "update should update the property provided in CandidateUpdateDTO"() {
        given:
        File tempFile = tempDir.resolve("candidates_test.json").toFile()
        tempFile.text = """
        [
            {
                "cpf": "12345678900",
                "name": "Mary Jane",
                "email": "jane@email.com",
                "age": 28,
                "state": "NY",
                "cep": "12345-678",
                "description": "Software Engineer",
                "skills": ["JAVA"],
                "liked": []
            }
        ]
        """

        CandidateUpdateDTO updateDTO = CandidateUpdateDTO.builder()
                .cpf("12345678900")
                .name("Spider Man")
                .description("Your Friendly Neighbourhood")
                .build()

        def repository = new CandidateRepository(tempFile.getAbsolutePath())
        when:
        def result = repository.update(updateDTO)
        then:
        result.cpf == "12345678900"
        result.name == "Spider Man"
        result.description == "Your Friendly Neighbourhood"
    }

    def "delete should clean the file when there was only one candidate and the cpf provided match"() {
        given:
        File tempFile = tempDir.resolve("candidates_test.json").toFile()
        tempFile.text = """
        [
            {
                "cpf": "12345678900",
                "name": "Mary Jane",
                "email": "jane@email.com",
                "age": 28,
                "state": "NY",
                "cep": "12345-678",
                "description": "Software Engineer",
                "skills": ["JAVA"],
                "liked": []
            }
        ]
        """
        def repository = new CandidateRepository(tempFile.getAbsolutePath())
        String cpfToDelete = "12345678900"
        when:
        repository.delete(cpfToDelete)
        then:
        repository.findAll().isEmpty()
    }
}
