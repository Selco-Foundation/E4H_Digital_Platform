#!/bin/bash

echo "Converting Mermaid diagrams to PNG..."
echo

echo "Converting system architecture diagram..."
mmdc -i system_architecture.mmd -o system_architecture.png --width 2000 --height 1500

echo "Converting database schema diagram..."
mmdc -i database_schema.mmd -o database_schema.png --width 2000 --height 1500

echo "Converting project creation workflow..."
mmdc -i 01_project_creation_workflow.mmd -o project_creation_workflow.png --width 2500 --height 2000

echo "Converting field plan creation workflow..."
mmdc -i 02_field_plan_creation_workflow.mmd -o field_plan_creation_workflow.png --width 2500 --height 2000

echo "Converting facility assignment workflow..."
mmdc -i 03_facility_assignment_workflow.mmd -o facility_assignment_workflow.png --width 2500 --height 2000

echo "Converting activity report workflow..."
mmdc -i 04_activity_report_workflow.mmd -o activity_report_workflow.png --width 2500 --height 2000

echo "Converting user management workflow..."
mmdc -i 05_user_management_workflow.mmd -o user_management_workflow.png --width 2500 --height 2000

echo "Converting conditional activation workflow..."
mmdc -i 06_conditional_activation_workflow.mmd -o conditional_activation_workflow.png --width 2500 --height 2000

echo "Converting mobile sync workflow..."
mmdc -i 07_mobile_sync_workflow.mmd -o mobile_sync_workflow.png --width 2500 --height 2000

echo "Converting legacy field plan workflow..."
mmdc -i field_plan_workflow.mmd -o legacy_field_plan_workflow.png --width 2000 --height 1200

echo
echo "All diagrams converted successfully!"
echo "Files created:"
echo "- system_architecture.png"
echo "- database_schema.png"
echo "- project_creation_workflow.png"
echo "- field_plan_creation_workflow.png"
echo "- facility_assignment_workflow.png"
echo "- activity_report_workflow.png"
echo "- user_management_workflow.png"
echo "- conditional_activation_workflow.png"
echo "- mobile_sync_workflow.png"
echo "- legacy_field_plan_workflow.png"
echo 