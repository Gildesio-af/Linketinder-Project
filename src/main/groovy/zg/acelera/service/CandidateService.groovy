package zg.acelera.service

import zg.acelera.domain.IPerson
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.dto.candidate.CandidateUpdateDTO
import zg.acelera.repository.CandidateRepository
import zg.acelera.repository.ICandidateRepository
import zg.acelera.domain.Skill

class CandidateService {
    private final ICandidateRepository repository

    CandidateService() {
        this.repository = new CandidateRepository()
    }

    void listAllCandidates() {
        def candidates = repository.findAll()
        if (candidates.isEmpty()) {
            println "Any candidates found in the system."
            return
        }

        println "--- CANDIDATES LIST ---"
        candidates.each { it.showDetails() }
    }

    void listCandidateByCpf(String cpf) {
        IPerson candidate = repository.findByCpf(cpf)
        if (candidate) {
            println("--- CANDIDATE DETAILS ---")
            candidate.showDetails()
        } else {
            println "No candidate found with CPF: $cpf"
        }
    }

    void listCandidatesBySkill(String skillName) {
        try {
            def skill = Skill.valueOf(skillName.toUpperCase())
            def candidates = repository.findBySkill(skill)
            if (candidates.isEmpty()) {
                println "No candidates found with the skill: $skillName."
                return
            }

            println "--- CANDIDATES WITH SKILL: ${skillName} ---"
            candidates.each { it.showDetails() }
        } catch (IllegalArgumentException e) {
            println "Error: ${e.message}"
        }
    }

    void registerCandidate(CandidateDTO candidate) {
        try {
            repository.save(candidate)
            println "Candidate registered successfully!"
        } catch (IllegalArgumentException e) {
            println "Error: ${e.message}"
        }
    }

    void updateCandidate(CandidateUpdateDTO candidateUpdate) {
        try {
            def existingCandidate = repository.findByCpf(candidateUpdate.cpf())
            if (!existingCandidate) {
                println "No candidate found with CPF: ${candidateUpdate.cpf()}"
                return
            }

            repository.update(candidateUpdate)
            println "Candidate updated successfully!"
        } catch (IllegalArgumentException e) {
            println "Error: ${e.message}"
        }
    }

    void deleteCandidate(String cpf) {
        try {
            def existingCandidate = repository.findByCpf(cpf)
            if (!existingCandidate) {
                println "No candidate found with CPF: $cpf"
                return
            }

            repository.delete(cpf)
            println "Candidate deleted successfully!"
        } catch (IllegalArgumentException e) {
            println "Error: ${e.message}"
        }
    }
}