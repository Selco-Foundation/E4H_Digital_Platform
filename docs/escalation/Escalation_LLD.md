Low-Level Design: Automated Escalation Email and Reporting
1. Feature Description
This document outlines the low-level design for an automated escalation system designed to notify relevant stakeholders about SLA-breached tickets and to provide a weekly report. The system utilizes two distinct cronjobs to perform these tasks, ensuring timely notification and regular reporting without manual intervention.

2. Master Data Configuration
The following master data will be defined and queried via the egov-mdms-service. These configurations allow for flexible, tenant-specific role definitions for recipients.

escalation.EscalationLevel
This master data now includes breachThresholdInHours to define the escalation trigger for each level.
```json
{
  "tenantId": "<state-code>",
  "module": "escalation",
  "EscalationLevel": [
    { "id": 1, "escalationLevel": "LEVEL_ONE", "breachThresholdInHours": 0 },
    { "id": 2, "escalationLevel": "LEVEL_TWO", "breachThresholdInHours": 48 }
  ]
}
```

escalation.EscalationRecipient
This master data maps each escalation level to the specific recipient roles, which are used to find the users to be notified.
```json
{
  "tenantid": "<state-code>",
  "moduleName": "escalation",
  "EscalationRecipient": [
    { "id": 1, "escalationLevel": "LEVEL_ONE", "recipientRoles": ["STATE_POC"] },
    { "id": 2, "escalationLevel": "LEVEL_TWO", "recipientRoles": ["CENTRAL_ONM_PROJECT_MANAGER"] }
  ]
}
```

3. Cronjob Details
Cronjob 1: Daily Escalation
This cronjob is responsible for immediate notifications of newly breached tickets.

Schedule: Daily at the end of the workday.

Trigger Condition: The cronjob now checks a ticket's escalations array and its slaHoursRemaining against the configurable breachThresholdInHours.

Level Two: slaHoursRemaining is less than or equal to 48 and greater than or equal to 0, AND the escalations array does not contain "LEVEL_TWO".

Level One: slaHoursRemaining is less than 0, AND the escalations array does not contain "LEVEL_ONE".

Process Flow:

The cronjob first queries egov-mdms-service for escalation.EscalationLevel to get the breach thresholds.

It then calls the appropriate im-services endpoint to search for all tickets that meet the Level Two trigger condition.

It queries egov-mdms-service for LEVEL_TWO recipient roles.

It then queries egov-user/v1/_search to retrieve the email addresses.

It constructs a single message for these tickets and sends it to the escalation-notification-email topic.

Next, the cronjob calls im-services again to find all tickets that meet the Level One trigger condition.

It queries egov-mdms-service for LEVEL_ONE recipient roles.

It queries egov-user/v1/_search to retrieve the relevant email addresses.

It constructs another single message for this set of tickets and sends it to the escalation-notification-email topic.

The egov-notification-mail service consumes both messages and sends the emails.

Finally, the cronjob updates the escalations array for all processed tickets by adding the appropriate string ("LEVEL_ONE" or "LEVEL_TWO") by calling the relevant im-services endpoint.

Cronjob 2: Weekly Escalation Report
This cronjob is responsible for generating a comprehensive report of escalated tickets.

Schedule: Weekly at the start of the day on Monday.

Trigger Condition:

Ticket's escalations array is not empty.

Ticket's lastModifiedDate is within the last working week.

Process Flow:

The cronjob calls the appropriate im-services endpoint to search for all tickets meeting the trigger conditions.

It generates a CSV or XLS file (TBD) containing the ticket data.

The file is uploaded to the egov-filestore service, which returns a unique filestoreId.

It queries egov-mdms-service and egov-user (same as Cronjob 1) to determine the recipient list.

A message is created containing the filestoreId, templateId, subject, and recipient emails.

This message is sent to the escalation-notification-email topic.

The egov-notification-mail service consumes the message, retrieves the file from egov-filestore using the filestoreId, and sends an email with the file attached.

No updates are made to the tickets themselves.

4. Sequence Diagrams
Daily Escalation Process
This diagram illustrates the step-by-step process of the daily cronjob, now with two levels of escalation and using an array for tracking.

```mmd
sequenceDiagram
    participant C as Cronjob 1
    participant IM_S as im-services
    participant MDMS as egov-mdms-service
    participant USER as egov-user
    participant N_TOPIC as escalation-notification-email Topic
    participant MAIL as egov-notification-mail Service
    participant DB as Database

    C->>MDMS: 1. Query for escalation thresholds (EscalationLevel)
    MDMS-->>C: 2. Return thresholds (e.g., LEVEL_TWO: 48, LEVEL_ONE: 0)

    C->>IM_S: 3. Search tickets with slaHoursRemaining <= 48 and >= 0 AND "LEVEL_TWO" not in escalations array
    IM_S-->>C: 4. Return list of Level Two tickets
    C->>MDMS: 5. Query for LEVEL_TWO recipient roles
    MDMS-->>C: 6. Return roles (e.g., ["CENTRAL_ONM_PROJECT_MANAGER"])
    C->>USER: 7. Search for users by role
    USER-->>C: 8. Return list of recipient emails
    C->>N_TOPIC: 9. Publish single message for Level Two escalation
    N_TOPIC->>MAIL: 10. Message received
    
    C->>IM_S: 11. Search tickets with slaHoursRemaining < 0 AND "LEVEL_ONE" not in escalations array
    IM_S-->>C: 12. Return list of Level One tickets
    C->>MDMS: 13. Query for LEVEL_ONE recipient roles
    MDMS-->>C: 14. Return roles (e.g., ["STATE_POC"])
    C->>USER: 15. Search for users by role
    USER-->>C: 16. Return list of recipient emails
    C->>N_TOPIC: 17. Publish single message for Level One escalation
    N_TOPIC->>MAIL: 18. Message received

    MAIL->>MAIL: 19. Generate and send both emails
    C->>DB: 20. Update all processed tickets, add appropriate escalation string to escalations array
    DB-->>C: 21. Acknowledges update
```
Weekly Report Process
This diagram details the flow of the weekly cronjob, which includes generating a file and attaching it to an email.
```mmd
sequenceDiagram
    participant C as Cronjob 2
    participant IM_S as im-services
    participant MDMS as egov-mdms-service
    participant USER as egov-user
    participant FS as egov-filestore
    participant N_TOPIC as escalation-notification-email Topic
    participant MAIL as egov-notification-mail Service

    C->>IM_S: 1. Search for tickets with non-empty escalations array (last week)
    IM_S-->>C: 2. Return list of tickets
    C->>C: 3. Generate CSV/XLS file from ticket data
    C->>FS: 4. Upload file
    FS-->>C: 5. Return filestoreId
    C->>MDMS: 6. Query for escalation recipient roles
    MDMS-->>C: 7. Return roles
    C->>USER: 8. Search for users by role
    USER-->>C: 9. Return list of recipient emails
    C->>N_TOPIC: 10. Publish message with filestoreId, recipients
    N_TOPIC->>MAIL: 11. Message received
    MAIL->>FS: 12. Download file using filestoreId
    FS-->>MAIL: 13. Return file data
    MAIL->>MAIL: 14. Generate email with file attachment
    MAIL-->>MAIL: 15. Send email to recipients
```
Ticket Workflow Update Sequence
This new diagram shows the process of a user updating a ticket through a workflow action, which resets the escalation status.
```mmd
sequenceDiagram
    participant UI as User Interface
    participant User as User
    participant IM_S as im-services
    participant DB as Database

    User->>UI: 1. Performs workflow action on ticket
    UI->>IM_S: 2. Call ticket update API
    IM_S->>DB: 3. Update ticket details, set escalations = []
    DB-->>IM_S: 4. Acknowledges update
    IM_S-->>UI: 5. Returns success response
    UI-->>User: 6. Shows confirmation to user
```
