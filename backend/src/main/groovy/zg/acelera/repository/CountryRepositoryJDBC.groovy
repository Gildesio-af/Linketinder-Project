package zg.acelera.repository

import zg.acelera.domain.Country

class CountryRepositoryJDBC implements ICountryRepository {

    @Override
    Country findById(UUID id) {
        return null
    }

    @Override
    List<Country> findAll() {
        return null
    }
}
