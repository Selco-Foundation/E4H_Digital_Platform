---
paths:
  - "lib/pages/**/*.dart"
  - "lib/widgets/**/*.dart"
---

# UI rules (pages & widgets)

- Use `digit_ui_components` before Material: `DigitCard`, `DigitButton`, `DigitTextFormInput`, `DigitSearchFormInput`, `DigitDropdown`, `DigitDivider`, `DigitTabBar`, `DigitStepper`.
- Colours/text only from the theme, never hex literals:
  `final theme = context.theme;` → `theme.colorTheme.primary.primary2`, `theme.colorTheme.text.primary`, `theme.colorTheme.generic.background`, `theme.digitTextTheme(context).<style>`.
- Before creating a widget, check `lib/widgets/` — e.g. `button/footer_button.dart`, `button/bom_buttons.dart`, `header/back_navigation_help_header.dart`, `facility_list_controls.dart`, `cards/report_card.dart`, `cards/report_detail_row.dart`, `cards/stepper.dart`, `progress_indicator/loading_indicator.dart`, `progress_indicator/operation_progress_overlay.dart`, `customized_digit_widget/image_uploader.dart`, `customized_digit_widget/file_uploader.dart`, `images/cached_image.dart`, `mdms/mdms_gate.dart`, `rectification_badge.dart`. Reuse or extend; don't duplicate.
- Error messages: `ScaffoldMessenger.of(context).showSnackBar(...)` with a translated message, as existing pages do. Don't show raw exception text.
- Every string via `context.translate(i18.x.y)`; add new keys to `lib/utils/i18_key_constants.dart` in the right group, and list the new keys (with English text) in your summary so they can be uploaded to localization.
- Lists: show loading, empty and error states, support pull-to-refresh (`RefreshableFacilityList`), paginate like `ScheduledVisitBloc` (`ScheduledVisitRepository.defaultPageSize`).
- Don't wrap screens in another `SafeArea` for the bottom bar — `AppSafeArea` is applied app-wide.
- Pages that need data take an optional repository/bloc param for tests.
- When given a design image, match layout and spacing to existing screens of the same type rather than guessing pixel values.
