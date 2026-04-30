import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:selco/utils/asset_images.dart';

import '../blocs/auth/authbloc.dart';
import '../blocs/role_selection/role_selection.dart';
import '../blocs/user_type/user_type.dart';
import '../model/response/responsemodel.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/role_login_resolver.dart';

@RoutePage()
class RoleSelectionPage extends StatefulWidget {
  const RoleSelectionPage({super.key});

  @override
  State<RoleSelectionPage> createState() => _RoleSelectionPageState();
}

class _RoleSelectionPageState extends State<RoleSelectionPage> {
  @override
  Widget build(BuildContext context) {
    final List<Roles> roles = context.read<AuthBloc>().state.maybeWhen(
          authenticated: (_, __, userRequest) => userRequest?.roles ?? const [],
          orElse: () => const [],
        );

    return BlocProvider(
      create: (_) => RoleSelectionBloc(roles: roles),
      child: const _RoleSelectionView(),
    );
  }
}

class _RoleSelectionView extends StatelessWidget {
  const _RoleSelectionView();

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    return Scaffold(
      body: ScrollableContent(
        enableFixedDigitButton: true,
        backgroundColor: theme.colorScheme.secondary,
        footer: BlocBuilder<RoleSelectionBloc, RoleSelectionState>(
          builder: (context, state) {
            return DigitCard(
              margin: const EdgeInsets.only(top: spacer2),
              cardType: CardType.secondary,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: state.availableRoles.map((role) {
                    final isSelected = state.selectedRole == role;
                    return Expanded(
                      child: Padding(
                        padding: const EdgeInsets.symmetric(
                          horizontal: spacer1 / 2,
                        ),
                        child: DigitButton(
                          label: role.label,
                          onPressed: () {
                            context.read<RoleSelectionBloc>().add(
                                  RoleSelectionRoleSelected(role),
                                );
                          },
                          type: isSelected
                              ? DigitButtonType.primary
                              : DigitButtonType.secondary,
                          size: DigitButtonSize.large,
                        ),
                      ),
                    );
                  }).toList(),
                ),
                SizedBox(
                  width: context.width,
                  child: DigitButton(
                    label: context.translate(i18.common.coreCommonProceed),
                    isDisabled: !state.canProceed,
                    onPressed: () {
                      final selectedRole = state.selectedRole;
                      if (selectedRole == null) return;

                      context.read<UserTypeBloc>().add(
                            UserTypeEvent.typeSelected(
                              selectedRole.userTypeValue,
                            ),
                          );

                      context.router.replace(
                        selectedRole.isAmc
                            ? const AmcHomeRoute()
                            : const HomeRoute(),
                      );
                    },
                    type: DigitButtonType.primary,
                    size: DigitButtonSize.large,
                  ),
                ),
              ],
            );
          },
        ),
        children: [
          SizedBox(
            height: context.height * 0.6,
            child: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Image.asset(
                    color: theme.colorTheme.paper.primary,
                    AssetImages.APP_LOGO,
                    height: spacer12 * 2,
                    width: spacer12 * 2,
                  ),
                  Text(
                    "Select Role",
                    style: textTheme.headingM.copyWith(
                      color: theme.colorTheme.primary.primary1,
                    ),
                  )
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
