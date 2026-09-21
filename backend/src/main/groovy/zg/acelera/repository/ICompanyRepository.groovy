package zg.acelera.repository

import zg.acelera.domain.Company
import zg.acelera.domain.IPerson
import zg.acelera.dto.company.CompanyDTO
import zg.acelera.dto.company.CompanyUpdateDTO

interface ICompanyRepository {
    IPerson findById(UUID id)
    Company findByJobId(UUID uuid)
    List<IPerson> findAll()
    IPerson findByCnpj(String cnpj)

    List<IPerson> findBySkill(String skill)
    Company save(Company company)
    Company update(Company company, UUID userId)

    void delete(UUID userId)
}