Application, which includes:

* Microservices - Fraud is communicating with Customer through exposed endpoints.
* It will not be asynchronous.

Eureka server is responsible for load balancing requests to multiple instances of microservices.
Moreover, thankfully to Eureka it is possible to communicate two microservices - Fraud and Customer.

API Gateway is used as external Load Balancer, which protects private network where application
is located. Moreover, it redirects all request coming APIGW Gateway to customer service.

ZipKin is used also within a project to trace requests across multiple services (and debug latency and failures).

In order to avoid latency in response from Notification microservice, message broker (RabbitMQ) is applied
(along with Spring AMQP).

Moreover, traffic is controlled by LoadBalancer.

Finally, application is containerized with Google Jib. Images are stored on a cloud - on jakubkap DockerHub account.

# Write how containers and images can be created. ?
After utilizing K8s there was no need for using Eureka Server - Kubernetes has built-in Service Discovery.
Similarly, with API Gateway.