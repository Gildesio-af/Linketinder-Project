package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder

@Canonical
@EqualsAndHashCode(includes = ['id'])
@Builder
class Job {
    UUID id
    String name
    String description

    Set<Skill> desiredSkills = [] as HashSet<Skill>
    Address address
    Company publisher
}
