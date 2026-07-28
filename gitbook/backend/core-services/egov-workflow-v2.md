# eGov Workflow v2

## Purpose

Workflow v2 is the workflow engine used to move applications or entities through configured states and actions.

## Source location

- Service path: `backend/core-services/egov-workflow-v2`
- README: `backend/core-services/egov-workflow-v2/README.md`
- Local setup: `backend/core-services/egov-workflow-v2/LOCALSETUP.md`
- Changelog: `backend/core-services/egov-workflow-v2/CHANGELOG.md`

## Responsibilities

- Defines workflow state machines through business services, states, and actions.
- Controls which roles can perform workflow actions.
- Supports inbox visibility and assignment behavior.
- Tracks comments, uploads, assignees, and state transitions.
- Supports overall SLA and state-level SLA configuration.

## Dependencies

- `egov-mdms`
- `egov-user`

## Workflow model

The README describes a three-level configuration hierarchy:

- `BusinessService`: top-level workflow definition for a business domain.
- `State`: an application/entity status and its allowed actions.
- `Action`: a transition that roles can perform, optionally moving the item to a next state.

Business services can be configured at tenant or state level.

## Important configuration

Documented properties include:

- `egov.wf.default.offset`
- `egov.wf.default.limit`
- `egov.wf.max.limit`
- `egov.wf.statelevel`
- `egov.wf.inbox.assignedonly`

## Operational notes

Workflow configuration directly affects inbox results, action buttons, assignment behavior, and SLA reporting. When a business process changes, update workflow config, service behavior, UI behavior, and this documentation together.
