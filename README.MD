Application, which includes:
* Microservices - Fraud is communicating with Customer through exposed endpoints.
* It will not be asynchronous.

Eureka server is responsible for load balancing requests to multiple instances of microservices.
Moreover, thankfully to Eureka it is possible to communicate two microservices - Fraud and Customer.

Moreover, traffic is controlled by LoadBalancer.