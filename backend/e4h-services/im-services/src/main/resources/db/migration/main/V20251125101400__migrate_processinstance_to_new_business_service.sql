with status_map as (
select * from (
	select ewbv.businessservice, state, ewsv.uuid as new_state_uuid from
	eg_wf_businessservice_v2 ewbv
	join eg_wf_state_v2 ewsv
	on ewbv.uuid = ewsv.businessserviceid
	where ewbv.tenantid = 'in'
	and ewbv.businessservice in ('Incident', 'Incident_Medium', 'Incident_High', 'Incident_Low')
) a full join (
	select ewbv.businessservice, ewbv.tenantid, state, ewsv.uuid as old_state_uuid from
	eg_wf_businessservice_v2 ewbv
	join eg_wf_state_v2 ewsv
	on ewbv.uuid = ewsv.businessserviceid
	where ewbv.tenantid != 'in'
	and ewbv.businessservice in ('Incident', 'Incident_Medium', 'Incident_High', 'Incident_Low')
) b on a.businessservice = b.businessservice and a.state is not distinct from b.state
	order by a.businessservice, a.state
)
update eg_wf_processinstance_v2
set status = sm.new_state_uuid
from status_map sm
where status = sm.old_state_uuid