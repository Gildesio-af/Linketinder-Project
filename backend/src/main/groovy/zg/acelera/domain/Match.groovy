package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder

import java.time.LocalDateTime

@Canonical
@EqualsAndHashCode(includes = ['id'])
@Builder
class Match {
    UUID id
    UUID candidateId
    UUID companyId
    UUID jobId
    LocalDateTime createdAt
}
