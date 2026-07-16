# OpenLMIS Configuration Tool

This repository contains the code of the OpenLMIS Configuration Tool. It converts
CSV input and mapping files into the JSON files that can be consumed by the OpenLMIS web API and
inserts them into the running OpenLMIS instance.

## Prerequisites

* Docker 1.11+
* Docker Compose 1.6+

## Quick start

1. Fork/clone this repository from GitHub.

```shell
git clone https://github.com/OpenLMIS/openlmis-refdata-seed.git
```

2. Build the application by using docker/gradle. After the build steps finish, you should see
   'BUILD SUCCESS'.

```shell
docker-compose -f docker-compose.builder.yml run builder
```

3. Execute the generated jar file from the build/libs directory.

```shell
java -jar build/libs/openlmis-refdata-seed-1.0.0-SNAPSHOT.jar
```

4. You can also run the tool in the debug mode (it will listen on port 5005).

```shell
gradle bootRun --debug-jvm
```

## Configuration file

The configuration file must be placed in the same directory as the tool (jar file) itself.

```shell
# config.properties
host=http://myopenlmis.instance.org
login=administrator
password=password
clientId=user-client
clientSecret=changeme
updateAllowed=true
autoVerifyEmails=false
directory=/home/user/inputFiles
```

## Export mode

In addition to seeding data *into* an instance, the tool can run in reverse: read the master data
*out* of a running OpenLMIS instance and write it back as CSV files that follow the same mapping
format. This is useful for capturing the current state of an instance and for detecting
configuration
drift (data added or changed via the UI/DB after the original seed).

Enable it by setting `mode=export` in the configuration file:

```shell
# config.properties
host=http://myopenlmis.instance.org
login=administrator
password=password
clientId=user-client
clientSecret=changeme
mode=export
directory=/home/user/mappingFiles
outputDirectory=/home/user/exportOutput
```

* `mode=export` switches the tool from seeding to exporting. When omitted (or set to anything else)
  the tool seeds as usual.
* `directory` must contain the **mapping files only** (`*_mapping.csv`). No data CSVs are required —
  the mappings alone tell the exporter which entities to fetch and how to shape each output file.
  The
  child mapping files referenced by `*_FROM_FILE_*` mappings (e.g. `SupportedPrograms_mapping.csv`)
  must be present too.
* `outputDirectory` is where the exported `<Entity>.csv` files are written.

Run it exactly like the seed mode:

```shell
java -jar build/libs/openlmis-refdata-seed-1.0.0-SNAPSHOT.jar
```

Each entity that has a mapping file and whose transformation types are all supported is fetched from
the API and written out; entities with an unsupported type are skipped with a log message listing
the
offending types.

Rows that differ are real drift. For a clean diff, generate the baseline **with this tool** rather
than hand-authoring it, so both sides share the exact column order and formatting the exporter
produces.

### Notes and known limitations

* **Column order** follows each mapping's `from` column order, and every mapped `from` is emitted
  even when its value is blank for all rows (so the header is stable for diffing).
* **Formatting**: booleans are written as `True`/`False`, dates as ISO `yyyy-MM-dd`, and text is
  written as UTF-8 (with a leading BOM, matching the seed CSVs).
* **`*_FROM_FILE_*` child files** are regenerated with deterministic synthetic join codes
  (`GEN_<hash>`). These keep the child file and its parent references internally consistent so the
  output re-imports correctly, but they will not reproduce the original hand-authored codes.
* **Supported programs** are exported per facility: each row carries the parent `facilityCode`. Only
  the columns declared in `SupportedPrograms_mapping.csv` are written — add
  `startDate,supportStartDate,DIRECT_DATE` and `locallyFulfilled,supportLocallyFulfilled,DIRECT`
  lines to that mapping to include those columns.
* **Blank by design**: a few fields cannot be recovered from the API and are left blank — e.g.
  `ValidSources`/`ValidDestinations` `geoLevelAffinityId`, `AuthUsers` role/email (the batch
  endpoint
  omits them), and `SupervisoryNodes.childNodes` (the hierarchy is expressed via `parentNode`).

## Input files

The input and mapping files are required to have a specific name that corresponds to the entity
name.

```
Programs.csv - Programs_mapping.csv
StockCardLineItemReasons.csv - StockCardLineItemReasons_mapping.csv
ValidReasons.csv - ValidReasons_mapping.csv
ValidSources.csv - ValidSources_mapping.csv
ValidDestinations.csv - ValidDestinations_mapping.csv
OrderableDisplayCategories.csv - OrderableDisplayCategories_mapping.csv
FacilityTypes.csv - FacilityTypes_mapping.csv
CommodityTypes.csv - CommodityTypes_mapping.csv
FacilityTypeApprovedProducts.csv - FacilityTypeApprovedProducts_mapping.csv
ProcessingSchedules.csv - ProcessingSchedules_mapping.csv
ProcessingPeriods.csv - ProcessingPeriods_mapping.csv
FacilityOperators.csv - FacilityOperators_mapping.csv
GeographicLevels.csv - GeographicLevels_mapping.csv
GeographicZones.csv - GeographicZones_mapping.csv
Facilities.csv - Facilities_mapping.csv
SupervisoryNodes.csv - SupervisoryNodes_mapping.csv
RequisitionGroups.csv - RequisitionGroups_mapping.csv
SupplyLines.csv - SupplyLines_mapping.csv
Roles.csv - Roles.csv_mapping.csv
Users.csv - Users.csv_mapping.csv
AuthUsers.csv - AuthUsers_mapping.csv
UserContactDetails.csv - UserContactDetails_mapping.csv
InventoryItems.csv - InventoryItems_mapping.csv
CatalogItems.csv - CatalogItems_mapping.csv
```

Each reference data piece has got its own input file. The input files are CSV files that contain a
header row with column names and then a list of entries. The mapping file exists for each input file
and defines transformation rules.

When processing user-related files, remember that the each `auth_user` needs related
`user_contact_details` entity
to be able to authorize. Previously, file `UserContactDetails.csv` was not used, and users emails
were specified in `Users.csv` file.
If the files to process will be created in this form, you should create `UserContactDetails.csv`
based on `Users.csv` as in the examples in section below.

**Sample input and mapping file**
___

UserContactDetails.csv

| username | phoneNumber | email            | allowNotify |
|----------|-------------|------------------|-------------|
| user12   |             | user12@gmail.com | TRUE        |

UserContactDetails_mapping.csv

| from        | to                  | type                         | entityName       | defaultValue |
|-------------|---------------------|------------------------------|------------------|--------------|
| username    | referenceDataUserId | TO_ID_BY_USERNAME            | User             |              |
| phoneNumber | phoneNumber         | DIRECT                       |                  |              |
| allowNotify | allowNotify         | DIRECT                       | GeographicLevel  |              |
| email       | emailDetails        | TO_OBJECT_FROM_FILE_BY_EMAIL | EmailDetails.csv |              |

EmailDetails.csv

| email            | verified |
|------------------|----------|
| user12@gmail.com | TRUE     |

EmailDetails_mapping.csv

| from     | to            | type   | entityName | defaultValue |
|----------|---------------|--------|------------|--------------|
| email    | email         | DIRECT |            |              |
| verified | emailVerified | DIRECT |            |              |

GeographicZones.csv

| code  | name         | level   | parent | catchementpopulation | latitude | longitude |
|-------|--------------|---------|--------|----------------------|----------|-----------|
| malaw | Malawi       | Country |        |                      |          |           |	
| cwes  | Central West | Zone    | malaw  |                      |          |           |
| cest  | Central East | Zone    | malaw  |                      |          |           |

GeographicZones_mapping.csv

| from   | to                  | type              | entityName      | defaultValue |
|--------|---------------------|-------------------|-----------------|--------------|
| code   | code                | DIRECT	           |                 |              |
| name   | name                | DIRECT            |                 |              |
| level  | geographicLevel     | TO_OBJECT_BY_CODE | GeographicLevel |              |
| parent | parent              | TO_OBJECT_BY_CODE | GeographicZone  |              |
|        | catchmentPopulation | USE_DEFAULT       |                 | 0            |

InventoryItems_mapping.csv

| from                       | to                         | type                        | entityName  | defaultValue | 
|----------------------------|----------------------------|-----------------------------|-------------|--------------| 
| id                         | id                         | DIRECT                      |             |              | 
| facilityCode               | facility                   | TO_OBJECT_BY_CODE           | Facility    |              | 
| catalogItem                | catalogItem                | FIND_CATALOG_ITEM_TO_OBJECT | CatalogItem |              | 
| programCode                | program                    | TO_OBJECT_BY_CODE           | Program     |              | 
| serialNumber               | equipmentTrackingId        | DIRECT                      |             |              | 
| referenceName              | referenceName              | DIRECT                      |             |              | 
| yearOfInstallation         | yearOfInstallation         | DIRECT                      |             |              | 
| yearOfWarrantyExpiry       | yearOfWarrantyExpiry       | DIRECT                      |             |              | 
| source                     | source                     | DIRECT                      |             |              | 
| functionalStatus           | functionalStatus           | DIRECT                      |             |              | 
| reasonNotWorkingOrNotInUse | reasonNotWorkingOrNotInUse | DIRECT                      |             |              | 
| utilization                | utilization                | DIRECT                      |             |              | 
| voltageStabilizer          | voltageStabilizer          | DIRECT                      |             |              | 
| backupGenerator            | backupGenerator            | DIRECT                      |             |              | 
| voltageRegulator           | voltageRegulator           | DIRECT                      |             |              | 
| manualTemperatureGauge     | manualTemperatureGauge     | DIRECT                      |             |              | 
| remoteTemperatureMonitor   | remoteTemperatureMonitor   | DIRECT                      |             |              | 
| additionalNotes            | additionalNotes            | DIRECT                      |             |              | 
| decommissionDate           | decommissionDate           | DIRECT                      |             |              | 

FindCatalogItemToObjectTypeConverter and FindCatalogItemTypeConverter handle
**FIND_CATALOG_ITEM_TO_OBJECT** and **FIND_CATALOG_ITEM**.

**catalogItem** is a concatenation of catalog item's equipment code and model using `||`
separator e.g. `E3/88-M||RCW 50 EG`.

Available transformation types are:

- **DIRECT** a value from the input column will be directly moved to the output column, without
  alterations

- **DIRECT_DATE** a date value from the input column will be parsed to ISO date format (yyyy-MM-dd)
  before moved to the output column

- **USE_DEFAULT** a value for the output will be taken directly from the defaultValue column of the
  mapping file

- **TO_OBJECT** a value from the input column will be transformed into an object; the desired input
  format is: key1:value1,key2:value2

- **TO_OBJECT_BY_<>** a value from the input column must be a single and unique value that will be
  used to search OLMIS for an existing instance based on one of the fields; the name of the mapping
  must reflect the field that is being used for searching - eg. TO_OBJECT_BY_NAME will look for
  instance based on the "name" property; the searched entity name must be specified in the mapping
  file; once a record with matching value for "name" property is found, that record is retrieved as
  a whole and inserted into output JSON

- **TO_ARRAY_BY_<>** a value from the input column must be an array of unique values that will be
  used to search OLMIS for existing instances based on one of the fields; all the rules from the
  above "to_object_by" mapping apply, the only difference being that we provide an array of values
  and receive an array of objects in the outcome JSON. The array must be provided in
  format: [value1,value2,value3]

- **TO_ID_BY_<>** a value from the input column must be a single and unique value that will be used
  to search OLMIS for an existing instance based on one of the fields; the name of the mapping must
  reflect the field that is being used for searching - eg. TO_ID_BY_NAME will look for instance
  based on the "name" property; the searched entity name must be specified in the mapping file; once
  a record with matching value for "name" property is found, only the ID of the record is retrieved
  and inserted into output JSON

- **TO_ARRAY_FROM_FILE_BY_<>** a value from the input column must be an array of unique values that
  will be used to search another input file for an entry to include in the outcome JSON; the name of
  the mapping must reflect the field that is being used for searching - eg.
  TO_ARRAY_FROM_FILE_BY_NAME will search input file entries by the "name" column; the name of the
  input file must be provided in the entityName column in the mapping file; the outcome JSON
  contains an array of objects, created based on the specified input file and using the mapping file
  of that specified input file

- **SKIP** no action will be done for the given input field; there will also be no entry for that
  value in the outcome JSON
