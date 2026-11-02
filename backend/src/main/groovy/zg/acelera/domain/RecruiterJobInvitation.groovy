package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder

import java.time.LocalDateTime

@Canonical
@EqualsAndHashCode(includes = ['id'])
@Builder
class RecruiterJobInvitation {
    enum Status {
        PENDING,
        ACCEPTED,
        DECLINED,
        CANCELLED,
        REVOKED
    }

    UUID id
    UUID recruiterId
    UUID companyId
    UUID jobId
    Status status = Status.PENDING
    LocalDateTime createdAt
    LocalDateTime respondedAt
    LocalDateTime revokedAt
}
