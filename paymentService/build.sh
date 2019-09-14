#!/bin/sh

s2i build . centos/python-36-centos7 markito/payment-service:v1 && docker push markito/payment-service:v1
