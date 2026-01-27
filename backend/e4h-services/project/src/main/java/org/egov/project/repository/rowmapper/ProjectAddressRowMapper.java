package org.egov.project.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.project.Address;
import org.egov.common.models.project.AddressType;
import org.egov.common.models.project.Project;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProjectAddressRowMapper implements ResultSetExtractor<List<Project>> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<Project> extractData(ResultSet rs) throws SQLException, DataAccessException {

        Map<String, Project> projectMap = new LinkedHashMap<>();
        while (rs.next()) {
            String project_id = rs.getString("projectId");

            if (!projectMap.containsKey(project_id)) {
                projectMap.put(project_id, createProjectObj(rs));
            }
        }

        return new ArrayList<>(projectMap.values());
    }

    private Project createProjectObj(ResultSet rs) throws SQLException, DataAccessException {
        Address address = getAddressObjFromResultSet(rs);
        Project project = getProjectObjFromResultSet(rs, address);
        return project;
    }

    /* Builds Address Object from Result Set */
    private Address getAddressObjFromResultSet(ResultSet rs) throws SQLException {
        String address_id = rs.getString("addressId");
        String address_tenantId = rs.getString("address_tenantId");
        String address_projectId = rs.getString("address_projectId");
        String address_doorNo = rs.getString("address_doorNo");
        Double address_latitude = rs.getDouble("address_latitude");
        Double address_longitude = rs.getDouble("address_longitude");
        Double address_locationAccuracy = rs.getDouble("address_locationAccuracy");
        String address_type = rs.getString("address_type");
        String address_addressLine1 = rs.getString("address_addressLine1");
        String address_addressLine2 = rs.getString("address_addressLine2");
        String address_landmark = rs.getString("address_landmark");
        String address_city = rs.getString("address_city");
        String address_pinCode = rs.getString("address_pinCode");
        String address_buildingName = rs.getString("address_buildingName");
        String address_street = rs.getString("address_street");
        String address_boundaryType = rs.getString("address_boundaryType");
        String address_boundary = rs.getString("address_boundary");

        Address address = Address.builder()
                .id(address_id)
                .tenantId(address_tenantId)
                .doorNo(address_doorNo)
                .latitude(address_latitude)
                .longitude(address_longitude)
                .locationAccuracy(address_locationAccuracy)
                .type(AddressType.fromValue(address_type))
                .addressLine1(address_addressLine1)
                .addressLine2(address_addressLine2)
                .landmark(address_landmark)
                .city(address_city)
                .pincode(address_pinCode)
                .buildingName(address_buildingName)
                .street(address_street)
                .boundaryType(address_boundaryType)
                .boundary(address_boundary)
                .build();

        if (address_id == null) {
            return null;
        }

        return address;
    }

    /* Builds Project Object from Result Set and address */
    private Project getProjectObjFromResultSet(ResultSet rs, Address address) throws SQLException {
        ProjectBasicFields basicFields = extractProjectBasicFields(rs);
        ProjectDateFields dateFields = extractProjectDateFields(rs);
        AuditDetails auditDetails = buildProjectAuditDetails(rs);
        JsonNode additionalDetails = getAdditionalDetail("project_additionalDetails", rs);

        return Project.builder()
                .id(basicFields.getId())
                .tenantId(basicFields.getTenantId())
                .projectNumber(basicFields.getProjectNumber())
                .name(basicFields.getName())
                .projectType(basicFields.getProjectType())
                .projectTypeId(basicFields.getProjectTypeId())
                .projectSubType(basicFields.getProjectSubType())
                .department(basicFields.getDepartment())
                .description(basicFields.getDescription())
                .referenceID(basicFields.getReferenceId())
                .startDate(dateFields.getStartDate())
                .endDate(dateFields.getEndDate())
                .isTaskEnabled(basicFields.getIsTaskEnabled())
                .parent(basicFields.getParent())
                .projectHierarchy(basicFields.getProjectHierarchy())
                .additionalDetails(additionalDetails)
                .natureOfWork(basicFields.getNatureOfWork())
                .isDeleted(basicFields.getIsDeleted())
                .rowVersion(basicFields.getRowVersion())
                .address(address)
                .auditDetails(auditDetails)
                .build();
    }

    private ProjectBasicFields extractProjectBasicFields(ResultSet rs) throws SQLException {
        return new ProjectBasicFields(
                rs.getString("projectId"),
                rs.getString("project_tenantId"),
                rs.getString("project_projectNumber"),
                rs.getString("project_name"),
                rs.getString("project_projectType"),
                rs.getString("project_projectTypeId"),
                rs.getString("project_projectSubtype"),
                rs.getString("project_department"),
                rs.getString("project_description"),
                rs.getString("project_referenceId"),
                rs.getBoolean("project_isTaskEnabled"),
                rs.getString("project_projectHierarchy"),
                rs.getString("project_parent"),
                rs.getString("project_natureOfWork"),
                rs.getBoolean("project_isDeleted"),
                rs.getInt("project_rowVersion")
        );
    }

    private ProjectDateFields extractProjectDateFields(ResultSet rs) throws SQLException {
        return new ProjectDateFields(
                rs.getLong("project_startDate"),
                rs.getLong("project_endDate")
        );
    }

    private AuditDetails buildProjectAuditDetails(ResultSet rs) throws SQLException {
        return AuditDetails.builder()
                .createdBy(rs.getString("project_createdBy"))
                .createdTime(rs.getLong("project_createdTime"))
                .lastModifiedBy(rs.getString("project_lastModifiedBy"))
                .lastModifiedTime(rs.getLong("project_lastModifiedTime"))
                .build();
    }

    // Helper classes for project field extraction
    private static class ProjectBasicFields {
        private final String id;
        private final String tenantId;
        private final String projectNumber;
        private final String name;
        private final String projectType;
        private final String projectTypeId;
        private final String projectSubType;
        private final String department;
        private final String description;
        private final String referenceId;
        private final Boolean isTaskEnabled;
        private final String projectHierarchy;
        private final String parent;
        private final String natureOfWork;
        private final Boolean isDeleted;
        private final Integer rowVersion;

        public ProjectBasicFields(String id, String tenantId, String projectNumber, String name,
                                 String projectType, String projectTypeId, String projectSubType,
                                 String department, String description, String referenceId,
                                 Boolean isTaskEnabled, String projectHierarchy, String parent,
                                 String natureOfWork, Boolean isDeleted, Integer rowVersion) {
            this.id = id;
            this.tenantId = tenantId;
            this.projectNumber = projectNumber;
            this.name = name;
            this.projectType = projectType;
            this.projectTypeId = projectTypeId;
            this.projectSubType = projectSubType;
            this.department = department;
            this.description = description;
            this.referenceId = referenceId;
            this.isTaskEnabled = isTaskEnabled;
            this.projectHierarchy = projectHierarchy;
            this.parent = parent;
            this.natureOfWork = natureOfWork;
            this.isDeleted = isDeleted;
            this.rowVersion = rowVersion;
        }

        public String getId() { return id; }
        public String getTenantId() { return tenantId; }
        public String getProjectNumber() { return projectNumber; }
        public String getName() { return name; }
        public String getProjectType() { return projectType; }
        public String getProjectTypeId() { return projectTypeId; }
        public String getProjectSubType() { return projectSubType; }
        public String getDepartment() { return department; }
        public String getDescription() { return description; }
        public String getReferenceId() { return referenceId; }
        public Boolean getIsTaskEnabled() { return isTaskEnabled; }
        public String getProjectHierarchy() { return projectHierarchy; }
        public String getParent() { return parent; }
        public String getNatureOfWork() { return natureOfWork; }
        public Boolean getIsDeleted() { return isDeleted; }
        public Integer getRowVersion() { return rowVersion; }
    }

    private static class ProjectDateFields {
        private final Long startDate;
        private final Long endDate;

        public ProjectDateFields(Long startDate, Long endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public Long getStartDate() { return startDate; }
        public Long getEndDate() { return endDate; }
    }

    private JsonNode getAdditionalDetail(String columnName, ResultSet rs)    throws SQLException {
        JsonNode additionalDetails = null;
        try {
            PGobject obj = (PGobject) rs.getObject(columnName);
            if (obj != null) {
                additionalDetails = objectMapper.readTree(obj.getValue());
            }
        } catch (IOException e) {
            throw new CustomException("PARSING ERROR", "Failed to parse additionalDetail object");
        }
        if (additionalDetails == null || additionalDetails.isEmpty())
            additionalDetails = null;
        return additionalDetails;
    }
}
