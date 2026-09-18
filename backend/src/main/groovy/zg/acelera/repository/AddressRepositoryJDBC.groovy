package zg.acelera.repository

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import zg.acelera.domain.Address
import zg.acelera.domain.Country
import zg.acelera.utils.exception.EntityNotFoundException

class AddressRepositoryJDBC implements IAddressRepository {
    final Sql sql

    AddressRepositoryJDBC(Sql sql) {
        this.sql = sql
    }

    @Override
    Address findById(UUID id) {
        GroovyRowResult row = sql.firstRow("SELECT * FROM addresses AS ad, countries WHERE ad.id = ? " +
                "AND ad.country_id = countries.id", [id])
        if (row) return createAddressFromRow(row)

        throw new EntityNotFoundException("Address with id ${id} not found")
    }

    @Override
    Set<Address> findByUserId(UUID userId) {
        List<GroovyRowResult> rows = sql.rows("SELECT * FROM addresses, countries WHERE addresses.user_id = ? " +
                "AND addresses.country_id = countries.id", [userId])
        if (rows) return rows.collect { row -> createAddressFromRow(row) }

        return [] as Set<Address>
    }

    @Override
    Address create(Address address, UUID userID) {
        String insertQuery = """
            INSERT INTO addresses (user_id, cep, street, number, complement, neighborhood, city, state, country_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING *
        """
        GroovyRowResult row = sql.firstRow(insertQuery, [
            userID,
            address.cep,
            address.street,
            address.number,
            address.complement,
            address.neighborhood,
            address.city,
            address.state,
            address.country.id
        ])

        return createAddressFromRow(row)
    }

    @Override
    Address update(Address address, UUID addressId) {
        String updateQuery = """
            UPDATE addresses
            SET 
                user_id = COALESCE(?, user_id),
                cep = COALESCE(?, cep),
                street = COALESCE(?, street),
                number = COALESCE(?, number),
                complement = COALESCE(?, complement),
                neighborhood = COALESCE(?, neighborhood),
                city = COALESCE(?, city),
                state = COALESCE(?, state),
                country_id = COALESCE(?, country_id)
            WHERE id = ? 
            RETURNING *
        """

        GroovyRowResult row = sql.firstRow(updateQuery, [
            address.userId,
            address.cep,
            address.street,
            address.number,
            address.complement,
            address.neighborhood,
            address.city,
            address.state,
            address.country.id,
            addressId
        ])
        if (row) return createAddressFromRow(row)

        throw new EntityNotFoundException("Address with id ${addressId} not found")
    }

    @Override
    void delete(UUID addressId) {
        String deleteQuery = "DELETE FROM addresses WHERE id = ?"
        int rowsAffected = sql.executeUpdate(deleteQuery, [addressId])
        if (rowsAffected == 0) {
            throw new EntityNotFoundException("Address with id ${addressId} not found")
        }
    }

    private static Address createAddressFromRow(GroovyRowResult row) {
        return new Address(
            id: UUID.fromString(row.id.toString()),
            userId: UUID.fromString(row.user_id.toString()),
            cep: row.cep,
            street: row.street,
            number: row.number,
            complement: row.complement,
            neighborhood: row.neighborhood,
            city: row.city,
            state: row.state,
            country: new Country(id: UUID.fromString(row.country_id.toString()))
        )
    }
}
