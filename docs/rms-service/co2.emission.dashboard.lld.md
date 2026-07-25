# Calculation formulas

See [docs/co2-dashboard/CO2_CALCULATION_FORMULAS.md](docs/co2-dashboard/CO2_CALCULATION_FORMULAS.md) for formulas, reference data, timeline rules, and archetype vs RMS paths.

# elastic indexes required

## co2-monthly-facility-index-v000001 index

```json
{
  "state": "",
  "district": "",
  "block": "",
  "boundary": {
    "countryCode": "",
    "stateCode": "",
    "districtCode": "",
    "blockCode": "",
    "facilityCode": ""
  },
  "facilityId": "",
  "hfrId": "",
  "ninId": "",
  "facilityType": "",
  "facilityName": "",
  "projectName": "",
  "tenantId": "",
  "geo-point": "<GeoPoint>",
  "isLive": "",
  "solarInstallationDate": "",
  "rmsInstallationDate": "",
  "solarCapacityKwp": 0,
  "co2EmissionsAvoidedInTonnes": 0,
  "month": 0,
  "year": 2026
}
```

## co2-monthly-projection-facility-index-v000001 index

```json
{
  "state": "",
  "district": "",
  "block": "",
  "boundary": {
    "countryCode": "",
    "stateCode": "",
    "districtCode": "",
    "blockCode": "",
    "facilityCode": ""
  },
  "facilityId": "",
  "hfrId": "",
  "ninId": "",
  "facilityType": "",
  "facilityName": "",
  "tenantId": "",
  "geo-point": "<GeoPoint>",
  "isLive": "",
  "solarInstallationDate": "",
  "rmsInstallationDate": "",
  "solarCapacityKwp": 0,
  "projectedCo2EmissionsAvoidedInTonnes": 0,
  "month": 0,
  "year": 2026
}
```

# CRON Config

Monthly CronJOB that pushes a trigger message into `carbon-emission-calculate` topic with month and year.

==get elmeasure's monthly data update time==
# Emission Calculation

`carbon-emission-calculate` topic is consumed in `im-services-analytics` service and fetches **all active facilities** from the facility registry via paginated `POST /facility-service/v2/facility/_bulk-search` (ordered by `created_at` ASC, page size `co2.batch.facility.size`).

Facilities missing `solarInstallationDate` or `solarSystemCapacityKwp` are skipped during processing.

fetch the projectIds and projectNames given a list of facility IDs; enhance project service to add `POST /v1/fetchProjectsByFacilities` endpoint that will execute a sql query to fetch this data ( no additional api calls to field-planner or field-planner-activity requried)

batch process facilities.

for every facility processed:
- compute monthly CO₂: archetype or RMS solar kWh × GIF; cap solar kWh at `solarSystemCapacityKwp × state_sunshine_hours × days_in_month` when kWp and sunshine reference exist (PRD)
- publish actual months (≤ batch month/year) to `save-co2-monthly-facility-indexer` → `co2-monthly-facility-index-v000001`
- recalculate and upsert projection months (> batch month/year) to `save-co2-monthly-projection-facility-indexer` → `co2-monthly-projection-facility-index-v000001` (same _id replaces prior doc)
- see `docs/egov-indexer/CO2_INDEXER_SETUP.md`

==see if we can poll monthly solar consumption, monthly grid consumption and monthly total consumption from elmeasure in bulk, i.e all facilities at once==

use redis if any fetched data is to be used across multiple facilities
# Facility module changes

- enhance facility search if required for paginated fetch — **done** (CO2 batch uses paginated `_bulk-search`)
- allow search by list of facility IDs
- add the fields solarInstallationDate, rmsInstallationDate and solarSystemCapacityKwp (`solar_system_capacity_kwp`) to the facility table
- archetype_lookup v2 final: seven MDMS facility types per state (no Unknown, no SC/HWC combined row)

# Tables to be created

Archetype lookup and archetype properties reference tables, Grid Intensity Factor table and State Sunshine hours reference table as per req. add id (pk) and tenantId columns.

## Grid Intensity Factor table

```
grid_intensity_factor:
- id (PK)
- tenantId
- financialYear (unique constrant)
- gridIntensityFactor
- projectedGridIntensityFactor
```

use `gridIntensityFactor` and fallback onto `projectedGridIntensityFactor` if `gridIntensityFactor` is missing, for calculations.
