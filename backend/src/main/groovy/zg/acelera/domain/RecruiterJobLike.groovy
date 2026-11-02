package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder

import java.time.LocalDateTime

@Canonical
@EqualsAndHashCode(includes = ['recruiterId', 'jobId', 'candidateId'])
@Builder
class RecruiterJobLike {
    enum Status {
        ACTIVE,
        REMOVED
    }

    UUID recruiterId
    UUID jobId
    UUID candidateId
    LocalDateTime createdAt
    LocalDateTime removedAt

    Status getStatus() {
        removedAt == null ? Status.ACTIVE : Status.REMOVED
    }
}
