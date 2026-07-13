# FraudShield GenAI

Spring Boot backend for fraud signal evaluation, risk scoring, decisioning, and investigation case creation.

## Stack
- Java 21
- Spring Boot 3.3
- Maven
- Vertex AI Gemini client (optional explanation generation)

## Core Flow
1. Input arrives at `POST /api/v1/risk/evaluate` with `bookingId`, `shipperId`, and `signals`.
2. `RiskAssessmentService` calculates `riskScore`, `riskLevel`, `decision`, and `confidence`.
3. `InvestigationCaseService` creates an investigation case and generates explanation text.
4. API returns `EvaluationResponse` for frontend.

## Key Endpoints
- `POST /api/v1/risk/evaluate`
- `GET /api/v1/risk/assessment/{assessmentId}`
- `GET /api/v1/risk/assessment/booking/{bookingId}`
- `POST /api/v1/cases/create/{bookingId}`
- `GET /api/v1/cases/{caseId}`
- `GET /api/v1/cases/booking/{bookingId}`
- `GET /api/v1/cases/status/{status}`
- `PATCH /api/v1/cases/{caseId}/status`

## Run Locally
```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

## Docker
```bash
docker build -t fraudshield-genai .
docker run -p 8080:8080 fraudshield-genai
```

## Sample Evaluate Request
```json
{
  "bookingId": "BK001",
  "shipperId": "SHIP0001",
  "signals": [
    {
      "signalCode": "DEST001",
      "signalName": "NEW_DESTINATION",
      "severity": "HIGH",
      "score": 25,
      "reason": "Destination not seen before"
    }
  ]
}
```
