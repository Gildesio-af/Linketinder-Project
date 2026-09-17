package zg.acelera.repository

import zg.acelera.domain.Address

interface IAddressRepository {
    Address findById(UUID id)
    Set<Address> findByUserId(UUID userId)
    Address create(Address address)
    Address update(Address address, UUID addressId)
    void delete(UUID addressId)
}
