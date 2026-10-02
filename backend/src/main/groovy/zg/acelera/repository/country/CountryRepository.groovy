package zg.acelera.repository.country

import zg.acelera.domain.Country

interface CountryRepository {
    Country findById(UUID id);
    List<Country> findAll();
}