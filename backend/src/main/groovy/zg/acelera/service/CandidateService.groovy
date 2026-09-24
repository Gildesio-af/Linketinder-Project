package zg.acelera.service

import zg.acelera.domain.Candidate
import zg.acelera.domain.Person
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.dto.candidate.CandidateResponseDTO
import zg.acelera.dto.candidate.CandidateUpdateDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.repository.candidate.CandidateRepository
import zg.acelera.utils.exception.EntityNotFoundException

class CandidateService {
    private final CandidateRepository repository
    private final AddressService addressService
    private final SkillService skillService

    CandidateService(CandidateRepository candidateRepository, AddressService addressService, SkillService skillService) {
        this.repository = candidateRepository
        this.addressService = addressService
        this.skillService = skillService
    }

    List<CandidateResponseDTO> listAllCandidates() {
        List<Person> candidates = repository.findAll()
        return candidates.collect { candidate ->
            CandidateResponseDTO.fromDomain(candidate as Candidate, addressService.getAddressesByUserId(candidate.id))
        }
    }

    CandidateResponseDTO listCandidateByCpf(String cpf) {
        CandidateResponseDTO candidateResponse
        try {
            Candidate candidate = repository.findByCpf(cpf) as Candidate
            candidateResponse = CandidateResponseDTO.fromDomain(candidate, addressService.getAddressesByUserId(candidate.id))
        } catch (EntityNotFoundException e) {
            println "Error: ${e.message}"
            return null
        }

        return candidateResponse
    }

    List<CandidateResponseDTO> listCandidatesBySkill(String skillName) {
        List<CandidateResponseDTO> candidatesResponse
        try {
            List<Person> candidates = repository.findBySkill(skillName)
            candidatesResponse = candidates.collect { candidate ->
                CandidateResponseDTO.fromDomain(candidate as Candidate, addressService.getAddressesByUserId(candidate.id))
            }
        } catch (EntityNotFoundException e) {
            println "Error: ${e.message}"
            return null
        }

        return candidatesResponse
    }

    CandidateResponseDTO registerCandidate(CandidateDTO candidateDTO, AddressCreateDTO addressDTO) {
        CandidateResponseDTO candidateResponse
        try {
            Candidate newCandidate = candidateDTO.toDomain()
            Candidate candidateSaved = repository.save(newCandidate)
            AddressResponseDTO newAddress = addressService.createAddress(addressDTO, candidateSaved.id)
            candidateResponse = CandidateResponseDTO.fromDomain(candidateSaved, [newAddress] as Set<AddressResponseDTO>)
        } catch (Exception e) {
            println "Error: ${e.message}"
            return null
        }
        return candidateResponse
    }

    Set<String> resolveSkillNamesToIds(Set<String> skillNames) {
        return skillNames.collect { name ->
            SkillResponseDTO skill = skillService.getSkillByName(name)
            if (!skill) throw new EntityNotFoundException("Skill '${name}' not found in the database.")
            return skill.id().toString()
        } as Set<String>
    }

    CandidateResponseDTO updateCandidate(CandidateUpdateDTO candidateUpdate, UUID userId) {
        CandidateResponseDTO candidateResponse
        try {
            Candidate updatedCandidate = repository.update(candidateUpdate.toCandidate(), userId)
            candidateResponse = CandidateResponseDTO.fromDomain(updatedCandidate, addressService.getAddressesByUserId(userId))
        } catch (EntityNotFoundException e) {
            println "Error: ${e.message}"
            return null
        }
        return candidateResponse
    }

    CandidateResponseDTO updateCandidate(CandidateUpdateDTO candidateUpdate) {
        try {
            Candidate existing = repository.findByCpf(candidateUpdate.cpf()) as Candidate
            return updateCandidate(candidateUpdate, existing.id)
        } catch (EntityNotFoundException e) {
            println "Error: ${e.message}"
            return null
        }
    }

    void deleteCandidate(UUID id) {
        try {
            repository.delete(id)
        } catch (Exception e) {
            println "Error: ${e.message}"
        }
    }

    void deleteCandidate(String cpf) {
        try {
            Candidate candidate = repository.findByCpf(cpf) as Candidate
            deleteCandidate(candidate.id)
        } catch (EntityNotFoundException e) {
            println "Error: ${e.message}"
        }
    }
}