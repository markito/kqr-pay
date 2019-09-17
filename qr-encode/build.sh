#!/bin/sh

s2i build . centos/python-36-centos7 markito/kiosk:v1 && docker push markito/kiosk:v1
