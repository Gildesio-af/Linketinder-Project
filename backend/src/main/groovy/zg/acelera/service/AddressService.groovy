package zg.acelera.service

import zg.acelera.domain.Address
import zg.acelera.dto.address.AddressDTO
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.address.AddressUpdateDTO
import zg.acelera.repository.IAddressRepository
import zg.acelera.utils.exception.EntityNotFoundException

class AddressService {
    private final IAddressRepository addressRepository

    AddressService(IAddressRepository addressRepository) {
        this.addressRepository = addressRepository
    }

    AddressResponseDTO getAddressById(UUID id) {
        Address address
        try {
            address = addressRepository.findById(id)
        } catch (Exception e) {
            e.printStackTrace()
            return null
        }

        return AddressResponseDTO.fromDomain(address)
    }

    Set<AddressResponseDTO> getAddressesByUserId(UUID userId) {
        Set<Address> addresses
        try {
            addresses = addressRepository.findByUserId(userId)
        } catch (Exception e) {
            e.printStackTrace()
            return []
        }

        return addresses.collect { address -> AddressResponseDTO.fromDomain(address) } as Set<AddressResponseDTO>
    }

    AddressDTO createAddress(AddressDTO addressDTO) {
        Address address = addressDTO.toDomain()
        Address createdAddress
        try {
            createdAddress = addressRepository.create(address)
        } catch (Exception e) {
            e.printStackTrace()
            return null
        }

        return new AddressDTO(
            id: createdAddress.id,
            street: createdAddress.street,
            cep: createdAddress.cep,
            number: createdAddress.number,
            complement: createdAddress.complement,
            neighborhood: createdAddress.neighborhood,
            city: createdAddress.city,
            state: createdAddress.state,
            countryId: createdAddress.country.id,
            userId: createdAddress.userId
        )
    }

    AddressDTO updateAddress(AddressUpdateDTO addressUpdateDTO, UUID addressId) {
        Address address = addressUpdateDTO.toDomain()
        Address updatedAddress
        try {
            updatedAddress = addressRepository.update(address, addressId)
        } catch (EntityNotFoundException e) {
            e.printStackTrace()
            return null
        }

        return new AddressDTO(
            id: updatedAddress.id,
            street: updatedAddress.street,
            cep: updatedAddress.cep,
            number: updatedAddress.number,
            complement: updatedAddress.complement,
            neighborhood: updatedAddress.neighborhood,
            city: updatedAddress.city,
            state: updatedAddress.state,
            countryId: updatedAddress.country.id,
            userId: updatedAddress.userId
        )
    }

    boolean deleteAddress(UUID addressId) {
        try {
            addressRepository.delete(addressId)
            return true
        } catch (EntityNotFoundException e) {
            e.printStackTrace()
            return false
        }
    }
}
