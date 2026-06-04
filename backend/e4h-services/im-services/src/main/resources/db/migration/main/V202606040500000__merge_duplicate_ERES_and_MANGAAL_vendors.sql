BEGIN;

UPDATE public.eg_org
SET is_active = false
WHERE code IN ('IN-0029','IN-0037');

-- ERES

UPDATE eg_incident_v2
SET lastmodifiedby = 'd492f99b-56b2-4789-aaf7-21a0308604b4'
WHERE lastmodifiedby = 'c68a617c-dd21-4e8f-b42b-808bf847d441';

UPDATE eg_wf_processinstance_v2
SET assigner = 'd492f99b-56b2-4789-aaf7-21a0308604b4'
WHERE assigner = 'c68a617c-dd21-4e8f-b42b-808bf847d441';

UPDATE eg_wf_processinstance_v2
SET createdby = 'd492f99b-56b2-4789-aaf7-21a0308604b4'
WHERE createdby = 'c68a617c-dd21-4e8f-b42b-808bf847d441';

UPDATE eg_wf_processinstance_v2
SET lastmodifiedby = 'd492f99b-56b2-4789-aaf7-21a0308604b4'
WHERE lastmodifiedby = 'c68a617c-dd21-4e8f-b42b-808bf847d441';

UPDATE eg_wf_assignee_v2
SET lastmodifiedby = 'd492f99b-56b2-4789-aaf7-21a0308604b4'
WHERE lastmodifiedby = 'c68a617c-dd21-4e8f-b42b-808bf847d441';

UPDATE eg_wf_assignee_v2
SET createdby = 'd492f99b-56b2-4789-aaf7-21a0308604b4'
WHERE createdby = 'c68a617c-dd21-4e8f-b42b-808bf847d441';

UPDATE eg_wf_assignee_v2
SET assignee = 'd492f99b-56b2-4789-aaf7-21a0308604b4'
WHERE assignee = 'c68a617c-dd21-4e8f-b42b-808bf847d441';

UPDATE eg_hrms_jurisdiction
SET employeeid = 'd492f99b-56b2-4789-aaf7-21a0308604b4'
WHERE employeeid = 'c68a617c-dd21-4e8f-b42b-808bf847d441';

-- MANGAAL

UPDATE eg_incident_v2
SET lastmodifiedby = 'f85df0e9-bbd9-4cc4-b11c-4917fbdb3319'
WHERE lastmodifiedby = 'b4375516-dea3-4201-b6e8-603f12971196';

UPDATE eg_wf_processinstance_v2
SET assigner = 'f85df0e9-bbd9-4cc4-b11c-4917fbdb3319'
WHERE assigner = 'b4375516-dea3-4201-b6e8-603f12971196';

UPDATE eg_wf_processinstance_v2
SET createdby = 'f85df0e9-bbd9-4cc4-b11c-4917fbdb3319'
WHERE createdby = 'b4375516-dea3-4201-b6e8-603f12971196';

UPDATE eg_wf_processinstance_v2
SET lastmodifiedby = 'f85df0e9-bbd9-4cc4-b11c-4917fbdb3319'
WHERE lastmodifiedby = 'b4375516-dea3-4201-b6e8-603f12971196';

UPDATE eg_wf_assignee_v2
SET lastmodifiedby = 'f85df0e9-bbd9-4cc4-b11c-4917fbdb3319'
WHERE lastmodifiedby = 'b4375516-dea3-4201-b6e8-603f12971196';

UPDATE eg_wf_assignee_v2
SET createdby = 'f85df0e9-bbd9-4cc4-b11c-4917fbdb3319'
WHERE createdby = 'b4375516-dea3-4201-b6e8-603f12971196';

UPDATE eg_wf_assignee_v2
SET assignee = 'f85df0e9-bbd9-4cc4-b11c-4917fbdb3319'
WHERE assignee = 'b4375516-dea3-4201-b6e8-603f12971196';

UPDATE eg_hrms_jurisdiction
SET employeeid = 'f85df0e9-bbd9-4cc4-b11c-4917fbdb3319'
WHERE employeeid = 'b4375516-dea3-4201-b6e8-603f12971196';

COMMIT;

