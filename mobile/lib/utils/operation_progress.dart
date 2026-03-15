class OperationTypes {
  static const submit = 'submit';
  static const reject = 'reject';
  static const sendBack = 'send_back';
  static const submitVisit = 'submit_visit';
}

class OperationStatuses {
  static const queued = 'queued';
  static const running = 'running';
  static const partial = 'partial';
  static const success = 'success';
  static const failed = 'failed';
}

class OperationCheckpointStatuses {
  static const pending = 'pending';
  static const success = 'success';
  static const failed = 'failed';
}

class OperationStage {
  final String key;
  final String label;

  const OperationStage(this.key, this.label);
}

class OperationProgressModel {
  final String activityFacilityId;
  final String operationType;
  final String status;
  final String stageKey;
  final String stageLabel;
  final int completedSteps;
  final int totalSteps;
  final int progressPercent;
  final int retryCount;
  final bool isBlocking;
  final String? errorMessage;

  const OperationProgressModel({
    required this.activityFacilityId,
    required this.operationType,
    required this.status,
    required this.stageKey,
    required this.stageLabel,
    required this.completedSteps,
    required this.totalSteps,
    required this.progressPercent,
    required this.retryCount,
    required this.isBlocking,
    this.errorMessage,
  });

  bool get isActive =>
      status == OperationStatuses.queued ||
      status == OperationStatuses.running ||
      status == OperationStatuses.partial;

  bool get isFailure => status == OperationStatuses.failed;
  bool get canRetry => isFailure;
}

class BulkOperationProgressModel {
  final int completed;
  final int total;
  final int progressPercent;
  final int activeCount;
  final String label;

  const BulkOperationProgressModel({
    required this.completed,
    required this.total,
    required this.progressPercent,
    required this.activeCount,
    required this.label,
  });
}

const List<OperationStage> submitStages = [
  OperationStage('preparing_submission', 'Preparing submission'),
  OperationStage('checking_saved_asset_data', 'Checking saved asset data'),
  OperationStage(
      'starting_secure_upload_service', 'Starting secure upload service'),
  OperationStage('uploading_asset_media', 'Uploading asset photos and videos'),
  OperationStage('uploading_completion_reports',
      'Uploading installation completion reports'),
  OperationStage('generating_bom_pdf', 'Generating BOM PDF'),
  OperationStage('submitting_bom', 'Submitting BOM'),
  OperationStage('submitting_assets', 'Submitting assets'),
  OperationStage(
      'finalizing_workflow_submission', 'Finalizing workflow submission'),
  OperationStage('cleaning_up_local_cache', 'Cleaning up local cache'),
  OperationStage('submission_successful', 'Submission successful'),
];

const List<OperationStage> rejectionStages = [
  OperationStage('preparing_rejection', 'Preparing rejection'),
  OperationStage(
      'validating_rejection_reasons', 'Validating rejection reasons'),
  OperationStage('loading_workflow_documents', 'Loading workflow documents'),
  OperationStage('submitting_rejection', 'Submitting rejection'),
  OperationStage('cleaning_up_local_cache', 'Cleaning up local cache'),
  OperationStage('rejection_successful', 'Rejection successful'),
];

const List<OperationStage> sendBackStages = [
  OperationStage('preparing_send_back', 'Preparing send back'),
  OperationStage(
      'validating_current_workflow_state', 'Validating current workflow state'),
  OperationStage('sending_report_back', 'Sending report back'),
  OperationStage('refreshing_local_data', 'Refreshing local data'),
  OperationStage('send_back_successful', 'Send back successful'),
];

List<OperationStage> stagesForOperation(String operationType) {
  switch (operationType) {
    case OperationTypes.reject:
      return rejectionStages;
    case OperationTypes.sendBack:
      return sendBackStages;
    case OperationTypes.submit:
    default:
      return submitStages;
  }
}

String operationTitle(String operationType) {
  switch (operationType) {
    case OperationTypes.reject:
      return 'Rejecting report';
    case OperationTypes.sendBack:
      return 'Sending report back';
    case OperationTypes.submit:
    default:
      return 'Submitting report';
  }
}

OperationStage stageForKey(String operationType, String stageKey) {
  return stagesForOperation(operationType).firstWhere(
    (stage) => stage.key == stageKey,
    orElse: () => OperationStage(stageKey, stageKey),
  );
}

int progressPercent({
  required int completedSteps,
  required int totalSteps,
}) {
  if (totalSteps <= 0) return 0;
  final raw = ((completedSteps / totalSteps) * 100).round();
  if (raw < 0) return 0;
  if (raw > 100) return 100;
  return raw;
}
