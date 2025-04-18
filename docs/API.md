# Twiggle API Documentation

## Overview
This document provides comprehensive documentation for the Twiggle API, a gardening and plant management platform. The API follows RESTful principles and uses JSON for request and response formats.

## Base URL
```
https://api.twiggle.com/api
```

## Authentication
All API endpoints require authentication using JWT tokens. Include the token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

## Rate Limiting
The API implements rate limiting using a standard rate limiter. Please contact support for specific rate limit details.

## Common Response Format
All API responses follow this standard format:
```json
{
  "success": boolean,
  "message": string,
  "data": object | array | null,
  "error": {
    "code": string,
    "message": string
  } | null
}
```

## Error Codes
- `INTERNAL_ERROR`: Internal server error
- `RESOURCE_NOT_FOUND`: Requested resource not found
- `VALIDATION_ERROR`: Input validation failed
- `UNAUTHORIZED`: Authentication required
- `FORBIDDEN`: Insufficient permissions

## API Endpoints

### Plants

#### Get All Plants
```http
GET /plants
```

Query Parameters:
- `page` (default: 0): Page number (0-based)
- `size` (default: 10): Page size
- `sort` (default: "createdAt"): Sort field
- `direction` (default: "DESC"): Sort direction ("ASC" or "DESC")

Response: Paginated list of plants

#### Search Plants
```http
GET /plants/search
```

Query Parameters:
- `query` (optional): Search term for name, description, type
- `gardenPlanId` (optional): Filter by garden plan ID
- `page` (default: 0): Page number
- `size` (default: 10): Page size
- `sort` (default: "createdAt"): Sort field
- `direction` (default: "DESC"): Sort direction

Response: Paginated list of matching plants

#### Advanced Plant Search
```http
GET /plants/search/advanced
```

Query Parameters:
- `name` (optional): Search by plant name
- `type` (optional): Search by plant type
- `wateringFrequency` (optional): Search by watering frequency
- `sunlightRequirements` (optional): Search by sunlight requirements
- `query` (optional): General search term
- `gardenPlanId` (optional): Filter by garden plan ID
- `page` (default: 0): Page number
- `size` (default: 10): Page size

Response: Paginated list of plants ordered by relevance

#### Get Plant by ID
```http
GET /plants/{id}
```

Response: Single plant details

#### Get Plants by Garden Plan
```http
GET /plants/garden-plan/{gardenPlanId}
```

Query Parameters:
- `page` (default: 0): Page number
- `size` (default: 10): Page size
- `sort` (default: "createdAt"): Sort field
- `direction` (default: "DESC"): Sort direction

Response: Paginated list of plants in the garden plan

#### Get Plants by Type
```http
GET /plants/type/{type}
```

Query Parameters:
- `page` (default: 0): Page number
- `size` (default: 10): Page size
- `sort` (default: "createdAt"): Sort field
- `direction` (default: "DESC"): Sort direction

Response: Paginated list of plants of specified type

#### Create Plant
```http
POST /plants
```

Request Body: Plant details in JSON format
Response: Created plant details

#### Update Plant
```http
PUT /plants/{id}
```

Request Body: Updated plant details in JSON format
Response: Updated plant details

#### Delete Plant
```http
DELETE /plants/{id}
```

Response: Success message

### Weather

#### Get Current Weather
```http
GET /weather/current
```

Query Parameters:
- `location` (required): Location name

Response: Current weather data

#### Get Current Weather by Coordinates
```http
GET /weather/current/coordinates
```

Query Parameters:
- `latitude` (required): Latitude (-90 to 90)
- `longitude` (required): Longitude (-180 to 180)

Response: Current weather data

#### Get Weather Forecast
```http
GET /weather/forecast
```

Query Parameters:
- `location` (required): Location name
- `days` (default: 3): Number of forecast days (1-7)

Response: Weather forecast data

#### Get Weather Forecast by Coordinates
```http
GET /weather/forecast/coordinates
```

Query Parameters:
- `latitude` (required): Latitude (-90 to 90)
- `longitude` (required): Longitude (-180 to 180)
- `days` (default: 3): Number of forecast days (1-7)

Response: Weather forecast data

#### Get Garden Weather
```http
GET /weather/garden
```

Query Parameters:
- `location` (required): Location name
- `gardenPlanId` (optional): Garden plan ID for plant-specific advice

Response: Weather data with gardening-specific information

#### Get Garden Weather by Coordinates
```http
GET /weather/garden/coordinates
```

Query Parameters:
- `latitude` (required): Latitude (-90 to 90)
- `longitude` (required): Longitude (-180 to 180)
- `gardenPlanId` (optional): Garden plan ID for plant-specific advice

Response: Weather data with gardening-specific information

#### Get Weather Hazards
```http
GET /weather/hazards
```

Query Parameters:
- `location` (required): Location name

Response: Weather hazard information

#### Get Weather Hazards by Coordinates
```http
GET /weather/hazards/coordinates
```

Query Parameters:
- `latitude` (required): Latitude (-90 to 90)
- `longitude` (required): Longitude (-180 to 180)

Response: Weather hazard information

### Garden Plans

#### Get All Garden Plans
```http
GET /garden-plans
```

Query Parameters:
- `page` (default: 0): Page number
- `size` (default: 10): Page size
- `sort` (default: "createdAt"): Sort field
- `direction` (default: "DESC"): Sort direction

Response: Paginated list of garden plans

#### Get Garden Plan by ID
```http
GET /garden-plans/{id}
```

Response: Single garden plan details

#### Create Garden Plan
```http
POST /garden-plans
```

Request Body: Garden plan details in JSON format
Response: Created garden plan details

#### Update Garden Plan
```http
PUT /garden-plans/{id}
```

Request Body: Updated garden plan details in JSON format
Response: Updated garden plan details

#### Delete Garden Plan
```http
DELETE /garden-plans/{id}
```

Response: Success message

### Plant Reminders

#### Get All Plant Reminders
```http
GET /plant-reminders
```

Query Parameters:
- `page` (default: 0): Page number
- `size` (default: 10): Page size
- `sort` (default: "createdAt"): Sort field
- `direction` (default: "DESC"): Sort direction

Response: Paginated list of plant reminders

#### Get Plant Reminder by ID
```http
GET /plant-reminders/{id}
```

Response: Single plant reminder details

#### Create Plant Reminder
```http
POST /plant-reminders
```

Request Body: Plant reminder details in JSON format
Response: Created plant reminder details

#### Update Plant Reminder
```http
PUT /plant-reminders/{id}
```

Request Body: Updated plant reminder details in JSON format
Response: Updated plant reminder details

#### Delete Plant Reminder
```http
DELETE /plant-reminders/{id}
```

Response: Success message

### Garden Images

#### Get All Garden Images
```http
GET /garden-images
```

Query Parameters:
- `page` (default: 0): Page number
- `size` (default: 10): Page size
- `sort` (default: "createdAt"): Sort field
- `direction` (default: "DESC"): Sort direction

Response: Paginated list of garden images

#### Get Garden Image by ID
```http
GET /garden-images/{id}
```

Response: Single garden image details

#### Upload Garden Image
```http
POST /garden-images
```

Request Body: Multipart form data with image file
Response: Uploaded garden image details

#### Delete Garden Image
```http
DELETE /garden-images/{id}
```

Response: Success message

### Image Comments

#### Get Image Comments
```http
GET /image-comments
```

Query Parameters:
- `imageId` (required): ID of the image
- `page` (default: 0): Page number
- `size` (default: 10): Page size
- `sort` (default: "createdAt"): Sort field
- `direction` (default: "DESC"): Sort direction

Response: Paginated list of comments for the image

#### Add Image Comment
```http
POST /image-comments
```

Request Body: Comment details in JSON format
Response: Created comment details

#### Delete Image Comment
```http
DELETE /image-comments/{id}
```

Response: Success message

### Image Likes

#### Get Image Likes
```http
GET /image-likes
```

Query Parameters:
- `imageId` (required): ID of the image
- `page` (default: 0): Page number
- `size` (default: 10): Page size
- `sort` (default: "createdAt"): Sort field
- `direction` (default: "DESC"): Sort direction

Response: Paginated list of likes for the image

#### Like Image
```http
POST /image-likes
```

Request Body: Like details in JSON format
Response: Created like details

#### Unlike Image
```http
DELETE /image-likes/{id}
```

Response: Success message

### Activities

#### Get User Activities
```http
GET /activities
```

Query Parameters:
- `page` (default: 0): Page number
- `size` (default: 10): Page size
- `sort` (default: "createdAt"): Sort field
- `direction` (default: "DESC"): Sort direction

Response: Paginated list of user activities

#### Get Activity by ID
```http
GET /activities/{id}
```

Response: Single activity details

#### Create Activity
```http
POST /activities
```

Request Body: Activity details in JSON format
Response: Created activity details

#### Delete Activity
```http
DELETE /activities/{id}
```

Response: Success message

### Profiles

#### Get User Profile
```http
GET /profiles
```

Response: User profile details

#### Update User Profile
```http
PUT /profiles
```

Request Body: Updated profile details in JSON format
Response: Updated profile details

## Data Models

### Plant
```json
{
  "id": "uuid",
  "name": "string",
  "description": "string",
  "type": "string",
  "wateringFrequency": "string",
  "sunlightRequirements": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### Weather
```json
{
  "temperature": "number",
  "humidity": "number",
  "windSpeed": "number",
  "precipitation": "number",
  "description": "string",
  "gardenAdvice": "string",
  "hazards": ["string"]
}
```

### Garden Plan
```json
{
  "id": "uuid",
  "name": "string",
  "description": "string",
  "location": "string",
  "plants": ["uuid"],
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### Plant Reminder
```json
{
  "id": "uuid",
  "plantId": "uuid",
  "type": "string",
  "frequency": "string",
  "nextDueDate": "datetime",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### Garden Image
```json
{
  "id": "uuid",
  "url": "string",
  "description": "string",
  "gardenPlanId": "uuid",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### Comment
```json
{
  "id": "uuid",
  "content": "string",
  "imageId": "uuid",
  "userId": "uuid",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### Like
```json
{
  "id": "uuid",
  "imageId": "uuid",
  "userId": "uuid",
  "createdAt": "datetime"
}
```

### Activity
```json
{
  "id": "uuid",
  "type": "string",
  "description": "string",
  "userId": "uuid",
  "createdAt": "datetime"
}
```

### Profile
```json
{
  "id": "uuid",
  "username": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "bio": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

## Best Practices

1. **Error Handling**
   - Always check the `success` field in responses
   - Handle error responses appropriately
   - Implement proper error logging

2. **Rate Limiting**
   - Implement exponential backoff for retries
   - Monitor rate limit headers
   - Cache responses when possible

3. **Authentication**
   - Store JWT tokens securely
   - Implement token refresh mechanism
   - Handle authentication errors gracefully

4. **Data Validation**
   - Validate all input data before sending
   - Handle validation errors appropriately
   - Use appropriate data types

5. **Pagination**
   - Implement proper pagination handling
   - Cache paginated results when possible
   - Handle empty results gracefully

## Support

For API support, please contact:
- Email: support@twiggle.com
- Documentation: https://docs.twiggle.com
- Status Page: https://status.twiggle.com 