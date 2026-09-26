package zg.acelera.user_interface

import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.dto.candidate.CandidateResponseDTO
import zg.acelera.dto.candidate.CandidateUpdateDTO
import zg.acelera.service.CandidateService
import zg.acelera.utils.reader.AddressConsoleReader
import zg.acelera.utils.reader.InputReader
import zg.acelera.utils.reader.SkillConsoleReader

import java.sql.SQLException
import java.time.LocalDate

class CandidateUI {
    private final CandidateService candidateService
    private final CandidateJobUI jobUI
    private final InputReader input
    private final AddressConsoleReader addressReader
    private final SkillConsoleReader skillReader

    CandidateUI(CandidateService candidateService, CandidateJobUI jobUI, InputReader inputReader, AddressConsoleReader addressReader, SkillConsoleReader skillReader) {
        this.candidateService = candidateService
        this.jobUI = jobUI
        this.input = inputReader
        this.addressReader = addressReader
        this.skillReader = skillReader
    }

    void showMenu() {
        boolean running = true

        while (running) {
            printCandidateMenu()
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
            CandidateDTO newCandidateDTO = getCandidateDataToCreate()

            if (!newCandidateDTO) {
                println "Error: Failed to create candidate data. Registration canceled."
                return
            }

            println "\n--- ADDRESS ---"
            AddressCreateDTO newAddressDTO = addressReader.readAddressData()

            CandidateResponseDTO candidateSaved = candidateService.registerCandidate(newCandidateDTO, newAddressDTO)
            if (candidateSaved) {
                println "\nCandidate registered successfully!"
                printCandidateResponse(candidateSaved)
            }
        } catch (IllegalArgumentException e) {
            println "Validation error: ${e.message}"
        } catch (SQLException e) {
            println "Database error: ${e.message}"
        } catch (Exception e) {
            println "Error: ${e.message}"
        }
    }

    void updateCandidate() {
        try {
            println "\n=== UPDATE CANDIDATE ==="
            CandidateUpdateDTO dto = getDataToUpdate()
            CandidateResponseDTO result = candidateService.updateCandidate(dto)
            if (result) {
                println "\nCandidate updated successfully!"
                printCandidateResponse(result)
            }

        } catch (IllegalArgumentException e) {
            println "Validation error: ${e.message}"
        } catch (SQLException e) {
            println "Database error: ${e.message}"
        } catch (Exception e) {
            println "Error: ${e.message}"
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
        println "Candidate deleted successfully."
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

        AddressPrinter.printAddressesFormated(candidate.addresses())
        println "------------------------------"
    }

    private printCandidateMenu() {
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
    }

    CandidateDTO getCandidateDataToCreate() {
        String cpf = input.readString("CPF (11 digits): ")
        String name = input.readString("Name: ")
        String lastName = input.readString("Last Name: ")
        String email = input.readString("E-mail: ")
        String password = input.readString("Password (min 6 characters): ")
        String description = input.readString("Description/Bio: ")
        LocalDate birthDate = input.readDate("Birth Date (dd/MM/yyyy): ")

        Set<String> skillNames = skillReader.getSkillsFromUser()

        Set<String> skillIds = candidateService.resolveSkillNamesToIds(skillNames)

        if (skillIds.size() != skillNames.size()) {
            throw new IllegalArgumentException("Some skills could not be recognized. Please ensure all skills are valid.")
        }

        return new CandidateDTO(cpf, name, email, password, description, lastName, birthDate, skillIds)
    }

    CandidateUpdateDTO getDataToUpdate() {
        String cpf = input.readString("Enter the CPF of the candidate you want to update: ")

        println "--- Enter the new values or press ENTER to keep the current value ---"
        String name = input.readString("New Name: ", false)
        String lastName = input.readString("New Last Name: ", false)
        String email = input.readString("New E-mail: ", false)
        String password = input.readString("New Password: ", false)
        String description = input.readString("New Description: ", false)
        LocalDate birthDate = input.readDate("New Birth Date (dd/MM/yyyy): ", false)

        return new CandidateUpdateDTO(cpf, name, email, password, description, lastName, birthDate)
    }
}
