import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_info_card.dart';
import 'package:digit_ui_components/widgets/atoms/digit_otp.dart';
import 'package:digit_ui_components/widgets/atoms/reactive_fields.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:reactive_forms/reactive_forms.dart';
import 'package:selco/utils/utils.dart';

import '../blocs/amc_otp/amc_otp.dart';
import '../blocs/selected_scheduled_visit/selected_scheduled_visit.dart';
import '../model/scheduled_visit/scheduled_visit.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';

@RoutePage()
class AmcOtpPage extends StatefulWidget {
  const AmcOtpPage({super.key});

  @override
  State<AmcOtpPage> createState() => _AmcOtpPageState();
}

class _AmcOtpPageState extends State<AmcOtpPage> {
  static const _otp = "otp";
  final TextEditingController otpController = TextEditingController();
  bool next = false;
  final FocusNode pinFocusNode = FocusNode();
  String? _currentScheduledVisitId;
  ScheduledVisit? scheduledVisit;

  @override
  void initState() {
    super.initState();

    context.read<SelectedScheduledVisitBloc>().state.whenOrNull(
        selected: (visit) {
      _currentScheduledVisitId = visit.id;
      scheduledVisit = visit;
    });
  }

  @override
  void dispose() {
    otpController.dispose();
    pinFocusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    final otpState = context.watch<AmcOtpBloc>().state;
    final isSubmitLoading = otpState.maybeWhen(
      submitLoading: () => true,
      orElse: () => false,
    );

    final isResendLoading = otpState.maybeWhen(
      resendLoading: () => true,
      orElse: () => false,
    );

    return BlocListener<AmcOtpBloc, AmcOtpState>(
      listener: (ctx, state) {
        state.maybeWhen(
          resendSuccess: () {
            ctx.showSnackBar(
              SnackBar(
                  content: Text(context.translate(i18.amcOtp.resendSuccess))),
            );
          },
          submitSuccess: () {
            ctx.showSnackBar(
              SnackBar(
                  content: Text(context.translate(i18.amcOtp.verifySuccess))),
            );
            ctx.router.push(const AmcHomeRoute());
          },
          failure: (msg) {
            ctx.showSnackBar(
              SnackBar(content: Text(msg)),
            );
          },
          orElse: () {},
        );
      },
      child: ReactiveFormBuilder(
          form: buildForm,
          builder: (context, form, child) {
            return Scaffold(
              body: ScrollableContent(
                enableFixedDigitButton: true,
                backgroundColor: theme.colorTheme.generic.background,
                footer: ReactiveFormConsumer(
                  builder: (context, form, child) {
                    final isFormValid = form.valid;
                    final isDisabled = !isFormValid ||
                        isSubmitLoading ||
                        isResendLoading ||
                        _currentScheduledVisitId == null;
                    return FooterButton(
                      isDisabled: isDisabled,
                      showSuffixIcon: false,
                      text: isResendLoading
                          ? context.translate(i18.amcOtp.resending)
                          : context.translate(i18.common.coreCommonSubmit),
                      onPress: () {
                        form.markAllAsTouched();
                        if (!form.valid || _currentScheduledVisitId == null)
                          return;

                        FocusManager.instance.primaryFocus?.unfocus();

                        final otp = (form.control(_otp).value as String).trim();
                        context.read<AmcOtpBloc>().add(AmcOtpEvent.submit(
                            visitId: _currentScheduledVisitId!,
                            schemaCode: "12345678",
                            version: 1,
                            otp: otp,
                            scheduledVisit: scheduledVisit));
                      },
                    );
                  },
                ),
                children: [
                  DigitCard(
                    margin: const EdgeInsets.all(spacer2),
                    children: [
                      const SizedBox(height: spacer6),
                      Center(
                        child: Text(
                          textAlign: TextAlign.center,
                          context.translate(i18.amcOtp.instruction),
                          style: textTheme.headingM.copyWith(
                              color: theme.colorTheme.primary.primary2),
                        ),
                      ),
                      Text.rich(
                        TextSpan(
                          style: textTheme.bodyL.copyWith(
                            color: theme.colorTheme.text.primary,
                          ),
                          children: [
                            TextSpan(
                              text:
                                  "${context.translate(i18.amcOtp.amcOtpReceiverInstruction1)} ",
                            ),
                            TextSpan(
                              text:
                                  "${scheduledVisit?.facility?.facility_poc_name ?? '___'} ",
                              style: textTheme.bodyL.copyWith(
                                color: theme.colorTheme.text.primary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            TextSpan(
                              text:
                                  "${context.translate(i18.amcOtp.amcOtpReceiverInstruction2)} ",
                            ),
                            TextSpan(
                              text: context
                                  .translate(i18.amcOtp.amcOtpMaskedPhone),
                              style: textTheme.bodyL.copyWith(
                                color: theme.colorTheme.text.primary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            TextSpan(
                              text: lastPhoneDigits(
                                scheduledVisit?.facility?.facility_poc_phone ??
                                    '****',
                                count: 4,
                              ),
                              style: textTheme.bodyL.copyWith(
                                color: theme.colorTheme.text.primary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ],
                        ),
                        textAlign: TextAlign.center,
                      ),
                      SizedBox(
                        width: context.width,
                        child: ReactiveWrapperField(
                          formControlName: _otp,
                          validationMessages: {
                            "required": (control) {
                              return context.translate(
                                '${i18.login.otpPlaceholder}_IS_REQUIRED',
                              );
                            },
                          },
                          builder: (field) => DigitOTPInput(
                            // label: context.translate(i18.amcOtp.enterCode),
                            inputFormatter: [
                              FilteringTextInputFormatter.digitsOnly
                            ],
                            errorMessage: field.errorText,
                            onChanged: (input) {
                              field.didChange(input);
                            },
                          ),
                        ),
                      ),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          GestureDetector(
                            onTap: isResendLoading
                                ? () {}
                                : () {
                                    final visitId = _currentScheduledVisitId;
                                    if (visitId == null) return;

                                    context.read<AmcOtpBloc>().add(
                                        AmcOtpEvent.resend(visitId: visitId));
                                  },
                            child: Text(
                              textAlign: TextAlign.end,
                              isResendLoading
                                  ? context.translate(i18.amcOtp.resending)
                                  : context.translate(i18.amcOtp.resendCode),
                              style: textTheme.linkM.copyWith(
                                  color: theme.colorTheme.alert.error),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                  Padding(
                    padding: const EdgeInsets.all(spacer2),
                    child: InfoCard(
                      title: "",
                      type: InfoType.warning,
                      description:
                          context.translate(i18.amcOtp.amcOtpNetworkWarning),
                    ),
                  )
                ],
              ),
            );
          }),
    );
  }

  FormGroup buildForm() => fb.group(<String, Object>{
        _otp: FormControl<String>(
          value: '',
          validators: [
            Validators.required,
            Validators.minLength(4),
            Validators.maxLength(4)
          ],
        ),
      });
}
