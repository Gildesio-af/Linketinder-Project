package zg.acelera.domain

import groovy.transform.Canonical

@Canonical
abstract class Person implements IPerson {
    UUID id
    String name
    String email
//    String state
//    String cep

    String description
    Address address
    Set<Person> liked = [] as HashSet<Person>
    Set<Skill> skills = [] as HashSet<Skill>Skill

    @Override
    void like(String id) {
        if (id)
            liked += id
    }
}
