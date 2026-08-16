package zg.acelera.domain

interface IPerson {
    String getName()
    String getEmail()
    String getState()
    String getCep()
    String getDescription()
    Set<IPerson> getLiked()
    Set<String> getSkills()
    void like(IPerson person)
    void dislike(IPerson person)
    void showDetails()

    void addSkill(Skill skill)
}