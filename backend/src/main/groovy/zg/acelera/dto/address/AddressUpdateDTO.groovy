package zg.acelera.dto.address

record AddressUpdateDTO(
        String cep,
        String street,
        String number,
        String city,
        String state,
        String country)
{
    AddressUpdateDTO() {
        if (!cep && cep().trim().length() <= 8)
            throw new IllegalArgumentException("CEP must be provided and have at least 8 characters")
        if (!street && street().trim().isEmpty())
            throw new IllegalArgumentException("Street must be provided and cannot be empty")
        if (!number && number().trim().isEmpty())
            throw new IllegalArgumentException("Number must be provided and cannot be empty")
        if (!city() && city().trim().isEmpty())
            throw new IllegalArgumentException("City must be provided and cannot be empty")
        if (!state() && state().trim().isEmpty())
            throw new IllegalArgumentException("State must be provided and cannot be empty")
        if (!country() && country().trim().isEmpty())
            throw new IllegalArgumentException("Country must be provided and cannot be empty")
    }
}
