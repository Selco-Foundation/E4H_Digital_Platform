
const SID_KEY = 'ga_session_id';
const LOGIN_SENT_KEY = 'ga_login_sent';

export function getSessionId() {
  let sid = sessionStorage.getItem(SID_KEY);
  if (!sid) {
    sid = Date.now() + '_' + Math.floor(Math.random() * 1e6);
    sessionStorage.setItem(SID_KEY, sid);
  }
  return sid;
}

export function wasLoginEventSent() {
  return sessionStorage.getItem(LOGIN_SENT_KEY) === '1';
}

export function markLoginEventSent() {
  sessionStorage.setItem(LOGIN_SENT_KEY, '1');
}
