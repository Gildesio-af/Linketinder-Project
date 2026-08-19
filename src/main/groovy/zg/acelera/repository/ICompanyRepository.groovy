package zg.acelera.repository

import zg.acelera.domain.Company
import zg.acelera.domain.IPerson
import zg.acelera.domain.Skill
import zg.acelera.dto.company.CompanyDTO
import zg.acelera.dto.company.CompanyUpdateDTO

interface ICompanyRepository {
    List<IPerson> findAll()
    IPerson findByCnpj(String cnpj)
    List<IPerson> findBySkill(Skill skill)
    Company save(CompanyDTO user)
    Company update(CompanyUpdateDTO user)
    void update(Company company)
    void delete(String cnpj)
}