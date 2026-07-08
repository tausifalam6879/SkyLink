import { mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const rootDir = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const sourcePath = resolve(
  rootDir,
  "services/skylink-api/src/main/resources/data/airports.csv"
);
const targetPath = resolve(rootDir, "skylink-web/public/data/airports.json");

const parseCsvLine = (line) => {
  const fields = [];
  let field = "";
  let quoted = false;

  for (let index = 0; index < line.length; index += 1) {
    const char = line[index];

    if (quoted) {
      if (char === '"' && line[index + 1] === '"') {
        field += '"';
        index += 1;
      } else if (char === '"') {
        quoted = false;
      } else {
        field += char;
      }
    } else if (char === '"') {
      quoted = true;
    } else if (char === ",") {
      fields.push(field);
      field = "";
    } else {
      field += char;
    }
  }

  fields.push(field);
  return fields;
};

const cleanValue = (value) => {
  const trimmed = String(value || "").trim();
  return trimmed === "\\N" ? "" : trimmed;
};

const toNumber = (value) => {
  const parsed = Number(cleanValue(value));
  return Number.isFinite(parsed) ? parsed : null;
};

const text = readFileSync(sourcePath, "utf8");
const lines = text.split(/\r?\n/).filter(Boolean);
const headers = parseCsvLine(lines[0]);

const airports = lines
  .slice(1)
  .map((line) => {
    const values = parseCsvLine(line);
    return Object.fromEntries(
      values.map((value, index) => [headers[index], cleanValue(value)])
    );
  })
  .filter(
    (row) =>
      row.iata_code &&
      row.type !== "closed" &&
      (row.municipality || row.name)
  )
  .map((row) => ({
    id: Number(row.id),
    iataCode: row.iata_code.toUpperCase(),
    icaoCode: row.icao_code || row.gps_code || row.ident,
    airportName: row.name,
    city: row.municipality || row.region_name || row.country_name,
    country: row.country_name,
    countryCode: row.iso_country,
    type: row.type,
    scheduledService: row.scheduled_service === "1",
    latitude: toNumber(row.latitude_deg),
    longitude: toNumber(row.longitude_deg),
    keywords: row.keywords,
    score: toNumber(row.score) || 0,
  }))
  .sort((first, second) => {
    if (first.scheduledService !== second.scheduledService) {
      return first.scheduledService ? -1 : 1;
    }

    if (first.score !== second.score) {
      return second.score - first.score;
    }

    return first.airportName.localeCompare(second.airportName);
  });

mkdirSync(dirname(targetPath), { recursive: true });
writeFileSync(targetPath, `${JSON.stringify(airports)}\n`);
console.log(`Generated ${airports.length} airports at ${targetPath}`);
