# My-App - Spring Boot Microservices on Kubernetes

## Overview
This is a Spring Boot microservices application designed to run on Kubernetes. It demonstrates modern microservices architecture patterns including service discovery, API routing, messaging, and distributed tracing.

## Architecture
- Services communicate via REST and RabbitMQ
- All services use `SPRING_PROFILES_ACTIVE=kube` for Kubernetes configuration
- Distributed tracing exported to Zipkin

## Running on Kubernetes (Minikube)

### Startup Order
Start services in the following order:
1. Postgres
2. RabbitMQ
3. Notification
4. Fraud
5. Customer
6. Zipkin

### Database Administration
Note: PostgreSQL is deployed as a Kubernetes service in the `infra` namespace. PgMyAdmin is not deployed as a separate container in the cluster; database administration can be performed using `kubectl port-forward` or external tools as needed.

### Prerequisites
- Install Minikube
- Install kubectl

### Start Minikube and Application
```bash
minikube start
```

### Build and Push Images
Build and push images to DockerHub **before** applying Kubernetes manifests. Pods will fail to start if images are not available in the registry.

**Step 1 — Build all images at once:**
```bash
mvn clean package -P build-docker-image
```

**Step 2 — Push each image to DockerHub:**
```bash
docker push your_username_on_DockerHub/customer:1.0-SNAPSHOT
docker push your_username_on_DockerHub/fraud:1.0-SNAPSHOT
docker push your_username_on_DockerHub/notification:1.0-SNAPSHOT
```

### Apply Manifests
```bash
kubectl apply -f k8s/minikube/bootstrap/postgres/
kubectl apply -f k8s/minikube/bootstrap/rabbitmq/
kubectl apply -f k8s/minikube/bootstrap/zipkin/
kubectl apply -f k8s/minikube/services/notification/
kubectl apply -f k8s/minikube/services/fraud/
kubectl apply -f k8s/minikube/services/customer/
```

### Verify
```bash
kubectl get pods
kubectl get services
minikube service zipkin
```

## Port Forwarding
### RabbitMQ
```bash
kubectl port-forward service/rabbitmq 15672:15672
```
Access RabbitMQ management UI at: http://localhost:15672 (guest/guest)

### Zipkin
```bash
kubectl port-forward service/zipkin 9411:9411
```
Access Zipkin UI at: http://localhost:9411

### Customer Service
```bash
kubectl port-forward service/customer 8080:8080
```
Access Customer Service at: http://localhost:8080

## Testing the Application

### Running Tests
```bash
mvn test
```

### API Testing
```bash
curl http://localhost:8080/api/v1/customers/1
curl -X POST -H "Content-Type: application/json" -d '{"name":"John Doe","email":"john@example.com"}' http://localhost:8080/api/v1/customers
```

### Testing with RabbitMQ
1. Send a POST request to create a customer
2. Check logs to see fraud service processes the event
3. Verify message was sent through RabbitMQ

### Testing Distributed Tracing
1. Access Zipkin UI at http://localhost:9411 (after enable port forwarding)
2. Make API calls to customer service
3. Verify traces show service-to-service communication

### Load Testing
```bash
ab -n 100 -c 10 http://localhost:8080/api/v1/customers/1
```

## Stopping Services
```bash
kubectl delete deployment --all
minikube stop
minikube delete
```

## Configuration Notes
- Use FQDN for cross-namespace database access
- Set `SPRING_JPA_HIBERNATE_DDL_AUTO=none` via environment variable
- RabbitMQ service requires named ports in Service YAML

## Common Issues & Fixes
- **ImagePullBackOff**: rebuild and load images using `eval $(minikube docker-env)`
- **CrashLoopBackOff**: check logs with `kubectl logs <pod>`
- **Database not found**: ensure DB is created and use FQDN
- **Readiness probe fails**: increase `initialDelaySeconds`

## Git Workflow
- Use commit prefixes: `feature(microservices):`, `documentation(microservices):`, `refactor(microservices):`
- Push changes: `git push origin <branch>`