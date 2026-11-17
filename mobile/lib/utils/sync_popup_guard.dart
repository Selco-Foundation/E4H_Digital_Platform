class SyncPopupGuard {
  SyncPopupGuard._();

  static bool _suppressNextHomePopup = false;

  static void suppressNextHomePopup() {
    _suppressNextHomePopup = true;
  }

  static bool consumeSuppression() {
    if (!_suppressNextHomePopup) return false;
    _suppressNextHomePopup = false;
    return true;
  }
}
