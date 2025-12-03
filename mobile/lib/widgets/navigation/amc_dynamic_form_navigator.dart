import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:selco/utils/utils.dart';

import '../../blocs/scheduled_visit/scheduled_visit.dart';
import '../../model/scheduled_visit/scheduled_visit.dart';
import '../../repositories/dynamic_form_repo.dart';
import '../../router/app_router.dart';

// Future<void> openAmcDynamicForm({
//   required BuildContext context,
//   required ScheduledVisit scheduledVisit,
//   required FormOrigin origin, // or enum, e.g. AmcOrigin
// }) async {
//   // 1. Compute your initial values here
//   final initialValues = await _buildInitialAmcValues(
//       context: context, scheduledVisit: scheduledVisit, origin: origin);
//
//   // 2. Push your dynamic form page
//   context.router.push(
//     AmcDynamicFormRoute(
//       pageName: 'AMC_Report',
//       uniqueIdentifier: 'AMC.SCHEDULED_MAINTENANCE',
//       schemaName: 'SELCO.AMC_SCHEDULED_MAINTENANCE',
//       scheduledVisitId: scheduledVisit.id!,
//       origin: origin,
//       initialFormValues: initialValues,
//     ),
//   );
// }

Future<void> openAmcDynamicForm({
  required BuildContext context,
  required ScheduledVisit scheduledVisit,
  required FormOrigin origin,
}) async {
  // show loader dialog
  showDialog(
    context: context,
    barrierDismissible: false,
    builder: (_) => const Center(
      child: CircularProgressIndicator(),
    ),
  );

  try {
    final initialValues = await _buildInitialAmcValues(
      context: context,
      scheduledVisit: scheduledVisit,
      origin: origin,
    );

    if (!context.mounted) return;

    // remove loader
    Navigator.of(context).pop();

    // navigate
    await context.router.push(
      AmcDynamicFormRoute(
        pageName: 'AMC_Report',
        uniqueIdentifier: 'AMC.SCHEDULED_MAINTENANCE',
        schemaName: 'SELCO.AMC_SCHEDULED_MAINTENANCE',
        scheduledVisit: scheduledVisit,
        origin: origin,
      ),
    );
  } catch (error) {
    if (!context.mounted) return;

    // remove loader
    Navigator.of(context).pop();

    // show error
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(error.toString())),
    );
  }
}

Future<Map<String, dynamic>> _buildInitialAmcValues({
  required BuildContext context,
  required ScheduledVisit scheduledVisit,
  required FormOrigin origin,
}) async {
  final userType = USER_TYPES.AMC.name;

  final isar = context.read<ScheduledVisitBloc>().isar;

  final formRepo = AmcDynamicFormRepository();
  final initialValues = await formRepo.getInitialFormValues(
    isar: isar,
    scheduledVisitId: scheduledVisit.id!,
    responsesFromModel: scheduledVisit.visitReport?.responses,
    userType: userType,
  );

  return initialValues ?? {};
}

Future<Map<String, dynamic>> buildInitialAmcValues({
  required BuildContext context,
  required ScheduledVisit scheduledVisit,
  required FormOrigin origin,
}) async {
  final userType = USER_TYPES.AMC.name;

  final isar = context.read<ScheduledVisitBloc>().isar;

  final formRepo = AmcDynamicFormRepository();
  final initialValues = await formRepo.getInitialFormValues(
    isar: isar,
    scheduledVisitId: scheduledVisit.id!,
    responsesFromModel: scheduledVisit.visitReport?.responses,
    userType: userType,
  );

  return initialValues ?? {"faults_observed": "YES"};
}
