package service

import spock.lang.Specification
import zg.acelera.service.CompanyService
import zg.acelera.repository.ICompanyRepository
import zg.acelera.dto.company.CompanyDTO

class CompanyServiceSpec extends Specification{
    def "registerCompany should call repository save successfully without throwing exceptions"() {
        given:
        def repositoryMock = Mock(ICompanyRepository)
        def service = new CompanyService(repositoryMock)

        CompanyDTO companyDto = CompanyDTO.builder()
                .cnpj("12345678000199")
                .name("Tech Corp")
                .build()

        when:
        service.registerCompany(companyDto)

        then:
        1 * repositoryMock.save(companyDto)
        notThrown(Exception)
    }

    def "registerCompany should catch IllegalArgumentException when repository save fails"() {
        given:
        def repositoryMock = Mock(ICompanyRepository)
        def service = new CompanyService(repositoryMock)

        CompanyDTO companyDto = CompanyDTO.builder()
                .cnpj("12345678000199")
                .name("Tech Corp")
                .build()

        when:
        service.registerCompany(companyDto)

        then:
        1 * repositoryMock.save(companyDto) >> {
            throw new IllegalArgumentException("Already exists a company with this CNPJ")
        }
        notThrown(Exception)
    }
}
