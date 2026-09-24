package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode

@Canonical
@EqualsAndHashCode(includes = ['id'])
abstract class PersonImpl implements Person {
    UUID id
    String name
    String email
    String password

    Set<Address> addresses = [] as HashSet<Address>
    String description
    Set<PersonImpl> liked = [] as HashSet<PersonImpl>
    Set<Skill> skills = [] as HashSet<Skill>

    @Override
    void like(String id) {
        if (id)
            liked += id
    }
}
