"""Whose boundaries a bulk facility upload may touch.

Run from the service root: `.venv/bin/python -m unittest discover -s tests` (standard library only).
"""
import os
import unittest
from unittest import mock

from app.schemas.boundary import Boundary
from app.schemas.request_info import RequestInfo
from app.utils.caller_scope import (
    CallerScope,
    CallerScopeError,
    UNRESTRICTED,
    is_system_caller,
    resolve_caller_scope,
    scope_from_jurisdictions,
)


def grant(boundary, boundary_type, roles=None, active=True):
    return {"boundary": boundary, "boundaryType": boundary_type, "isActive": active,
            "roles": [{"code": r} for r in (roles or [])]}


def block(code):
    return Boundary(country="India", state="India_Karnataka", district=code.rsplit("_", 1)[0], block=code, code=code)


BLOCKS = [block("India_Karnataka_Koppal_Kushtagi"), block("India_Karnataka_Koppal_Yelburga"),
          block("India_Karnataka_Koppal2_Other"), block("India_Karnataka_Mysuru_Kote")]


class ScopeFromJurisdictionsTest(unittest.TestCase):

    def test_a_district_grant_covers_its_blocks_and_not_a_lookalike(self):
        scope = scope_from_jurisdictions([grant("India_Karnataka_Koppal", "District")])

        self.assertEqual([b.code for b in scope.filter_blocks(BLOCKS)],
                         ["India_Karnataka_Koppal_Kushtagi", "India_Karnataka_Koppal_Yelburga"])
        self.assertFalse(scope.allows("India_Karnataka_Koppal2_Other"))

    def test_a_state_selection_is_cut_down_to_the_callers_district(self):
        scope = scope_from_jurisdictions([grant("India_Karnataka_Koppal", "District")])
        requested_state_blocks = [b for b in BLOCKS if b.state == "India_Karnataka"]

        self.assertEqual(len(scope.filter_blocks(requested_state_blocks)), 2)

    def test_a_country_grant_is_unrestricted(self):
        self.assertTrue(scope_from_jurisdictions([grant("India", "Country")]).unrestricted)

    def test_a_grant_for_other_roles_does_not_count_but_an_unscoped_one_does(self):
        scope = scope_from_jurisdictions([
            grant("India_Karnataka_Mysuru", "District", roles=["PROJECT_MANAGER"]),
            grant("India_Karnataka_Koppal", "District"),
            grant("India_Karnataka_Hassan", "District", roles=["FACILITY_ADMIN"]),
        ])

        self.assertEqual(set(scope.codes), {"India_Karnataka_Koppal", "India_Karnataka_Hassan"})

    def test_inactive_and_blank_grants_are_ignored(self):
        scope = scope_from_jurisdictions([grant("India_Karnataka_Mysuru", "District", active=False),
                                          grant("", "District"), None,
                                          grant("India_Karnataka_Koppal", "District")])

        self.assertEqual(scope.codes, ("India_Karnataka_Koppal",))

    def test_no_usable_grant_is_refused_not_unrestricted(self):
        for grants in ([], None, [grant("India_Karnataka_Mysuru", "District", roles=["PROJECT_MANAGER"])]):
            with self.assertRaises(CallerScopeError) as raised:
                scope_from_jurisdictions(grants)
            self.assertEqual(raised.exception.status_code, 403)

    def test_a_block_grant_covers_only_that_block(self):
        scope = scope_from_jurisdictions([grant("India_Karnataka_Koppal_Kushtagi", "Block")])

        self.assertEqual([b.code for b in scope.filter_blocks(BLOCKS)], ["India_Karnataka_Koppal_Kushtagi"])


class CallerScopeTest(unittest.TestCase):

    def test_unrestricted_allows_anything_and_keeps_every_block(self):
        self.assertTrue(UNRESTRICTED.allows("anything"))
        self.assertEqual(UNRESTRICTED.filter_blocks(BLOCKS), BLOCKS)

    def test_a_blank_code_is_never_allowed_when_scoped(self):
        self.assertFalse(CallerScope(False, ("India_Karnataka_Koppal",)).allows(""))
        self.assertFalse(CallerScope(False, ("India_Karnataka_Koppal",)).allows(None))


class SystemCallerTest(unittest.TestCase):

    def test_both_the_type_and_the_role_are_required(self):
        self.assertTrue(is_system_caller({"type": "SYSTEM", "roles": [{"code": "SYSTEM_USER"}]}))
        self.assertFalse(is_system_caller({"type": "SYSTEM", "roles": [{"code": "FACILITY_ADMIN"}]}))
        self.assertFalse(is_system_caller({"type": "EMPLOYEE", "roles": [{"code": "SYSTEM_USER"}]}))
        self.assertFalse(is_system_caller(None))


class ResolveCallerScopeTest(unittest.TestCase):

    ENV = {"USER_SERVICE_URL": "http://user", "HRMS_SERVICE_URL": "http://hrms"}

    def request_info(self, token="tok", claimed_uuid="spoofed-uuid"):
        return RequestInfo(**{"authToken": token, "userInfo": {"uuid": claimed_uuid,
                                                               "roles": [{"code": "FACILITY_ADMIN", "name": "x"}]}})

    def resolve(self, user, employee=None, user_error=None, hrms_error=None, info=None):
        with mock.patch.dict(os.environ, self.ENV), \
                mock.patch("app.utils.caller_scope.UserServiceClient") as users, \
                mock.patch("app.utils.caller_scope.HRMSServiceClient") as hrms:
            users.return_value.get_user_by_token.return_value = user
            users.return_value.get_user_by_token.side_effect = user_error
            hrms.return_value.get_employee_by_uuid.return_value = employee
            hrms.return_value.get_employee_by_uuid.side_effect = hrms_error
            try:
                return resolve_caller_scope(info or self.request_info()), hrms
            except CallerScopeError as error:
                return error, hrms

    def test_the_caller_is_the_token_user_not_the_one_the_request_claims(self):
        user = {"uuid": "real-uuid", "type": "EMPLOYEE", "roles": [{"code": "FACILITY_ADMIN"}]}
        employee = {"jurisdictions": [grant("India_Karnataka_Koppal", "District")]}

        scope, hrms = self.resolve(user, employee)

        self.assertEqual(scope.codes, ("India_Karnataka_Koppal",))
        self.assertEqual(hrms.return_value.get_employee_by_uuid.call_args.args[0], "real-uuid")

    def test_a_system_caller_is_unrestricted_without_reading_jurisdictions(self):
        scope, hrms = self.resolve({"uuid": "u", "type": "SYSTEM", "roles": [{"code": "SYSTEM_USER"}]})

        self.assertTrue(scope.unrestricted)
        hrms.return_value.get_employee_by_uuid.assert_not_called()

    def test_failures_are_refused(self):
        user = {"uuid": "u", "type": "EMPLOYEE", "roles": [{"code": "FACILITY_ADMIN"}]}
        cases = {
            "no token": (self.resolve(user, info=self.request_info(token=None))[0], 401),
            "invalid token": (self.resolve(None)[0], 401),
            "user service down": (self.resolve(None, user_error=RuntimeError("down"))[0], 503),
            "not a facility admin": (self.resolve({"uuid": "u", "type": "EMPLOYEE", "roles": [{"code": "PROJECT_MANAGER"}]})[0], 403),
            "hrms down": (self.resolve(user, hrms_error=RuntimeError("down"))[0], 503),
            "no employee record": (self.resolve(user, employee=None)[0], 403),
            "no jurisdictions": (self.resolve(user, employee={"jurisdictions": []})[0], 403),
        }
        for name, (error, status) in cases.items():
            with self.subTest(name):
                self.assertIsInstance(error, CallerScopeError)
                self.assertEqual(error.status_code, status)

    def test_missing_service_urls_are_refused(self):
        with mock.patch.dict(os.environ, {}, clear=True):
            with self.assertRaises(CallerScopeError) as raised:
                resolve_caller_scope(self.request_info())
        self.assertEqual(raised.exception.status_code, 503)


if __name__ == "__main__":
    unittest.main()
