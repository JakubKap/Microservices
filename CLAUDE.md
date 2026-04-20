# Project Knowledge Base

## Application Overview
This is a Spring Boot microservices application with multiple services:
- **Eureka Server** (service registry) - not needed in K8s
- **API Gateway** (apigw) - not needed in K8s, Kubernetes handles routing
- **Customer Service** (port 8080)
- **Fraud Service** (port 8081)
- **Notification Service** (port 8082)
- **PostgreSQL** (database)
- **RabbitMQ** (message broker)
- **Zipkin** (distributed tracing)

## Architecture
- Services communicate via REST and RabbitMQ
- Eureka provides service discovery (not required in K8s)
- API Gateway routes `/api/v1/customers/**` to customer service (not required in K8s)
- All services use `SPRING_PROFILES_ACTIVE=kube` for Kubernetes configuration
- Tracing exported to Zipkin at `http://zipkin:9411/api/v2/spans`

## Running on Kubernetes (Minikube)

### Startup Order
Start services in the following order:
1. Postgres (along with PgMyAdmin)
2. RabbitMQ
3. Notification
4. Fraud
5. Customer
6. Zipkin

Kubernetes handles service discovery and networking automatically; Eureka and API Gateway are not needed in the cluster.

### Prerequisites
- Install Minikube: `curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64 && sudo install minikube-linux-amd64 /usr/local/bin/minikube`
- Install kubectl: `curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.kdl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && sudo install kubectl /usr/local/bin/`

### Start Minikube
```bash
minikube start --driver=docker
```

### Build and Push Images
The deployment manifests reference images like `jakubkap/customer:1.0-SNAPSHOT`. Build and push them:

**Using Jib (recommended):**
```bash
mvn compile jib:build -Dimage=localhost:5000/yourusername/image:tag
```

**Using Docker and loading into Minikube context:**
```bash
eval $(minikube docker-env)
mvn clean package
# Build each service's Docker image
docker build -t localhost:5000/customer:1.0-SNAPSHOT customer/
# ... repeat for other services
```

### Apply Manifests
```bash
# Bootstrap infra (StatefulSets, Services, ConfigMaps) - see infra directory
kubectl apply -f k8s/minikube/bootstrap/postgres/
kubectl apply -f k8s/minikube/bootstrap/rabbitmq/
kubectl apply -f k8s/minikube/bootstrap/zipkin/

# Deploy services in recommended order
kubectl apply -f k8s/minikube/services/postgres/  # if needed separately
kubectl apply -f k8s/minikube/services/rabbitmq/
kubectl apply -f k8s/minikube/services/notification/
kubectl apply -f k8s/minikube/services/fraud/
kubectl apply -f k8s/minikube/services/customer/
kubectl apply -f k8s/minikube/services/zipkin/
```

### Verify
```bash
kubectl get pods
kubectl get services
minikube service zipkin  # Access Zipkin UI
```

## Configuration Notes
- Service names for DB/broker: use fully qualified DNS when cross-namespace:
  - `postgres.infra.svc.cluster.local`
  - `rabbitmq.infra.svc.cluster.local`
  - `zipkin.infra.svc.cluster.local`
- If all services share the same namespace, short names (`postgres`, `rabbitmq`) work.
- For `ddl-auto`, you can set via env: `SPRING_JPA_HIBERNATE_DDL_AUTO=none` to prevent schema creation issues.
- RabbitMQ service requires named ports in the Service YAML.

## Key Fixes Applied
- Deployed PostgreSQL, RabbitMQ, and Zipkin as Kubernetes Deployments + Services in `infra` namespace.
- Used initContainers to wait for PostgreSQL before starting apps.
- Set `SPRING_JPA_HIBERNATE_DDL_AUTO=none` via env to prevent Hibernate from trying to create tables.
- Used FQDN for cross-namespace DB access (`postgres.infra.svc.cluster.local`).
- Added readiness probes with sufficient `initialDelaySeconds`.

## Deployment Files
- Infra: `k8s/minikube/bootstrap/`
- Services: `k8s/minikube/services/`
- App configs (Kubernetes profiles): `*/src/main/resources/application-kube.yml`

## Common Issues & Fixes
- **ImagePullBackOff**: rebuild and load images into Minikube's Docker context using `eval $(minikube docker-env)`.
- **CrashLoopBackOff**: check `kubectl logs <pod>` and `kubectl describe pod <pod>`; ensure dependencies are reachable.
- **Database not found**: ensure DB is created and credentials match; use FQDN for cross-namespace access.
- **Readiness probe fails**: increase `initialDelaySeconds` or ensure app starts listening on the expected port.

## Git Workflow
- Use commit prefixes: `feature(microservices):`, `documentation(microservices):`, `refactor(microservices):`.
- Push changes after commit: `git push origin <branch>`.
