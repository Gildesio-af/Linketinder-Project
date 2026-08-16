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
        println "=== Candidato(a): ${name} ==="
        println "Idade: ${age} anos | CPF: ${cpf}"
        println "E-mail: ${email}"
        println "Local: ${state} - CEP: ${cep}"
        println "Descrição: ${description}"
        println "Competências: ${skills.join(', ')}"
        println "==============================\n"
    }
}
