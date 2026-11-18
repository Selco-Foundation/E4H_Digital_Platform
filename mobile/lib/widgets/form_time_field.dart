import 'package:flutter/material.dart';
import 'package:reactive_forms/reactive_forms.dart';
import 'package:digit_forms_engine/models/property_schema/property_schema.dart';
import '../utils/form_time_value.dart';
import '../utils/extensions.dart';
import '../utils/error_localization.dart';

/// A local clock time, stored as HH:mm without date or timezone conversion.
class FormTimeField extends StatelessWidget {
  const FormTimeField({
    super.key,
    required this.formControlName,
    required this.label,
    this.readOnly = false,
  });

  final String formControlName;
  final String label;
  final bool readOnly;

  static TimeOfDay? parse(String? value) {
    final match = RegExp(
      r'^(\d{1,2}):(\d{2})$',
    ).firstMatch(value?.trim() ?? '');
    if (match == null) return null;
    final hour = int.parse(match[1]!);
    final minute = int.parse(match[2]!);
    if (hour > 23 || minute > 59) return null;
    return TimeOfDay(hour: hour, minute: minute);
  }

  static String format(TimeOfDay time) =>
      '${time.hour.toString().padLeft(2, '0')}:'
      '${time.minute.toString().padLeft(2, '0')}';

  @override
  Widget build(BuildContext context) => ReactiveTextField<String>(
        key: ValueKey(formControlName),
        formControlName: formControlName,
        readOnly: true,
        validationMessages: {
          ValidationMessage.pattern: (_) =>
              context.translate(ErrorKeys.invalidValue),
          ValidationMessage.required: (_) =>
              context.translate(ErrorKeys.requiredField),
        },
        decoration: InputDecoration(
          labelText: context.translate(label),
          suffixIcon: const Icon(Icons.access_time),
        ),
        onTap: readOnly
            ? null
            : (control) async {
                if (control.disabled) return;
                final selected = await showTimePicker(
                  context: context,
                  initialTime: parse(control.value) ?? TimeOfDay.now(),
                  initialEntryMode: TimePickerEntryMode.dialOnly,
                  builder: (context, child) => MediaQuery(
                    data: MediaQuery.of(
                      context,
                    ).copyWith(alwaysUse24HourFormat: true),
                    child: child!,
                  ),
                );
                if (selected == null || !context.mounted) return;
                control.updateValue(format(selected));
                control.markAsTouched();
              },
      );
}

List<Map<String, Widget>> timeFormComponents(
  PropertySchema page, {
  bool readOnly = false,
  String prefix = '',
}) =>
    [
      for (final entry
          in page.properties?.entries ?? <MapEntry<String, PropertySchema>>[])
        if (isTimeProperty(entry.value))
          {
            '$prefix${entry.key}': FormTimeField(
              formControlName: '$prefix${entry.key}',
              label: entry.value.label ?? '',
              readOnly: readOnly ||
                  entry.value.readOnly == true ||
                  entry.value.displayOnly == true,
            ),
          }
        else if (entry.value.properties != null)
          ...timeFormComponents(
            entry.value,
            readOnly: readOnly || entry.value.readOnly == true,
            prefix: '$prefix${entry.key}.',
          ),
    ];
