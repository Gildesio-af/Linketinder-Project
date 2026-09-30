package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.builder.Builder

import java.time.LocalDate

@Canonical
@Builder(includeSuperProperties = true)
class Candidate extends UserBase {
    String cpf
    String lastName
    LocalDate birthDate
}
