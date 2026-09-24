package zg.acelera.domain

interface Person {
    UUID getId()
    String getName()
    String getEmail()
    String getPassword()
    Set<Address> getAddresses()
    String getDescription()
    Set<Skill> getSkills()
    void like(String id)
    void dislike(Person person)
    void showDetails()
}