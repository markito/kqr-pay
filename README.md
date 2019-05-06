# kqr-pay
Sample serverless QR payment system using Knative.

# Application Architecture

![Architecture Diagram](img/arch.png?raw=true "Architecture Diagram")

QR (quick response) codes are increasing in popularity for a variety of use cases, from loyaty programs to complete **cashless** payment systems, which is what we will simulate on this tutorial as a sample serverless application.

There are 2 services as part of the architecture:

- **A Kiosk QR encoder**: The in-store service that generates a QR code when a customer hit a *Payment* button after scanning all products. It can be scanned to process the payment.
- **A Kiosk QR reader**: A web application that can be used from a mobile device or web broswer. A customer would have her credit card on file and after scanning the QR code the system process the payment using that information. Once payment is processed another backend service would notify the in-store Kiosk of the event. 

The serverless aspects of the application allow for the system to auto-scale based on store demand like during peak hours or scale down during hours of less store traffic.

## Requirements

- An OpenShift 4 cluster with:
    - Knative v0.4+ 
    - Tekton v0.1+
    - Istio and Kiali
- `kn`, `knctl`, `s2i` and `docker` on the development environment.
- Knative OpenShift Ingress 

`oc apply --filename https://github.com/bbrowning/knative-openshift-ingress/releases/download/v0.0.1/release.yaml`

## Setup 
Let's start by creating one of the services using `kn` - Which is the official Knative CLI, still in early stages and under development. At the time I'm writing this there are no released builds for `kn` yet, so you will have to build one manually following [these instructions](https://github.com/knative/client/blob/master/DEVELOPMENT.md#building-knative-client).

Once you have a `kn` binary in your `$PATH` proceed to the next steps. 

We will also use `knctl` in one of the examples below. Download the binary from https://github.com/cppforlife/knctl/releases

# Building the services

## Build Kiosk QR encoder

We are going to use [`s2i`](https://github.com/openshift/source-to-image) tool to build the application and **produce a container without creating a `Dockerfile`**. 

`s2i build qr-encode centos/python-36-centos7 markito/qr-encode:v1`

Push the image to a container registry. For example: `docker push markito/qr-encode:v1` or use the internal container registry in OpenShift. For details on that check this [blog post](https://blog.openshift.com/getting-started-docker-registry/). 

## Build Kiosk QR reader 

Build the application using `docker build`, assuming a user would then need to create a specific `Dockerfile` describing some dependencies for the application.

`docker build qr-decode/ -t markito/qr-decode:v1`

Push the image to a container registry. For example: `docker push markito/qr-decode:v1`

# Deploy the aplications as serverless services in Knative

## Create the kiosk-encoder service:

`kn service create kiosk-decoder --image markito/qr-decode:v1 -n markito`

Hit the endpoint to test using curl or a browser. To obtain the URL for the service use `kn service get` and use the value from the domain column. 

## Create the kiosk-decoder service: 

`kn service create kiosk-encoder --image markito/qr-encode:v1 -n markito`

## Bonus: Add some load to the system

In order to show how auto-scale works, you can add some load to the cluster using a tool like `wrk`.  There is a shell script under the `qr-encode` project that can be used to cause some load for 30s.

`wrk -t10 -c50 -d30s  http://kiosk-encoder.markito.apps.openshift.codeready.cloud/encode`

Use the Openshift console to visualize pods coming up and down or monitor the live traffic using Kiali. 

## Bonus: Create the Quarkus version of the kiosk-decoder

Now we will switch gears a bit and use a different tool to create Knative services (`knctl`) and deploy a Quarkus based application. If you cloned this repository the source code for this app is under the quarkus folder.

### Build a container for the Quarkus application

To build the Quarkus version of the kiosk application, use the plugin integrated with `Maven` that allows for a streamlined experience to produce containers.  From the `quarkus-kiosk` directory you can execute:

1. Build the application
`./mvnw package -Dnative-image.docker-build=true`

2. Build the container image using the JVM profile
`docker build -f src/main/docker/Dockerfile.jvm -t markito/quarkus-decoder:v1 .`

### Create the quarkus-decoder service

`knctl deploy -s quarkus-kiosk -i markito/quarkus-decoder:v1 --managed-route=false`

This will create a service but not yet expose a route to it. Using Knative service rollout strategy we will use some predefined revision labels to direct traffic at a given revision. Since we have a single revision available let's send 100% of the traffic to it.

`knctl rollout --route quarkus-kiosk -p quarkus-kiosk:latest=100%`

Now let's modify something in our application, like the *background color* of the landing page, then build and deploy a new version of the service.

`docker build -f src/main/docker/Dockerfile.jvm -t markito/quarkus-decoder:v2 .`

PS: Note the use of `v2` as part of the tag.

Then create a new revision of the service:

`knctl deploy -s quarkus-kiosk -i markito/quarkus-decoder:v2 --managed-route=false`

Now rollout traffic to the new version of the application, but to peform some A/B testing we'll give it 50% of traffic.

`knctl rollout --route quarkus-kiosk -p quarkus-kiosk:latest=50% -p quarkus-kiosk:previous=50%`

Observe the traffic going to the different versions of the service using Kiali.

# Automating the build and deployment steps with a Tekton pipeline 

TBD.

<!-- 
# Bonus 2: Create a Function using Azure Functions

Initiate project 

```
func init --worker-runtime python --docker
Writing .gitignore
Writing host.json
Writing local.settings.json
Writing /Users/markito/projects/redhat/summit2019/whatdevs/kqr-pay/azf/.vscode/extensions.json
Writing Dockerfile
Writing .dockerignore
```

Create Function

```
func function new --name taxCalc --language python
Select a template:
1. Azure Blob Storage trigger
2. Azure Cosmos DB trigger
3. Azure Event Grid trigger
4. Azure Event Hub trigger
5. HTTP trigger
6. Azure Queue Storage trigger
7. Azure Service Bus Queue trigger
8. Azure Service Bus Topic trigger
9. Timer trigger
Choose option: 5
HTTP trigger``` 
 -->
