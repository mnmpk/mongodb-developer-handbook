package com.mongodb.javabasic.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.mongodb.javabasic.model.TspPortInfo;

public interface TspPortInfoRepository extends MongoRepository<TspPortInfo, String> {
    @Query(value = "{'airports.iata_airport_code': ?0}", fields = "{'country_code': 1, 'local_currency': 1, '_id': 0}")
    TspPortInfo findPortByAirportCode(String airportCode);

    @Query(value = "{'airports.iata_airport_code':{$in: ?0}}", fields = "{'country_code': 1, 'local_currency': 1, '_id': 0}")
    List<TspPortInfo> findPortsByAirportCodes(Set<String> airportCodes);

    @Query(value = "{'port_code': ?0}", fields = "{'country_code': 1, 'local_currency': 1, '_id': 0}")
    TspPortInfo findPortByPortCode(String portCode);

    @Query(value = "{'port_code': {$in: ?0}}", fields = "{'country_code': 1, 'local_currency': 1, '_id': 0}")
    List<TspPortInfo> findPortsByPortCodes(Set<String> portCodes);
}
