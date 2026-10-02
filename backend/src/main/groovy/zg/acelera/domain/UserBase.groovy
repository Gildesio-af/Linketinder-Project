package zg.acelera.domain

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode

@Canonical
@EqualsAndHashCode(includes = ['id'])
abstract class UserBase implements User {
    UUID id
    String name
    String email
    String password

    Set<Address> addresses = [] as HashSet<Address>
    String description
    Set<Skill> skills = [] as HashSet<Skill>
}
