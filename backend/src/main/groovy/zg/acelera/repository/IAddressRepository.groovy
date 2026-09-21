package zg.acelera.repository

import zg.acelera.domain.Address

interface IAddressRepository {
    Address findById(UUID id)
    Set<Address> findByUserId(UUID userId)
    Address findByJobId(UUID jobId)
    Address create(Address address, UUID userId)
    Address update(Address address, UUID addressId)
    void delete(UUID addressId)
}
