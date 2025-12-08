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
            { "field": "check_earthing_pits", "label": "Earthing Pits are safe and surroundings are cleaned" },
            { "field": "check_earthing_cables", "label": "Cables & Connections found secure and no wear and tear observed" }
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
      "properties": [
        { "field": "battery_bank_voltage", "label": "Battery Bank Voltage" },
        { "field": "inverter_reading", "label": "Inverter display (Reading after measurement)" },
      ],
      "children": [
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
        { "field": "immediate_fix_done", "label": "If yes, was an immediate fix implemented?" },
        { "field": "parts_repaired", "label": "If yes, were any parts repaired?" },
        { "field": "informed_CRM", "label": "If yes, and further rectification is needed, did you inform the health staff or CRM to raise a ticket through Saura-eMitra?" },

        { "field": "service_person_remarks", "label": "Service Person (Remarks)" },
        { "field": "health_staff_remarks", "label": "Health Staff (Remarks)" },
        { "field": "img1_taken", "label": "Clear Geo tagged image of the service person with health facility name board" }
      ]
    }
  ]
}
