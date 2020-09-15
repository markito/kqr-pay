#!/bin/bash

echo '######### Deploy QR Code Generator #########'
kn service create qrgen --image markito/qr-encode:v1  --autoscale-window 20s --requests-memory=100Mi --concurrency-target=1

echo '######### Deploy Front End #########'
kn service create frontend --image markito/frontend:v1 --min-scale=1 --concurrency-target=5 --env PAYMENT_SERVICE="http://payment-service-modern-serverless-quarkus.apps.devint.openshiftknativedemo.org"

echo '######### Deploy Payment Service #########'
kn service create payment-service --image markito/payment-service:v1 --requests-memory=100Mi --concurrency-target=1 --min-scale=1

kn service list 