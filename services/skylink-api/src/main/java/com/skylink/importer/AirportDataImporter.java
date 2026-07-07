package com.skylink.importer;

import com.skylink.entity.Airport;
import com.skylink.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AirportDataImporter implements CommandLineRunner {

    private static final int BATCH_SIZE = 1000;

    private final AirportRepository airportRepository;

    @Override
    public void run(String... args) throws Exception {

        if (airportRepository.count() > 0) {
            log.info(
                    "Airport import skipped. Airports already exist: {}",
                    airportRepository.count()
            );
            return;
        }

        log.info("Starting airport data import...");

        ClassPathResource resource =
                new ClassPathResource("data/airports.csv");

        int imported = 0;
        int skipped = 0;

        List<Airport> batch =
                new ArrayList<>(BATCH_SIZE);

        try (
                Reader reader =
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
                            .setIgnoreEmptyLines(true)
                            .setTrim(true)
                            .get()
                            .parse(reader);

            for (CSVRecord record : records) {

                try {

                    Long externalAirportId =
                            parseLong(
                                    getValue(record, "id")
                            );

                    if (externalAirportId == null) {
                        skipped++;
                        continue;
                    }

                    String airportName =
                            normalize(
                                    getValue(record, "name")
                            );

                    if (airportName == null) {
                        skipped++;
                        continue;
                    }

                    Airport airport =
                            Airport.builder()
                                    .externalAirportId(
                                            externalAirportId
                                    )
                                    .iataCode(
                                            normalizeCode(
                                                    getValue(
                                                            record,
                                                            "iata_code"
                                                    ),
                                                    3
                                            )
                                    )
                                    .icaoCode(
                                            normalizeCode(
                                                    getValue(
                                                            record,
                                                            "icao_code"
                                                    ),
                                                    4
                                            )
                                    )
                                    .airportName(
                                            airportName
                                    )
                                    .city(
                                            normalize(
                                                    getValue(
                                                            record,
                                                            "municipality"
                                                    )
                                            )
                                    )
                                    .country(
                                            normalize(
                                                    getValue(
                                                            record,
                                                            "country_name"
                                                    )
                                            )
                                    )
                                    .countryCode(
                                            normalizeCode(
                                                    getValue(
                                                            record,
                                                            "iso_country"
                                                    ),
                                                    2
                                            )
                                    )
                                    .airportType(
                                            normalize(
                                                    getValue(
                                                            record,
                                                            "type"
                                                    )
                                            )
                                    )
                                    .latitude(
                                            parseDouble(
                                                    getValue(
                                                            record,
                                                            "latitude_deg"
                                                    )
                                            )
                                    )
                                    .longitude(
                                            parseDouble(
                                                    getValue(
                                                            record,
                                                            "longitude_deg"
                                                    )
                                            )
                                    )
                                    .elevation(
                                            parseDouble(
                                                    getValue(
                                                            record,
                                                            "elevation_ft"
                                                    )
                                            )
                                    )
                                    .timezone(null)
                                    .active(true)
                                    .build();

                    batch.add(airport);

                    if (batch.size() >= BATCH_SIZE) {

                        airportRepository.saveAll(batch);

                        imported += batch.size();

                        log.info(
                                "Imported {} airports...",
                                imported
                        );

                        batch.clear();
                    }

                } catch (Exception exception) {

                    skipped++;

                    log.debug(
                            "Skipping airport record {}: {}",
                            record.getRecordNumber(),
                            exception.getMessage()
                    );
                }
            }

            if (!batch.isEmpty()) {

                airportRepository.saveAll(batch);

                imported += batch.size();

                batch.clear();
            }
        }

        log.info(
                "Airport import completed. Imported: {}, Skipped: {}",
                imported,
                skipped
        );
    }

    private String getValue(
            CSVRecord record,
            String column
    ) {

        if (!record.isMapped(column)) {
            return null;
        }

        return record.get(column);
    }

    private String normalize(String value) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        if (
                normalized.isEmpty()
                        || normalized.equals("\\N")
        ) {
            return null;
        }

        return normalized;
    }

    private String normalizeCode(
            String value,
            int maxLength
    ) {

        String normalized =
                normalize(value);

        if (normalized == null) {
            return null;
        }

        normalized =
                normalized.toUpperCase();

        if (normalized.length() > maxLength) {
            return null;
        }

        return normalized;
    }

    private Long parseLong(String value) {

        String normalized =
                normalize(value);

        if (normalized == null) {
            return null;
        }

        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Double parseDouble(String value) {

        String normalized =
                normalize(value);

        if (normalized == null) {
            return null;
        }

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}