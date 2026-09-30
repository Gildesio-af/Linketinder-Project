package zg.acelera.service

import zg.acelera.domain.Company
import zg.acelera.domain.User
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.company.CompanyCreateDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.company.CompanyUpdateDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.repository.company.CompanyRepository
import zg.acelera.utils.exception.EntityNotFoundException

class CompanyService {
    private final CompanyRepository companyRepository
    private final AddressService addressService
    private final SkillService skillService

    CompanyService(CompanyRepository companyRepository, AddressService addressService, SkillService skillService) {
        this.companyRepository = companyRepository
        this.addressService = addressService
        this.skillService = skillService
    }

    List<CompanyResponseDTO> listAllCompanies() {
        List<User> allCompanies = companyRepository.findAll()
        return allCompanies.collect { company ->
            CompanyResponseDTO.fromDomain(company as Company, addressService.getAddressesByUserId(company.id))
        }
    }

    CompanyResponseDTO listCompanyByCnpj(String cnpj) {
        Company companyByCnpj = companyRepository.findByCnpj(cnpj) as Company
        return CompanyResponseDTO.fromDomain(companyByCnpj, addressService.getAddressesByUserId(companyByCnpj.id))
    }

    List<CompanyResponseDTO> listCompaniesBySkill(String skillName) {
        List<User> companiesBySkill = companyRepository.findBySkill(skillName)
        return companiesBySkill.collect { company ->
            CompanyResponseDTO.fromDomain(company as Company, addressService.getAddressesByUserId(company.id))
        }
    }

    CompanyResponseDTO registerCompany(CompanyCreateDTO companyDTO, AddressCreateDTO addressDTO) {
        Company newCompany = companyDTO.toDomain()
        Company companySaved = companyRepository.save(newCompany)

        if (addressDTO) addressService.createAddress(addressDTO, companySaved.id)

        return CompanyResponseDTO.fromDomain(companySaved, addressService.getAddressesByUserId(companySaved.id))
    }

    Set<String> resolveSkillNamesToIds(Set<String> skillNames) {
        return skillNames.collect { name ->
            SkillResponseDTO skill = skillService.getSkillByName(name)
            if (!skill) throw new EntityNotFoundException("Skill '${name}' not found in the database.")
            return skill.id().toString()
        } as Set<String>
    }

    CompanyResponseDTO updateCompany(CompanyUpdateDTO companyUpdate, UUID userId) {
        Company updatedCompany = companyRepository.update(companyUpdate.toCompany(), userId)
        return CompanyResponseDTO.fromDomain(updatedCompany, addressService.getAddressesByUserId(userId))
    }

    CompanyResponseDTO updateCompany(CompanyUpdateDTO companyUpdate) {
        Company existingCompany = companyRepository.findByCnpj(companyUpdate.cnpj()) as Company
        return updateCompany(companyUpdate, existingCompany.id)
    }

    void deleteCompany(UUID id) {
        companyRepository.delete(id)
    }

    void deleteCompany(String cnpj) {
        Company company = companyRepository.findByCnpj(cnpj) as Company
        deleteCompany(company.id)
    }

    CompanyResponseDTO getCompanyByJobId(UUID uuid) {
        Company company = companyRepository.findByJobId(uuid) as Company
        return CompanyResponseDTO.fromDomain(company, addressService.getAddressesByUserId(company.id))
    }
}