package zg.acelera.dto.company

import groovy.transform.ImmutableOptions
import zg.acelera.domain.Company
import zg.acelera.dto.address.AddressResponseDTO

@ImmutableOptions(knownImmutableClasses = [AddressResponseDTO])
record CompanyResponseDTO(
        UUID id,
        String name,
        String email,
        String description,
        String cnpj,
        Set<AddressResponseDTO> addresses
) {
    static CompanyResponseDTO fromDomain(Company company, Set<AddressResponseDTO> addresses) {
        return new CompanyResponseDTO(
                id: company.id,
                name: company.name,
                email: company.email,
                description: company.description,
                cnpj: company.cnpj,
                addresses: addresses
        )
    }
}
