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
  "projectedCo2EmissionsAvoidedInTonnes": 0,
  "month": 0,
  "year": 2026
}
```

# CRON Config

Monthly CronJOB that pushes a trigger message into `carbon-emission-calculate` topic with month and year.

==get elmeasure's monthly data update time==
# Emission Calculation

`carbon-emission-calculate` topic is consumed in `im-services-analytics` service and will fetch facilities marked for carbon emissions avoided visibility from MDMS.

In the future, all facilities are treated as marked for visibility then the facilities are to be fetched in batches, paginated and ordered by created time asc.

fetch the projectIds and projectNames given a list of facility IDs; enhance project service to add `POST /v1/fetchProjectsByFacilities` endpoint that will execute a sql query to fetch this data ( no additional api calls to field-planner or field-planner-activity requried)

batch process facilities.

for every facility processed:
- add an entry to `co2-monthly-facility-index-v000001` index.
- remove existing entries for that facilityID from `co2-monthly-projection-facility-index-v000001` index
- recalculate all months projections for that facility and bulk insert into `co2-monthly-projection-facility-index-v000001` index

==see if we can poll monthly solar consumption, monthly grid consumption and monthly total consumption from elmeasure in bulk, i.e all facilities at once==

use redis if any fetched data is to be used across multiple facilities
# Facility module changes

- enhance facility search if required for paginated fetch
- allow search by list of facility IDs
- add the fields solarInstallationDate and rmsInstallationDate to the facility table

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
