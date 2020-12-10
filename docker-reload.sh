#!/bin/zsh

# Rebuild locally and replace the WAR
echo "######## Rebuild & Replace WAR ########"
echo "./gradlew clean build"
./gradlew clean build
echo "docker cp ./build/libs/cas.war cas:/cas-overlay"
docker cp ./build/libs/cas.war cas:/cas-overlay
echo "################# Done ################"

# Sync configuration files
echo "########## Sync Config Files ##########"
echo "docker exec -d cas sh -c \"rm -rf /etc/cas/config/*\""
docker exec -d cas sh -c "rm -rf /etc/cas/config/*"
echo "docker cp ./etc/cas/config/local/cas-local.properties cas:/etc/cas/config/cas.properties"
docker cp ./etc/cas/config/local/cas-local.properties cas:/etc/cas/config/cas.properties
echo "docker cp ./etc/cas/config/local/instn-authn-local.xsl cas:/etc/cas/config/instn-authn.xsl"
docker cp ./etc/cas/config/local/instn-authn-local.xsl cas:/etc/cas/config/instn-authn.xsl
echo "docker cp ./etc/cas/config/local/log4j2-local.xml cas:/etc/cas/config/log4j2.xml"
docker cp ./etc/cas/config/local/log4j2-local.xml cas:/etc/cas/config/log4j2.xml
echo "################# Done ################"

# Sync JSON registered service files
echo "####### Sync Service Definition #######"
echo "docker exec -d cas sh -c \"rm -rf /etc/cas/services/*\""
docker exec -d cas sh -c "rm -rf /etc/cas/services/*"
echo "docker cp ./etc/cas/services/local/. cas:/etc/cas/services"
docker cp ./etc/cas/services/local/. cas:/etc/cas/services
echo "################# Done ################"

# Restart the container
echo "########## Restart Container ##########"
echo "docker restart cas"
docker restart cas
echo "docker logs -f --tail 0 cas"
echo "####### OSF CAS LOG STARTS HERE #######"
docker logs -f --tail 0 cas
