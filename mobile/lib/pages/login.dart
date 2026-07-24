import 'dart:async';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:reactive_forms/reactive_forms.dart';
import 'package:recase/recase.dart';

import '../blocs/auth/authbloc.dart';
import '../blocs/user_type/user_type.dart';
import '../data/secure_storage/secureStore.dart';
import '../router/app_router.dart';
import '../utils/app_logger.dart';
import '../utils/envConfig.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/role_login_resolver.dart';
import '../utils/utils.dart';
import '../widgets/navigation/navbar.dart';
import '../widgets/privacy_policy/login_consent_checkbox.dart';
import '../widgets/privacy_policy/policy_webview_dialog.dart';

@RoutePage()
class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  var passwordVisible = false;
  bool isPrivacyEnabled = false;
  bool _isConsentStatusLoading = true;
  bool _hasAcceptedConsent = false;
  bool _shouldPersistConsentOnAuthentication = false;
  static const _userId = 'userId';
  static const _password = 'password';

  @override
  void initState() {
    super.initState();
    unawaited(_loadConsentStatus());
  }

  Future<void> _loadConsentStatus() async {
    var hasAcceptedConsent = false;
    try {
      hasAcceptedConsent = await SecureStore().hasAcceptedLoginConsent();
    } catch (error, stackTrace) {
      AppLogger.instance.error(
        title: 'Login consent read failed',
        message: error.toString(),
        stackTrace: stackTrace,
      );
    }

    if (!mounted) return;
    setState(() {
      _hasAcceptedConsent = hasAcceptedConsent;
      _isConsentStatusLoading = false;
    });
  }

  Future<void> _persistConsentAfterAuthentication() async {
    if (_hasAcceptedConsent || !_shouldPersistConsentOnAuthentication) {
      return;
    }

    try {
      await SecureStore().setLoginConsentAccepted();
      _hasAcceptedConsent = true;
    } catch (error, stackTrace) {
      AppLogger.instance.error(
        title: 'Login consent write failed',
        message: error.toString(),
        stackTrace: stackTrace,
      );
    }
  }

  void _openPolicy({
    required String title,
    required String relativePath,
  }) {
    final uri = buildEnvironmentUrl(
      envConfig.variables.baseUrl,
      relativePath,
    );

    if (uri == null) {
      context.showSnackBar(
        SnackBar(
          content: Text(
            context.translate(i18.login.policyUrlNotConfigured),
          ),
          backgroundColor: const Light().alertError,
        ),
      );
      return;
    }

    showDialog<void>(
      context: context,
      useSafeArea: false,
      builder: (_) => PolicyWebViewDialog(
        title: title,
        uri: uri,
      ),
    );
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
                    if (!_isConsentStatusLoading && !_hasAcceptedConsent)
                      LoginConsentCheckbox(
                        value: isPrivacyEnabled,
                        onChanged: (value) {
                          setState(() {
                            isPrivacyEnabled = value;
                          });
                        },
                        prefixText: context.translate(i18.login.consentPrefix),
                        privacyPolicyText:
                            context.translate(i18.login.privacyPolicy),
                        connectorText:
                            context.translate(i18.login.consentConnector),
                        termsAndConditionsText:
                            context.translate(i18.login.termsAndConditions),
                        onPrivacyPolicyTap: () {
                          _openPolicy(
                            title: context.translate(i18.login.privacyPolicy),
                            relativePath: envConfig.variables.privacyPolicyUrl,
                          );
                        },
                        onTermsAndConditionsTap: () {
                          _openPolicy(
                            title:
                                context.translate(i18.login.termsAndConditions),
                            relativePath:
                                envConfig.variables.termsAndConditionsUrl,
                          );
                        },
                      ),
                    BlocConsumer<AuthBloc, AuthState>(
                      listener: (context, state) {
                        state.whenOrNull(
                          error: (message) {
                            _shouldPersistConsentOnAuthentication = false;
                            context.showSnackBar(SnackBar(
                              content: Text(context.translate(message)),
                              backgroundColor: const Light().alertError,
                            ));
                          },
                          authenticated:
                              (accesstoken, refreshtoken, userRequest) async {
                            await _persistConsentAfterAuthentication();
                            if (!context.mounted) return;

                            final resolution = RoleLoginResolver.resolveRoles(
                              userRequest?.roles ?? const [],
                            );

                            if (resolution.requiresSelection) {
                              context.router.replace(
                                const AuthenticatedRouteWrapper(
                                  children: [RoleSelectionRoute()],
                                ),
                              );
                              return;
                            }

                            final directUserType = resolution.directUserType ??
                                USER_TYPES.FIELD_STAFF;
                            context.read<UserTypeBloc>().add(
                                  UserTypeEvent.typeSelected(
                                    directUserType.name.toLowerCase(),
                                  ),
                                );

                            if (directUserType == USER_TYPES.AMC) {
                              context.router.replace(
                                const AuthenticatedRouteWrapper(
                                  children: [AmcHomeRoute()],
                                ),
                              );
                              return;
                            }

                            context.router.replace(
                              const AuthenticatedRouteWrapper(),
                            );
                          },
                        );
                      },
                      builder: (context, state) {
                        return state.maybeWhen(
                          loading: () => DigitButton(
                            isDisabled: true,
                            label: context.translate(i18.common.loading),
                            type: DigitButtonType.primary,
                            onPressed: () {},
                            size: DigitButtonSize.large,
                            mainAxisSize: MainAxisSize.max,
                          ),
                          orElse: () => DigitButton(
                            isDisabled: _isConsentStatusLoading ||
                                (!_hasAcceptedConsent && !isPrivacyEnabled),
                            label:
                                context.translate(i18.common.coreCommonLogin),
                            type: DigitButtonType.primary,
                            onPressed: () {
                              if (_isConsentStatusLoading ||
                                  (!_hasAcceptedConsent && !isPrivacyEnabled)) {
                                return;
                              }

                              form.markAllAsTouched();
                              if (!form.valid) return;

                              _shouldPersistConsentOnAuthentication =
                                  !_hasAcceptedConsent && isPrivacyEnabled;
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
