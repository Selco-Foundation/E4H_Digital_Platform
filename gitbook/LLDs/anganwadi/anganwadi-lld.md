# Anganwadi LLD

188 anganwadi's in meghalaya

## Anganwadi accomodation in facilities

- rename health facilities to "facilities" in UI and dashboards
- update all existing health facilities and any future health facilities to be of facility_category of `HEALTH`.
- use facility_category of `ANGANWADI` for anganwadi facilities.
- update facility search apis to accept facility_category as query.
- update usages of facility search to use the facility_category filter where applicable.
- facility ingestion excel sheets should add facility_category column with dropdown of two options.( the options must be fetched from MDMS, currently the two options should be `HEALTH` and `ANGANWADI`. ). HFR and NIN ID should become non-mandatory columns / mandatory if facility_category is `HEALTH`. Add a column for AW (anganwadi worker - type number) - use the aw number as username if facility_category is `ANGANWADI`.
- AW users logging in, should result in a fetch of facility they belong to and using the _first_ returned facility's facility_category to determine the branding to be used on the UI.
- possible future, not needed right now - if CRMs/ complaint resolvers need  to be made scoped to only anganwadis, then we can store user scope in their profile and then query to limit results to the specific facility categories.

## Anganwadi Dashboards

- facility_category filter will be added on all dashboards.
- the boundaries won't need to be remade for anganwadis
- any query for specifically anganwadis or health facilities can be managed by this filter.
- dashboard users that are required to have view over only anganwadi facilities can be created the same way we create users that have view only over states.
