package user_interface

import spock.lang.Specification
import zg.acelera.dto.company.CompanyDTO
import zg.acelera.user_interface.CompanyUI
import zg.acelera.user_interface.InputReader
import zg.acelera.service.CompanyService


class CompanyUISpec extends Specification{
    def "registerCompany should successfully read inputs and call service when data is valid"() {
        given:
        def inputMock = Mock(InputReader)
        def serviceMock = Mock(CompanyService)
        def viewClass = new CompanyUI(serviceMock, inputMock)

        inputMock.readString("CNPJ (14 digits, without punctuation): ") >> "12345678000199"
        inputMock.readString("Name: ") >> "Tech Corp"
        inputMock.readString("Corporate E-mail: ") >> "contact@techcorp.com"
        inputMock.readString("State (UF): ") >> "SP"
        inputMock.readString("CEP (8 digits, without punctuation): ") >> "12345678"
        inputMock.readString("Description/Bio: ") >> "Tech Company"
        inputMock.readSkills() >> (["JAVA", "PYTHON"] as Set)

        when:
        viewClass.registerCompany()

        then:
        1 * serviceMock.registerCompany({ CompanyDTO dto ->
            dto.cnpj() == "12345678000199" &&
                    dto.name() == "Tech Corp" &&
                    dto.corporateEmail() == "contact@techcorp.com" &&
                    dto.skills().contains("JAVA")
        })
        notThrown(Exception)
    }

    def "registerCompany should catch exception and not call service when validation fails"() {
        given:
        def inputMock = Mock(InputReader)
        def serviceMock = Mock(CompanyService)
        def viewClass = new CompanyUI(serviceMock, inputMock)

        inputMock.readString("CNPJ (14 digits, without punctuation): ") >> "123"
        inputMock.readString("Name: ") >> "Tech Corp"
        inputMock.readString("Corporate E-mail: ") >> "contact@techcorp.com"
        inputMock.readString("State (UF): ") >> "SP"
        inputMock.readString("CEP (8 digits, without punctuation): ") >> "12345678"
        inputMock.readString("Description/Bio: ") >> "Tech Company"
        inputMock.readSkills() >> (["JAVA"] as Set)

        when:
        viewClass.registerCompany()

        then:
        0 * serviceMock.registerCompany(_)
        notThrown(Exception)
    }
}
