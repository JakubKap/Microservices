Application, which includes:

* Microservices - Fraud is communicating with Customer through exposed endpoints.
* It will not be asynchronous.

Eureka server is responsible for load balancing requests to multiple instances of microservices.
Moreover, thankfully to Eureka it is possible to communicate two microservices - Fraud and Customer.

API Gateway is used as external Load Balancer, which protects private network where application
is located. Moreover, it redirects all request coming APIGW Gateway to customer service.

ZipKin is used also within a project to trace requests across multiple services (and debug latency and failures).

Moreover, traffic is controlled by LoadBalancer.