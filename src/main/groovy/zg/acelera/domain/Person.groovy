package zg.acelera.domain

abstract class Person implements IPerson {
    String name
    String email
    String state
    String cep
    String description
    Set<IPerson> liked = [] as HashSet<IPerson>
    Set<Skill> skills = [] as HashSet<Skill>

    @Override
    void addSkill(Skill skill) {
        if(skill)
            skills.add(skill)
    }
}
