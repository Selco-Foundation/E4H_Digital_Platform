# User roles and permissions

This page documents what each role can and cannot do, based on what is actually implemented in the codebase (IM Services workflow configuration, the RMS ticket-pause feature, vendor/facility org roles in MDMS, and the escalation-email logic in IM Services Analytics) — not a separate product spec. It should be reviewed and confirmed by the platform team, since some behavior (marked below) is inferred from code rather than stated in product documentation.

Five roles are covered: **Facility Staff (HCR)**, **Vendor / Technician**, **CRM Operator**, **State Coordinator**, and **State POC**.

> **Note on State Coordinator:** the codebase, MDMS role configuration, and workflow definitions do not define a separate "State Coordinator" role — there is no role constant, MDMS entry, or workflow reference for it anywhere in the repository. State Coordinator is documented here as equivalent to **State POC** (`STATE_POC`), which is the role actually implemented for state-level oversight. If a distinct State Coordinator role is intended, it still needs to be defined and built.

## Facility Staff (HCR)

The Health Centre Representative — the facility-level point of contact who reports issues with their facility's solar installation. Implemented as the `COMPLAINANT` role in the IM Services workflow.

| Can do | Cannot do |
|---|---|
| Raise a new ticket for their facility (issue, theft, or RMS-device ticket) | Assign, resolve, or reject any ticket — that belongs to the Complaint Assessor / Complaint Resolver roles |
| Reopen a ticket that was rejected | Raise a ticket for a different issue type while an uninstall ticket for the same facility is still pending resolution |
| Rate a ticket after it has been resolved and closed | See or act on tickets or facilities other than their own |

## Vendor / Technician

Field staff employed by a vendor organization who carry out installation, AMC, and repair work, and resolve tickets assigned to them. Implemented as `FIELD_STAFF` / `AMC_FIELD_STAFF` (vendor-org roles in MDMS) together with the `COMPLAINT_RESOLVER` workflow role — the latter is granted automatically to users of vendor organizations with subtype `AMC_VENDOR`.

| Can do | Cannot do |
|---|---|
| Resolve a ticket assigned to them, or send it back for reassignment | Assign a ticket to themselves or to another resolver — assignment is a Complaint Assessor action |
| Flag a ticket as needing a spare part, or as out of warranty | Raise a new ticket on behalf of a facility |
| Complete a scheduled AMC visit, verified by an OTP entered on-site | See or act on tickets belonging to a different vendor organization |

## CRM Operator

The employee-app role that manages RMS's automatic ticket creation and receives theft-related alerts. Implemented as the `CRM` reporter type / role referenced in the RMS ticket-pause feature and in incident enrichment.

| Can do | Cannot do |
|---|---|
| Report a ticket on behalf of a facility | Resolve a ticket directly — that requires the Complaint Resolver (vendor/technician) role |
| Pause RMS's automatic ticket creation for a specific facility, for a chosen duration, with an optional reason — and resume it early | See the pause/resume screen at all if they don't hold the CRM role (it's hidden from field staff and other roles by design) |
| View and manage the list of currently paused facilities within their boundary scope | Clear the backlog of tickets RMS would have raised during a pause — those situations are not created retroactively once the pause ends |
| Receive an SMS alert when a theft ticket sits unassigned past the configured threshold | |

## State Coordinator / State POC

State-level oversight role that receives escalation reporting but does not act on individual tickets directly. Implemented as `STATE_POC` in the IM Services Analytics escalation module; scoped to a single state per role registration (as opposed to `CENTRAL_POC`, which is scoped at the country level).

| Can do | Cannot do |
|---|---|
| Receive daily/weekly escalation email reports scoped to their state, with ticket counts broken down by workflow state, including CSV attachments | Assign, resolve, reject, or otherwise transition a ticket — `STATE_POC` does not appear in the role list of any workflow action, so it has no direct workflow rights |
| See counts across **all** workflow states in their escalation report (unlike `CENTRAL_POC`, which gets a combined report covering only the first two escalation levels) | Act outside their assigned state boundary — escalation queries are scoped to the one state the role is registered against |

## Related roles not covered above

The IM Services workflow also defines `COMPLAINT_ASSESSOR` (triages new tickets — assigns or rejects them) and `COMPLAINT_FACILITATOR_1` / `COMPLAINT_FACILITATOR_2` (referred to in code comments as "State SPOC" and "Tech POC", handling RMS-device assignment and out-of-warranty escalation). These aren't part of the five roles requested here, but are noted so this page doesn't read as though they were missed.
