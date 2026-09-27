package zg.acelera.utils.reader

import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.service.CountryService

class AddressConsoleReader {
    private final InputReader input
    private final CountryService countryService

    AddressConsoleReader(InputReader input, CountryService countryService) {
        this.input = input
        this.countryService = countryService
    }

    AddressCreateDTO readAddressData() {
        String cep = input.readString("CEP (8 digits): ")
        String street = input.readString("Street: ")
        String number = input.readString("Number: ")
        String complement = input.readString("Complement (press ENTER to skip): ", false)
        String neighborhood = input.readString("Neighborhood: ")
        String city = input.readString("City: ")
        String state = input.readString("State (UF): ")

        List<CountryDTO> countries = countryService.getAllCountries()

        if (!countries) {
            println "Error: No countries available in the database."
            throw new IllegalStateException("No countries available.")
        }

        List<String> countryNames = countries.collect { "${it.name()} (${it.code()})" } as List<String>
        String selectedCountry = input.readOption("Select the country:", countryNames)
        int selectedIndex = countryNames.indexOf(selectedCountry)
        String countryId = countries[selectedIndex].id().toString()

        return new AddressCreateDTO(null, cep, street, number, city, state, complement, neighborhood, countryId, null)
    }
}
