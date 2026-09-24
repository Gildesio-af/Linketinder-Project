package zg.acelera.service

import zg.acelera.domain.Company
import zg.acelera.domain.Person
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.company.CompanyCreateDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.company.CompanyUpdateDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.repository.company.CompanyRepository
import zg.acelera.utils.exception.EntityNotFoundException

import java.sql.SQLException

class CompanyService {
    private final CompanyRepository repository
    private final AddressService addressService
    private final SkillService skillService

    CompanyService(CompanyRepository companyRepository, AddressService addressService, SkillService skillService) {
        this.repository = companyRepository
        this.addressService = addressService
        this.skillService = skillService
    }

    List<CompanyResponseDTO> listAllCompanies() {
        List<Person> companies = repository.findAll()
        return companies.collect { company ->
            CompanyResponseDTO.fromDomain(company as Company, addressService.getAddressesByUserId(company.id))
        }
    }

    CompanyResponseDTO listCompanyByCnpj(String cnpj) {
        try {
            Company company = repository.findByCnpj(cnpj) as Company
            return CompanyResponseDTO.fromDomain(company, addressService.getAddressesByUserId(company.id))
        } catch (EntityNotFoundException e) {
            println "Error: ${e.message}"
            return null
        }
    }

    List<CompanyResponseDTO> listCompaniesBySkill(String skillName) {
        try {
            List<Person> companies = repository.findBySkill(skillName)
            return companies.collect { company ->
                CompanyResponseDTO.fromDomain(company as Company, addressService.getAddressesByUserId(company.id))
            }
        } catch (EntityNotFoundException e) {
            println "Error: ${e.message}"
            return null
        }
    }

    CompanyResponseDTO registerCompany(CompanyCreateDTO companyDTO, AddressCreateDTO addressDTO) {
        try {
            Company newCompany = companyDTO.toDomain()
            Company companySaved = repository.save(newCompany)
            if (addressDTO) {
                addressService.createAddress(addressDTO, companySaved.id)
            }
            return CompanyResponseDTO.fromDomain(companySaved, addressService.getAddressesByUserId(companySaved.id))
        } catch (Exception e) {
            println "Error: ${e.message}"
            return null
        }
    }

    CompanyResponseDTO registerCompany(CompanyCreateDTO companyDTO) {
        return registerCompany(companyDTO, null)
    }

    Set<String> resolveSkillNamesToIds(Set<String> skillNames) {
        return skillNames.collect { name ->
            SkillResponseDTO skill = skillService.getSkillByName(name)
            if (!skill) throw new EntityNotFoundException("Skill '${name}' not found in the database.")
            return skill.id().toString()
        } as Set<String>
    }

    CompanyResponseDTO updateCompany(CompanyUpdateDTO companyUpdate, UUID userId) {
        try {
            Company updatedCompany = repository.update(companyUpdate.toCompany(), userId)
            return CompanyResponseDTO.fromDomain(updatedCompany, addressService.getAddressesByUserId(userId))
        } catch (EntityNotFoundException e) {
            println "Error: ${e.message}"
            return null
        }
    }

    CompanyResponseDTO updateCompany(CompanyUpdateDTO companyUpdate) {
        try {
            Company existingCompany = repository.findByCnpj(companyUpdate.cnpj()) as Company
            return updateCompany(companyUpdate, existingCompany.id)
        } catch (EntityNotFoundException e) {
            println "Error: ${e.message}"
            return null
        }
    }

    void deleteCompany(UUID id) {
        try {
            repository.delete(id)
        } catch (SQLException e) {
            println "Error: ${e.message}"
        }
    }

    void deleteCompany(String cnpj) {
        try {
            Company company = repository.findByCnpj(cnpj) as Company
            deleteCompany(company.id)
        } catch (Exception e) {
            println "Error: ${e.message}"
        }
    }

    CompanyResponseDTO getCompanyByJobId(UUID uuid) {
        try {
            Company company = repository.findByJobId(uuid) as Company
            return CompanyResponseDTO.fromDomain(company, null)
        } catch (EntityNotFoundException e) {
            println "Error: ${e.message}"
            return null
        }
    }
}