package zg.acelera.user_interface

import zg.acelera.controller.CompanyController
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.company.CompanyCreateDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.company.CompanyUpdateDTO
import zg.acelera.utils.reader.AddressConsoleReader
import zg.acelera.utils.reader.InputReader
import zg.acelera.utils.reader.SkillConsoleReader

import java.sql.SQLException

class CompanyUI {
    private final CompanyController controller
    private final CompanyJobUI jobUI
    private final InputReader input
    private final AddressConsoleReader addressReader
    private final SkillConsoleReader skillReader

    CompanyUI(CompanyController controller, CompanyJobUI jobUI, InputReader inputReader, AddressConsoleReader addressReader, SkillConsoleReader skillReader) {
        this.controller = controller
        this.jobUI = jobUI
        this.input = inputReader
        this.addressReader = addressReader
        this.skillReader = skillReader
    }

    void showMenu() {
        boolean running = true

        while (running) {
            printCompanyMenu()
            String option = input.readString("Choose an option: ")

            switch (option) {
                case "1": listAllCompanies(); break
                case "2": searchCompanyByCnpj(); break
                case "3": searchCompaniesBySkill(); break
                case "4": registerCompany(); break
                case "5": updateCompany(); break
                case "6": deleteCompany(); break
                case "7": jobUI.manageJobs(); break
                case "0":
                    println "Going back to the Main Menu..."
                    running = false
                    break
                default:
                    println "Invalid option. Please try again."
            }
        }
    }

    void registerCompany() {
        println "\n=== REGISTER COMPANY ==="
        try {
            CompanyCreateDTO dto = getCompanyDataToCreate()

            println "\n--- ADDRESS DATA ---"
            AddressCreateDTO addressDTO = addressReader.readAddressData()

            CompanyResponseDTO result = controller.register(dto, addressDTO)
            if (result) {
                println "\nCompany registered successfully!"
                printCompanyResponse(result)
            }

        } catch (IllegalArgumentException e) {
            println "Validation error: ${e.message}"
        } catch (SQLException e) {
            println "Database error: ${e.message}"
        } catch (Exception e) {
            println "Error: ${e.message}"
        }
    }

    void updateCompany() {
        println "\n=== UPDATE COMPANY ==="
        try {
            CompanyUpdateDTO dto = getCompanyDataToUpdate()
            CompanyResponseDTO result = controller.update(dto)

            if (result) {
                println "\nCompany updated successfully!"
                printCompanyResponse(result)
            }

        } catch (IllegalArgumentException e) {
            println "Validation error: ${e.message}"
        } catch (SQLException e) {
            println "Database error: ${e.message}"
        } catch (Exception e) {
            println "Error: ${e.message}"
        }
    }

    void searchCompanyByCnpj() {
        println "\n=== SEARCH COMPANY ==="
        String cnpj = input.readString("Enter the CNPJ: ")
        CompanyResponseDTO result = controller.findByCnpj(cnpj)
        if (result) {
            printCompanyResponse(result)
        } else {
            println "No company found with CNPJ: ${cnpj}"
        }
    }

    void searchCompaniesBySkill() {
        println "\n=== FILTER BY SKILL ==="
        String skill = input.readString("Enter the name of the skill (e.g. JAVA): ")
        List<CompanyResponseDTO> results = controller.findBySkill(skill)
        if (results && !results.isEmpty()) {
            results.each { printCompanyResponse(it) }
        } else {
            println "No companies found with skill: ${skill}"
        }
    }

    void deleteCompany() {
        println "\n=== DELETE COMPANY ==="
        String cnpj = input.readString("Enter the CNPJ of the company you want to delete: ")
        controller.delete(cnpj)
        println "Company deleted successfully (if found)."
    }

    void listAllCompanies() {
        println "\n=== ALL COMPANIES ==="
        List<CompanyResponseDTO> companies = controller.listAll()
        if (companies && !companies.isEmpty()) {
            companies.each { printCompanyResponse(it) }
        } else {
            println "No companies registered."
        }
    }

    private static void printCompanyResponse(CompanyResponseDTO company) {
        println "------------------------------"
        println "  ID: ${company.id()}"
        println "  Name: ${company.name()}"
        println "  CNPJ: ${company.cnpj()}"
        println "  E-mail: ${company.email()}"
        println "  Description: ${company.description()}"
        if (company.skills() && !company.skills().isEmpty()) {
            String skillNames = company.skills().collect { it.name() }.join(', ')
            println "  Desired Skills: ${skillNames}"
        } else {
            println "  Desired Skills: (none)"
        }

        AddressPrinter.printAddressesFormated(company.addresses())
        println "------------------------------"
    }

    private printCompanyMenu() {
        println """
            ========================================
                   LINKETINDER - COMPANY MENU
            ========================================
            1. Show all companies
            2. Search company by CNPJ
            3. Search companies by Skill
            4. Register company
            5. Update company
            6. Delete company
            7. Manage Jobs (requires login)
            0. Go back to Main Menu
            ========================================
            """
    }

    CompanyCreateDTO getCompanyDataToCreate() {
        String cnpj = input.readString("CNPJ (14 digits, without punctuation): ")
        String name = input.readString("Name: ")
        String email = input.readString("Corporate E-mail: ")
        String password = input.readString("Password (min 6 characters): ")
        String description = input.readString("Description/Bio: ")

        Set<String> skillNames = skillReader.getSkillsFromUser()
        if (!skillNames || skillNames.isEmpty())
            throw new IllegalArgumentException("No skills selected. Registration canceled.")

        Set<String> skillIds = controller.resolveSkillNamesToIds(skillNames)

        return new CompanyCreateDTO(cnpj, name, email, password, description, skillIds)
    }

    CompanyUpdateDTO getCompanyDataToUpdate() {
        String cnpj = input.readString("Enter the CNPJ of the company you want to update: ")
        println "--- Enter the new values or press ENTER to keep the current value ---"
        String name = input.readString("New Name: ", false)
        String email = input.readString("New Corporate E-mail: ", false)
        String password = input.readString("New Password: ", false)
        String description = input.readString("New Description: ", false)

        return new CompanyUpdateDTO(cnpj, name, email, password, description)
    }
}
