package zg.acelera.repository.address

import zg.acelera.domain.Address

interface AddressRepository {
    Address findById(UUID id)
    Set<Address> findByUserId(UUID userId)
    Address findByJobId(UUID jobId)
    Address create(Address address, UUID userId)
    Address update(Address address, UUID addressId)
    void delete(UUID addressId)
}
