# Infinity Tech Inventory Service

A robust, production-ready microservice for managing product inventory in e-commerce applications. Built with Spring Boot, this service handles real-time stock reservations, releases, deductions, and queries to prevent overselling and ensure accurate inventory tracking.

## Features

- **Stock Reservation**: Temporarily hold stock during checkout to prevent overselling
- **Stock Release**: Release reserved stock when orders are cancelled or abandoned
- **Stock Deduction**: Permanently reduce stock after successful payment confirmation
- **Stock Query**: Retrieve current available stock levels for products
- **Event-Driven Architecture**: Integrates with Kafka for asynchronous processing
- **Security**: OAuth2/JWT authentication and authorization
- **Monitoring**: Spring Boot Actuator for health checks and metrics
- **Circuit Breaker**: Resilience4j for fault tolerance
- **Database Migrations**: Flyway for schema versioning
- **API Documentation**: OpenAPI/Swagger UI integration

## Tech Stack

- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Database**: PostgreSQL
- **Messaging**: Apache Kafka (Spring Cloud Stream)
- **Security**: Spring Security with OAuth2 Resource Server
- **Service Discovery**: Netflix Eureka
- **Configuration**: Spring Cloud Config
- **Migration**: Flyway
- **Documentation**: SpringDoc OpenAPI
- **Logging**: Loki Logback Appender
- **Containerization**: Docker
- **Build Tool**: Maven

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL 12+
- Apache Kafka 2.8+
- Docker (for containerized deployment)
- Config Server (Spring Cloud Config) running on port 8888
- Eureka Server for service discovery

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-org/infinity-tech-inventory-service.git
   cd infinity-tech-inventory-service
   ```

2. Install dependencies:
   ```bash
   ./mvnw dependency:resolve
   ```

## Configuration

The service uses Spring Cloud Config for externalized configuration. Default configuration is loaded from `application.yml` and can be overridden via config server.

Key configuration properties:

- `spring.profiles.active`: Active profiles (default: `docker,dev`)
- `spring.application.name`: Service name (`inventory-service`)
- `spring.config.import`: Config server URI (default: `http://localhost:8888/`)

Environment variables:
- `CONFIG_SERVER_URI`: Override config server URL

## Running Locally

1. Ensure prerequisites are running (PostgreSQL, Kafka, Config Server, Eureka)

2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

3. The service will start on port 8080 (default)

## Docker Deployment

Build and run with Docker:

```bash
# Build the image
docker build -t inventory-service:latest .

# Run the container
docker run -p 8080:8080 \
  -e CONFIG_SERVER_URI=http://config-server:8888 \
  inventory-service:latest
```

For production, use docker-compose or Kubernetes with proper environment variables and secrets.

## API Documentation

API documentation is available via Swagger UI at:
- Local: http://localhost:8080/swagger-ui.html
- Production: https://api.infinitytech.com/swagger-ui.html

OpenAPI specification: `src/main/resources/openapi/v1/inventory-openapi.yaml`

### Key Endpoints

- `POST /api/v1/inventory/reserve` - Reserve stock
- `POST /api/v1/inventory/release` - Release reserved stock
- `POST /api/v1/inventory/deduct` - Deduct stock permanently
- `GET /api/v1/inventory/{productId}` - Get current stock level

All endpoints require JWT Bearer token authentication.

## Testing

Run unit and integration tests:

```bash
./mvnw test
```

Run with specific profile:

```bash
./mvnw test -Dspring.profiles.active=test
```

## Monitoring and Health Checks

- Health endpoint: `GET /actuator/health`
- Metrics: `GET /actuator/metrics`
- Info: `GET /actuator/info`

Logs are configured with Loki integration for centralized logging.

## Database

The service uses PostgreSQL with Flyway for database migrations. Migrations are located in `src/main/resources/db/migration/`.

## Security

- Authentication via OAuth2/JWT
- Authorization based on roles and scopes
- Secure communication with TLS in production

## Deployment

### Production Checklist

- [ ] Configure production database
- [ ] Set up Kafka cluster
- [ ] Configure OAuth2 provider
- [ ] Set up monitoring and alerting
- [ ] Configure load balancer
- [ ] Enable TLS/SSL
- [ ] Set up log aggregation
- [ ] Configure backup and recovery

### Kubernetes Deployment

Use the provided Helm charts or Kubernetes manifests for deployment:

```bash
helm install inventory-service ./helm/inventory-service
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes and add tests
4. Submit a pull request

## License

Proprietary License - All rights reserved. See LICENSE file for details.

## Support

For support, contact Infinity Tech Support at infinitytech@support.co.za
