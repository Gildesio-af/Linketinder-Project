package zg.acelera.domain

abstract class Person implements IPerson {
    String name
    String email
    String state
    String cep
    String description
    Set<String> liked = [] as HashSet<String>
    Set<Skill> skills = [] as HashSet<Skill>

    @Override
    void like(String id) {
        if (id)
            liked += id
    }
}
