package org.egov.inbox.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.inbox.web.model.workflow.ProcessInstance;

import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {

	@JsonProperty("ProcessInstance")
	private ProcessInstance ProcessInstance;
	
	@JsonProperty("project")
	private Map<String,Object>	project;
	
	@JsonProperty("serviceObject")
	private Map<String,Object>	serviceObject;
}
