package zg.acelera.domain

interface IPerson {
    String getName()
    String getEmail()
    String getState()
    String getCep()
    String getDescription()
    Set<String> getLiked()
    Set<String> getSkills()
    void like(String id)
    void dislike(IPerson person)
    void showDetails()
}