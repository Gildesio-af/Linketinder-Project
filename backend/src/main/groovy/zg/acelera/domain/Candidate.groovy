package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.builder.Builder

import java.time.LocalDate

@Canonical
@Builder(includeSuperProperties = true)
class Candidate extends Person {
    String cpf
    String lastName
    LocalDate birthDate

    Set<Job> likedJobs = [] as HashSet<Job>

    @Override
    void dislike(IPerson person) {
        if(person && liked.contains(person))
            liked -= person
    }

    @Override
    void showDetails() {
        println "=== Candidate: ${name} ==="
        println "CPF: ${cpf}"
        println "Email: ${email}"
        println "Descrption: ${description}"
        println "Skills: ${skills.join(', ')}"
        println("Liked Companies: ${liked.collect { it.name }.join(', ')}")
        println("Address: ${address.street}, ${address.number}, ${address.country.name} - ${address.country.code}, ${address.city} ${address.state}, ${address.cep}")
        println "==============================\n"
    }
}
