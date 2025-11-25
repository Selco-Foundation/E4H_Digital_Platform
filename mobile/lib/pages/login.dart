import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:reactive_forms/reactive_forms.dart';
import 'package:recase/recase.dart';
import 'package:selco/utils/utils.dart';

import '../blocs/auth/authbloc.dart';
import '../blocs/user_type/user_type.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/navigation/navbar.dart';

@RoutePage()
class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  var passwordVisible = false;
  bool isPrivacyEnabled = false;
  static const _userId = 'userId';
  static const _password = 'password';

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      appBar: const Navbar(showMenu: false, showLeading: false),
      body: ScrollableContent(
        backgroundColor: theme.colorTheme.generic.background,
        footer: const Padding(
          padding: EdgeInsets.only(bottom: spacer2),
          child: PoweredByDigit(
            version: '',
          ),
        ),
        children: [
          ReactiveFormBuilder(
            form: buildForm,
            builder: (context, form, child) {
              return DigitCard(
                  margin: const EdgeInsets.all(spacer2),
                  children: [
                    Text(
                      context.translate(i18.common.coreCommonLogin).headerCase,
                      style: textTheme.headingXl.copyWith(
                        color: theme.colorTheme.primary.primary2,
                      ),
                    ),
                    ReactiveWrapperField(
                      formControlName: _userId,
                      validationMessages: {
                        "required": (control) {
                          return context.translate(
                            '${i18.login.userIdPlaceholder}_IS_REQUIRED',
                          );
                        },
                      },
                      builder: (field) => LabeledField(
                        label: "User ID",
                        capitalizedFirstLetter: false,
                        isRequired: true,
                        child: DigitTextFormInput(
                          keyboardType: TextInputType.text,
                          errorMessage: field.errorText,
                          onChange: (value) {
                            form.control(_userId).value = value;
                          },
                        ),
                      ),
                    ),
                    ReactiveWrapperField(
                      formControlName: _password,
                      validationMessages: {
                        "required": (control) {
                          return context.translate(
                            '${i18.login.passwordPlaceholder}_IS_REQUIRED',
                          );
                        },
                      },
                      builder: (field) => LabeledField(
                        label: "Password",
                        isRequired: true,
                        child: DigitPasswordFormInput(
                          errorMessage: field.errorText,
                          onChange: (value) {
                            form.control(_password).value = value;
                          },
                          keyboardType: TextInputType.text,
                        ),
                      ),
                    ),
                    BlocConsumer<AuthBloc, AuthState>(
                      listener: (context, state) {
                        state.whenOrNull(
                          error: (message) {
                            context.showSnackBar(SnackBar(
                              content: Text(message),
                              backgroundColor: const Light().alertError,
                            ));
                          },
                          authenticated:
                              (accesstoken, refreshtoken, userRequest) {
                            final hasSupervisorRole = userRequest?.roles.any(
                                    (role) =>
                                        role.code ==
                                        'INSTALLATION_REPORT_PART_B_EDITOR') ??
                                false;
                            final hasAMCRole = userRequest?.roles.any(
                                    (role) => role.code == 'AMC_FIELD_STAFF') ??
                                false;
                            if (hasAMCRole) {
                              context
                                  .read<UserTypeBloc>()
                                  .add(UserTypeEvent.typeSelected(
                                    USER_TYPES.AMC.name.toLowerCase(),
                                  ));
                              context.router.replace(
                                  const AuthenticatedRouteWrapper(
                                      children: const [AmcHomeRoute()]));
                              return;
                            } else if (hasSupervisorRole) {
                              context.read<UserTypeBloc>().add(
                                  UserTypeEvent.typeSelected(USER_TYPES
                                      .SUPERVISOR.name
                                      .toLowerCase()));
                            } else {
                              context.read<UserTypeBloc>().add(
                                  const UserTypeEvent.typeSelected("user"));
                            }
                            context.router
                                .replace(const AuthenticatedRouteWrapper());
                          },
                        );
                      },
                      builder: (context, state) {
                        return state.maybeWhen(
                          loading: () => DigitButton(
                            isDisabled: true,
                            label: 'Loading...',
                            type: DigitButtonType.primary,
                            onPressed: () {},
                            size: DigitButtonSize.large,
                            mainAxisSize: MainAxisSize.max,
                          ),
                          orElse: () => DigitButton(
                            label:
                                context.translate(i18.common.coreCommonLogin),
                            type: DigitButtonType.primary,
                            onPressed: () {
                              form.markAllAsTouched();
                              if (!form.valid) return;

                              FocusManager.instance.primaryFocus?.unfocus();
                              context.read<AuthBloc>().add(
                                    AuthEvent.login(
                                      username: (form.control(_userId).value
                                              as String)
                                          .trim(),
                                      password: (form.control(_password).value
                                              as String)
                                          .trim(),
                                    ),
                                  );
                            },
                            size: DigitButtonSize.large,
                            mainAxisSize: MainAxisSize.max,
                          ),
                        );
                      },
                    ),
                    DigitButton(
                        label: "Forgot Password",
                        mainAxisSize: MainAxisSize.max,
                        type: DigitButtonType.tertiary,
                        size: DigitButtonSize.medium,
                        onPressed: () =>
                            context.router.push(const ForgotPasswordRoute())),
                  ]);
            },
          ),
        ],
      ),
    );
  }

  FormGroup buildForm() => fb.group(<String, Object>{
        _userId: FormControl<String>(
          value: '',
          validators: [Validators.required],
        ),
        _password: FormControl<String>(
          validators: [Validators.required],
          value: '',
        ),
      });
}
