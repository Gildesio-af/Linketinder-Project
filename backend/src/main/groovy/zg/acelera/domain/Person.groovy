package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode

@Canonical
@EqualsAndHashCode(includes = ['id'])
abstract class Person implements IPerson {
    UUID id
    String name
    String email
    String password

    Set<Address> addresses = [] as HashSet<Address>
    String description
    Set<Person> liked = [] as HashSet<Person>
    Set<Skill> skills = [] as HashSet<Skill>

    @Override
    void like(String id) {
        if (id)
            liked += id
    }
}
