package zg.acelera.utils

import zg.acelera.dto.address.AddressDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.repository.CountryRepositoryJDBC
import zg.acelera.repository.SkillRepositoryJDBC
import zg.acelera.service.CountryService
import zg.acelera.service.SkillService
import zg.acelera.user_interface.InputReader
import zg.acelera.utils.db.DatabaseManager

class DataManager {
    private static SkillService skillService = new SkillService(new SkillRepositoryJDBC(DatabaseManager.getSql()))
    private static CountryService countryService = new CountryService(new CountryRepositoryJDBC(DatabaseManager.getSql()))
    private static InputReader input = new InputReader()

    DataManager(SkillService skillService, CountryService countryService) {
        this.skillService = skillService
        this.countryService = countryService
    }

    static Set<String> readSkillsFromDatabase() {
        Set<SkillResponseDTO> availableSkills = skillService.getAllSkills()
        if (!availableSkills || availableSkills.isEmpty()) {
            println "No skills available in the database."
            return [] as Set
        }

        println "\n--- AVAILABLE SKILLS ---"
        List<SkillResponseDTO> skillList = availableSkills.toList()
        skillList.eachWithIndex { skill, index ->
            println "  ${index + 1}. ${skill.name()}"
        }

        String selectedInput = input.readString("Enter the numbers of the desired skills, separated by commas (e.g. 1,3,5): ")
        Set<String> selectedNames = [] as Set

        selectedInput.split(',').each { entry ->
            try {
                int idx = Integer.parseInt(entry.trim()) - 1
                if (idx >= 0 && idx < skillList.size()) {
                    selectedNames.add(skillList[idx].name())
                } else {
                    println "Warning: Number ${entry.trim()} is out of range. Skipping."
                }
            } catch (NumberFormatException e) {
                println "Warning: '${entry.trim()}' is not a valid number. Skipping."
            }
        }

        return selectedNames
    }

    static AddressDTO readAddressData() {
        String cep = input.readString("CEP (8 digits): ")
        String street = input.readString("Street: ")
        String number = input.readString("Number: ")
        String complement = input.readString("Complement (press ENTER to skip): ", false)
        String neighborhood = input.readString("Neighborhood: ")
        String city = input.readString("City: ")
        String state = input.readString("State (UF): ")

        List<CountryDTO> countries = countryService.getAllCountries()
        if (!countries || countries.isEmpty()) {
            println "Error: No countries available in the database."
            throw new IllegalStateException("No countries available.")
        }

        List<String> countryNames = countries.collect { "${it.name()} (${it.code()})" } as List<String>
        String selectedCountry = input.readOption("Select the country:", countryNames)
        int selectedIndex = countryNames.indexOf(selectedCountry)
        String countryId = countries[selectedIndex].id().toString()

        return new AddressDTO(null, cep, street, number, city, state, complement, neighborhood, countryId, null)
    }
}
