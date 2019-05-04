# kqr-pay
Sample serverless QR payment system using Knative.

# Application Architecture

(!image)[!image]

QR (quick response) codes are increasing in popularity for a variety of use cases, from loyaty programs to complete **cashless** payment systems, which is what we will build on this tutorial as a sample app.

There are 2 services as part of the architecture:

- **A Kiosk QR encoder**: The in-store service that after a user scan all products and hit a *Payment* button, generates a QR code that can be scanned to process the payment.
- **A Kiosk QR reader**: It's a web application that can be used from a mobile device or web broswer.A user would have her credit card on file and after scanning the QR code the system would process the payment using that information. Once payment is processed another backend service would notify the in-store Kiosk of the event. 

The serverless aspects of the application allow for the system to auto-scale based on store demand and peak hours and scale down during hours of less store traffic.

## Requirements

- An OpenShift 4 cluster with:
    - Knative v0.4+ 
    - Tekton v0.1+
    - Istio and Kiali 
- kn, knctl, s2i and docker on the development environment.

# Building the services

## Build Kiosk QR encoder

We are going to use [`s2i`](https://github.com/openshift/source-to-image) tool to build the application and produce a container without the need to create a `Dockerfile`. 

`s2i build qr-encode centos/python-36-centos7 markito/qr-encode:v1`

Push the image to a container registry. For example: `docker push markito/qr-encode:v1` or use the internal container registry in OpenShift. For details on that check this [blog post](https://blog.openshift.com/getting-started-docker-registry/). 

## Build Kiosk QR reader 

On this step we are going to build the application using `docker build`, assuming the user would then need to create a specific `Dockerfile` describing some dependencies for the application.

`docker build qr-decode/ -t markito/qr-decode:v1`

Push the image to a container registry. For example: `docker push markito/qr-decode:v1`

## Build container for Quarkus App 

To build the Quarkus version of the kiosk application, use the plugin integrated with `Maven` that allows for a streamlined experience to produce containers.  From the `quarkus-kiosk` directory you can execute:

1. Build the application
`./mvnw package -Dnative-image.docker-build=true`

2. Build the container using JVM
`docker build -f src/main/docker/Dockerfile.jvm -t markito/quarkus-decoder:v1 .`

# Deploy the aplications as serverless services in Knative

## Setup 
Let's start by creating one of the services using `kn` - Which is the official Knative CLI, still in early stages and under development. At the time I'm writing this there are no released builds for `kn` yet, so you will have to build one manually following [these instructions](https://github.com/knative/client/blob/master/DEVELOPMENT.md#building-knative-client). 

Once you have a `kn` binary in your `$PATH` proceed to the next steps. 


# Create the kiosk-encoder service:

`kn service create kiosk-decoder --image markito/qr-decode:v1 -n markito`

Hit the endpoint to test using curl or a browser. To obtain the URL for the service use `kn service get` and use the value from the domain column. 

# Create the kiosk-decoder service: 

`kn service create kiosk-decoder --image markito/qr-decode:v1 -n markito`

# Bonus: Create the Quarkus version of the kiosk-decoder

`kn service create kiosk-decoder --image markito/qr-decode:v1 -n markito`



# Automating the build and deployment steps with a Tekton pipeline 

TBD.