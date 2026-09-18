package zg.acelera.service

import zg.acelera.domain.Address
import zg.acelera.domain.Candidate
import zg.acelera.domain.IPerson
import zg.acelera.dto.address.AddressDTO
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.dto.candidate.CandidateResponseDTO
import zg.acelera.dto.candidate.CandidateUpdateDTO
import zg.acelera.repository.CandidateRepository
import zg.acelera.repository.ICandidateRepository
import zg.acelera.utils.exception.EntityNotFoundException

class CandidateService {
    private final ICandidateRepository repository
    private final AddressService addressService

    CandidateService(ICandidateRepository candidateRepository = new CandidateRepository(), AddressService addressService) {
        this.repository = candidateRepository
        this.addressService = addressService
    }

    List<CandidateResponseDTO> listAllCandidates() {
        List<IPerson> candidates = repository.findAll()
        return candidates.collect { candidate ->
            CandidateResponseDTO.fromDomain(candidate as Candidate, addressService.getAddressesByUserId(candidate.id))
        }
    }

    //TODO: mudar nome do método
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

    //TODO: mudar nome do método
    List<CandidateResponseDTO> listCandidatesBySkill(String skillName) {
        List<CandidateResponseDTO> candidatesResponse
        try {
            List<IPerson> candidates = repository.findBySkill(skillName)
            candidatesResponse = candidates.collect { candidate ->
                CandidateResponseDTO.fromDomain(candidate as Candidate, addressService.getAddressesByUserId(candidate.id))
            }
        } catch (EntityNotFoundException e) {
            println "Error: ${e.message}"
            return null
        }

        return candidatesResponse
    }

    CandidateResponseDTO registerCandidate(CandidateDTO candidate, AddressDTO addressDTO) {
        CandidateResponseDTO candidateResponse
        try {
            Candidate newCandidate = candidate.toDomain()
            Candidate candidateSaved = repository.save(newCandidate)
            AddressResponseDTO newAddress = addressService.createAddress(addressDTO, candidateSaved.id)
            candidateResponse = CandidateResponseDTO.fromDomain(candidateSaved, newAddress as Set<AddressResponseDTO>)
        } catch (EntityNotFoundException e) {
            println "Error: ${e.message}"
            return null
        }
        return candidateResponse
    }

    CandidateResponseDTO updateCandidate(CandidateUpdateDTO candidateUpdate, UUID userId) {
        CandidateResponseDTO candidateResponse
        try {
            Candidate addressUpdated = repository.update(candidateUpdate.toCandidate() , userId)
            candidateResponse = CandidateResponseDTO.fromDomain(addressUpdated, addressService.getAddressesByUserId(userId))
        } catch (EntityNotFoundException e) {
            println "Error: ${e.message}"
            return null
        }
        return candidateResponse
    }

    void deleteCandidate(UUID id) {
        try {
            repository.delete(id)
        } catch (IllegalArgumentException e) {
            println "Error: ${e.message}"
        }
    }
}