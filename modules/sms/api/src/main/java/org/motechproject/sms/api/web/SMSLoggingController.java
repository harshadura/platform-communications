package org.motechproject.sms.api.web;

import org.motechproject.sms.api.service.SmsAuditService;
import org.motechproject.sms.api.service.SmsRecordSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class SMSLoggingController {

    @Autowired
    private SmsAuditService smsAuditService;

    @RequestMapping(value = "/smslogging", method = RequestMethod.GET)
    @ResponseBody
    public SmsLoggingRecords getSmsRecords(GridSettings settings) {
        SmsRecordSearchCriteria criteria = settings.toSmsRecordSearchCriteria();
        SmsRecords smsRecords = smsAuditService.findAllSmsRecords(criteria);

        return new SmsLoggingRecords(settings.getPage(), settings.getRows(), smsRecords);
    }

}
