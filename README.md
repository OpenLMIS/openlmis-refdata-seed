# OpenLMIS Reference Data seed tool

This repository contains the code of the OpenLMIS Reference Data seeding tool. The tool converts
CSV input and mapping files into the JSON files that can be consumed by the OpenLMIS web API and inserts them into the running OpenLMIS instance.

## Prerequisites
* Java 8
* Maven 3

## Quick start
1. Fork/clone this repository from GitHub.

```shell
git clone https://github.com/OpenLMIS/openlmis-refdata-seed.git
```
2. Build the application by using maven. After the build steps finish, you should see 'BUILD SUCCESS'.

```shell
mvn clean install
```
3. Execute the generated jar file from the target directory.

```shell
java -jar target/refdata.seed.jar
```
Alternatively, you can also run the application in the debug mode.

```shell
mvn spring-boot:run -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

This will build the application and wait for a debugger to attach on port 5005.

4. Configuration file

The configuration file must be placed in the same directory as the tool (jar file) itself. 
```shell
# config.properties
host=http://myopenlmis.instance.org
login=administrator
password=password
clientId=user-client
clientSecret=changeme
directory=/home/user/inputFiles
```

5. Input files

The input and mapping files are required to have a specific name that corresponds to the entity name.
```
Programs.csv - Programs_mapping.csv
StockAdjustmentReasons.csv - StockAdjustmentReasons_mapping.csv
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
```

Each reference data piece has got its own input file. The input files are CSV files that contain a header row with column names and then a list of entries. The mapping file exists for each input file and defines transformation rules.


**Sample input and mapping file**
___
GeographicZones.csv

| code  | name           | level    | parent | catchementpopulation | latitude | longitude |
| ----- | -------------- | -------- | ------ | -------------------- | -------- | --------- |
| malaw | Malawi         | Country  |        |                      |          |           |	
| cwes  | Central West   | Zone     | malaw  |                      |          |           |	
| cest  | Central East   | Zone     | malaw  |                      |          |           |


GeographicZones_mapping.csv

| from        | to                   | type              | entityName         | defaultValue  |
| ----------- | -------------------- | ----------------- | ------------------ | ------------- |
| code        | code                 | DIRECT	           |                    |               |
| name        | name                 | DIRECT            |                    |               |	
| level       | geographicLevel      | TO_OBJECT_BY_CODE | GeographicLevel    |               |
| parent      | parent               | TO_OBJECT_BY_CODE | GeographicZone     |               |
|             | catchmentPopulation  | USE_DEFAULT       |                    | 0             |
	
Available transformation types are:

 - **DIRECT** a value from the input column will be directly moved to the output column, without alterations

 - **USE_DEFAULT** a value for the output will be taken directly from the defaultValue column of the mapping file

 - **TO_OBJECT** a value from the input column will be transformed into an object; the desired input format is: key1:value1,key2:value2

 - **TO_OBJECT_BY_<>** a value from the input column must be a single and unique value that will be used to search OLMIS for an existing instance based on one of the fields; the name of the mapping must reflect the field that is being used for searching - eg. TO_OBJECT_BY_NAME will look for instance based on the "name" property; the searched entity name must be specified in the mapping file; once a record with matching value for "name" property is found, that record is retrieved as a whole and inserted into output JSON

 - **TO_ARRAY_BY_<>** a value from the input column must be an array of unique values that will be used to search OLMIS for existing instances based on one of the fields; all the rules from the above "to_object_by" mapping apply, the only difference being that we provide an array of values and receive an array of objects in the outcome JSON. The array must be provided in format: [value1,value2,value3]

 - **TO_ID_BY_<>** a value from the input column must be a single and unique value that will be used to search OLMIS for an existing instance based on one of the fields; the name of the mapping must reflect the field that is being used for searching - eg. TO_ID_BY_NAME will look for instance based on the "name" property; the searched entity name must be specified in the mapping file; once a record with matching value for "name" property is found, only the ID of the record is retrieved and inserted into output JSON

 - **TO_ARRAY_FROM_FILE_BY_<>** a value from the input column must be an array of unique values that will be used to search another input file for an entry to include in the outcome JSON; the name of the mapping must reflect the field that is being used for searching - eg. TO_ARRAY_FROM_FILE_BY_NAME will search input file entries by the "name" column; the name of the input file must be provided in the entityName column in the mapping file; the outcome JSON contains an array of objects, created based on the specified input file and using the mapping file of that specified input file

 - **SKIP** no action will be done for the given input field; there will also be no entry for that value in the outcome JSON
