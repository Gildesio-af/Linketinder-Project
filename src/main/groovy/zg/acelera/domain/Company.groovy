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
        println "=== Company(a): ${name} ==="
        println "CNPJ: ${cnpj}"
        println "Corporate email: ${email}"
        println "Place: ${state} - CEP: ${cep}"
        println "Description: ${description}"
        println "Desired Skills: ${skills.join(', ')}"
        println "==============================\n"
    }
}
