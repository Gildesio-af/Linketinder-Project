package zg.acelera.service

import zg.acelera.domain.IPerson
import zg.acelera.dto.company.CompanyDTO
import zg.acelera.dto.company.CompanyUpdateDTO
import zg.acelera.repository.CompanyRepository
import zg.acelera.repository.ICompanyRepository
import zg.acelera.domain.Skill

class CompanyService {
    private final ICompanyRepository repository

    CompanyService(ICompanyRepository repository) {
        this.repository = repository
    }

    void listAllCompanies() {
        def companies = repository.findAll()
        if (companies.isEmpty()) {
            println "Any companies found in the system."
            return
        }

        println "--- COMPANIES LIST ---"
        companies.each { it.showDetails() }
    }

    void listCompanyByCnpj(String cnpj) {
        IPerson company = repository.findByCnpj(cnpj)
        if (company) {
            println("--- COMPANY DETAILS ---")
            company.showDetails()
        } else {
            println "No company found with CNPJ: $cnpj"
        }
    }

    void listCompaniesBySkill(String skillName) {
        try {
            def skill = Skill.valueOf(skillName.toUpperCase())
            def companies = repository.findBySkill(skill)
            if (companies.isEmpty()) {
                println "No companies found with the skill: $skillName."
                return
            }

            println "--- COMPANIES WITH SKILL: ${skillName} ---"
            companies.each { it.showDetails() }
        } catch (IllegalArgumentException e) {
            println "Error: ${e.message}"
        }
    }

    void registerCompany(CompanyDTO company) {
        try {
            repository.save(company)
            println "Company registered successfully!"
        } catch (IllegalArgumentException e) {
            println "Error: ${e.message}"
        }
    }

    void updateCompany(CompanyUpdateDTO companyUpdate) {
        try {
            def existingCompany = repository.findByCnpj(companyUpdate.cnpj())
            if (!existingCompany) {
                println "No company found with CNPJ: ${companyUpdate.cnpj()}"
                return
            }

            repository.update(companyUpdate)
            println "Company updated successfully!"
        } catch (IllegalArgumentException e) {
            println "Error: ${e.message}"
        }
    }

    void deleteCompany(String cnpj) {
        try {
            def existingCompany = repository.findByCnpj(cnpj)
            if (!existingCompany) {
                println "No company found with CNPJ: $cnpj"
                return
            }

            repository.delete(cnpj)
            println "Company deleted successfully!"
        } catch (IllegalArgumentException e) {
            println "Error: ${e.message}"
        }
    }
}