package zg.acelera.utils.reader

import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.service.SkillService

class SkillConsoleReader {
    private final SkillService skillService
    private final InputReader input

    SkillConsoleReader(SkillService skillService, InputReader input) {
        this.skillService = skillService
        this.input = input
    }

    Set<String> getSkillsFromUser() {
        Set<SkillResponseDTO> availableSkills = skillService.getAllSkills()

        if (!availableSkills) {
            println "No skills available in the database."
            return [] as Set<String>
        }

        println "\n--- AVAILABLE SKILLS ---"
        List<SkillResponseDTO> skillList = availableSkills.toList()
        skillList.eachWithIndex { skill, index ->
            println "  ${index + 1}. ${skill.name()}"
        }

        String selectedInput = input.readString("Enter the numbers (e.g. 1,3,5): ")
        Set<String> selectedNames = [] as Set<String>

        selectedInput.split(',').each { entry ->
            try {
                int idx = Integer.parseInt(entry.trim()) - 1
                if (idx >= 0 && idx < skillList.size()) {
                    selectedNames.add(skillList[idx].name())
                }
            } catch (Exception e) {
                println "Skipping invalid entry: ${entry.trim()}"
            }
        }
        return selectedNames
    }
}
