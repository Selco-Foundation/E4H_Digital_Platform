export const amcConfig = {
  "sectionName": "Scheduled Maintenance (AMC) Report",
  "properties": [
    { "field": "service_person_name", "label": "Service person Name" },
    { "field": "service_person_contact", "label": "Service person contact no" },
  ],
  "children": [
    {
      "sectionName": "I. Checklist during scheduled service visit",
      "children": [
        {
          "sectionName": "Solar Panels",
          "properties": [
            { "field": "check_solar_panels_cleaned", "label": "All Panels Cleaned during AMC visit" },
            { "field": "check_mounting_structure", "label": "Mounting structure secured and all nuts and bolts were tightened" },
            { "field": "check_cables_panels", "label": "Cables & Connections found secure and no wear and tear observed (at solar panels)" }
          ]
        },
        {
          "sectionName": "Inverter",
          "properties": [
            { "field": "check_inverter_cleaned", "label": "Inverter cleaned during visit" },
            { "field": "check_inverter_cables", "label": "Cables & Connections found secure and no wear and tear observed" }
          ]
        },
        {
          "sectionName": "Battery",
          "properties": [
            { "field": "check_battery_connections", "label": "Cables and Connections were checked at all battery terminals and found OK" },
            { "field": "check_battery_jelly", "label": "Applied petroleum jelly at all battery terminals" },
            { "field": "check_battery_water", "label": "Filled the distilled water till the required level" },
            { "field": "check_battery_backup", "label": "Battery Back-up found normal" },
            { "field": "check_battery_cleaned", "label": "All battery cleaned and maintained during visit" },
            { "field": "check_battery_corrosion", "label": "No corrosion observed at the battery terminals" }
          ]
        },
        {
          "sectionName": "Earthing and Surge Protection",
          "properties": [
            { "field": "check_earthing", "label": "Earthing working properly" },
            { "field": "check_surge", "label": "Surge Protection working properly" },
            { "field": "check_solar_array", "label": "Earthing Pits are safe and surroundings are cleaned" },
            { "field": "check_general_condition", "label": "Cables & Connections found secure and no wear and tear observed" }
          ]
        },
        {
          "sectionName": "Others",
          "properties": [
            { "field": "check_lightning_arrestor", "label": "Lightning Arrestor" },
            { "field": "check_gipb", "label": "Grid Input Protection Box (GIPB)" },
            { "field": "check_ajb", "label": "Array Junction Box (AJB)" },
            { "field": "check_changeover", "label": "Changeover Switch" }
          ]
        }
      ]
    },

    {
      "sectionName": "II. Performance and Preventive Maintenance",
      "children": [
        {
          "sectionName": "Battery Bank Voltage",
          "properties": [
            { "field": "battery_design_voltage", "label": "As per design" },
            { "field": "battery_observed_voltage", "label": "Observed during visit" }
          ]
        },
        {
          "sectionName": "Inverter display",
          "properties": [
            { "field": "inverter_reading", "label": "(Reading after measurement)" }
          ]
        },
        {
          "sectionName": "Panel Voltage",
          "properties": [
            { "field": "panel_string_1", "label": "String-1" },
            { "field": "panel_string_2", "label": "String-2" },
            { "field": "panel_string_3", "label": "String-3" },
            { "field": "panel_string_4", "label": "String-4" },
            { "field": "panel_string_5", "label": "String-5" }
          ]
        }
      ]
    },

    {
      "sectionName": "III. Faults & Corrective Actions",
      "properties": [
        { "field": "faults_observed", "label": "Any Faults observed during visit" },
        { "field": "immediate_fix_done", "label": "If Yes, Immediate fix done" },
        { "field": "parts_repaired", "label": "If Yes, Any Parts repaired" },
        { "field": "fault_rectification_status", "label": "Fault rectification status (If applicable - tick appropriately)" },
        { "field": "scheduled_plan_date", "label": "If not/partially completed, mention the scheduled plan date (DD/MM/YYYY)" },
        { "field": "any_parts_replaced", "label": "Any parts Replaced" },

        { "field": "part1_index", "label": "Parts replaced at site – 1: No." },
        { "field": "part1_component", "label": "Parts replaced at site – 1: Component" },
        { "field": "part1_make", "label": "Parts replaced at site – 1: Make" },
        { "field": "part1_serial", "label": "Parts replaced at site – 1: Serial No." },
        { "field": "part1_qty", "label": "Parts replaced at site – 1: Quantity" },
        { "field": "part1_remarks", "label": "Parts replaced at site – 1: Remarks" },

        { "field": "part2_index", "label": "Parts replaced at site – 2: No." },
        { "field": "part2_component", "label": "Parts replaced at site – 2: Component" },
        { "field": "part2_make", "label": "Parts replaced at site – 2: Make" },
        { "field": "part2_serial", "label": "Parts replaced at site – 2: Serial No." },
        { "field": "part2_qty", "label": "Parts replaced at site – 2: Quantity" },
        { "field": "part2_remarks", "label": "Parts replaced at site – 2: Remarks" },

        { "field": "part3_index", "label": "Parts replaced at site – 3: No." },
        { "field": "part3_component", "label": "Parts replaced at site – 3: Component" },
        { "field": "part3_make", "label": "Parts replaced at site – 3: Make" },
        { "field": "part3_serial", "label": "Parts replaced at site – 3: Serial No." },
        { "field": "part3_qty", "label": "Parts replaced at site – 3: Quantity" },
        { "field": "part3_remarks", "label": "Parts replaced at site – 3: Remarks" },

        { "field": "additional_remarks", "label": "Additional remarks" },
        { "field": "service_person_remarks", "label": "Service Person (Remarks)" },
        { "field": "end_user_remarks", "label": "End user (Remarks)" },
        { "field": "img1_taken", "label": "Clear Geo tagged image of the service person with health facility name board" }
      ]
    }
  ]
}
