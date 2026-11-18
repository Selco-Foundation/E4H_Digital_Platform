import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:reactive_forms/reactive_forms.dart';
import '../../lib/widgets/form_time_field.dart';

void main() {
  Future<FormGroup> render(
    WidgetTester tester,
    String value, {
    bool readOnly = false,
  }) async {
    final form = FormGroup({'time': FormControl<String>(value: value)});
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: ReactiveForm(
            formGroup: form,
            child: FormTimeField(
              formControlName: 'time',
              label: 'Time of recording',
              readOnly: readOnly,
            ),
          ),
        ),
      ),
    );
    return form;
  }

  testWidgets('saved time initializes picker; confirming stores HH:mm', (
    tester,
  ) async {
    final form = await render(tester, '09:05');
    expect(tester.widget<TextField>(find.byType(TextField)).readOnly, isTrue);
    await tester.tap(find.byType(TextField));
    await tester.pumpAndSettle();
    final picker = tester.widget<TimePickerDialog>(
      find.byType(TimePickerDialog),
    );
    expect(picker.initialTime, const TimeOfDay(hour: 9, minute: 5));
    expect(picker.initialEntryMode, TimePickerEntryMode.dialOnly);
    await tester.tap(find.text('OK'));
    await tester.pumpAndSettle();
    expect(form.control('time').value, '09:05');
    expect(form.control('time').touched, isTrue);
  });

  testWidgets('cancel preserves invalid legacy text', (tester) async {
    final form = await render(tester, 'legacy text');
    await tester.tap(find.byType(TextField));
    await tester.pumpAndSettle();
    expect(find.byType(TimePickerDialog), findsOneWidget);
    await tester.tap(find.text('Cancel'));
    await tester.pumpAndSettle();
    expect(form.control('time').value, 'legacy text');
    expect(find.text('legacy text'), findsOneWidget);
  });

  testWidgets('read-only field displays saved time without opening picker', (
    tester,
  ) async {
    final form = await render(tester, '17:30', readOnly: true);
    await tester.tap(find.byType(TextField));
    await tester.pumpAndSettle();
    expect(find.byType(TimePickerDialog), findsNothing);
    expect(form.control('time').value, '17:30');
    expect(find.text('17:30'), findsOneWidget);
  });

  testWidgets('disabled controls cannot open a picker', (tester) async {
    final form = await render(tester, '14:30');
    form.control('time').markAsDisabled();
    await tester.pump();
    await tester.tap(find.byType(TextField));
    await tester.pumpAndSettle();
    expect(find.byType(TimePickerDialog), findsNothing);
    expect(form.control('time').value, '14:30');
  });
}
