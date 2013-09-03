package org.motechproject.sms.model;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.motechproject.sms.json.SmsDeserializer;

import java.util.Objects;

@JsonDeserialize(using = SmsDeserializer.class)
public class Sms {
    private String fromAddress;
    private String toAddress;
    private String subject;
    private String message;

    public Sms(String fromAddress, String toAddress, String subject, String message) {
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.subject = subject;
        this.message = message;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public String getSubject() {
        return subject;
    }

    /**
     * @deprecated As of release 0.21, replaced by {@link #getMessage()}
     */
    @Deprecated
    public String getText() {
        return message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromAddress, toAddress, subject, message);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final Sms other = (Sms) obj;

        return Objects.equals(this.fromAddress, other.fromAddress)
                && Objects.equals(this.toAddress, other.toAddress)
                && Objects.equals(this.subject, other.subject)
                && Objects.equals(this.message, other.message);
    }

    @Override
    public String toString() {
        return String.format(
                "Sms{fromAddress='%s', toAddress='%s', subject='%s', message='%s'}",
                fromAddress, toAddress, subject, message);
    }
}
