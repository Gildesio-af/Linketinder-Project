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
        List<Person> allCandidates = repository.findAll()
        return allCandidates.collect { candidate ->
            CandidateResponseDTO.fromDomain(candidate as Candidate, addressService.getAddressesByUserId(candidate.id))
        }
    }

    CandidateResponseDTO listCandidateByCpf(String cpf) {
        CandidateResponseDTO candidateResponse
        Candidate searchedCandidate = repository.findByCpf(cpf) as Candidate
        candidateResponse = CandidateResponseDTO.fromDomain(searchedCandidate, addressService.getAddressesByUserId(searchedCandidate.id))

        return candidateResponse
    }

    List<CandidateResponseDTO> listCandidatesBySkill(String skillName) {
        List<Person> candidates = repository.findBySkill(skillName)

        List<CandidateResponseDTO> candidateResponseDTOS = candidates.collect { candidate ->
            CandidateResponseDTO.fromDomain(candidate as Candidate, addressService.getAddressesByUserId(candidate.id))
        }

        return candidateResponseDTOS
    }

    CandidateResponseDTO registerCandidate(CandidateDTO candidateDTO, AddressCreateDTO addressDTO) {
        CandidateResponseDTO candidateResponse
        Candidate newCandidate = candidateDTO.toDomain()
        Candidate candidateSaved = repository.save(newCandidate)
        AddressResponseDTO newAddress = addressService.createAddress(addressDTO, candidateSaved.id)
        candidateResponse = CandidateResponseDTO.fromDomain(candidateSaved, [newAddress] as Set<AddressResponseDTO>)

        return candidateResponse
    }

    Set<String> resolveSkillNamesToIds(Set<String> skillNames) {
        return skillNames.collect { name ->
            SkillResponseDTO skill = skillService.getSkillByName(name)

            if (!skill) throw new EntityNotFoundException("Skill '${name}' not found in the database.")

            return skill.id().toString()
        } as Set<String>
    }

    CandidateResponseDTO updateCandidate(CandidateUpdateDTO candidateUpdate) {
        CandidateResponseDTO candidateResponse

        Candidate candidateToUpdate = repository.findByCpf(candidateUpdate.cpf()) as Candidate
        Candidate updatedCandidate = repository.update(candidateUpdate.toCandidate(), candidateToUpdate.id)

        candidateResponse = CandidateResponseDTO.fromDomain(updatedCandidate, addressService.getAddressesByUserId(candidateToUpdate.id))

        return candidateResponse
    }

    private void deleteCandidateById(UUID id) {
        repository.delete(id)
    }

    void deleteCandidate(String cpf) {
        Candidate candidate = repository.findByCpf(cpf) as Candidate
        deleteCandidateById(candidate.id)
    }
}