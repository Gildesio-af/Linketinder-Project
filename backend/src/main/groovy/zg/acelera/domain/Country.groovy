package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder

@Canonical
@EqualsAndHashCode(includes = ['id'])
@Builder
class Country {
    UUID id
    String name
    String code
}
