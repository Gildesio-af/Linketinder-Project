package zg.acelera.service

import zg.acelera.domain.Recruiter
import zg.acelera.dto.recruiter.RecruiterCreateDTO
import zg.acelera.dto.recruiter.RecruiterResponseDTO
import zg.acelera.dto.recruiter.RecruiterUpdateDTO
import zg.acelera.repository.recruiter.RecruitmentAreaRepository
import zg.acelera.repository.recruiter.RecruiterRepository
import zg.acelera.utils.exception.EntityNotFoundException

class RecruiterService {
    private final RecruiterRepository recruiterRepository
    private final RecruitmentAreaRepository recruitmentAreaRepository

    RecruiterService(RecruiterRepository recruiterRepository, RecruitmentAreaRepository recruitmentAreaRepository) {
        this.recruiterRepository = recruiterRepository
        this.recruitmentAreaRepository = recruitmentAreaRepository
    }

    RecruiterResponseDTO register(RecruiterCreateDTO dto) {
        ensureAreaExists(dto.recruitmentAreaId())
        ensureEmailAvailable(dto.email())
        RecruiterResponseDTO.fromDomain(recruiterRepository.save(dto.toDomain()))
    }

    RecruiterResponseDTO create(RecruiterCreateDTO dto) {
        register(dto)
    }

    RecruiterResponseDTO update(UUID authenticatedUserId, UUID targetUserId, RecruiterUpdateDTO dto) {
        if (authenticatedUserId == null || targetUserId == null || authenticatedUserId != targetUserId)
            throw new SecurityException('Recruiters may only update their own profile.')

        Recruiter current = recruiterRepository.findById(targetUserId)
        if (current.status != null && current.status.name() == 'INACTIVE')
            throw new SecurityException('Inactive recruiter accounts cannot be updated.')

        if (dto.recruitmentAreaId() != null)
            ensureAreaExists(dto.recruitmentAreaId())
        if (dto.email() != null && dto.email() != current.email)
            ensureEmailAvailable(dto.email(), targetUserId)

        RecruiterResponseDTO.fromDomain(recruiterRepository.update(dto.toDomain(), targetUserId))
    }

    RecruiterResponseDTO updateProfile(UUID authenticatedUserId, RecruiterUpdateDTO dto) {
        update(authenticatedUserId, authenticatedUserId, dto)
    }

    private void ensureAreaExists(UUID areaId) {
        boolean exists = recruitmentAreaRepository.findAll().any { row ->
            Object value = row.id ?: row.get('id')
            value instanceof UUID ? value == areaId : value?.toString() == areaId.toString()
        }
        if (!exists)
            throw new IllegalArgumentException('Recruitment area does not exist.')
    }

    private void ensureEmailAvailable(String email, UUID currentUserId = null) {
        try {
            Recruiter existing = recruiterRepository.findByEmail(email)
            if (existing != null && existing.id != currentUserId)
                throw new IllegalArgumentException('Email is already in use.')
        } catch (EntityNotFoundException ignored) {
            // The repository uses EntityNotFoundException for an available email.
        }
    }
}
