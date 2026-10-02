package zg.acelera.user_interface

import zg.acelera.dto.address.AddressResponseDTO

class AddressPrinter {
    static void printAddressesFormated(Set<AddressResponseDTO> addresses) {
        if (addresses.isEmpty()) {
            println "No addresses found."
            return
        }

        addresses.each { addr ->
            println "  Address: ${addr.street()}, ${addr.number()} - ${addr.neighborhood()}, ${addr.city()} - ${addr.state()}, ${addr.cep()}"
            if (addr.country()) {
                println "  Country: ${addr.country().name()} (${addr.country().code()})"
            }
        }
    }
}
