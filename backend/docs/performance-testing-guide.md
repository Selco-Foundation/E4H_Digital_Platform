# Performance Testing Guide - E4H Digital Platform

## Overview
This guide addresses connection refused errors during performance testing, specifically with the `egov-user` service.

## Common Issues and Solutions

### 1. Connection Refused Errors

**Error Messages:**
```
{"ResponseInfo":null,"Errors":[{"code":"CustomException","message":"I/O error on POST request for \"http://egov-user.core-dev:8080/user/_details\": Connection refused","description":"I/O error on POST request for \"http://egov-user.core-dev:8080/user/_details\": Connection refused","params":null}]}
```

**Root Causes:**
- Service discovery issues in Kubernetes namespace `core-dev`
- Network connectivity problems
- Service overload during high load
- Insufficient connection pool configuration

### 2. Configuration Enhancements

#### A. Application Properties
The following configurations have been added to improve performance:

```properties
# Enhanced Connection Pool Settings
spring.http.client.connection-timeout=60000
spring.http.client.read-timeout=120000
spring.http.client.write-timeout=120000

# RestTemplate Configuration
rest.template.connection.timeout=60000
rest.template.read.timeout=120000
rest.template.connection.pool.max=100
rest.template.connection.pool.default=50
rest.template.connection.pool.keepalive=600000

# Retry Configuration
service.retry.maxAttempts=5
service.retry.backoff.delay=2000
service.retry.backoff.multiplier=2.0
service.retry.backoff.maxDelay=30000

# Circuit Breaker Configuration
resilience4j.circuitbreaker.instances.egov-user.sliding-window-size=10
resilience4j.circuitbreaker.instances.egov-user.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.egov-user.wait-duration-in-open-state=30000
```

#### B. Enhanced RestTemplate Configuration
- Increased connection pool size
- Added retry handlers
- Enhanced timeout configurations
- Added request/response logging

#### C. Improved Retry Logic
- Exponential backoff strategy
- Specific error handling for connection issues
- Dedicated egov-user service retry configuration

### 3. Performance Testing Best Practices

#### A. Pre-Testing Checklist
1. **Verify Service Availability:**
   ```bash
   # Check if egov-user service is running
   kubectl get pods -n core-dev | grep egov-user
   
   # Check service endpoints
   kubectl get endpoints -n core-dev | grep egov-user
   ```

2. **Network Connectivity:**
   ```bash
   # Test connectivity from within the cluster
   kubectl run test-pod --image=busybox -n core-dev --rm -it --restart=Never -- wget -O- http://egov-user.core-dev:8080/health
   ```

3. **Resource Monitoring:**
   ```bash
   # Monitor resource usage
   kubectl top pods -n core-dev
   kubectl describe pod <egov-user-pod-name> -n core-dev
   ```

#### B. Load Testing Configuration
1. **Gradual Load Increase:**
   - Start with 10 concurrent users
   - Increase by 50% every 5 minutes
   - Monitor error rates and response times

2. **Monitoring Metrics:**
   - Connection pool utilization
   - Response time percentiles
   - Error rates by service
   - Circuit breaker status

3. **Resource Scaling:**
   ```yaml
   # Example HorizontalPodAutoscaler
   apiVersion: autoscaling/v2
   kind: HorizontalPodAutoscaler
   metadata:
     name: egov-user-hpa
     namespace: core-dev
   spec:
     scaleTargetRef:
       apiVersion: apps/v1
       kind: Deployment
       name: egov-user
     minReplicas: 3
     maxReplicas: 10
     metrics:
     - type: Resource
       resource:
         name: cpu
         target:
           type: Utilization
           averageUtilization: 70
   ```

### 4. Troubleshooting Steps

#### A. Immediate Actions
1. **Check Service Health:**
   ```bash
   curl -f http://egov-user.core-dev:8080/health
   ```

2. **Verify DNS Resolution:**
   ```bash
   nslookup egov-user.core-dev.svc.cluster.local
   ```

3. **Check Network Policies:**
   ```bash
   kubectl get networkpolicies -n core-dev
   ```

#### B. Advanced Debugging
1. **Enable Debug Logging:**
   ```properties
   logging.level.org.egov.inbox=DEBUG
   logging.level.org.springframework.web.client=DEBUG
   logging.level.org.apache.http=DEBUG
   ```

2. **Monitor Circuit Breaker:**
   ```bash
   curl http://localhost:8080/actuator/circuitbreakers
   ```

3. **Check Connection Pool Status:**
   ```bash
   curl http://localhost:8080/actuator/metrics/http.client.connections
   ```

### 5. Performance Optimization Recommendations

#### A. Infrastructure Level
1. **Increase Resource Limits:**
   ```yaml
   resources:
     requests:
       memory: "512Mi"
       cpu: "250m"
     limits:
       memory: "1Gi"
       cpu: "500m"
   ```

2. **Optimize JVM Settings:**
   ```yaml
   env:
   - name: JAVA_OPTS
     value: "-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
   ```

#### B. Application Level
1. **Use Connection Pooling:**
   - Configure appropriate pool sizes
   - Set connection timeouts
   - Enable connection validation

2. **Implement Circuit Breakers:**
   - Prevent cascade failures
   - Provide fallback mechanisms
   - Monitor failure rates

3. **Optimize Database Connections:**
   - Use connection pooling
   - Configure appropriate timeouts
   - Monitor connection usage

### 6. Monitoring and Alerting

#### A. Key Metrics to Monitor
1. **Response Time:**
   - P50, P95, P99 percentiles
   - Service-specific response times

2. **Error Rates:**
   - HTTP 5xx errors
   - Connection refused errors
   - Circuit breaker trips

3. **Resource Utilization:**
   - CPU and memory usage
   - Connection pool utilization
   - Network I/O

#### B. Alerting Rules
```yaml
# Example Prometheus alerting rules
groups:
- name: performance-alerts
  rules:
  - alert: HighErrorRate
    expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
    for: 2m
    labels:
      severity: warning
    annotations:
      summary: "High error rate detected"
      
  - alert: ConnectionRefused
    expr: rate(connection_refused_total[5m]) > 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "Connection refused errors detected"
```

### 7. Recovery Procedures

#### A. Service Recovery
1. **Restart Service:**
   ```bash
   kubectl rollout restart deployment/egov-user -n core-dev
   ```

2. **Scale Up:**
   ```bash
   kubectl scale deployment egov-user --replicas=5 -n core-dev
   ```

3. **Check Logs:**
   ```bash
   kubectl logs -f deployment/egov-user -n core-dev
   ```

#### B. Data Recovery
1. **Verify Data Integrity:**
   ```sql
   SELECT COUNT(*) FROM users WHERE created_date >= NOW() - INTERVAL '1 hour';
   ```

2. **Check Transaction Logs:**
   - Review application logs for failed transactions
   - Identify data inconsistencies

### 8. Prevention Strategies

#### A. Proactive Monitoring
1. **Health Checks:**
   - Implement comprehensive health checks
   - Monitor service dependencies
   - Set up automated recovery

2. **Capacity Planning:**
   - Regular load testing
   - Resource usage analysis
   - Scaling recommendations

#### B. Code Quality
1. **Error Handling:**
   - Implement proper exception handling
   - Add retry mechanisms
   - Use circuit breakers

2. **Performance Testing:**
   - Regular performance regression testing
   - Load testing in staging environment
   - Performance benchmarks

## Conclusion

By implementing these configurations and following the best practices outlined in this guide, you should be able to significantly reduce connection refused errors during performance testing. The key is to:

1. **Monitor proactively** - Set up comprehensive monitoring and alerting
2. **Scale appropriately** - Ensure sufficient resources for expected load
3. **Handle failures gracefully** - Implement retry mechanisms and circuit breakers
4. **Test regularly** - Conduct performance testing in staging environments

For additional support, please refer to the application logs and monitoring dashboards for specific error patterns and performance metrics. 