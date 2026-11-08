-- Backfill project_staff so that removing the PROJECT_MANAGER bypass from project search does
-- not hide projects from the people who have been working on them.
--
-- Until now a project manager saw every project in the tenant, because ProjectAddressQueryBuilder
-- skipped the project_staff join for that role. With the join applied to everyone, a project with
-- no staff rows is invisible to everyone - and nothing has ever written a row for a project's
-- creator. This fills that in from what the database already records.
--
-- Four sources, deduplicated. A user present in several of them gets exactly one row:
--
--   1. project.createdby          - whoever created it
--   2. project.lastmodifiedby     - the most recent editor (a single overwritten column, so it
--                                   names one person, not the history)
--   3. project_facility           - createdby and lastmodifiedby, by projectid
--   4. eg_wf_processinstance_v2   - createdby of every workflow transition on the project
--
-- Source 4 belongs to egov-workflow-v2, not to this service. It is read only if the table is
-- visible from this database, so the migration still applies where the two are kept apart. Match
-- is on businessid alone: project ids are random UUIDs, and the businessservice value
-- FACILITY_INSTALLATION is shared with field-planner and field-planner-activity, so filtering on
-- it would narrow nothing while implying otherwise.
--
-- project_facility rows are taken regardless of isdeleted. Someone who attached a facility that
-- was later removed still worked on the project, and the cost of including them is that they can
-- see it - which is the point. Expect some rows to carry a system or ingestion uuid, since bulk
-- upload writes that table.
--
-- Safe to re-run: every insert skips pairs that already have a live row.

-- 1. Collapse any pre-existing duplicate (projectid, staffid) pairs before the unique index can be
--    added. Soft-deleted rather than removed - the duplicate is redundant, not wrong, and the
--    oldest row (lowest id on a tie) is kept so the surviving audit trail is the original one.
UPDATE project_staff ps
   SET isdeleted = true,
       lastmodifiedby = 'DATA_MIGRATION',
       lastmodifiedtime = (extract(epoch from now()) * 1000)::bigint
 WHERE (ps.isdeleted IS NULL OR ps.isdeleted = false)
   AND EXISTS (
        SELECT 1
          FROM project_staff keep
         WHERE keep.projectid = ps.projectid
           AND keep.staffid = ps.staffid
           AND (keep.isdeleted IS NULL OR keep.isdeleted = false)
           AND (coalesce(keep.createdtime, 0) < coalesce(ps.createdtime, 0)
                OR (coalesce(keep.createdtime, 0) = coalesce(ps.createdtime, 0)
                    AND keep.id < ps.id))
   );

-- 2. Backfill from the tables this service owns.
INSERT INTO project_staff (id, tenantid, projectid, staffid, createdby, lastmodifiedby,
                           createdtime, lastmodifiedtime, rowversion, isdeleted)
SELECT gen_random_uuid()::text,
       s.tenantid,
       s.projectid,
       s.staffid,
       'DATA_MIGRATION',
       'DATA_MIGRATION',
       (extract(epoch from now()) * 1000)::bigint,
       (extract(epoch from now()) * 1000)::bigint,
       1,
       false
  FROM (
        SELECT DISTINCT p.id AS projectid, p.tenantid, u.staffid
          FROM project p
          CROSS JOIN LATERAL (VALUES (p.createdby), (p.lastmodifiedby)) AS u(staffid)
         WHERE nullif(trim(u.staffid), '') IS NOT NULL
         UNION
        SELECT DISTINCT p.id, p.tenantid, f.staffid
          FROM project p
          JOIN project_facility pf ON pf.projectid = p.id
          CROSS JOIN LATERAL (VALUES (pf.createdby), (pf.lastmodifiedby)) AS f(staffid)
         WHERE nullif(trim(f.staffid), '') IS NOT NULL
  ) s
 WHERE NOT EXISTS (
        SELECT 1
          FROM project_staff ps
         WHERE ps.projectid = s.projectid
           AND ps.staffid = s.staffid
           AND (ps.isdeleted IS NULL OR ps.isdeleted = false)
   );

-- 3. Backfill from the workflow history, when it is reachable from here. Running after step 2
--    means its NOT EXISTS also sees the rows that step inserted, so the two cannot overlap.
DO $$
BEGIN
    IF to_regclass('public.eg_wf_processinstance_v2') IS NULL THEN
        RAISE NOTICE 'eg_wf_processinstance_v2 not visible from this database - skipping the workflow-history source. Project staff was backfilled from project and project_facility only.';
        RETURN;
    END IF;

    INSERT INTO project_staff (id, tenantid, projectid, staffid, createdby, lastmodifiedby,
                               createdtime, lastmodifiedtime, rowversion, isdeleted)
    SELECT gen_random_uuid()::text,
           s.tenantid,
           s.projectid,
           s.staffid,
           'DATA_MIGRATION',
           'DATA_MIGRATION',
           (extract(epoch from now()) * 1000)::bigint,
           (extract(epoch from now()) * 1000)::bigint,
           1,
           false
      FROM (
            SELECT DISTINCT p.id AS projectid, p.tenantid, w.createdby AS staffid
              FROM project p
              JOIN eg_wf_processinstance_v2 w ON w.businessid = p.id
             WHERE nullif(trim(w.createdby), '') IS NOT NULL
      ) s
     WHERE NOT EXISTS (
            SELECT 1
              FROM project_staff ps
             WHERE ps.projectid = s.projectid
               AND ps.staffid = s.staffid
               AND (ps.isdeleted IS NULL OR ps.isdeleted = false)
       );
END
$$;

-- 4. Stop duplicates coming back. PsUniqueCombinationValidator checks this in the application, but
--    creates are persisted asynchronously, so two concurrent requests can both validate before
--    either is written. A duplicate matters because the project count query counts rows over this
--    join: one extra row inflates the total and paginates into empty pages.
--
--    Partial, so that soft-deleted history does not block re-staffing someone who was removed.
CREATE UNIQUE INDEX IF NOT EXISTS uk_project_staff_project_user_active
    ON project_staff (projectid, staffid)
 WHERE isdeleted IS NULL OR isdeleted = false;
