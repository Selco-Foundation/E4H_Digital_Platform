# Inbox API Performance Optimizations

## Problem
The Inbox API was experiencing a 50% error rate during load/performance testing due to:
1. Connection failures to downstream services (user service, workflow service)
2. Database performance issues (missing indexes)
3. Resource exhaustion (thread pools, connection pools)
4. Timeout issues

## Solutions Implemented

### 1. Database Optimizations
- **Added Database Indexes**: Created migration `V20250627014000__add_indexes_incident.sql` with indexes on:
  - `applicationstatus`
  - `createdtime`
  - `accountid`
  - `incidenttype`
  - `tenantid`
  - `incidentid`
  - Composite indexes for common query patterns

- **Fixed Flyway Configuration**: 
  - Added `spring.flyway.enabled=true` (was missing)
  - Kept legacy properties for backward compatibility

### 2. Connection Pool Configuration
- **HikariCP Settings**:
  - `maximum-pool-size=20`
  - `minimum-idle=5`
  - `connection-timeout=30000`
  - `idle-timeout=600000`
  - `max-lifetime=1800000`
  - `leak-detection-threshold=60000`

### 3. Thread Pool Optimizations
- **Async Thread Pool**:
  - Increased from fixed 2 processors to dynamic based on available processors
  - Core pool size: `Math.max(4, availableProcessors)`
  - Max pool size: `Math.max(8, availableProcessors * 2)`
  - Queue capacity: `Math.max(20, availableProcessors * 10)`

### 4. External Service Resilience
- **RestTemplate Configuration**:
  - Connect timeout: 5 seconds
  - Read timeout: 10 seconds
  - Proper timeout handling

- **Circuit Breaker Pattern**:
  - User service failures don't crash the API
  - Returns minimal user objects when user service is down
  - Workflow service failures are handled gracefully

- **Retry Logic**:
  - 3 retry attempts with exponential backoff
  - 100ms, 200ms, 300ms delays

### 5. Server Configuration
- **Tomcat Settings**:
  - `threads.max=200`
  - `threads.min-spare=10`
  - `connection-timeout=20000`
- **Async Request Timeout**: 30 seconds

### 6. Query Optimizations
- **Fixed Bug**: Corrected phcType query to use correct column
- **Pagination**: Enforced proper limit/offset handling
- **Index Usage**: Queries now use proper indexes

### 7. Health Monitoring
- **Detailed Health Endpoint**: `/health/detailed`
- **Dependency Monitoring**: Checks user service and workflow service status
- **Graceful Degradation**: Service continues working even when dependencies are down

## Expected Results
1. **Reduced Error Rate**: From 50% to <5%
2. **Improved Response Times**: Faster database queries with indexes
3. **Better Resource Utilization**: Proper connection and thread pool management
4. **Resilience**: Service continues working when downstream services fail
5. **Monitoring**: Better visibility into service health

## Testing Recommendations
1. **Apply Database Migration**: Run the Flyway migration to add indexes
2. **Load Test**: Re-run performance tests with the same parameters
3. **Monitor Logs**: Check for any remaining connection issues
4. **Health Checks**: Use `/health/detailed` endpoint to monitor dependencies

## Configuration Files Modified
- `application.properties`: Added connection pool, timeout, and Flyway settings
- `AsyncConfig.java`: Increased thread pool sizes
- `RestTemplateConfig.java`: Added timeout configuration
- `UserService.java`: Added circuit breaker pattern
- `WorkflowService.java`: Added error handling
- `ServiceRequestRepository.java`: Added retry logic
- `IMQueryBuilder.java`: Fixed phcType query bug
- `HealthController.java`: Added health monitoring

## Migration File
- `V20250627014000__add_indexes_incident.sql`: Database indexes for performance 