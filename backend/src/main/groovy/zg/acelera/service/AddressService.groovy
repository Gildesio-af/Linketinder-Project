package zg.acelera.service

import zg.acelera.domain.Address
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.address.AddressUpdateDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.repository.address.AddressRepository
import zg.acelera.utils.exception.EntityNotFoundException

class AddressService {
    private final AddressRepository addressRepository
    private final CountryService countryService

    AddressService(AddressRepository addressRepository, CountryService countryService) {
        this.addressRepository = addressRepository
        this.countryService = countryService
    }

    AddressResponseDTO getAddressById(UUID id) {
        Address address = addressRepository.findById(id)
        CountryDTO countryDTO = countryService.getCountryById(address.country.id)
        return AddressResponseDTO.fromDomain(address, countryDTO)
    }

    Set<AddressResponseDTO> getAddressesByUserId(UUID userId) {
        Set<AddressResponseDTO> addressesResponse = new HashSet<>()
        Set<Address> addresses = addressRepository.findByUserId(userId)
        addresses.each { address ->
            CountryDTO countryDTO = countryService.getCountryById(address.country.id)
            addressesResponse += AddressResponseDTO.fromDomain(address, countryDTO)
        }

        return addressesResponse
    }

    AddressResponseDTO getAddressByJobId(UUID jobId) {
        Address address = addressRepository.findByJobId(jobId)
        CountryDTO countryDTO = countryService.getCountryById(address.country.id)
        return AddressResponseDTO.fromDomain(address, countryDTO)
    }

    AddressResponseDTO createAddress(AddressCreateDTO addressDTO, UUID userId) {
        Address address = addressDTO.toDomain()

        Address createdAddress = addressRepository.create(address, userId)
        CountryDTO countryDTO = countryService.getCountryById(createdAddress.country.id)
        return AddressResponseDTO.fromDomain(createdAddress, countryDTO)
    }

    AddressResponseDTO updateAddress(AddressUpdateDTO addressUpdateDTO, UUID addressId) {
        Address address = addressUpdateDTO.toDomain()

        Address updatedAddress = addressRepository.update(address, addressId)
        CountryDTO countryDTO = countryService.getCountryById(updatedAddress.country.id)
        return AddressResponseDTO.fromDomain(updatedAddress, countryDTO)
    }

    void deleteAddress(UUID addressId) {
        addressRepository.delete(addressId)
    }
}
