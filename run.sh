#!/bin/sh

gradle clean build

CURRENT_DIR=$('pwd')
mkdir -p build/execute

if [ -d tmp ]
then
    cd tmp

    if [ -f data.zip ]
    then
        unzip -o data.zip
        find . -name "*.csv" -exec cp -vf {} ${CURRENT_DIR}/build/execute \;
    else
        echo "Missing reference data zip file"
    fi

    cd ..
else
    echo "Missing tmp directory with reference data zip file"
fi

cd build/execute

rm -vrf config.properties
touch config.properties

echo "host=${HOST}" >> config.properties
echo "login=${LOGIN}" >> config.properties
echo "password=${PASSWORD}" >> config.properties
echo "clientId=${CLIENT_ID}" >> config.properties
echo "clientSecret=${CLIENT_SECRET}" >> config.properties
echo "directory=${CURRENT_DIR}/build/execute" >> config.properties
echo "updateAllowed=${UPDATE_ALLOWED}" >> config.properties

cp ../libs/openlmis-refdata-seed-1.0.0-SNAPSHOT.jar tool.jar

java -jar tool.jar
