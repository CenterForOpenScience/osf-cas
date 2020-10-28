#!/bin/bash

# Rebuild locally and replace the WAR
./gradlew clean build
docker cp ./build/libs/cas.war cas:/cas-overlay

# Sync configuration files
docker exec -d cas sh -c "rm -rf /etc/cas/config/*"
docker cp ./etc/cas/config/local/cas-local.properties cas:/etc/cas/config/cas.properties
docker cp ./etc/cas/config/local/log4j2-local.xml cas:/etc/cas/config/log4j2.xml

# Sync JSON registered service files
docker exec -d cas sh -c "rm -rf /etc/cas/services/*"
docker cp ./etc/cas/services/local/. cas:/etc/cas/services

# Restart the container
docker restart cas
docker logs -f --tail 0 cas
