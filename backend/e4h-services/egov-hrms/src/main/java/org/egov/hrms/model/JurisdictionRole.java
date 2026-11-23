package org.egov.hrms.model;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * A role the employee holds at one specific jurisdiction, rather than across the whole
 * employee record. Carries the same fields a caller sends in user.roles, so the client can
 * pass the object it already has; the extra fields are set by the server.
 *
 * A jurisdiction with no roles applies to every role the employee holds.
 */
@Builder
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"auditDetails"})
@Getter
@NoArgsConstructor
@Setter
@ToString
public class JurisdictionRole {

	/** Server-set. */
	private String id;

	/** Server-set from the parent jurisdiction. */
	private String jurisdictionId;

	@NotNull
	@Size(min=2, max=100)
	private String code;

	/** Not persisted. Filled on search from the employee's user.roles entry with the same code. */
	@Size(min=2, max=100)
	private String name;

	@Size(max=256)
	private String description;

	@Size(max = 256)
	private String tenantId;

	private AuditDetails auditDetails;

}
