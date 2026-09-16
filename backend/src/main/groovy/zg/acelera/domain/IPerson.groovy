package zg.acelera.domain

interface IPerson {
    String getName()
    String getEmail()
    Address getAddress()
    String getDescription()
    Set<Person> getLiked()
    Set<Skill> getSkills()
    void like(String id)
    void dislike(IPerson person)
    void showDetails()
}