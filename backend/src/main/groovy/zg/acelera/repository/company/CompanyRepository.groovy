package zg.acelera.repository.company

import zg.acelera.domain.Company
import zg.acelera.domain.User

interface CompanyRepository {
    User findById(UUID id)
    Company findByJobId(UUID uuid)
    List<User> findAll()
    User findByCnpj(String cnpj)

    List<User> findBySkill(String skill)
    Company save(Company company)
    Company update(Company company, UUID userId)

    void delete(UUID userId)
}