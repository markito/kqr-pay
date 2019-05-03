#!/bin/sh

wrk -t10 -c50 -d30s  http://kiosk-encoder.markito.apps.openshift.codeready.cloud/encode
