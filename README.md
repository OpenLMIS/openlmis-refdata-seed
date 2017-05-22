# OpenLMIS Reference Data seed tool

This repository contains the code of the OpenLMIS Reference Data seeding tool. The tool converts
CSV input and mapping files into the JSON files that can be consumed by the OpenLMIS web API and inserts them into the running OpenLMIS instance.

## Prerequisites
* Java 8
* Maven 3

## Quick start
1. Fork/clone this repository from GitHub.

```shell
git clone https://github.com/OpenLMIS/mw-refdata-seed.git
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
