package zg.acelera.controller

import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.company.CompanyCreateDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.company.CompanyUpdateDTO
import zg.acelera.service.CompanyService

class CompanyController {
    private final CompanyService companyService

    CompanyController(CompanyService companyService) {
        this.companyService = companyService
    }

    List<CompanyResponseDTO> listAll() {
        return companyService.listAllCompanies()
    }

    CompanyResponseDTO findByCnpj(String cnpj) {
        return companyService.listCompanyByCnpj(cnpj)
    }

    List<CompanyResponseDTO> findBySkill(String skill) {
        return companyService.listCompaniesBySkill(skill)
    }

    CompanyResponseDTO register(CompanyCreateDTO companyDTO, AddressCreateDTO addressDTO) {
        return companyService.registerCompany(companyDTO, addressDTO)
    }

    CompanyResponseDTO update(CompanyUpdateDTO updateDTO) {
        return companyService.updateCompany(updateDTO)
    }

    void delete(String cnpj) {
        companyService.deleteCompany(cnpj)
    }

    Set<String> resolveSkillNamesToIds(Set<String> skillNames) {
        return companyService.resolveSkillNamesToIds(skillNames)
    }
}
