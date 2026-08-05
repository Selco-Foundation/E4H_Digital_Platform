import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/auth/authbloc.dart';
import '../blocs/role_selection/role_selection.dart';
import '../blocs/user_type/user_type.dart';
import '../model/response/responsemodel.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/role_login_resolver.dart';
import '../widgets/home/home_item_card.dart';

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
    final screenWidth = context.width;

    final theme = Theme.of(context);
    return Scaffold(
      backgroundColor: DigitTheme.instance.colorScheme.surface,
      body: Padding(
        padding: const EdgeInsets.symmetric(horizontal: spacer2),
        child: ScrollableContent(
          backgroundColor: theme.colorTheme.generic.background,
          footer: const PoweredByDigit(version: ''),
          slivers: [
            BlocBuilder<RoleSelectionBloc, RoleSelectionState>(
              builder: (context, state) {
                return SliverPadding(
                  padding: const EdgeInsets.only(top: spacer6),
                  sliver: SliverGrid(
                    delegate: SliverChildBuilderDelegate(
                      (context, index) {
                        final role = state.availableRoles[index];
                        return HomeItemCard(
                          icon: _iconForRole(role),
                          label: role.moduleLabel,
                          labelPadding: role.isAssessment
                              ? const EdgeInsets.symmetric(
                                  horizontal: spacer2,
                                )
                              : null,
                          fitLabelOnOneLine: role.isAssessment,
                          onPressed: () {
                            context.read<RoleSelectionBloc>().add(
                                  RoleSelectionRoleSelected(role),
                                );
                            context.read<UserTypeBloc>().add(
                                  UserTypeEvent.typeSelected(
                                    role.userTypeValue,
                                  ),
                                );

                            context.router.replace(
                              role.isAssessment
                                  ? const AssessmentHomeRoute()
                                  : role.isAmc
                                      ? const AmcHomeRoute()
                                      : const HomeRoute(),
                            );
                          },
                        );
                      },
                      childCount: state.availableRoles.length,
                    ),
                    gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: 2,
                      mainAxisSpacing: spacer4,
                      childAspectRatio:
                          (screenWidth / 2) / (170 * (screenWidth / 375)),
                    ),
                  ),
                );
              },
            )
          ],
          children: const [],
        ),
      ),
    );
  }

  IconData _iconForRole(RoleSelectionOption role) {
    switch (role) {
      case RoleSelectionOption.staff:
        return Icons.person_outline;
      case RoleSelectionOption.supervisor:
        return Icons.supervisor_account_outlined;
      case RoleSelectionOption.amc:
        return Icons.home_repair_service_outlined;
      case RoleSelectionOption.assessment:
        return Icons.business_center_outlined;
    }
  }
}
