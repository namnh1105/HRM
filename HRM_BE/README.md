# Worksphere API

A comprehensive authentication and user management system built with Spring Boot, featuring JWT-based authentication, MapStruct mapping, and Swagger documentation.

## Features

- 🔐 **JWT Authentication** with access and refresh tokens
- 👤 **User Management** with profile updates and password changes
- 🛡️ **Spring Security** integration
- 📚 **Swagger/OpenAPI** documentation
- 🗺️ **MapStruct** for clean object mapping
- 🌍 **Environment Variables** support with .env files
- 🏗️ **Modular Monolith** architecture

## Quick Start

### 1. Environment Setup

Copy the example environment file:
```bash
cp .env.example .env
```

Edit `.env` with your configuration:
```env
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=worksphere_db
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT Configuration (Generate your own secret)
JWT_SECRET=your_base64_encoded_secret
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# Server Configuration
SERVER_PORT=8080
```

### 2. Database Setup

Make sure PostgreSQL is running and create the database:
```sql
CREATE DATABASE worksphere_db;
```

### 3. Redis Setup (Optional)

Install and start Redis server for caching:
```bash
redis-server
```

### 4. Run the Application

```bash
./gradlew bootRun
```

## API Documentation

Once the application is running, access Swagger UI at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

## API Endpoints

### Authentication Endpoints
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh-token` - Refresh access token
- `POST /api/auth/logout` - User logout
- `POST /api/auth/logout-all` - Logout from all devices

### User Management Endpoints
- `GET /api/users/me` - Get current user profile
- `GET /api/users/{userId}` - Get user by ID
- `PUT /api/users/profile` - Update user profile
- `PUT /api/users/change-password` - Change password
- `DELETE /api/users/deactivate` - Deactivate account

### Health Check
- `GET /api/health` - Application health status

## Environment Variables

| Variable | Description | Default | Available Values |
|----------|-------------|---------|------------------|
| `STAGE` | Application stage/environment | development | development, testing, staging, production |
| `DB_HOST` | Database host | localhost | - |
| `DB_PORT` | Database port | 5432 | - |
| `DB_NAME` | Database name | worksphere_db | - |
| `DB_USERNAME` | Database username | postgres | - |
| `DB_PASSWORD` | Database password | - | - |
| `REDIS_HOST` | Redis host | localhost | - |
| `REDIS_PORT` | Redis port | 6379 | - |
| `JWT_SECRET` | JWT signing secret (base64) | - | - |
| `JWT_ACCESS_TOKEN_EXPIRATION` | Access token expiration (ms) | 900000 | - |
| `JWT_REFRESH_TOKEN_EXPIRATION` | Refresh token expiration (ms) | 604800000 | - |
| `SERVER_PORT` | Server port | 8080 | - |
| `APP_NAME` | Application name | Worksphere API | - |
| `APP_VERSION` | Application version | 1.0.0 | - |

## Stage-based Configuration

The application supports different stages with specific logging and configuration:

### Development Stage (`STAGE=development`)
- **Logging**: DEBUG level for application code, SQL logging enabled
- **Console**: Colored output with detailed information
- **JPA**: SQL queries and parameters logged
- **Purpose**: Local development and debugging

### Production Stage (`STAGE=production`)
- **Logging**: WARN level for application, ERROR for root
- **Files**: 
  - Main logs: `logs/worksphere-production.log`
  - Error logs: `logs/worksphere-errors.log`
- **Security**: Stack traces hidden from error responses
- **Purpose**: Live production environment

## Configuration Files

- `application.properties` - Base configuration
- `application-development.properties` - Development overrides
- `application-production.properties` - Production overrides
- `logback-spring.xml` - Advanced logging configuration

## Architecture

The project follows a modular monolith architecture:

```
src/main/java/com/hainam/worksphere/
├── auth/           # Authentication module
│   ├── config/     # Security configuration
│   ├── controller/ # Auth REST controllers
│   ├── domain/     # Auth entities
│   ├── dto/        # Auth DTOs
│   ├── mapper/     # MapStruct mappers
│   ├── repository/ # Auth repositories
│   ├── security/   # Security components
│   ├── service/    # Auth services
│   └── util/       # JWT utilities
├── user/           # User management module
│   ├── controller/ # User REST controllers
│   ├── domain/     # User entities
│   ├── dto/        # User DTOs
│   ├── mapper/     # MapStruct mappers
│   ├── repository/ # User repositories
│   └── service/    # User services
└── shared/         # Shared components
    ├── config/     # Common configuration
    ├── controller/ # Health endpoints
    ├── dto/        # Common DTOs
    └── exception/  # Exception handling
```

## Security

- **JWT Tokens**: Stateless authentication with access and refresh tokens
- **Password Hashing**: BCrypt with salt
- **Token Rotation**: Refresh tokens are rotated on login
- **Token Revocation**: Support for logout and logout-all functionality
- **CORS**: Configurable CORS policy
- **Input Validation**: Jakarta Bean Validation

## Development

### Traditional Setup

#### Build
```bash
./gradlew build
```

#### Test
```bash
./gradlew test
```

#### Clean Build
```bash
./gradlew clean build
```

### Docker Setup

#### Prerequisites
- Docker and Docker Compose installed

#### Quick Start with Docker
```bash
# Copy environment file and configure it
cp .env.example .env

# Build and run with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

#### Build Docker Image Only
```bash
# Build the Docker image
docker build -t worksphere:latest .

# Run the container
docker run -p 8080:8080 \
  -e JWT_SECRET=your-secret \
  -e GOOGLE_OAUTH2_CLIENT_ID=your-client-id \
  worksphere:latest
```

#### Development with Docker
```bash
# Run in development mode
docker-compose -f docker-compose.yml up -d

# Access application at http://localhost:8080
# Access Redis at localhost:6379
```

## Technologies Used

- **Spring Boot 3.2.11** - Framework
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Data persistence
- **PostgreSQL** - Primary database
- **Redis** - Caching and session storage
- **JWT (jsonwebtoken)** - Token-based auth
- **MapStruct** - Object mapping
- **Swagger/OpenAPI** - API documentation
- **Lombok** - Code generation
- **Gradle** - Build tool
- **Docker & Docker Compose** - Containerization
- **Testcontainers** - Integration testing

## Testing

### Unit Tests
Run unit tests (default):
```bash
./gradlew test
```

### Integration Tests
Integration tests require Docker to be running. They use Testcontainers to spin up real Redis instances.

**Prerequisites:**
1. Install Docker Desktop
2. Ensure Docker is running

**Run integration tests:**
```bash
# Run all integration tests
./gradlew integrationTest

# Or manually enable integration tests
./gradlew test -Dintegration.tests.enabled=true
```

**Note:** If Docker is not available, integration tests will be automatically skipped with a warning message.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.
