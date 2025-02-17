package com.mongodb.javabasic.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.mongodb.javabasic.model.TspCountryInfo;

public interface TspCountryInfoRepository extends MongoRepository<TspCountryInfo, String> {
    @Query(value = "{'ports.airports.iata_airport_code': ?0}", fields = "{'country_code': 1, 'local_currency': 1, '_id': 0}")
    TspCountryInfo findCountryByAirportCode(String airportCode);

    @Query(value = "{'ports.airports.iata_airport_code':{$in: ?0}}", fields = "{'country_code': 1, 'local_currency': 1, '_id': 0}")
    List<TspCountryInfo> findCountriesByAirportCodes(Set<String> airportCodes);

    @Query(value = "{'ports.port_code': ?0}", fields = "{'country_code': 1, 'local_currency': 1, '_id': 0}")
    TspCountryInfo findCountryByPortCode(String portCode);

    @Query(value = "{'ports.port_code': {$in: ?0}}", fields = "{'country_code': 1, 'local_currency': 1, '_id': 0}")
    List<TspCountryInfo> findCountriesByPortCodes(Set<String> portCodes);
}
