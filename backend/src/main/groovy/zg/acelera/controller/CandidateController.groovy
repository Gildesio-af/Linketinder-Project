package zg.acelera.controller

import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.dto.candidate.CandidateResponseDTO
import zg.acelera.dto.candidate.CandidateUpdateDTO
import zg.acelera.service.CandidateService

class CandidateController {
    private final CandidateService candidateService

    CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService
    }

    List<CandidateResponseDTO> listAll() {
        return candidateService.listAllCandidates()
    }

    CandidateResponseDTO findByCpf(String cpf) {
        return candidateService.listCandidateByCpf(cpf)
    }

    List<CandidateResponseDTO> findBySkill(String skill) {
        return candidateService.listCandidatesBySkill(skill)
    }

    CandidateResponseDTO register(CandidateDTO candidateDTO, AddressCreateDTO addressDTO) {
        return candidateService.registerCandidate(candidateDTO, addressDTO)
    }

    CandidateResponseDTO update(CandidateUpdateDTO updateDTO) {
        return candidateService.updateCandidate(updateDTO)
    }

    void delete(String cpf) {
        candidateService.deleteCandidate(cpf)
    }

    Set<String> resolveSkillNamesToIds(Set<String> skillNames) {
        return candidateService.resolveSkillNamesToIds(skillNames)
    }
}
