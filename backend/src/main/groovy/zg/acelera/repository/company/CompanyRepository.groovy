package zg.acelera.repository.company

import zg.acelera.domain.Company
import zg.acelera.domain.Person

interface CompanyRepository {
    Person findById(UUID id)
    Company findByJobId(UUID uuid)
    List<Person> findAll()
    Person findByCnpj(String cnpj)

    List<Person> findBySkill(String skill)
    Company save(Company company)
    Company update(Company company, UUID userId)

    void delete(UUID userId)
}