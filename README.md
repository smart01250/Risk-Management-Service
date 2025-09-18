# Risk Management Service - Java Spring Boot

A comprehensive risk management service for Kraken trading accounts built with Java Spring Boot. This service provides real-time monitoring, order processing, and automated risk threshold enforcement.

## Features

- **User Management**: Register users with Kraken API credentials and unique client IDs
- **Real-time Risk Monitoring**: Continuous monitoring of account balances and risk thresholds
- **Order Processing**: Handle trading signals with inverse and pyramid logic
- **Automated Risk Management**: Automatically close positions and disable trading when limits are exceeded
- **Daily Reset**: Re-enable trading at 00:01 UTC each day
- **REST API**: Complete RESTful API with Swagger documentation
- **Background Monitoring**: Scheduled tasks for continuous risk assessment

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA** with Hibernate
- **H2 Database** (development) / **PostgreSQL** (production)
- **Maven** for dependency management
- **Swagger/OpenAPI 3** for API documentation
- **Kraken Futures API** integration

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+ (optional - can use IDE instead)

### Installation

1. Clone the repository
2. Navigate to the project directory
3. Build and run the application:

**Option 1: Using Maven (if installed)**
```bash
mvn clean install
mvn spring-boot:run
```

**Option 2: Using the provided scripts**
```bash
# On Windows
run.bat

# On Linux/Mac
chmod +x run.sh
./run.sh
```

**Option 3: Using an IDE**
- Import the project into IntelliJ IDEA, Eclipse, or Spring Tool Suite
- Run the main class: `com.assessment.riskmanagement.RiskManagementApplication`

The service will start on `http://localhost:8080`

### API Documentation

Once the service is running, access the Swagger UI at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

### Health Check

Check service health at:
- **Health**: http://localhost:8080/api/v1/monitoring/health

## API Endpoints

### User Management
- `POST /api/v1/users/register` - Register new user with Kraken credentials
- `GET /api/v1/users` - Get all users
- `GET /api/v1/users/{clientId}` - Get user by client ID
- `PUT /api/v1/users/{clientId}` - Update user settings
- `DELETE /api/v1/users/{clientId}` - Delete user

### Order Management
- `POST /api/v1/orders/webhook` - Process trading signals
- `GET /api/v1/orders/user/{clientId}` - Get user orders
- `GET /api/v1/orders/{orderId}` - Get order by ID

### Risk Management
- `POST /api/v1/risk/check/{clientId}` - Check user risk
- `POST /api/v1/risk/check-all` - Check all users risk
- `POST /api/v1/risk/reset-trading` - Reset daily trading
- `GET /api/v1/risk/events` - Get all risk events
- `GET /api/v1/risk/events/{clientId}` - Get user risk events

### Monitoring
- `GET /api/v1/monitoring/status` - Get monitoring status
- `POST /api/v1/monitoring/enable` - Enable monitoring
- `POST /api/v1/monitoring/disable` - Disable monitoring
- `GET /api/v1/monitoring/health` - Health check

## Usage Examples

### 1. Register a User

```bash
curl -X POST "http://localhost:8080/api/v1/users/register" \
  -H "Content-Type: application/json" \
  -d '{
    "krakenApiKey": "your-api-key",
    "krakenPrivateKey": "your-private-key",
    "dailyRiskAbsolute": 1000.00,
    "dailyRiskPercentage": 5.0
  }'
```

### 2. Send Trading Signal

```bash
curl -X POST "http://localhost:8080/api/v1/orders/webhook" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "1234567890",
    "symbol": "BTCUSD",
    "strategy": "MyStrategy",
    "action": "buy",
    "orderQty": 0.1,
    "maxriskperday%": 2.0,
    "inverse": false,
    "pyramid": true,
    "stopLoss%": 2.5
  }'
```

### 3. Check Risk Status

```bash
curl -X POST "http://localhost:8080/api/v1/risk/check/1234567890"
```

## Configuration

Key configuration properties in `application.yml`:

```yaml
risk-management:
  kraken:
    base-url: https://futures.kraken.com
    api-version: v3
  monitoring:
    check-interval-seconds: 30
    timezone: UTC
  risk:
    default-daily-risk-percentage: 2.0
```

## Risk Management Logic

### Daily Risk Limits
- **Absolute Risk**: Maximum dollar amount that can be lost per day
- **Percentage Risk**: Maximum percentage of initial balance that can be lost per day
- When either limit is exceeded:
  1. All open orders are cancelled
  2. Trading is disabled until next day 00:01 UTC
  3. Risk event is logged

### Trading Logic
- **Inverse**: Close existing positions before opening opposite side orders
- **Pyramid**: Allow multiple same-side orders for the same strategy
- **Stop Loss**: Automatic stop loss orders based on percentage

### Monitoring
- Continuous monitoring every 30 seconds (configurable)
- Daily reset at 00:01 UTC
- Real-time balance checking via Kraken API

## Database Schema

### Users Table
- User credentials and risk settings
- Trading status and balance tracking

### Orders Table
- Order history and status
- Strategy and risk parameters

### Risk Events Table
- Risk threshold breaches
- Actions taken and timestamps

## Development

### Running Tests
```bash
mvn test
```

### Building for Production
```bash
mvn clean package -Pprod
```

### Database Console (Development)
Access H2 console at: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:riskdb`
- Username: `sa`
- Password: `password`

## Production Deployment

For production deployment:

1. Update `application-prod.yml` with PostgreSQL configuration
2. Set environment variables for sensitive data
3. Use external configuration for Kraken API credentials
4. Enable SSL/TLS
5. Configure proper logging and monitoring

## Security Considerations

- API keys are stored encrypted in the database
- All API endpoints should be secured with authentication
- Use HTTPS in production
- Implement rate limiting
- Regular security audits

## Support

For issues and questions, please refer to the API documentation or contact the development team.
