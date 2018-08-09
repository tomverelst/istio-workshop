
# Service Meshes with Istio

This repository contains all related materials for the Services Meshes with Istio workshop.

Ordina JWorks

Cloud Platforms Competence Center

## Installing Istio

### Prerequisites

- Kubernetes platform (kubectl, minikube)
- Downloading Istio and setting up istioctl 
- Requirements for Pods and Services


### Install kubectl

https://kubernetes.io/docs/tasks/tools/install-kubectl/

If you are on macOS with Homebrew:

```
$ brew install kubernetes-cli
```


### Setting up minikube for Istio

https://github.com/kubernetes/minikube

https://github.com/kubernetes/minikube/releases

If you are on macOS with Homebrew:

```
$ brew cask install minikube
```

Make sure the Ingress addon is enabled

```
$ minikube addons enable ingress
```

**Kubernetes v1.9**

```
$ minikube start --memory=8192 --cpus=4 --kubernetes-version=v1.9.4 \
    --extra-config=controller-manager.cluster-signing-cert-file="/var/lib/localkube/certs/ca.crt" \
    --extra-config=controller-manager.cluster-signing-key-file="/var/lib/localkube/certs/ca.key" \
    --extra-config=apiserver.admission-control="NamespaceLifecycle,LimitRanger,ServiceAccount,PersistentVolumeLabel,DefaultStorageClass,DefaultTolerationSeconds,MutatingAdmissionWebhook,ValidatingAdmissionWebhook,ResourceQuota" \
    --vm-driver=`your_vm_driver_choice`
```

**Kubernetes v1.10**

```
$ minikube start --memory=8192 --cpus=4 --kubernetes-version=v1.10.0 \
    --extra-config=controller-manager.cluster-signing-cert-file="/var/lib/localkube/certs/ca.crt" \
    --extra-config=controller-manager.cluster-signing-key-file="/var/lib/localkube/certs/ca.key" \
    --vm-driver=`your_vm_driver_choice`
```


### Downloading Istio and setting up istioctl

Go to the [Istio release page](https://github.com/istio/istio/releases) to download the installation file corresponding to your OS. 
On a macOS or Linux system, 
you can run the following command to download and extract the latest release automatically:

```
$ curl -L https://git.io/getLatestIstio | sh -
```

Change directory to the downloaded distribution:

```
$ cd istio-1.0.0
```

The installation directory contains:

- Installation .yaml files for Kubernetes in install/
- Sample applications in samples/
- The istioctl client binary in the bin/ directory. istioctl is used when manually injecting Envoy as a sidecar proxy and for creating routing rules and policies.
- The istio.VERSION configuration file

### Requirements for Pods and Services
To be a part of the service mesh, pods and services in a Kubernetes cluster must satisfy the following requirements:

- **Named ports**: Service ports must be named. The port names must be of the form <protocol>[-<suffix>] with http, http2, grpc, mongo, or redis as the <protocol> in order to take advantage of Istio’s routing features. For example, name: http2-foo or name: http are valid port names, but name: http2foo is not. If the port name does not begin with a recognized prefix or if the port is unnamed, traffic on the port will be treated as plain TCP traffic (unless the port explicitly uses Protocol: UDP to signify a UDP port).

- **Service association**: If a pod belongs to multiple Kubernetes services, the services cannot use the same port number for different protocols, for instance HTTP and TCP.

- **Deployments with app label**: It is recommended that pods deployed using the Kubernetes Deployment have an explicit app label in the deployment specification. Each deployment specification should have a distinct app label with a value indicating something meaningful. The app label is used to add contextual information in distributed tracing.

## Installing


### Install Custom Resource Definitions 

Install Istio’s Custom Resource Definitions via kubectl apply, and wait a few seconds for the CRDs to be committed in the kube-apiserver:

```
$ kubectl apply -f istio/install/kubernetes/helm/istio/templates/crds.yaml
```

### Install Istio

To Install Istio and enforce mutual TLS authentication between sidecars by default:

```
$ kubectl apply -f istio/install/kubernetes/istio-demo-auth.yaml
```

Label the default namespace to enable injection

``` 
$ kubectl label namespace default istio-injection=enabled
```

### Verify your installation

Make sure all required services are up and ready: 

```
$ kubectl get svc -n istio-system
NAME                       TYPE           CLUSTER-IP      EXTERNAL-IP       PORT(S)                                                               AGE
istio-citadel              ClusterIP      10.47.247.12    <none>            8060/TCP,9093/TCP                                                     7m
istio-egressgateway        ClusterIP      10.47.243.117   <none>            80/TCP,443/TCP                                                        7m
istio-galley               ClusterIP      10.47.254.90    <none>            443/TCP                                                               7m
istio-ingress              LoadBalancer   10.47.244.111   35.194.55.10      80:32000/TCP,443:30814/TCP                                            7m
istio-ingressgateway       LoadBalancer   10.47.241.20    130.211.167.230   80:31380/TCP,443:31390/TCP,31400:31400/TCP                            7m
istio-pilot                ClusterIP      10.47.250.56    <none>            15003/TCP,15005/TCP,15007/TCP,15010/TCP,15011/TCP,8080/TCP,9093/TCP   7m
istio-policy               ClusterIP      10.47.245.228   <none>            9091/TCP,15004/TCP,9093/TCP                                           7m
istio-sidecar-injector     ClusterIP      10.47.245.22    <none>            443/TCP                                                               7m
istio-statsd-prom-bridge   ClusterIP      10.47.252.184   <none>            9102/TCP,9125/UDP                                                     7m
istio-telemetry            ClusterIP      10.47.250.107   <none>            9091/TCP,15004/TCP,9093/TCP,42422/TCP                                 7m
jaeger-agent               ClusterIP      None             <none>           5775/UDP,6831/UDP,6832/UDP                                            1m
jaeger-collector           ClusterIP      10.105.10.124    <none>           14267/TCP,14268/TCP                                                   1m
jaeger-query               ClusterIP      10.97.220.114    <none>           16686/TCP                                                             1m
prometheus                 ClusterIP      10.97.100.91     <none>           9090/TCP                                                              1m
servicegraph               ClusterIP      10.97.191.50     <none>           8088/TCP                                                              1m
tracing                    ClusterIP      10.111.61.100    <none>           80/TCP                                                                1m
zipkin                     ClusterIP      10.99.130.200    <none>           9411/TCP                                                              1m```
```

Make sure all required pods are up and ready: 

```
$ kubectl get pods -n istio-system
NAME                                       READY     STATUS        RESTARTS   AGE
istio-citadel-75c88f897f-zfw8b             1/1       Running       0          1m
istio-egressgateway-7d8479c7-khjvk         1/1       Running       0          1m
istio-galley-6c749ff56d-k97n2              1/1       Running       0          1m
istio-ingress-7f5898d74d-t8wrr             1/1       Running       0          1m
istio-ingressgateway-7754ff47dc-qkrch      1/1       Running       0          1m
istio-policy-74df458f5b-jrz9q              2/2       Running       0          1m
istio-sidecar-injector-645c89bc64-v5n4l    1/1       Running       0          1m
istio-statsd-prom-bridge-949999c4c-xjz25   1/1       Running       0          1m
istio-telemetry-676f9b55b-k9nkl            2/2       Running       0          1m
prometheus-86cb6dd77c-hwvqd                1/1       Running       0          1m
servicegraph-7875b75b4f-2tlsd              1/1       Running       0          1m
```

## Accessing applications

If your cluster is running in an environment that does not support an external load balancer (e.g., minikube), 
the EXTERNAL-IP of istio-ingress and istio-ingressgateway will say <pending>.
You will need to access it using the service NodePort, or use port-forwarding instead.

## Addons
### Grafana
Establish port forward from local port 3000 to the Grafana instance:
```
$ kubectl -n istio-system port-forward $(kubectl -n istio-system get pod -l app=grafana \
  -o jsonpath='{.items[0].metadata.name}') 3000:3000
```

Browse to http://localhost:3000 and navigate to Istio Dashboard

### Tracing
Establish port forward from local port 
```
$ kubectl port-forward -n istio-system \
  $(kubectl get pod -n istio-system -l app=istio-tracing -o jsonpath='{.items[0].metadata.name}') \
  9411:9411
```

Browse to http://localhost:9411

### Prometheus
```
$ kubectl -n istio-system port-forward \
  $(kubectl -n istio-system get pod -l app=prometheus -o jsonpath='{.items[0].metadata.name}') \
  9090:9090
```

### Service Graph
```
$ kubectl -n istio-system port-forward \
  $(kubectl -n istio-system get pod -l app=servicegraph -o jsonpath='{.items[0].metadata.name}') \
  8088:8088
```

Browse to http://localhost:8088/dotviz


## Bookinfo sample


``` 
$ kubectl apply -f istio/samples/bookinfo/platform/kube/bookinfo.yaml
$ kubectl apply -f istio/samples/bookinfo/networking/bookinginfo-gateway.yaml
```

### Traffic Management

Before you can use Istio's traffic management features,
like circuit breakers, traffic routing, fault injection, etc..
You must first define the available **subsets** (versions) in destination rules.

```
$ kubectl apply -f istio/samples/bookinfo/networking/destination-rule-all-mtls.yaml  
```

### Configuring Request Routing

To route to one version only, you apply virtual services that set the default version for the microservices. In this case, the virtual services will route all traffic to v1 of each microservice.

``` 
$ kubectl apply -f istio/samples/bookinfo/networking/virtual-service-all-v1.yaml
```

Now let's try to route one specific user to reviews v2:

``` 
$ kubectl apply -f istio/samples/bookinfo/networking/virtual-service-reviews-test-v2.yaml
```

On the /productpage of the Bookinfo app, log in as user jason.
Refresh the browser. What do you see? The star ratings appear next to each review.


### Fault injection

**Injecting delay**

To test the Bookinfo application microservices for resiliency, 
inject a 7s delay between the reviews:v2 and ratings microservices for user jason. 
This test will uncover a bug that was intentionally introduced into the Bookinfo app.

``` 
$ kubectl apply -f istio/samples/bookinfo/networking/virtual-service-ratings-test-delay.yaml
```

You expect the Bookinfo home page to load without errors in approximately 7 seconds. 
However, there is a problem: the Reviews section displays an error message:

Error fetching product reviews!
Sorry, product reviews are currently unavailable for this book.

You’ve found a bug. There are hard-coded timeouts in the microservices that have caused the reviews service to fail.

The timeout between the productpage and the reviews service is 6 seconds - coded as 3s + 1 retry for 6s total. 
The timeout between the reviews and ratings service is hard-coded at 10 seconds. 
Because of the delay we introduced, the /productpage times out prematurely and throws the error.

Change the delay rule to use a 2.8 second delay and then run it against the v3 version of reviews.

**Injecting an HTTP abort fault**

Another way to test microservice resiliency is to introduce an HTTP abort fault. In this task, you will introduce an HTTP abort to the ratings microservices for the test user jason.

In this case, you expect the page to load immediately and display the product ratings not available message.

Create a fault injection rule to send an HTTP abort for user jason:

``` 
$ kubectl apply -f istio/samples/bookinfo/networking/virtual-service-ratings-test-abort.yaml
```

On the /productpage, log in as user jason.

If the rule propagated successfully to all pods, 
the page loads immediately and the product ratings not available message appears.

### Traffic shifting

A common use case is to migrate traffic gradually from one version of a microservice to another. 
In Istio, you accomplish this goal by configuring a sequence of rules that route a percentage of traffic to one service or another. 
In this task, you will send %50 of traffic to reviews:v1 and %50 to reviews:v3. 
Then, you will complete the migration by sending %100 of traffic to reviews:v3.

``` 
$ kubectl apply -f istio/samples/bookinfo/networking/virtual-service-all-v1.yaml
``` 

Transfer 50% of the traffic from reviews:v1 to reviews:v3 with the following command:

```
$ kubectl apply -f istio/samples/bookinfo/networking/virtual-service-reviews-50-v3.yaml
``` 

Refresh the /productpage in your browser and you now see red colored star ratings approximately 50% of the time.
This is because the v3 version of reviews accesses the star ratings service, but the v1 version does not.

> With the current Envoy sidecar implementation, you may need to refresh the /productpage many times 
–perhaps 15 or more–to see the proper distribution. 
You can modify the rules to route 90% of the traffic to v3 to see red stars more often.