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
import '../../router/app_router.dart';
import '../../utils/i18_key_constants.dart' as i18;

class CustomDrawer extends StatelessWidget {
  const CustomDrawer({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    // final appInitializationBloc = context.read<AppInitialization>();
    // final appConfig =
    //     (appInitializationBloc.state as Initialized).appConfig;
    // final languages = appConfig;
    // final localizationModulesList = appConfig;
    // final authBloc = context.read<AuthBloc>();
    // bool isDistributor = authBloc.state != const AuthState.unauthenticated()
    //     ? context.loggedInUserRoles
    //     .where(
    //       (role) => role.code == RolesType.distributor.toValue(),
    // )
    //     .toList()
    //     .isNotEmpty
    //     : false;

    // return BlocBuilder<AppInitialization, InitState>(
    //   builder: (context, state) {
    //     final actionMap = state.entityActionMapping;
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
                    // context.router.push(UserQRDetailsRoute());
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
                  context.router.replaceAll([const HomeRoute()]);
                },
                icon: Icons.home,
              ),
              // if (appInitializationBloc.state is Initialized) ...[
              //   SidebarItem(
              //     title: AppLocalizations.of(context).translate(
              //       i18.common.coreCommonlanguage,
              //     ),
              //     isSearchEnabled: false,
              //     icon: Icons.language,
              //     onPressed: () {},
              //     children: (localizationModulesList != null)
              //         ? buildLanguage(localizationModulesList, languages,
              //             context, appConfig)
              //         : null,
              //   )
              // ],
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
                      // context.router.push(ProfileRoute());
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
    //   },
    // );
  }
}

class LanguageButtonsWidget extends StatelessWidget {
  const LanguageButtonsWidget({
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        DigitButton(
          type: DigitButtonType.secondary,
          label: "ENGLISH",
          onPressed: () {},
          size: DigitButtonSize.large,
        ),
      ],
    );
  }
}
