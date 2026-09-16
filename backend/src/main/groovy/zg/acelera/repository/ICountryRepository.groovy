package zg.acelera.repository

import zg.acelera.domain.Country

interface ICountryRepository {
    Country findById(UUID id);
    List<Country> findAll();
}