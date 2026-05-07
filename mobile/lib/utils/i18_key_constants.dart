library i18;

const common = Common();
const welcome = Welcome();
const login = Login();
const forgotPassword = ForgotPassword();
const scanner = Scanner();

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
