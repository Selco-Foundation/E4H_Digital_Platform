import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/helper_widget/digit_profile.dart';
import 'package:digit_ui_components/widgets/molecules/hamburger.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:qr_flutter/qr_flutter.dart';

import '../../blocs/auth/authbloc.dart';
import '../../blocs/localization/app_localization.dart';
import '../../blocs/user_type/user_type.dart';
import '../../model/response/responsemodel.dart';
import '../../router/app_router.dart';
import '../../utils/i18_key_constants.dart' as i18;
import '../../utils/role_login_resolver.dart';
import '../../utils/utils.dart';
import '../privacy_policy/policy_dialog_launcher.dart';

class CustomDrawer extends StatelessWidget {
  const CustomDrawer({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<AuthBloc, AuthState>(
      builder: (context, state) {
        return Padding(
          padding: const EdgeInsets.only(top: kToolbarHeight),
          child: SideBar(
            profile: state.maybeMap(
              authenticated: (value) => ProfileWidget(
                leading: GestureDetector(
                  onTap: () {
                    Navigator.of(context, rootNavigator: true).pop();
                  },
                  child: QrImageView(
                    data: value.userRequest!.uuid,
                    version: QrVersions.auto,
                    size: 150.0,
                  ),
                ),
                title: value.userRequest!.name.toString(),
                description: value.userRequest!.mobileNumber.toString(),
              ),
              orElse: () => null,
            ),
            sidebarItems: [
              SidebarItem(
                title: AppLocalizations.of(context).translate(
                  i18.common.coreCommonHome,
                ),
                onPressed: () {
                  Navigator.of(context, rootNavigator: true).pop();

                  final List<Roles> roles = state.maybeWhen(
                    authenticated: (_, __, userRequest) =>
                        userRequest?.roles ?? const [],
                    orElse: () => const [],
                  );
                  final resolution = RoleLoginResolver.resolveRoles(roles);

                  if (resolution.requiresSelection) {
                    context.router.replaceAll([const RoleSelectionRoute()]);
                    return;
                  }

                  final userType =
                      resolution.directUserType ?? USER_TYPES.FIELD_STAFF;
                  context.read<UserTypeBloc>().add(
                        UserTypeEvent.typeSelected(
                          userType.name.toLowerCase(),
                        ),
                      );

                  if (userType == USER_TYPES.AMC) {
                    context.router.replaceAll([const AmcHomeRoute()]);
                  } else if (userType == USER_TYPES.ASSESSOR) {
                    context.router.replaceAll([const AssessmentHomeRoute()]);
                  } else {
                    context.router.replaceAll([const HomeRoute()]);
                  }
                },
                icon: Icons.home,
              ),
              SidebarItem(
                title: AppLocalizations.of(context).translate(
                  i18.common.coreCommonProfile,
                ),
                icon: Icons.person,
                onPressed: () async {
                  final connectivityResult =
                      await (Connectivity().checkConnectivity());
                  final isOnline = connectivityResult.firstOrNull ==
                          ConnectivityResult.wifi ||
                      connectivityResult.firstOrNull ==
                          ConnectivityResult.mobile;

                  if (isOnline) {
                    if (context.mounted) {
                      Navigator.of(context, rootNavigator: true).pop();
                    }
                  } else {
                    if (context.mounted) {
                      showCustomPopup(
                        context: context,
                        builder: (ctx) => Popup(
                          title: AppLocalizations.of(context).translate(
                            i18.common.connectionLabel,
                          ),
                          description: AppLocalizations.of(context).translate(
                            i18.common.connectionContent,
                          ),
                          actions: [
                            DigitButton(
                                label: AppLocalizations.of(context).translate(
                                  i18.common.coreCommonOk,
                                ),
                                onPressed: () =>
                                    Navigator.of(context, rootNavigator: true)
                                        .pop(),
                                type: DigitButtonType.primary,
                                size: DigitButtonSize.large)
                          ],
                        ),
                      );
                    }
                  }
                },
              ),
              SidebarItem(
                title: AppLocalizations.of(context).translate(
                  i18.login.privacyPolicy,
                ),
                onPressed: () {
                  Navigator.of(context, rootNavigator: true).pop();
                  showPrivacyPolicy(context);
                },
                icon: Icons.privacy_tip_outlined,
              ),
              SidebarItem(
                title: AppLocalizations.of(context).translate(
                  i18.login.termsAndConditions,
                ),
                onPressed: () {
                  Navigator.of(context, rootNavigator: true).pop();
                  showTermsAndConditions(context);
                },
                icon: Icons.policy_outlined,
              ),
            ],
            logOutDigitButtonLabel: AppLocalizations.of(context)
                .translate(i18.common.coreCommonLogout),
            onLogOut: () {
              context.read<AuthBloc>().add(const AuthEvent.logout());
              AutoRouter.of(context)
                  .replace(const UnauthenticatedRouteWrapper());
            },
            footer: const PoweredByDigit(
              version: '',
            ),
          ),
        );
      },
    );
  }
}
