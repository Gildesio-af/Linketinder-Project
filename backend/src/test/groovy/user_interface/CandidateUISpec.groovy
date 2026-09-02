package user_interface

import spock.lang.Specification
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.service.CandidateService
import zg.acelera.user_interface.CandidateUI
import zg.acelera.user_interface.InputReader

class CandidateUISpec extends Specification{

    def "registerCandidate should successfully read inputs and call service when data is valid"() {
        given:
        InputReader inputMock = Mock()
        CandidateService serviceMock = Mock()
        CandidateUI viewClass = new CandidateUI(serviceMock, inputMock)

        inputMock.readString("CPF (11 digits, without punctuation): ") >> "12345678900"
        inputMock.readString("Name: ") >> "Mestre Kamy"
        inputMock.readString("E-mail: ") >> "mk@email.com"
        inputMock.readInt("Age: ") >> 80
        inputMock.readString("State (UF): ") >> "DB"
        inputMock.readString("CEP (8 digits, without punctuation): ") >> "12345678"
        inputMock.readString("Description/Bio: ") >> "Software Engineer"
        inputMock.readSkills() >> (["JAVA"] as Set)

        when:
        viewClass.registerCandidate()

        then:
        1 * serviceMock.registerCandidate({ CandidateDTO dto ->
            dto.cpf() == "12345678900" &&
                    dto.name() == "Mestre Kamy" &&
                    dto.age() == 80 &&
                    dto.state() == "DB" &&
                    dto.cep() == "12345678" &&
                    dto.description() == "Software Engineer" &&
                    dto.skills().contains("JAVA")
        })
    }

    def "registerCandidate should catch exception and not call service when validation fails"() {
        given:
        def inputMock = Mock(InputReader)
        def serviceMock = Mock(CandidateService)
        def viewClass = new CandidateUI(serviceMock, inputMock)

        inputMock.readString("CPF (11 digits, without punctuation): ") >> "123"
        inputMock.readString("Name: ") >> "Mestre Kamy"
        inputMock.readString("E-mail: ") >> "mk@email.com"
        inputMock.readInt("Age: ") >> 80
        inputMock.readString("State (UF): ") >> "DB"
        inputMock.readString("CEP (8 digits, without punctuation): ") >> "12345678"
        inputMock.readString("Description/Bio: ") >> "Software Engineer"
        inputMock.readSkills() >> (["JAVA"] as Set)

        when:
        viewClass.registerCandidate()

        then:
        0 * serviceMock.registerCandidate(_)
    }
}
