#!/bin/bash
set -ex
set -o pipefail

# DockerHub
USERNAME=...
PASSWORD=...
docker login -u $USERNAME -p $PASSWORD

# echo "Building image front-proxy:v21 ..."
# cd ../envoy/production
# docker build -t envoy -f Dockerfile .
# docker tag envoy kameri/envoy:v21
# docker push kameri/envoy:v21
