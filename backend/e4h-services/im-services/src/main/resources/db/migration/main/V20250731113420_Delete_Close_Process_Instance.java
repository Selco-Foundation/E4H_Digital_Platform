package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

-public class V1_4__Process_each_incident extends BaseJavaMigration {
+public class V20250731113420_Delete_Close_Process_Instance extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        List<String> incidentIds = getIncidentIds(context);

        for (String incidentId : incidentIds) {
            List<Map<String, String>> workflowRows = processWorkflowForIncident(context, incidentId);

            // Check the condition
            if (workflowRows.size() >= 2 &&
                    "CLOSE".equalsIgnoreCase(workflowRows.get(0).get("action")) &&
                    "REJECT".equalsIgnoreCase(workflowRows.get(1).get("action"))) {

                // Delete the CLOSE action record using its unique ID
                String idToDelete = workflowRows.get(0).get("id");
                deleteWorkflowRecordById(context, idToDelete);
            }
        }
    }

    private List<String> getIncidentIds(Context context) throws Exception {
        List<String> incidentIds = new ArrayList<>();
        String sql = "SELECT DISTINCT incidentid FROM eg_incident_v2";

        try (PreparedStatement ps = context.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String incidentId = rs.getString("incidentid");
                incidentIds.add(incidentId);
            }
        }

        return incidentIds;
    }

    private List<Map<String, String>> processWorkflowForIncident(Context context, String incidentId) throws Exception {
        List<Map<String, String>> results = new ArrayList<>();

        String sql = "SELECT id, businessid, action, status " +
                "FROM eg_wf_processinstance_v2 " +
                "WHERE businessid = ? " +
                "ORDER BY lastmodifiedtime DESC";

        try (PreparedStatement ps = context.getConnection().prepareStatement(sql)) {
            ps.setString(1, incidentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    row.put("id", rs.getString("id"));
                    row.put("businessid", rs.getString("businessid"));
                    row.put("action", rs.getString("action"));
                    row.put("status", rs.getString("status"));
                    results.add(row);
                }
            }
        }

        return results;
    }

    private void deleteWorkflowRecordById(Context context, String id) throws Exception {
        String deleteSql = "DELETE FROM eg_wf_processinstance_v2 WHERE id = ?";

        try (PreparedStatement ps = context.getConnection().prepareStatement(deleteSql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }
}
