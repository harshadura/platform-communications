package org.motechproject.sms.service;

import org.motechproject.sms.audit.SmsRecord;
import org.motechproject.sms.audit.SmsRecords;

import java.util.List;

public interface SmsAuditService {

    void log(SmsRecord smsRecord);

    List<SmsRecord> findAllSmsRecords();

    SmsRecords findAllSmsRecords(SmsRecordSearchCriteria criteria);

}
