package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.builder.Builder

import java.time.LocalDate

@Canonical
@Builder(includeSuperProperties = true)
class Recruiter extends UserBase {
    UUID recruitmentAreaId
    String cpf
    LocalDate birthDate
    RecruiterStatus status = RecruiterStatus.ACTIVE
}
