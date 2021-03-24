#!/bin/bash
# Building Spring application
mvn package -DskipTests
# Build docker and then tag it for both OPL and DOP
echo "Building for ci-docker.corpnet.pl/mixewaytesting/scanner"
docker build . -t ci-docker.corpnet.pl/mixewaytesting/scanner:latest
echo "Building for ci-docker.corpnet.pl/library/mixewaytesting/scanner"
docker build . -t ci-docker.corpnet.pl/library/mixewaytesting/scanner:latest
echo "Building for registry.d00b.pl/mixeway/mixewaytesting"
docker build . -t registry.d00b.pl/mixeway/mixewaytesting:latest

# Pushing image
echo "Pushing ci-docker.corpnet.pl/mixewaytesting/scanner"
docker push ci-docker.corpnet.pl/mixewaytesting/scanner:latest
echo "Pushing ci-docker.corpnet.pl/library/mixewaytesting/scanner"
docker push ci-docker.corpnet.pl/library/mixewaytesting/scanner:latest
echo "Pushing registry.d00b.pl/mixeway/mixewaytesting"
docker push registry.d00b.pl/mixeway/mixewaytesting:latest