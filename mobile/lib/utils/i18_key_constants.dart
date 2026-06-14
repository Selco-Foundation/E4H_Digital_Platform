library i18;

const common = Common();
const welcome = Welcome();
const login = Login();
const forgotPassword = ForgotPassword();
const scanner = Scanner();
const appShell = AppShell();
const mdmsGate = MdmsGate();
const draft = Draft();
const inbox = Inbox();
const inboxAssetSummary = InboxAssetSummary();
const selectHealthFacility = SelectHealthFacility();
const assetCount = AssetCount();
const addNewAsset = AddNewAsset();
const mediaUpload = MediaUpload();
const assetSummary = AssetSummary();
const dynamicForm = DynamicForm();
const amcDynamicForm = AmcDynamicForm();
const amcOtp = AmcOtp();
const amcDraft = AmcDraft();
const amcInbox = AmcInbox();
const amcHome = AmcHome();
const amcReportHome = AmcReportHome();
const amcMediaUpload = AmcMediaUpload();
const amcSelectFacility = AmcSelectFacility();
const home = Home();
const bomButtons = BomButtons();
const installationImages = InstallationImages();
const installationCompletionCertificate = InstallationCompletionCertificate();
const overallAssetSummary = OverallAssetSummary();
const submitForApproval = SubmitForApproval();
const pdfViewer = PdfViewer();
const sharedCards = SharedCards();
const progressOverlay = ProgressOverlay();
const assetTypeDetail = AssetTypeDetail();
const selectAssetType = SelectAssetType();
const specification = Specification();
const submittedSaveSuccess = SubmittedSaveSuccess();
const syncLoading = SyncLoading();

class Common {
  const Common();
  String get coreCommonProceed => 'CORE_COMMON_PROCEED';
  String get coreCommonSubmit => 'CORE_COMMON_SUBMIT';
  String get coreCommonSave => 'CORE_COMMON_SAVE';
  String get coreCommonOk => 'CORE_COMMON_OK';
  String get coreCommonProfile => 'CORE_COMMON_PROFILE';
  String get coreCommonLogout => 'CORE_COMMON_LOGOUT';
  String get coreCommonLogin => 'CORE_COMMON_LOGIN';
  String get coreCommonHome => 'CORE_COMMON_HOME';
  String get connectionLabel => 'CORE_COMMON_CONNECTION_LABEL';
  String get connectionContent => 'CORE_COMMON_CONNECTION_CONTENT';
  String get coreCommonNext => 'CORE_COMMON_NEXT';
  String get coreCommonEdit => 'CORE_COMMON_EDIT';
  String get loading => 'CORE_COMMON_LOADING';
  String get loadingSchema => 'CORE_COMMON_LOADING_SCHEMA';
  String get loadingAppData => 'CORE_COMMON_LOADING_APP_DATA';
  String get formSchemaMissing => 'CORE_COMMON_FORM_SCHEMA_MISSING';
  String get couldNotFetchLocation => 'CORE_COMMON_COULD_NOT_FETCH_LOCATION';
  String get syncFailed => 'CORE_COMMON_SYNC_FAILED';
  String get bomSyncFailed => 'CORE_COMMON_BOM_SYNC_FAILED';
  String get error => 'CORE_COMMON_ERROR';
  String get failed => 'CORE_COMMON_FAILED';
  String get tokenExpiredLoginAgain => 'CORE_COMMON_TOKEN_EXPIRED_LOGIN_AGAIN';
  String get retry => 'CORE_COMMON_RETRY';
  String get sort => 'CORE_COMMON_SORT';
  String get sortBy => 'CORE_COMMON_SORT_BY';
  String get clear => 'CORE_COMMON_CLEAR';
  String get newestFirst => 'CORE_COMMON_NEWEST_FIRST';
  String get oldestFirst => 'CORE_COMMON_OLDEST_FIRST';
  String get submissionDate => 'CORE_COMMON_SUBMISSION_DATE';
  String get optional => 'CORE_COMMON_OPTIONAL';
  String get uploadImages => 'CORE_COMMON_UPLOAD_IMAGES';
  String get uploadVideos => 'CORE_COMMON_UPLOAD_VIDEOS';
  String get images => 'CORE_COMMON_IMAGES';
  String get videos => 'CORE_COMMON_VIDEOS';
  String get capacity => 'CORE_COMMON_CAPACITY';
  String get unit => 'CORE_COMMON_UNIT';
  String get voltage => 'CORE_COMMON_VOLTAGE';
  String get current => 'CORE_COMMON_CURRENT';
  String get serialNumber => 'CORE_COMMON_SERIAL_NUMBER';
  String get scan => 'CORE_COMMON_SCAN';
  String get status => 'CORE_COMMON_STATUS';
  String get dateAssigned => 'CORE_COMMON_DATE_ASSIGNED';
  String get solutionDoc => 'CORE_COMMON_SOLUTION_DOC';
  String get state => 'CORE_COMMON_STATE';
  String get district => 'CORE_COMMON_DISTRICT';
  String get block => 'CORE_COMMON_BLOCK';
  String get backToHome => 'CORE_COMMON_BACK_TO_HOME';
  String get isRequired => 'CORE_COMMON_IS_REQUIRED';
  String get hasInvalidFormat => 'CORE_COMMON_HAS_INVALID_FORMAT';
  String get mustBeNumber => 'CORE_COMMON_MUST_BE_NUMBER';
  String get belowMinimum => 'CORE_COMMON_BELOW_MINIMUM';
  String get aboveMaximum => 'CORE_COMMON_ABOVE_MAXIMUM';
  String get pleaseCorrect => 'CORE_COMMON_PLEASE_CORRECT';
}

class Welcome {
  const Welcome();
  String get welcomeTitle => 'WELCOME_TITLE';
  String get welcomeDescription => 'WELCOME_DESCRIPTION';
  String get welcomeMenuTitleOne => 'WELCOME_MENU_TITLE_ONE';
  String get welcomeMenuDescOne => 'WELCOME_MENU_DESCRIPTION_ONE';
  String get welcomeMenuTitleTwo => 'WELCOME_MENU_TITLE_TWO';
  String get welcomeMenuDescTwo => 'WELCOME_MENU_DESCRIPTION_TWO';
  String get welcomeMenuTitleThree => 'WELCOME_MENU_TITLE_THREE';
  String get welcomeMenuDescThree => 'WELCOME_MENU_DESCRIPTION_THREE';
  String get welcomeMenuTitleFour => 'WELCOME_MENU_TITLE_FOUR';
  String get welcomeMenuDescFour => 'WELCOME_MENU_DESCRIPTION_FOUR';
  String get welcomeMenuTitleFive => 'WELCOME_MENU_TITLE_FIVE';
  String get welcomeMenuDescFive => 'WELCOME_MENU_DESCRIPTION_FIVE';
}

class Login {
  const Login();
  String get labelText => 'LOGIN_LABEL_TEXT';
  String get userIdPlaceholder => 'USER_ID_PLACEHOLDER';
  String get passwordPlaceholder => 'PASSWORD_PLACEHOLDER';
  String get actionLabel => 'LOGIN_ACTION_LABEL';
  String get mobileNumberPlaceholder => 'MOBILE_NUMBER_PLACEHOLDER';
  String get otpPlaceholder => 'OTP_PLACEHOLDER';
  String get errorNoNetwork => 'LOGIN_ERROR_NO_NETWORK';
  String get errorNoInternet => 'LOGIN_ERROR_NO_INTERNET';
  String get errorConnectionFailed => 'LOGIN_ERROR_CONNECTION_FAILED';
  String get errorRequestTimeout => 'LOGIN_ERROR_REQUEST_TIMEOUT';
  String get errorInvalidCredentials => 'LOGIN_ERROR_INVALID_CREDENTIALS';
  String get errorServer => 'LOGIN_ERROR_SERVER';
  String get errorUnknown => 'LOGIN_ERROR_UNKNOWN';
}

class Scanner {
  const Scanner();
  String get unableToScan => 'UNABLE_TO_SCAN';
  String get scanValidResource => 'SCAN_VALID_RESOURCE';
  String get resourceAlreadyScanned => 'RESOURCE_ALREADY_SCANNED';
  String get resourceAlreadyScanned2 => 'Resource_already_scanned';
  String get resourcesAlreadyScanned => 'Resources_already_scanned';
  String get scannerLabel => 'SCANNER_LABEL';
  String get noOfResourceScanned => 'NO_OF_RESOURCE_SCANNED';
  String get resourcesScanned => 'RESOURCES_SCANNED';
  String get resourceScanned => 'RESOURCE_SCANNED';
  String get saveScannedResource => 'SAVE_SCANNED_RESOURCE';
  String get flashOn => 'FLASH_ON';
  String get flashOff => 'FLASH_OFF';
  String get manualScan => 'MANUAL_SCAN';
  String get enterManualCode => 'ENTER_MANUAL_CODE';
  String get scanFromPhoto => 'SCAN_FROM_PHOTO';
  String get choosePhoto => 'CHOOSE_PHOTO';
  String get noCodeFoundInPhoto => 'NO_CODE_FOUND_IN_PHOTO';
  String get failedToReadPhoto => 'FAILED_TO_READ_PHOTO';
  String get resourceCode => 'RESOURCE_CODE';
  String get coreCommonSubmit => 'Core_common_submit';
  String get coreCommonSubmit2 => 'CORE_COMMON_SUBMIT';
  String get scannerDialogTitle => 'SCANNER_DIALOG_TITLE';
  String get scannerDialogContent => 'SCANNER_DIALOG_CONTENT';
  String get scannerDialogPrimaryAction => 'SCANNER_DIALOG_PRIMARY_ACTION';
  String get scannerDialogSecondaryAction => 'SCANNER_DIALOG_SECONDARY_ACTION';
}

class ForgotPassword {
  const ForgotPassword();
  String get labelText => 'FORGOT_PASSWORD_LABEL_TEXT';
  String get contentText => 'FORGOT_PASSWORD_CONTENT_TEXT';
  String get primaryActionLabel => 'PRIMARY_ACTION_LABEL';
  String get actionLabel => 'FORGOT_PASSWORD_ACTION_LABEL';
}

class AppShell {
  const AppShell();
  String get loading => 'APP_SHELL_LOADING';
}

class MdmsGate {
  const MdmsGate();
  String get loadingAppData => 'MDMS_GATE_LOADING_APP_DATA';
}

class Draft {
  const Draft();
  String get syncFailed => 'DRAFT_SYNC_FAILED';
  String get somethingWentWrong => 'DRAFT_SOMETHING_WENT_WRONG';
  String get allDraftsSynced => 'DRAFT_ALL_DRAFTS_SYNCED';
  String get sync => 'DRAFT_SYNC';
  String get submittedReports => 'DRAFT_SUBMITTED_REPORTS';
  String get noUnsyncedReportsFound => 'DRAFT_NO_UNSYNCED_REPORTS_FOUND';
}

class Inbox {
  const Inbox();
  String get forReview => 'INBOX_FOR_REVIEW';
  String get rejected => 'INBOX_REJECTED';
  String get approved => 'INBOX_APPROVED';
  String get title => 'INBOX_TITLE';
  String get searchHealthFacility => 'INBOX_SEARCH_HEALTH_FACILITY';
  String get noProjectsToDisplay => 'INBOX_NO_PROJECTS_TO_DISPLAY';
}

class InboxAssetSummary {
  const InboxAssetSummary();
  String get noProjectSelected => 'INBOX_ASSET_SUMMARY_NO_PROJECT_SELECTED';
  String get reportSentBackSuccessfully =>
      'INBOX_ASSET_SUMMARY_REPORT_SENT_BACK_SUCCESSFULLY';
  String get assetSyncFailed => 'INBOX_ASSET_SUMMARY_ASSET_SYNC_FAILED';
  String get addMoreDetails => 'INBOX_ASSET_SUMMARY_ADD_MORE_DETAILS';
  String get sendingBack => 'INBOX_ASSET_SUMMARY_SENDING_BACK';
  String get sendBack => 'INBOX_ASSET_SUMMARY_SEND_BACK';
  String get sendBackReportConfirmationTitle =>
      'INBOX_ASSET_SUMMARY_SEND_BACK_REPORT_CONFIRMATION_TITLE';
  String get sendBackReportConfirmationDescription =>
      'INBOX_ASSET_SUMMARY_SEND_BACK_REPORT_CONFIRMATION_DESCRIPTION';
  String get close => 'INBOX_ASSET_SUMMARY_CLOSE';
  String get summaryOverview => 'INBOX_ASSET_SUMMARY_SUMMARY_OVERVIEW';
  String get installationCompletionReport =>
      'INBOX_ASSET_SUMMARY_INSTALLATION_COMPLETION_REPORT';
}

class SelectHealthFacility {
  const SelectHealthFacility();
  String get title => 'SELECT_HEALTH_FACILITY_TITLE';
  String get noProjectsFound => 'SELECT_HEALTH_FACILITY_NO_PROJECTS_FOUND';
  String get resumeInstallationReport =>
      'SELECT_HEALTH_FACILITY_RESUME_INSTALLATION_REPORT';
  String get startInstallationReport =>
      'SELECT_HEALTH_FACILITY_START_INSTALLATION_REPORT';
  String get submitForApproval => 'SELECT_HEALTH_FACILITY_SUBMIT_FOR_APPROVAL';
}

class AssetCount {
  const AssetCount();
  String get title => 'ASSET_COUNT_TITLE';
  String get chooseAssetType => 'ASSET_COUNT_CHOOSE_ASSET_TYPE';
  String get inverters => 'ASSET_COUNT_INVERTERS';
  String get batteries => 'ASSET_COUNT_BATTERIES';
  String get panels => 'ASSET_COUNT_PANELS';
}

class AddNewAsset {
  const AddNewAsset();
  String get cameraPermissionRequired =>
      'ADD_NEW_ASSET_CAMERA_PERMISSION_REQUIRED';
  String get locationPermissionRequired =>
      'ADD_NEW_ASSET_LOCATION_PERMISSION_REQUIRED';
  String get couldNotProcessImage => 'ADD_NEW_ASSET_COULD_NOT_PROCESS_IMAGE';
  String get scanSerialNumber => 'ADD_NEW_ASSET_SCAN_SERIAL_NUMBER';
  String get supportingPhoto => 'ADD_NEW_ASSET_SUPPORTING_PHOTO';
  String get type => 'ADD_NEW_ASSET_TYPE';
}

class MediaUpload {
  const MediaUpload();
  String get addAllImages => 'MEDIA_UPLOAD_ADD_ALL_IMAGES';
}

class AssetSummary {
  const AssetSummary();
  String get loadingSummary => 'ASSET_SUMMARY_LOADING_SUMMARY';
  String get errorLoadingSummary => 'ASSET_SUMMARY_ERROR_LOADING_SUMMARY';
  String get summary => 'ASSET_SUMMARY_SUMMARY';
  String get submit => 'ASSET_SUMMARY_SUBMIT';
  String get selectReasonOrEnterAdditionalDetails =>
      'ASSET_SUMMARY_SELECT_REASON_OR_ENTER_ADDITIONAL_DETAILS';
  String get rejectionReason => 'ASSET_SUMMARY_REJECTION_REASON';
  String get reason => 'ASSET_SUMMARY_REASON';
  String get additionalDetails => 'ASSET_SUMMARY_ADDITIONAL_DETAILS';
  String get detailsForSelectedReason =>
      'ASSET_SUMMARY_DETAILS_FOR_SELECTED_REASON';
  String get addReason => 'ASSET_SUMMARY_ADD_REASON';
  String get back => 'ASSET_SUMMARY_BACK';
  String get healthFacilityDetails => 'ASSET_SUMMARY_HEALTH_FACILITY_DETAILS';
  String get name => 'ASSET_SUMMARY_NAME';
  String get count => 'ASSET_SUMMARY_COUNT';
  String get specifications => 'ASSET_SUMMARY_SPECIFICATIONS';
  String get system => 'ASSET_SUMMARY_SYSTEM';
  String get details => 'ASSET_SUMMARY_DETAILS';
  String get warrantyStartDate => 'ASSET_SUMMARY_WARRANTY_START_DATE';
  String get warrantyDuration => 'ASSET_SUMMARY_WARRANTY_DURATION';
  String get brand => 'ASSET_SUMMARY_BRAND';
}

class DynamicForm {
  const DynamicForm();
  String get submit => 'DYNAMIC_FORM_SUBMIT';
}

class AmcDynamicForm {
  const AmcDynamicForm();
  String get loading => 'AMC_DYNAMIC_FORM_LOADING';
}

class AmcOtp {
  const AmcOtp();
  String get resendSuccess => 'AMC_OTP_RESEND_SUCCESS';
  String get verifySuccess => 'AMC_OTP_VERIFY_SUCCESS';
  String get resending => 'AMC_OTP_RESENDING';
  String get instruction => 'AMC_OTP_INSTRUCTION';
  String get enterCode => 'AMC_OTP_ENTER_CODE';
  String get resendCode => 'AMC_OTP_RESEND_CODE';
  String get amcOtpReceiverInstruction1 => "AMC_OTP_RECEIVER_INSTRUCTION_1";
  String get amcOtpReceiverInstruction2 => "AMC_OTP_RECEIVER_INSTRUCTION_2";
  String get amcOtpMaskedPhone => "AMC_OTP_MASKED_PHONE";
  String get amcOtpNetworkWarning => "AMC_OTP_NETWORK_WARNING";
}

class AmcDraft {
  const AmcDraft();
  String get pendingOtpApproval => 'AMC_DRAFT_PENDING_OTP_APPROVAL';
  String get pendingApproval => 'AMC_DRAFT_PENDING_APPROVAL';
  String get noDraftsToDisplay => 'AMC_DRAFT_NO_DRAFTS_TO_DISPLAY';
  String get amcDraftPendingCompletionApproval =>
      'AMC_DRAFT_PENDING_COMPLETION_CODE_APPROVAL';
}

class AmcInbox {
  const AmcInbox();
  String get noVisitsToDisplay => 'AMC_INBOX_NO_VISITS_TO_DISPLAY';
}

class AmcHome {
  const AmcHome();
  String get formSchemaLoadFailed => 'AMC_HOME_FORM_SCHEMA_LOAD_FAILED';
  String get amcReport => 'AMC_HOME_AMC_REPORT';
  String get dataSync => 'AMC_HOME_DATA_SYNC';
}

class AmcReportHome {
  const AmcReportHome();
  String get installationReport => 'AMC_REPORT_HOME_INSTALLATION_REPORT';
  String get newAmcReport => 'AMC_REPORT_HOME_NEW_AMC_REPORT';
  String get newAmcReportDescription =>
      'AMC_REPORT_HOME_NEW_AMC_REPORT_DESCRIPTION';
  String get inboxDescription => 'AMC_REPORT_HOME_INBOX_DESCRIPTION';
  String get pendingApprovalDescription =>
      'AMC_REPORT_HOME_PENDING_APPROVAL_DESCRIPTION';
}

class AmcMediaUpload {
  const AmcMediaUpload();
  String get selfieInstruction => 'AMC_MEDIA_UPLOAD_SELFIE_INSTRUCTION';
  String get rejectionList => 'AMC_MEDIA_UPLOAD_REJECTION_LIST';
  String get noRejectionReasonsFound =>
      'AMC_MEDIA_UPLOAD_NO_REJECTION_REASONS_FOUND';
}

class AmcSelectFacility {
  const AmcSelectFacility();
  String get title => 'AMC_SELECT_FACILITY_TITLE';
  String get noVisitsFound => 'AMC_SELECT_FACILITY_NO_VISITS_FOUND';
  String get failedToLoadVisits => 'AMC_SELECT_FACILITY_FAILED_TO_LOAD_VISITS';
  String get amcDate => 'AMC_SELECT_FACILITY_AMC_DATE';
  String get report => 'AMC_SELECT_FACILITY_REPORT';
  String get submitForApproval => 'AMC_SELECT_FACILITY_SUBMIT_FOR_APPROVAL';
}

class Home {
  const Home();
  String get syncFailed => 'HOME_SYNC_FAILED';
  String get somethingWentWrong => 'HOME_SOMETHING_WENT_WRONG';
  String get allDraftsSynced => 'HOME_ALL_DRAFTS_SYNCED';
  String get lastSyncedPrefix => 'HOME_LAST_SYNCED_PREFIX';
  String get notSynced => 'HOME_NOT_SYNCED';
  String get dataNotSynced => 'HOME_DATA_NOT_SYNCED';
  String get skip => 'HOME_SKIP';
  String get syncData => 'HOME_SYNC_DATA';
  String get installationReport => 'HOME_INSTALLATION_REPORT';
  String get dataSync => 'HOME_DATA_SYNC';
}

class BomButtons {
  const BomButtons();
  String get installationCompletionCertificate =>
      'BOM_BUTTON_INSTALLATION_COMPLETION_CERTIFICATE';
}

class InstallationImages {
  const InstallationImages();
  String get couldNotFetchLocation =>
      'INSTALLATION_IMAGES_COULD_NOT_FETCH_LOCATION';
  String get noConfigurationFound =>
      'INSTALLATION_IMAGES_NO_CONFIGURATION_FOUND';
  String get title => 'INSTALLATION_IMAGES_TITLE';
  String get back => 'INSTALLATION_IMAGES_BACK';
}

class InstallationCompletionCertificate {
  const InstallationCompletionCertificate();
  String get title => 'INSTALLATION_COMPLETION_CERTIFICATE_TITLE';
  String get uploadPrompt =>
      'INSTALLATION_COMPLETION_CERTIFICATE_UPLOAD_PROMPT';
  String get uploadPdf => 'INSTALLATION_COMPLETION_CERTIFICATE_UPLOAD_PDF';
  String get uploadRequired =>
      'INSTALLATION_COMPLETION_CERTIFICATE_UPLOAD_REQUIRED';
  String get maxFilesAllowed =>
      'INSTALLATION_COMPLETION_CERTIFICATE_MAX_FILES_ALLOWED';
  String get filePathUnavailable =>
      'INSTALLATION_COMPLETION_CERTIFICATE_FILE_PATH_UNAVAILABLE';
  String get onlyPdfAllowed =>
      'INSTALLATION_COMPLETION_CERTIFICATE_ONLY_PDF_ALLOWED';
  String get acceptedFormats =>
      'INSTALLATION_COMPLETION_CERTIFICATE_ACCEPTED_FORMATS';
  String get maxFileSize => 'INSTALLATION_COMPLETION_CERTIFICATE_MAX_FILE_SIZE';
}

class OverallAssetSummary {
  const OverallAssetSummary();
  String get allAssetsSubmittedSuccessfully =>
      'OVERALL_ASSET_SUMMARY_ALL_ASSETS_SUBMITTED_SUCCESSFULLY';
  String get addMoreAssets => 'OVERALL_ASSET_SUMMARY_ADD_MORE_ASSETS';
  String get errorLoadingCounts => 'OVERALL_ASSET_SUMMARY_ERROR_LOADING_COUNTS';
  String get upload => 'OVERALL_ASSET_SUMMARY_UPLOAD';
  String get requiredInstallationCompletionCertificate =>
      'OVERALL_ASSET_SUMMARY_REQUIRED_INSTALLATION_COMPLETION_CERTIFICATE';
  String get uploadRequiredInstallationCompletionCertificate =>
      'OVERALL_ASSET_SUMMARY_UPLOAD_REQUIRED_INSTALLATION_COMPLETION_CERTIFICATE';
}

class SubmitForApproval {
  const SubmitForApproval();
  String get bomSyncFailed => 'SUBMIT_FOR_APPROVAL_BOM_SYNC_FAILED';
  String get requiredInstallationImages =>
      'SUBMIT_FOR_APPROVAL_REQUIRED_INSTALLATION_IMAGES';
  String get enterRequiredInstallationImages =>
      'SUBMIT_FOR_APPROVAL_ENTER_REQUIRED_INSTALLATION_IMAGES';
  String get requiredInstallationCompletionCertificate =>
      'SUBMIT_FOR_APPROVAL_REQUIRED_INSTALLATION_COMPLETION_CERTIFICATE';
  String get uploadRequiredInstallationCompletionCertificate =>
      'SUBMIT_FOR_APPROVAL_UPLOAD_REQUIRED_INSTALLATION_COMPLETION_CERTIFICATE';
  String get summary => 'SUBMIT_FOR_APPROVAL_SUMMARY';
  String get installationCompletionReport =>
      'SUBMIT_FOR_APPROVAL_INSTALLATION_COMPLETION_REPORT';
  String get scanUploadCompletionReport =>
      'SUBMIT_FOR_APPROVAL_SCAN_UPLOAD_COMPLETION_REPORT';
  String get upload => 'SUBMIT_FOR_APPROVAL_UPLOAD';
  String get rejectionList => 'SUBMIT_FOR_APPROVAL_REJECTION_LIST';
  String get noRejectionReasonsFound =>
      'SUBMIT_FOR_APPROVAL_NO_REJECTION_REASONS_FOUND';
  String get errorLoadingCounts => 'SUBMIT_FOR_APPROVAL_ERROR_LOADING_COUNTS';
  String get view => 'SUBMIT_FOR_APPROVAL_VIEW';
  String get rejectionReasons => 'SUBMIT_FOR_APPROVAL_REJECTION_REASONS';
  String get reason => 'SUBMIT_FOR_APPROVAL_REASON';
}

class PdfViewer {
  const PdfViewer();
  String get failedToLoadDocument => 'PDF_VIEWER_FAILED_TO_LOAD_DOCUMENT';
  String get pageOf => 'PDF_VIEWER_PAGE_OF';
  String get of => 'PDF_VIEWER_OF';
}

class SharedCards {
  const SharedCards();
  String get summary => 'SHARED_CARDS_SUMMARY';
  String get viewSummary => 'SHARED_CARDS_VIEW_SUMMARY';
  String get submitForApproval => 'SHARED_CARDS_SUBMIT_FOR_APPROVAL';
  String get viewReport => 'SHARED_CARDS_VIEW_REPORT';
  String get viewDetails => 'SHARED_CARDS_VIEW_DETAILS';
  String get resubmitForApproval => 'SHARED_CARDS_RESUBMIT_FOR_APPROVAL';
  String get addDetails => 'SHARED_CARDS_ADD_DETAILS';
}

class ProgressOverlay {
  const ProgressOverlay();
  String get somethingWentWrong => 'PROGRESS_OVERLAY_SOMETHING_WENT_WRONG';
  String get pleaseWait => 'PROGRESS_OVERLAY_PLEASE_WAIT';
  String get close => 'PROGRESS_OVERLAY_CLOSE';
}

class AssetTypeDetail {
  const AssetTypeDetail();
  String get defaultTodayDate => 'ASSET_TYPE_DETAIL_DEFAULT_TODAY_DATE';
}

class SelectAssetType {
  const SelectAssetType();
  String get title => 'SELECT_ASSET_TYPE_TITLE';
  String get selectAssetType => 'SELECT_ASSET_TYPE_SELECT_ASSET_TYPE';
}

class Specification {
  const Specification();
  String get totalCapacity => 'SPECIFICATION_TOTAL_CAPACITY';
}

class SubmittedSaveSuccess {
  const SubmittedSaveSuccess();
  String get title => 'SUBMITTED_SAVE_SUCCESS_TITLE';
  String get description => 'SUBMITTED_SAVE_SUCCESS_DESCRIPTION';
}

class SyncLoading {
  const SyncLoading();
  String get preparingSync => 'SYNC_LOADING_PREPARING_SYNC';
  String get successful => 'SYNC_LOADING_SUCCESSFUL';
  String get syncingReports => 'SYNC_LOADING_SYNCING_REPORTS';
  String get completedSuffix => 'SYNC_LOADING_COMPLETED_SUFFIX';
  String get of => 'SYNC_LOADING_OF';
}
