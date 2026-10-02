package zg.acelera.utils.reader

import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.service.SkillService

class SkillConsoleReader {
    private final InputReader input
    private final SkillService skillService

    SkillConsoleReader(InputReader input, SkillService skillService) {
        this.skillService = skillService
        this.input = input
    }

    Set<String> getSkillsFromUser() {
        Set<SkillResponseDTO> availableSkills = skillService.getAllSkills()

        if (!availableSkills) {
            println "No skills available in the database."
            return [] as Set<String>
        }

        List<SkillResponseDTO> skillList = availableSkills.toList()
        printSkills(skillList)

        String selectedInput = input.readString("Enter the numbers (e.g. 1,3,5): ")
        return processSelectedSkills(selectedInput, skillList)
    }

    private void printSkills(List<SkillResponseDTO> skillList) {
        println "\n--- AVAILABLE SKILLS ---"
        skillList.eachWithIndex { skill, index ->
            println "  ${index + 1}. ${skill.name()}"
        }
    }

    private Set<String> processSelectedSkills(String rawInput, List<SkillResponseDTO> skillList) {
        return rawInput.split(',').findResults { entry ->
            try {
                int indexChoice = Integer.parseInt(entry.trim()) - 1

                if (indexChoice >= 0 && indexChoice < skillList.size()) {
                    return skillList[indexChoice].name()
                }

                println "Skipping out-of-bounds entry: ${entry.trim()}"
                return null
            } catch (NumberFormatException ignored) {
                println "Skipping invalid entry: ${entry.trim()}"
                return null
            }
        } as Set<String>
    }
}
