package org.motechproject.sms.service;

import org.joda.time.DateTime;
import org.motechproject.commons.api.Range;
import org.motechproject.commons.couchdb.query.QueryParam;
import org.motechproject.sms.audit.SmsDeliveryStatus;
import org.motechproject.sms.audit.SmsType;

import java.util.HashSet;
import java.util.Set;

public class SmsRecordSearchCriteria {

    private Set<SmsType> smsTypes = new HashSet<>();
    private String phoneNumber;
    private String messageContent;
    private Range<DateTime> messageTimeRange;
    private Set<SmsDeliveryStatus> smsDeliveryStatuses = new HashSet<>();
    private String referenceNumber;
    private QueryParam queryParam = new QueryParam();

    public SmsRecordSearchCriteria withSmsTypes(Set<SmsType> smsTypes) {
        this.smsTypes.addAll(smsTypes);
        return this;
    }

    public SmsRecordSearchCriteria withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public SmsRecordSearchCriteria withMessageContent(String messageContent) {
        this.messageContent = messageContent;
        return this;
    }

    public SmsRecordSearchCriteria withMessageTime(DateTime messageTime) {
        this.messageTimeRange = new Range<>(messageTime, messageTime);
        return this;
    }

    public SmsRecordSearchCriteria withMessageTimeRange(Range<DateTime> messageTimeRange) {
        this.messageTimeRange = messageTimeRange;
        return this;
    }

    public SmsRecordSearchCriteria withDeliveryStatuses(Set<SmsDeliveryStatus> smsDeliveryStatuses) {
        this.smsDeliveryStatuses.addAll(smsDeliveryStatuses);
        return this;
    }

    public SmsRecordSearchCriteria withReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
        return this;
    }

    public SmsRecordSearchCriteria withQueryParam(QueryParam queryParam) {
        this.queryParam = queryParam;
        return this;
    }

    // Getters

    public Set<String> getSmsTypes() {
        return toStringSet(smsTypes);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public Range<DateTime> getMessageTimeRange() {
        return messageTimeRange;
    }

    public Set<String> getSmsDeliveryStatuses() {
        return toStringSet(smsDeliveryStatuses);
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public QueryParam getQueryParam() {
        return queryParam;
    }

    private Set<String> toStringSet(Set<? extends Enum> items) {
        Set<String> itemStringSet = new HashSet<>();
        for (Enum item : items) {
            itemStringSet.add(item.name());
        }
        return itemStringSet;
    }
}
