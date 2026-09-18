package zg.acelera.service

import zg.acelera.domain.Address
import zg.acelera.dto.address.AddressDTO
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.address.AddressUpdateDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.repository.IAddressRepository
import zg.acelera.utils.exception.EntityNotFoundException

class AddressService {
    private final IAddressRepository addressRepository
    private final CountryService countryService

    AddressService(IAddressRepository addressRepository, CountryService countryService) {
        this.addressRepository = addressRepository
        this.countryService = countryService
    }

    AddressResponseDTO getAddressById(UUID id) {
        AddressResponseDTO addressResponse
        try {
            Address address = addressRepository.findById(id)
            CountryDTO countryDTO = countryService.getCountryById(address.country.id)
            addressResponse = AddressResponseDTO.fromDomain(address, countryDTO)
        } catch (Exception e) {
            e.printStackTrace()
            return null
        }

        return addressResponse
    }

    Set<AddressResponseDTO> getAddressesByUserId(UUID userId) {
        Set<AddressResponseDTO> addressesResponse = new HashSet<>()
        try {
            Set<Address> addresses = addressRepository.findByUserId(userId)
            addresses.each { address ->
            CountryDTO countryDTO = countryService.getCountryById(address.country.id)
            addressesResponse += AddressResponseDTO.fromDomain(address, countryDTO)}
        } catch (Exception e) {
            e.printStackTrace()
            return []
        }

        return addressesResponse
    }

    AddressResponseDTO createAddress(AddressDTO addressDTO, UUID userId) {
        Address address = addressDTO.toDomain()
        AddressResponseDTO addressResponse
        try {
            Address createdAddress = addressRepository.create(address, userId)
            CountryDTO countryDTO = countryService.getCountryById(createdAddress.country.id)
            addressResponse = AddressResponseDTO.fromDomain(createdAddress, countryDTO)
        } catch (Exception e) {
            e.printStackTrace()
            return null
        }

        return addressResponse
    }

    AddressResponseDTO updateAddress(AddressUpdateDTO addressUpdateDTO, UUID addressId) {
        Address address = addressUpdateDTO.toDomain()
        Address updatedAddress
        try {
            updatedAddress = addressRepository.update(address, addressId)
        } catch (EntityNotFoundException e) {
            e.printStackTrace()
            return null
        }

        return AddressResponseDTO.fromDomain(updatedAddress)
    }

    void deleteAddress(UUID addressId) {
        try {
            addressRepository.delete(addressId)
        } catch (EntityNotFoundException e) {
            e.printStackTrace()
        }
    }
}
