# kqr-pay
Sample QR app for Knative

# Building applications

## Build Kiosk QR encoder

We are going to use `s2i` utility from OpenShift that will build the application and produce a container without the need to create a `Dockerfile`.

`s2i build qr-encode centos/python-36-centos7 markito/qr-encode:v1`

## Build Kiosk QR reader 

On this step we are going to build the application using `docker build`, assuming the user would then need to create a specific `Dockerfile` describing some dependencies for the application.

`docker build qr-decode/ -t markito/qr-decode:v99`

## Build container for Quarkus App 

For the Quarkus version of the application, in order to compile the application to a native format, Quarkus offers some specific built-in plugin integrated with `Maven` that allows for a streamlined experience to produce containers.  From the `quarkus-kiosk` directory you can execute:

1. Build the application
`./mvnw package  -Dnative-image.docker-build=true`

2. Build the container using JVM
`docker build -f src/main/docker/Dockerfile.jvm -t markito/quarkus-decoder:v1 .`