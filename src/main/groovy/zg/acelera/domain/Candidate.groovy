package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder

@Canonical
@EqualsAndHashCode(includes = ['cpf'])
@Builder(includeSuperProperties = true)
class Candidate extends Person {
    String cpf
    int age

    @Override
    void like(IPerson person) {
        if(person)
            liked += person
    }

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
        println "State: ${state} - CEP: ${cep}"
        println "Descrption: ${description}"
        println "Skills: ${skills.join(', ')}"
        println "==============================\n"
    }
}
