import 'package:digit_ui_components/digit_components.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../lib/blocs/auth/authbloc.dart';
import '../../lib/blocs/app_init/app_init.dart';
import '../../lib/blocs/user_type/user_type.dart';
import '../../lib/model/response/responsemodel.dart';
import '../../lib/pages/login.dart';
import '../../lib/router/app_router.dart';
import 'report_bookmarks_test.dart' as support;

class SessionAuth extends Bloc<AuthEvent, AuthState> implements AuthBloc {
  SessionAuth(super.initialState);
  void restore(AuthState state) => emit(state);
  @override
  dynamic noSuchMethod(Invocation invocation) => throw UnimplementedError();
}

class RecordingRouter extends AppRouter {
  final destinations = <PageRouteInfo>[];
  @override
  Future<T?> replace<T extends Object?>(PageRouteInfo route,
      {OnNavigationFailure? onFailure}) async {
    destinations.add(route);
    return null;
  }
}

void main() {
  for (final entry in {
    'installation': <String>[],
    'AMC': ['AMC_FIELD_STAFF'],
    'assessment': ['ENUMERATOR'],
    'role selection': ['AMC_FIELD_STAFF', 'ENUMERATOR'],
  }.entries) {
    for (final restoredBeforeOpen in [true, false]) {
      testWidgets(
          '${entry.key} session restored before open: $restoredBeforeOpen',
          (tester) async {
        FlutterSecureStorage.setMockInitialValues({});
        final authenticated = AuthState.authenticated(
            accesstoken: 'saved-token',
            refreshtoken: 'saved-refresh',
            userRequest: support.user.copyWith(
                roles: entry.value
                    .map((code) =>
                        Roles(code: code, name: code, tenantId: 'tenant'))
                    .toList()));
        final auth = SessionAuth(restoredBeforeOpen
            ? authenticated
            : const AuthState.unauthenticated());
        final router = RecordingRouter();
        final type = UserTypeBloc();
        final init = support.FakeInit();
        addTearDown(() async {
          await auth.close();
          await type.close();
          await init.close();
          router.dispose();
        });
        await tester.pumpWidget(MultiBlocProvider(
          providers: [
            BlocProvider<AuthBloc>.value(value: auth),
            BlocProvider<UserTypeBloc>.value(value: type),
            BlocProvider<AppInitialization>.value(value: init),
          ],
          child: MaterialApp(
              theme: DigitTheme.instance.mobileTheme,
              home: StackRouterScope(
                  controller: router, stateHash: 0, child: const LoginPage())),
        ));
        await tester.pumpAndSettle();
        if (!restoredBeforeOpen) {
          expect(router.destinations, isEmpty);
          auth.restore(authenticated);
          await tester.pumpAndSettle();
        }
        expect(router.destinations, hasLength(1));
        final destination = router.destinations.single;
        expect(destination.routeName, AuthenticatedRouteWrapper.name);
        final expected = {
          'AMC': AmcHomeRoute.name,
          'assessment': AssessmentHomeRoute.name,
          'role selection': RoleSelectionRoute.name,
        }[entry.key];
        if (expected != null) {
          expect(destination.initialChildren!.single.routeName, expected);
        } else {
          expect(destination.initialChildren, isNull);
        }
        auth.restore(const AuthState.loading());
        auth.restore(authenticated);
        await tester.pumpAndSettle();
        expect(router.destinations, hasLength(1));
      });
    }
  }
}
