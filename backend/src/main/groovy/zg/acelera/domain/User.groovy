package zg.acelera.domain

interface User {
    UUID getId()
    String getName()
    String getEmail()
    String getPassword()
    Set<Address> getAddresses()
    String getDescription()
    Set<Skill> getSkills()
}