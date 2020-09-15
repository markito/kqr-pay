#!/bin/bash

echo '######### Building QR Encoder #########'
s2i build qr-encode centos/python-36-centos7 markito/qr-encode:v1
docker push markito/qr-encode:v1

echo '######### Building Front End #########'
cd frontend
`./mvnw package -Dnative-image.docker-build=true`
docker build -f src/main/docker/Dockerfile.jvm -t markito/frontend:v1 .
docker push markito/frontend:v1 
cd -

echo '######### Building Payment Service #########'
s2i build paymentService centos/python-36-centos7 markito/payment-service:v1
docker push markito/payment-service:v1

docker images | head -4
