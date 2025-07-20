Write-Host "Converting Mermaid diagrams to PNG..." -ForegroundColor Green
Write-Host

Write-Host "Converting system architecture diagram..." -ForegroundColor Yellow
mmdc -i system_architecture.mmd -o system_architecture.png --width 2000 --height 1500

Write-Host "Converting database schema diagram..." -ForegroundColor Yellow
mmdc -i database_schema.mmd -o database_schema.png --width 2000 --height 1500

Write-Host "Converting project creation workflow..." -ForegroundColor Yellow
mmdc -i 01_project_creation_workflow.mmd -o project_creation_workflow.png --width 2500 --height 2000

Write-Host "Converting field plan creation workflow..." -ForegroundColor Yellow
mmdc -i 02_field_plan_creation_workflow.mmd -o field_plan_creation_workflow.png --width 2500 --height 2000

Write-Host "Converting facility assignment workflow..." -ForegroundColor Yellow
mmdc -i 03_facility_assignment_workflow.mmd -o facility_assignment_workflow.png --width 2500 --height 2000

Write-Host "Converting activity report workflow..." -ForegroundColor Yellow
mmdc -i 04_activity_report_workflow.mmd -o activity_report_workflow.png --width 2500 --height 2000

Write-Host "Converting user management workflow..." -ForegroundColor Yellow
mmdc -i 05_user_management_workflow.mmd -o user_management_workflow.png --width 2500 --height 2000

Write-Host "Converting conditional activation workflow..." -ForegroundColor Yellow
mmdc -i 06_conditional_activation_workflow.mmd -o conditional_activation_workflow.png --width 2500 --height 2000

Write-Host "Converting mobile sync workflow..." -ForegroundColor Yellow
mmdc -i 07_mobile_sync_workflow.mmd -o mobile_sync_workflow.png --width 2500 --height 2000

Write-Host "Converting legacy field plan workflow..." -ForegroundColor Yellow
mmdc -i field_plan_workflow.mmd -o legacy_field_plan_workflow.png --width 2000 --height 1200

Write-Host
Write-Host "All diagrams converted successfully!" -ForegroundColor Green
Write-Host "Files created:" -ForegroundColor Cyan
Write-Host "- system_architecture.png" -ForegroundColor White
Write-Host "- database_schema.png" -ForegroundColor White
Write-Host "- project_creation_workflow.png" -ForegroundColor White
Write-Host "- field_plan_creation_workflow.png" -ForegroundColor White
Write-Host "- facility_assignment_workflow.png" -ForegroundColor White
Write-Host "- activity_report_workflow.png" -ForegroundColor White
Write-Host "- user_management_workflow.png" -ForegroundColor White
Write-Host "- conditional_activation_workflow.png" -ForegroundColor White
Write-Host "- mobile_sync_workflow.png" -ForegroundColor White
Write-Host "- legacy_field_plan_workflow.png" -ForegroundColor White
Write-Host

Read-Host "Press Enter to continue..." 