package org.motechproject.sms.service;

import org.joda.time.DateTime;
import org.motechproject.event.MotechEvent;
import org.motechproject.sms.event.SmsEvents;

import java.util.List;
import java.util.Map;
import java.util.Objects;


public class OutgoingSms {
    private List<String> recipients;
    private String message;
    private String config;
    private DateTime deliveryTime;
    private Integer failureCount = 0;
    private String motechId;
    private String providerId;

    public OutgoingSms() {
    }

    public OutgoingSms(MotechEvent event) {
        Map<String, Object> params = event.getParameters();
        config = (String) params.get(SmsEvents.CONFIG);
        recipients = (List<String>) params.get(SmsEvents.RECIPIENTS);
        message = (String) params.get(SmsEvents.MESSAGE);
        deliveryTime = (DateTime) params.get(SmsEvents.DELIVERY_TIME);
        if (params.containsKey(SmsEvents.FAILURE_COUNT)) {
            failureCount = (Integer) params.get(SmsEvents.FAILURE_COUNT);
        }
        motechId = (String) params.get(SmsEvents.MOTECH_ID);
        providerId = (String) params.get(SmsEvents.PROVIDER_ID);
    }

    public OutgoingSms(String config, List<String> recipients, String message, DateTime deliveryTime) {
        this.recipients = recipients;
        this.message = message;
        this.config = config;
        this.deliveryTime = deliveryTime;
    }

    public OutgoingSms(String config, List<String> recipients, String message) {
        this.recipients = recipients;
        this.message = message;
        this.config = config;
    }

    public OutgoingSms(List<String> recipients, String message, DateTime deliveryTime) {
        this.recipients = recipients;
        this.message = message;
        this.deliveryTime = deliveryTime;
    }

    public OutgoingSms(List<String> recipients, String message) {

        this.recipients = recipients;
        this.message = message;
    }

    public String getConfig() {
        return config;
    }

    public Boolean hasConfig() {
        return config != null && !config.isEmpty();
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DateTime getDeliveryTime() {
        return deliveryTime;
    }

    public Boolean hasDeliveryTime() {
        return deliveryTime != null;
    }


    public void setDeliveryTime(DateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public Boolean hasMotechId() {
        return motechId != null && !motechId.isEmpty();
    }

    public String getMotechId() {
        return motechId;
    }

    public void setMotechId(String motechId) {
        this.motechId = motechId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipients, message, deliveryTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final OutgoingSms other = (OutgoingSms) obj;

        return Objects.equals(this.config, other.config)
                && Objects.equals(this.recipients, other.recipients)
                && Objects.equals(this.message, other.message)
                && Objects.equals(this.deliveryTime, other.deliveryTime)
                && Objects.equals(this.failureCount, other.failureCount)
                && Objects.equals(this.motechId, other.motechId)
                && Objects.equals(this.providerId, other.providerId);
    }

    @Override
    public String toString() {
        return String.format(
            "Sms{config='%s', recipients='%s', message='%s', motechId='%s', providerId='%s', deliveryTime='%s', failureCount='%d'}",
            config, recipients, message.replace("\n", "\\n"), motechId, providerId, deliveryTime, failureCount);
    }
}
