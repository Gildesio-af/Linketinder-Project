package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder

@Canonical
@Builder(includeSuperProperties = true)
class Candidate extends Person {
    String cpf
    int age

    @Override
    void dislike(IPerson person) {
        if(person && liked.contains(person))
            liked -= person
    }

    @Override
    void showDetails() {
        println "=== Candidate: ${name} ==="
        println "Age: ${age} years old | CPF: ${cpf}"
        println "Email: ${email}"
        println "Descrption: ${description}"
        println "Skills: ${skills.join(', ')}"
        println("Liked Companies: ${liked.collect { it.name }.join(', ')}")
        println("Address: ${address.street}, ${address.number}, ${address.country.name} - ${address.country.code}, ${address.city} ${address.state}, ${address.cep}")
        println "==============================\n"
    }
}
