package zg.acelera.user_interface

import zg.acelera.dto.address.AddressDTO
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.dto.candidate.CandidateResponseDTO
import zg.acelera.dto.candidate.CandidateUpdateDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.service.CandidateService
import zg.acelera.service.CountryService
import zg.acelera.service.SkillService
import zg.acelera.utils.DataManager

import java.time.LocalDate

class CandidateUI {
    private final CandidateService candidateService
    private final SkillService skillService
    private final CountryService countryService
    private final JobUI jobUI
    private final InputReader input

    CandidateUI(CandidateService candidateService, SkillService skillService, CountryService countryService, JobUI jobUI, InputReader inputReader) {
        this.candidateService = candidateService
        this.skillService = skillService
        this.countryService = countryService
        this.jobUI = jobUI
        this.input = inputReader
    }

    void showMenu() {
        boolean running = true

        while (running) {
            println """
            ========================================
                   LINKETINDER - CANDIDATE MENU
            ========================================
            1. Show all candidates
            2. Search candidate by CPF
            3. Search candidates by Skill
            4. Register candidate
            5. Update candidate
            6. Delete candidate
            7. View Available Jobs
            0. Go back to Main Menu
            ========================================
            """
            String option = input.readString("Choose an option: ")

            switch (option) {
                case "1": listAllCandidates(); break
                case "2": searchCandidateByCpf(); break
                case "3": searchCandidatesBySkill(); break
                case "4": registerCandidate(); break
                case "5": updateCandidate(); break
                case "6": deleteCandidate(); break
                case "7": jobUI.candidateViewJobsMenu(); break
                case "0":
                    println "Going back to the Main Menu..."
                    running = false
                    break
                default:
                    println "Invalid option. Please try again."
            }
        }
    }

    void registerCandidate() {
        println "\n=== REGISTER CANDIDATE ==="
        try {
            String cpf = input.readString("CPF (11 digits, without punctuation): ")
            String name = input.readString("Name: ")
            String lastName = input.readString("Last Name: ")
            String email = input.readString("E-mail: ")
            String password = input.readString("Password (min 6 characters): ")
            String description = input.readString("Description/Bio: ")
            LocalDate birthDate = input.readDate("Birth Date (dd/MM/yyyy): ")

            Set<String> skillNames = DataManager.readSkillsFromDatabase()
            if (!skillNames || skillNames.isEmpty()) {
                println "Error: No skills selected. Registration canceled."
                return
            }

            Set<String> skillIds = candidateService.resolveSkillNamesToIds(skillNames)

            CandidateDTO dto = new CandidateDTO(cpf, name, email, password, description, lastName, birthDate, skillIds)

            println "\n--- ADDRESS DATA ---"
            AddressDTO addressDTO = DataManager.readAddressData()

            CandidateResponseDTO result = candidateService.registerCandidate(dto, addressDTO)
            if (result) {
                println "\nCandidate registered successfully!"
                printCandidateResponse(result)
            }

        } catch (IllegalArgumentException e) {
            println "Validation error: ${e.message}"
        } catch (Exception e) {
            println "Error: ${e.message}"
        }
    }

    void updateCandidate() {
        println "\n=== UPDATE CANDIDATE ==="
        String cpf = input.readString("Enter the CPF of the candidate you want to update: ")

        println "--- Enter the new values or press ENTER to keep the current value ---"
        try {
            String name = input.readString("New Name: ", false)
            String lastName = input.readString("New Last Name: ", false)
            String email = input.readString("New E-mail: ", false)
            String password = input.readString("New Password: ", false)
            String description = input.readString("New Description: ", false)
            LocalDate birthDate = input.readDate("New Birth Date (dd/MM/yyyy): ", false)

            CandidateUpdateDTO dto = new CandidateUpdateDTO(cpf, name, email, password, description, lastName, birthDate)
            CandidateResponseDTO result = candidateService.updateCandidate(dto)
            if (result) {
                println "\nCandidate updated successfully!"
                printCandidateResponse(result)
            }

        } catch (IllegalArgumentException e) {
            println "Validation error: ${e.message}"
        }
    }

    void searchCandidateByCpf() {
        println "\n=== SEARCH CANDIDATE ==="
        String cpf = input.readString("Enter the CPF: ")
        CandidateResponseDTO result = candidateService.listCandidateByCpf(cpf)
        if (result) {
            printCandidateResponse(result)
        } else {
            println "No candidate found with CPF: ${cpf}"
        }
    }

    void searchCandidatesBySkill() {
        println "\n=== FILTER BY SKILL ==="
        String skill = input.readString("Enter the name of the skill (e.g. JAVA): ")
        List<CandidateResponseDTO> results = candidateService.listCandidatesBySkill(skill)
        if (results && !results.isEmpty()) {
            results.each { printCandidateResponse(it) }
        } else {
            println "No candidates found with skill: ${skill}"
        }
    }

    void deleteCandidate() {
        println "\n=== DELETE CANDIDATE ==="
        String cpf = input.readString("Enter the CPF of the candidate you want to delete: ")
        candidateService.deleteCandidate(cpf)
        println "Candidate deleted successfully (if found)."
    }

    void listAllCandidates() {
        println "\n=== ALL CANDIDATES ==="
        List<CandidateResponseDTO> candidates = candidateService.listAllCandidates()
        if (candidates && !candidates.isEmpty()) {
            candidates.each { printCandidateResponse(it) }
        } else {
            println "No candidates registered."
        }
    }

    private static void printCandidateResponse(CandidateResponseDTO candidate) {
        println "------------------------------"
        println "  ID: ${candidate.id()}"
        println "  Name: ${candidate.name()} ${candidate.lastName() ?: ''}"
        println "  CPF: ${candidate.cpf()}"
        println "  E-mail: ${candidate.email()}"
        println "  Description: ${candidate.description()}"
        println "  Birth Date: ${candidate.birthDate()}"
        if (candidate.skills() && !candidate.skills().isEmpty()) {
            String skillNames = candidate.skills().collect { it.name() }.join(', ')
            println "  Skills: ${skillNames}"
        } else {
            println "  Skills: (none)"
        }
        if (candidate.address()) {
            candidate.address().each { addr ->
                println "  Address: ${addr.street()}, ${addr.number()} - ${addr.neighborhood()}, ${addr.city()} - ${addr.state()}, ${addr.cep()}"
                if (addr.country()) {
                    println "  Country: ${addr.country().name()} (${addr.country().code()})"
                }
            }
        }
        println "------------------------------"
    }
}
