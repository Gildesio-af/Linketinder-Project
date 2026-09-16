package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder

@Canonical
@EqualsAndHashCode(includes = ['cnpj'])
@Builder(includeSuperProperties = true)
class Company extends Person{
    String cnpj

    @Override
    void dislike(IPerson person) {
        if(person && liked.contains(person))
            liked -= person
    }

    @Override
    void showDetails() {
        println "=== Company(a): ${name} ==="
        println "CNPJ: ${cnpj}"
        println "Corporate email: ${email}"
        println "Description: ${description}"
        println "Desired Skills: ${skills.join(', ')}"
        println("Liked Candidates: ${liked.collect { it.name }.join(', ')}")
        println("Address: ${address.street}, ${address.number}, ${address.country.name} - ${address.country.code}, ${address.city} ${address.state}, ${address.cep}")
        println "==============================\n"
    }
}
