"""The facility template and upload hold a caller to their jurisdiction.

Run from the service root: `.venv/bin/python -m unittest discover -s tests -t .` (standard library
plus the service's own dependencies; HTTP services are mocked).
"""
import io
import json
import unittest
from unittest import mock

import pandas as pd
from fastapi import FastAPI
from fastapi.testclient import TestClient

from app.api.endpoints import file_ingestion, template_generation
from app.api.endpoints.template_generation import router as template_router
from app.schemas.boundary import Boundary
from app.utils.caller_scope import CallerScope, CallerScopeError, UNRESTRICTED

KOPPAL = CallerScope(False, ("India_Karnataka_Koppal",))


def block(code):
    return Boundary(country="India", state="India_Karnataka", district=code.rsplit("_", 1)[0], block=code, code=code)


ALL_BLOCKS = [block("India_Karnataka_Koppal_Kushtagi"), block("India_Karnataka_Koppal_Yelburga"),
              block("India_Karnataka_Mysuru_Kote")]


class FacilityTemplateScopeTest(unittest.TestCase):

    def setUp(self):
        app = FastAPI()
        app.include_router(template_router, prefix="/template")
        self.client = TestClient(app)
        self.generated = []

        def capture(self_, output_path, facility_schema, boundary_data, vendor_data):
            self.generated.append([b.code for b in boundary_data])
            with open(output_path, "wb") as handle:
                handle.write(b"xlsx")

        patches = [
            mock.patch.object(template_generation.FacilityTemplateService, "get_all_boundaries",
                              return_value=list(ALL_BLOCKS)),
            mock.patch.object(template_generation.FacilityTemplateService, "get_all_vendor_codes", return_value=[]),
            mock.patch.object(template_generation.FacilityTemplateService, "generate_template_file", capture),
            mock.patch.object(template_generation, "MDMSClient"),
        ]
        for patch in patches:
            patch.start()
            self.addCleanup(patch.stop)

    def download(self, scope, boundary_codes=None, boundary_data=None):
        data = {"request_info": json.dumps({"authToken": "tok"})}
        if boundary_codes is not None:
            data["boundary_codes"] = json.dumps(boundary_codes)
        if boundary_data is not None:
            data["boundary_data"] = json.dumps(boundary_data)
        with mock.patch("app.decorators.rbac_validator.resolve_caller_scope", return_value=scope):
            return self.client.post("/template/facilityIngestion", data=data)

    def test_a_state_only_selection_is_cut_down_to_the_callers_district(self):
        response = self.download(KOPPAL, boundary_codes=["India_Karnataka"])

        self.assertEqual(response.status_code, 200)
        self.assertEqual(self.generated, [["India_Karnataka_Koppal_Kushtagi", "India_Karnataka_Koppal_Yelburga"]])

    def test_a_district_outside_the_callers_jurisdiction_is_refused(self):
        response = self.download(KOPPAL, boundary_codes=["India_Karnataka_Mysuru"])

        self.assertEqual(response.status_code, 403)
        self.assertEqual(self.generated, [])

    def test_no_selection_gives_the_callers_district_not_every_block(self):
        response = self.download(KOPPAL)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(self.generated[0]), 2)

    def test_the_older_boundary_tree_is_held_to_the_scope_too(self):
        tree = {"type": "country", "boundaryCode": "India", "children": [
            {"type": "state", "boundaryCode": "India_Karnataka", "children": [
                {"type": "district", "boundaryCode": "India_Karnataka_Mysuru", "children": [
                    {"type": "block", "boundaryCode": "India_Karnataka_Mysuru_Kote", "children": []}]}]}]}

        response = self.download(KOPPAL, boundary_data=tree)

        self.assertEqual(response.status_code, 403)
        self.assertEqual(self.generated, [])

    def test_an_unrestricted_caller_gets_everything_selected(self):
        response = self.download(UNRESTRICTED, boundary_codes=["India_Karnataka"])

        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(self.generated[0]), 3)

    def test_a_caller_who_cannot_be_checked_gets_no_template(self):
        for status in (401, 403, 503):
            with self.subTest(status):
                data = {"request_info": json.dumps({"authToken": "tok"}), "boundary_codes": json.dumps(["India_Karnataka"])}
                with mock.patch("app.decorators.rbac_validator.resolve_caller_scope",
                                side_effect=CallerScopeError(status, "no")):
                    response = self.client.post("/template/facilityIngestion", data=data)
                self.assertEqual(response.status_code, status)
        self.assertEqual(self.generated, [])


def sheet(*rows):
    return pd.DataFrame([{"State (Mandatory)": "Karnataka", "District (Mandatory)": district,
                          "Block (Mandatory)": blk, "Boundary Code (Mandatory)": code,
                          "status": "", "error": ""} for district, blk, code in rows])


class FacilityRowScopeTest(unittest.TestCase):

    def test_validation_fails_only_the_rows_outside_the_jurisdiction(self):
        df = sheet(("Koppal", "Kushtagi", "India_Karnataka_Koppal_Kushtagi"),
                   ("Mysuru", "Kote", "India_Karnataka_Mysuru_Kote"))

        file_ingestion._fail_rows_outside_jurisdiction(df, KOPPAL)

        self.assertEqual(list(df["status"]), ["", "FAILED"])
        self.assertIn("outside your jurisdiction", df.at[1, "error"])
        self.assertEqual(df.at[1, "Boundary Code (Mandatory)"], "")
        self.assertEqual(df.at[0, "Boundary Code (Mandatory)"], "India_Karnataka_Koppal_Kushtagi")

    def test_validation_leaves_an_unrestricted_callers_sheet_alone(self):
        df = sheet(("Mysuru", "Kote", "India_Karnataka_Mysuru_Kote"))

        file_ingestion._fail_rows_outside_jurisdiction(df, UNRESTRICTED)

        self.assertEqual(list(df["status"]), [""])

    def create(self, df, scope):
        client = mock.Mock()
        patches = {
            "collect_identifier_conflicts": {},
            "collect_hfr_nin_errors_for_row": [],
            "collect_anganwadi_poc_username_errors_for_row": [],
            "collect_coordinate_errors_for_row": [],
            "resolve_mapped_vendor_for_facility_row": {},
            "create_facility_payload": {"facilities": [{"name": "f"}]},
            "_apply_facility_create_response": None,
        }
        with mock.patch.multiple(file_ingestion, **{k: mock.Mock(return_value=v) for k, v in patches.items()}):
            file_ingestion._bulk_create_facilities_for_ingestion(
                df=df, facility_client=client, org_client=None, request_info=mock.Mock(),
                facility_schema=[], are_facilities_onm_ready=False, vendor_mapping_cache={}, caller_scope=scope)
        return client

    def test_create_does_not_send_a_row_outside_the_jurisdiction(self):
        df = sheet(("Koppal", "Kushtagi", "India_Karnataka_Koppal_Kushtagi"),
                   ("Mysuru", "Kote", "India_Karnataka_Mysuru_Kote"))

        client = self.create(df, KOPPAL)

        self.assertEqual(df.at[1, "status"], "failed")
        self.assertIn("outside your jurisdiction", df.at[1, "error"])
        sent = client.create_facility.call_args.args[0]["facilities"]
        self.assertEqual(len(sent), 1, "only the in-scope row is created")

    def test_create_sends_nothing_when_every_row_is_outside(self):
        df = sheet(("Mysuru", "Kote", "India_Karnataka_Mysuru_Kote"))

        client = self.create(df, KOPPAL)

        self.assertEqual(list(df["status"]), ["failed"])
        client.create_facility.assert_not_called()

    def test_create_for_an_unrestricted_caller_sends_every_row(self):
        df = sheet(("Koppal", "Kushtagi", "India_Karnataka_Koppal_Kushtagi"),
                   ("Mysuru", "Kote", "India_Karnataka_Mysuru_Kote"))

        client = self.create(df, UNRESTRICTED)

        self.assertEqual(len(client.create_facility.call_args.args[0]["facilities"]), 2)


if __name__ == "__main__":
    unittest.main()
