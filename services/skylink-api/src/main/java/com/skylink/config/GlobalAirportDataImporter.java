package com.skylink.config;

import com.skylink.entity.Airport;
import com.skylink.repository.AirportRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GlobalAirportDataImporter
        implements CommandLineRunner {

    private static final int BATCH_SIZE = 1000;

    private final AirportRepository airportRepository;

    public GlobalAirportDataImporter(
            AirportRepository airportRepository
    ) {
        this.airportRepository = airportRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println(
                "SkyLink Global Airport Import Started..."
        );

        ClassPathResource resource =
                new ClassPathResource("data/airports.csv");

        if (!resource.exists()) {

            System.out.println(
                    "airports.csv not found."
            );

            return;
        }

        List<Airport> existingAirports =
                airportRepository.findAll();

        Set<Long> existingExternalIds =
                new HashSet<>();

        Map<String, Long> iataCodeOwners =
                new HashMap<>();

        Map<String, Long> icaoCodeOwners =
                new HashMap<>();

        for (Airport airport : existingAirports) {

            if (airport.getExternalAirportId() != null) {

                existingExternalIds.add(
                        airport.getExternalAirportId()
                );
            }

            if (airport.getIataCode() != null) {

                iataCodeOwners.put(
                        airport.getIataCode()
                                .toUpperCase(),
                        airport.getExternalAirportId()
                );
            }

            if (airport.getIcaoCode() != null) {

                icaoCodeOwners.put(
                        airport.getIcaoCode()
                                .toUpperCase(),
                        airport.getExternalAirportId()
                );
            }
        }

        int importedCount = 0;
        int skippedCount = 0;
        int duplicateIataCount = 0;
        int duplicateIcaoCount = 0;

        List<Airport> airportBatch =
                new ArrayList<>(BATCH_SIZE);

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        resource.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            Iterable<CSVRecord> records =
                    CSVFormat.DEFAULT
                            .builder()
                            .setHeader()
                            .setSkipHeaderRecord(true)
                            .get()
                            .parse(reader);

            for (CSVRecord record : records) {

                Long externalAirportId =
                        parseLong(
                                record.get("id")
                        );

                if (externalAirportId == null) {

                    skippedCount++;

                    continue;
                }

                if (existingExternalIds.contains(
                        externalAirportId
                )) {

                    skippedCount++;

                    continue;
                }

                String airportName =
                        cleanText(
                                record.get("name")
                        );

                if (airportName == null) {

                    skippedCount++;

                    continue;
                }

                String iataCode =
                        cleanCode(
                                record.get("iata_code"),
                                3
                        );

                String icaoCode =
                        cleanCode(
                                record.get("gps_code"),
                                4
                        );

                if (iataCode != null) {

                    Long ownerExternalId =
                            iataCodeOwners.get(
                                    iataCode
                            );

                    if (ownerExternalId != null
                            && !ownerExternalId.equals(
                            externalAirportId
                    )) {

                        System.out.println(
                                "Duplicate IATA skipped: "
                                        + iataCode
                                        + " | Airport: "
                                        + airportName
                        );

                        iataCode = null;

                        duplicateIataCount++;
                    }
                }

                if (icaoCode != null) {

                    Long ownerExternalId =
                            icaoCodeOwners.get(
                                    icaoCode
                            );

                    if (ownerExternalId != null
                            && !ownerExternalId.equals(
                            externalAirportId
                    )) {

                        System.out.println(
                                "Duplicate ICAO skipped: "
                                        + icaoCode
                                        + " | Airport: "
                                        + airportName
                        );

                        icaoCode = null;

                        duplicateIcaoCount++;
                    }
                }

                String countryCode =
                        cleanText(
                                record.get("iso_country")
                        );

                Airport airport =
                        Airport.builder()
                                .externalAirportId(
                                        externalAirportId
                                )
                                .iataCode(
                                        iataCode
                                )
                                .icaoCode(
                                        icaoCode
                                )
                                .airportName(
                                        airportName
                                )
                                .city(
                                        cleanText(
                                                record.get(
                                                        "municipality"
                                                )
                                        )
                                )
                                .country(
                                        countryCode
                                )
                                .countryCode(
                                        countryCode
                                )
                                .airportType(
                                        cleanText(
                                                record.get(
                                                        "type"
                                                )
                                        )
                                )
                                .latitude(
                                        parseDouble(
                                                record.get(
                                                        "latitude_deg"
                                                )
                                        )
                                )
                                .longitude(
                                        parseDouble(
                                                record.get(
                                                        "longitude_deg"
                                                )
                                        )
                                )
                                .elevation(
                                        parseDouble(
                                                record.get(
                                                        "elevation_ft"
                                                )
                                        )
                                )
                                .timezone(null)
                                .active(true)
                                .build();

                airportBatch.add(
                        airport
                );

                existingExternalIds.add(
                        externalAirportId
                );

                if (iataCode != null) {

                    iataCodeOwners.put(
                            iataCode,
                            externalAirportId
                    );
                }

                if (icaoCode != null) {

                    icaoCodeOwners.put(
                            icaoCode,
                            externalAirportId
                    );
                }

                if (airportBatch.size()
                        >= BATCH_SIZE) {

                    airportRepository
                            .saveAllAndFlush(
                                    airportBatch
                            );

                    importedCount +=
                            airportBatch.size();

                    airportBatch.clear();

                    System.out.println(
                            "Imported airports: "
                                    + importedCount
                    );
                }
            }

            if (!airportBatch.isEmpty()) {

                airportRepository
                        .saveAllAndFlush(
                                airportBatch
                        );

                importedCount +=
                        airportBatch.size();

                airportBatch.clear();
            }
        }

        System.out.println(
                "========================================"
        );

        System.out.println(
                "SkyLink Global Airport Import Complete"
        );

        System.out.println(
                "Imported: "
                        + importedCount
        );

        System.out.println(
                "Skipped: "
                        + skippedCount
        );

        System.out.println(
                "Duplicate IATA Codes: "
                        + duplicateIataCount
        );

        System.out.println(
                "Duplicate ICAO Codes: "
                        + duplicateIcaoCount
        );

        System.out.println(
                "Total Airports in MySQL: "
                        + airportRepository.count()
        );

        System.out.println(
                "========================================"
        );
    }

    private String cleanCode(
            String value,
            int requiredLength
    ) {

        String cleanedValue =
                cleanText(value);

        if (cleanedValue == null) {

            return null;
        }

        cleanedValue =
                cleanedValue.toUpperCase();

        if (!cleanedValue.matches(
                "^[A-Z0-9]{"
                        + requiredLength
                        + "}$"
        )) {

            return null;
        }

        return cleanedValue;
    }

    private String cleanText(
            String value
    ) {

        if (value == null) {

            return null;
        }

        String cleanedValue =
                value.trim();

        if (cleanedValue.isBlank()) {

            return null;
        }

        return cleanedValue;
    }

    private Long parseLong(
            String value
    ) {

        try {

            return Long.parseLong(
                    value.trim()
            );

        } catch (Exception exception) {

            return null;
        }
    }

    private Double parseDouble(
            String value
    ) {

        try {

            if (value == null
                    || value.isBlank()) {

                return null;
            }

            return Double.parseDouble(
                    value.trim()
            );

        } catch (Exception exception) {

            return null;
        }
    }
}