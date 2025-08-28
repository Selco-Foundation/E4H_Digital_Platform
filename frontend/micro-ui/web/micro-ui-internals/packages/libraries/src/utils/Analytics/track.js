
import { gtag } from './gtag';
import { baseParams } from './baseParams';
import { EV } from './events';

export function trackLogin(rolesCsv = "unknown") {
  gtag("event", EV.USER_LOGIN, baseParams({
    user_role: rolesCsv
  }));
}

export function trackPageView(page_name, extra = {}) {
  gtag('event', EV.PAGE_VIEW, baseParams({ page_name, ...extra }));
}

export function trackButtonClick(button_name, extra = {}) {
  gtag('event', EV.BUTTON_CLICK, baseParams({ button_name, ...extra }));
}

export function trackSubmitTicket(extra = {}) {
  gtag('event', EV.SUBMIT_TICKET, baseParams({ button_name: 'submit_ticket', page_name: 'new_ticket_page', ...extra }));
}

export function trackMedia(action /* 'upload_media' | 'stream_video' | 'download_image' */, extra = {}) {
  gtag('event', action, baseParams(extra));
}

export function trackFilterUsage(filter_type, filter_value, extra = {}) {
  gtag('event', EV.FILTER_USAGE, baseParams({ filter_type, filter_value, ...extra }));
}
