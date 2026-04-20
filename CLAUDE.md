# Project Knowledge Base

## Application Overview
This is a Spring Boot microservices application with multiple services:
- **Eureka Server** (service registry)
- **API Gateway** (routes to customer service)
- **Customer Service** (port 8080)
- **Fraud Service** (port 8081)
- **Notification Service** (port 8082)
- **PostgreSQL** (database)
- **RabbitMQ** (message broker)
- **Zipkin** (distributed tracing)

## Architecture
- Services communicate via REST and RabbitMQ
- Eureka provides service discovery
- API Gateway routes `/api/v1/customers/**` to customer service
- All services use `SPRING_PROFILES_ACTIVE=kube` for Kubernetes configuration
- Tracing exported to Zipkin at `http://zipkin:9411/api/v2/spans`

## Running Locally (Docker Compose)
```bash
docker-compose up
```
Services will be available:
- API Gateway: http://localhost:8083
- Customer: http://localhost:8080
- Fraud: http://localhost:8081
- Notification: http://localhost:8082
- Eureka: http://localhost:8761
- Zipkin: http://localhost:9411
- RabbitMQ: http://localhost:15672 (guest/guest)
- PostgreSQL: localhost:5433

## Running on Kubernetes (Minikube)

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

**Using Docker:**
```bash
docker build -t localhost:5000/customer:1.0-SNAPSHOT customer/
```

**For Minikube's Docker context:**
```bash
eval $(minikube docker-env)
mvn clean package jib:build -Dimage=image:latest
```

### Apply Kubernetes Manifests
```bash
# Bootstrap resources (StatefulSets, Services, ConfigMaps)
kubectl apply -f k8s/minikube/bootstrap/

# Service deployments
kubectl apply -f k8s/minikube/services/
```

### Verify
```bash
kubectl get pods
kubectl get services
minikube service apigw  # Access API gateway
```

## Configuration Files
- **Docker Compose:** `docker-compose.yml`
- **Kubernetes Manifests:** `k8s/minikube/`
- **Service Configs:** `*/src/main/resources/application-kube.yml` and `application-docker.yml`

## Development Notes
- Every change must be committed and pushed to the repository
- Use descriptive commit messages following: `type(scope): description`
- After committing, push to remote: `git push origin branch-name`

## Git Workflow
1. Make changes to files
2. Stage: `git add <file>`
3. Commit: `git commit -m "type(scope): description"`
4. Push: `git push origin <branch>`

## Commit Convention
- `feat(scope):` - new feature
- `fix(scope):` - bug fix
- `docs(scope):` - documentation changes
- `refactor(scope):` - code refactoring
- `test(scope):` - test additions/changes
- `chore(scope):` - maintenance tasks

## Common Issues
- **Image pull failures:** Rebuild images with `eval $(minikube docker-env)` for Minikube's Docker context
- **Service not reachable:** Check `kubectl get pods` and `kubectl logs <pod-name>`
- **Port conflicts:** Ensure no other services use ports 8080-8083, 8761, 9411, 5433, 15672, 5672