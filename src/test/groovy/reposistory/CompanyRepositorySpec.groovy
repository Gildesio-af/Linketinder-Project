package reposistory

import spock.lang.Specification
import spock.lang.TempDir
import zg.acelera.repository.CompanyRepository
import zg.acelera.dto.company.CompanyDTO

import java.nio.file.Path

class CompanyRepositorySpec extends Specification{
    @TempDir
    Path tempDir

    def "save should store a new company and return it when CNPJ does not exist"() {
        given:
        File tempFile = tempDir.resolve("companies_test.json").toFile()
        tempFile.text = "[]"
        def repository = new CompanyRepository(tempFile.getAbsolutePath())


        CompanyDTO newCompany = CompanyDTO.builder()
                .cnpj("12345678000199")
                .name("Umbrella Corporation")
                .corporateEmail("umbrella@corporation.com")
                .state("SP")
                .cep("12345678")
                .description("A leading zumbi technology company")
                .skills(["JAVA", "PYTHON", "GO"] as Set<String>)
                .build()

        when:
        def result = repository.save(newCompany)

        then:
        result != null
        result.cnpj == "12345678000199"
        result.name == "Umbrella Corporation"
        result.email == "umbrella@corporation.com"
        result.state == "SP"
        result.cep == "12345678"
        result.description == "A leading zumbi technology company"
        result.skills.size() == 3
    }

    def "save should throw IllegalArgumentException when already exists a company with the same CNPJ provided"() {
        given:
        File tempFile = tempDir.resolve("companies_test.json").toFile()
        tempFile.text = """
        [
            {
                "cnpj": "12345678000199",
                "name": "Umbrella Corporation",
                "email": "umbrella@corporation.com"
            }
        ]
        """
        def repository = new CompanyRepository(tempFile.getAbsolutePath())

        CompanyDTO duplicateCompany = CompanyDTO.builder()
                .cnpj("12345678000199")
                .name("Capsule Corp")
                .corporateEmail("capsule@corp.com")
                .build()

        when:
        repository.save(duplicateCompany)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Already exists a company with this CNPJ: 12345678000199"
    }
}
