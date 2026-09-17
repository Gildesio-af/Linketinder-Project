package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder

@Canonical
@EqualsAndHashCode(includes = ['id'])
@Builder
class Address {
    UUID id
    String cep
    String street
    String number
    String complement
    String neighborhood
    String city
    String state
    UUID userId

    Country country

    String toString() {
        return "${street}, ${number} - ${neighborhood}, ${city} - ${state}, ${cep} - ${country?.name}-${country?.code}, " +
               "\ncomplemento: ${complement ? complement : 'N/A'}"
    }
}
