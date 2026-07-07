package com.skylink.repository;

import com.skylink.entity.Airport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirportRepository
        extends JpaRepository<Airport, Long> {

    boolean existsByExternalAirportId(
            Long externalAirportId
    );

    boolean existsByIataCodeIgnoreCase(
            String iataCode
    );

    boolean existsByIcaoCodeIgnoreCase(
            String icaoCode
    );

    Optional<Airport> findByIataCodeIgnoreCase(
            String iataCode
    );

    Optional<Airport> findByIcaoCodeIgnoreCase(
            String icaoCode
    );

    @Query("""
            SELECT a
            FROM Airport a
            WHERE a.active = true

            AND a.iataCode IS NOT NULL

            AND TRIM(a.iataCode) <> ''

            AND a.airportType IN (
                'large_airport',
                'medium_airport',
                'small_airport'
            )

            AND (
                LOWER(a.iataCode)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(a.icaoCode)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(a.city)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(a.airportName)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))

                OR LOWER(a.country)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
            )

            ORDER BY

                CASE

                    WHEN LOWER(a.iataCode)
                        = LOWER(:keyword)
                    THEN 1

                    WHEN LOWER(a.icaoCode)
                        = LOWER(:keyword)
                    THEN 2

                    WHEN LOWER(a.city)
                        = LOWER(:keyword)
                    THEN 3

                    WHEN LOWER(a.city)
                        LIKE LOWER(CONCAT(:keyword, '%'))
                    THEN 4

                    WHEN LOWER(a.airportName)
                        = LOWER(:keyword)
                    THEN 5

                    WHEN LOWER(a.airportName)
                        LIKE LOWER(CONCAT(:keyword, '%'))
                    THEN 6

                    WHEN LOWER(a.city)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    THEN 7

                    WHEN LOWER(a.airportName)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    THEN 8

                    WHEN LOWER(a.country)
                        = LOWER(:keyword)
                    THEN 9

                    WHEN LOWER(a.country)
                        LIKE LOWER(CONCAT(:keyword, '%'))
                    THEN 10

                    ELSE 11

                END,

                CASE

                    WHEN a.airportType = 'large_airport'
                    THEN 1

                    WHEN a.airportType = 'medium_airport'
                    THEN 2

                    WHEN a.airportType = 'small_airport'
                    THEN 3

                    ELSE 4

                END,

                a.city ASC,

                a.airportName ASC
            """)
    List<Airport> searchAirports(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT *
                    FROM airports a

                    WHERE a.active = true

                    AND a.iata_code IS NOT NULL

                    AND TRIM(a.iata_code) <> ''

                    AND a.airport_type IN (
                        'large_airport',
                        'medium_airport',
                        'small_airport'
                    )

                    AND a.latitude IS NOT NULL

                    AND a.longitude IS NOT NULL

                    ORDER BY (

                        6371 * ACOS(

                            LEAST(

                                1.0,

                                GREATEST(

                                    -1.0,

                                    COS(RADIANS(:latitude))

                                    * COS(RADIANS(a.latitude))

                                    * COS(

                                        RADIANS(a.longitude)

                                        - RADIANS(:longitude)

                                    )

                                    + SIN(RADIANS(:latitude))

                                    * SIN(RADIANS(a.latitude))

                                )

                            )

                        )

                    ) ASC

                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<Airport> findNearbyAirports(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("limit") Integer limit
    );
}