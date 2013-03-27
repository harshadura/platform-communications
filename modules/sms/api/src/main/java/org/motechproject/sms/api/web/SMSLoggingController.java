package org.motechproject.sms.api.web;

import org.motechproject.sms.api.domain.SmsRecord;
import org.motechproject.sms.api.service.SmsAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
public class SMSLoggingController {

    @Autowired
    private SmsAuditService smsAuditService;

    @RequestMapping(value = "/smslogging", method = RequestMethod.GET)
    public SmsLoggingRecords getSmsRecords(GridSettings param) {
        List<SmsRecord> records = smsAuditService.findAllSmsRecords();
        return null;
    }

}
