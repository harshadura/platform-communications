package org.motechproject.sms.http;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.motechproject.config.service.ConfigurationService;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.scheduler.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.sms.alert.MotechStatusMessage;
import org.motechproject.sms.audit.SmsAuditService;
import org.motechproject.sms.audit.SmsRecord;
import org.motechproject.sms.configs.Config;
import org.motechproject.sms.configs.ConfigProp;
import org.motechproject.sms.configs.ConfigReader;
import org.motechproject.sms.configs.Configs;
import org.motechproject.sms.service.OutgoingSms;
import org.motechproject.sms.templates.Response;
import org.motechproject.sms.templates.Template;
import org.motechproject.sms.templates.TemplateReader;
import org.motechproject.sms.templates.Templates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.motechproject.commons.date.util.DateUtil.now;
import static org.motechproject.sms.audit.SmsType.OUTBOUND;
import static org.motechproject.sms.event.SmsEvents.outboundEvent;

/**
 * This is the main meat - here we talk to the providers using HTTP
 */
@Service
public class SmsHttpService {

    private Logger logger = LoggerFactory.getLogger(SmsHttpService.class);
    private ConfigReader configReader;
    private Configs configs;
    private Templates templates;
    @Autowired
    private EventRelay eventRelay;
    @Autowired
    private HttpClient commonsHttpClient;
    @Autowired
    private MotechSchedulerService schedulerService;
    @Autowired
    private SmsAuditService smsAuditService;
    @Autowired
    ConfigurationService configurationService;
    @Autowired
    MotechStatusMessage motechStatusMessage;


    @Autowired
    public SmsHttpService(@Qualifier("smsSettings") SettingsFacade settingsFacade, TemplateReader templateReader) {

        //todo: unified module-wide caching & refreshing strategy
        configReader = new ConfigReader(settingsFacade);
        templates = templateReader.getTemplates();
    }

    static private String printableMethodParams(HttpMethod method) {
        if (method.getClass().equals(PostMethod.class)) {
            PostMethod p = (PostMethod)method;
            StringBuilder sb = new StringBuilder();
            for(org.apache.commons.httpclient.NameValuePair pair : p.getParameters()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(String.format("%s: %s", pair.getName(), pair.getValue()));
            }
            return "POST Parameters: " + sb.toString();
        }
        else if (method.getClass().equals(GetMethod.class)) {
            GetMethod g = (GetMethod)method;
            return String.format("GET QueryString: %s", g.getQueryString());
        }

        throw new IllegalStateException(String.format("Unexpected HTTP method: %s", GetMethod.class));
    }

    private void handleAuth(Map<String, String> props, Config config, Template template) {
        if (props.containsKey("username") && props.containsKey("password")) {
            String u = props.get("username");
            String p = props.get("password");
            commonsHttpClient.getParams().setAuthenticationPreemptive(true);
            commonsHttpClient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(u, p));
        }
        else {
            String message;
            if (props.containsKey("username")) {
                message = String.format("Config %s: missing password", config.getName());
            }
            else if (props.containsKey("password")) {
                message = String.format("Config %s: missing username", config.getName());
            }
            else {
                message = String.format("Config %s: missing username and password", config.getName());
            }
            motechStatusMessage.alert(message);
            throw new IllegalStateException(message);
        }
    }

    private void delayProviderAccess(Template template) {
        //todo: serialize access to configs, ie: one provider may allow 100 sms/min and another may allow 10...
        //This prevents us from sending more messages per second than the provider allows
        Integer milliseconds = template.getOutgoing().getMillisecondsBetweenMessages();
        logger.debug("Sleeping {}ms", milliseconds);
        try {
            Thread.sleep(milliseconds);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        logger.debug("Thread id {}", Thread.currentThread().getId());
    }

    private Map<String, String> generateProps(OutgoingSms sms, Template template, Config config) {
        Map<String, String> props = new HashMap<String, String>();
        props.put("recipients", template.recipientsAsString(sms.getRecipients()));
        props.put("message", sms.getMessage());
        props.put("motechId", sms.getMotechId());
        props.put("callback", configurationService.getPlatformSettings().getServerUrl() + "/module/sms/status/" +
                config.getName());

        for (ConfigProp configProp : config.getProps()) {
            props.put(configProp.getName(), configProp.getValue());
        }

        // ***** WARNING *****
        // This displays usernames & passwords in the server log! But then again, so does the settings UI...
        // ***** WARNING *****
        if (logger.isDebugEnabled()) {
            for (String key : props.keySet()) {
                logger.debug("PROP {}: {}", key, props.get(key));
            }
        }

        return props;
    }

    public synchronized void send(OutgoingSms sms) {

        //todo: right now we reload the configs for every call, but when we switch to the new config system we should
        //todo: be able to cache that and only reload when the config system detects a change.
        configs = configReader.getConfigs();

        Config config = configs.getConfigOrDefault(sms.getConfig());
        Template template = templates.getTemplate(config.getTemplateName());
        HttpMethod httpMethod = null;
        Integer failureCount = sms.getFailureCount();
        Integer httpStatus = null;
        String httpResponse = null;
        String errorMessage = null;
        Map<String, String> props = generateProps(sms, template, config);
        List<MotechEvent> events = new ArrayList<MotechEvent>();
        List<SmsRecord> auditRecords = new ArrayList<SmsRecord>();

        try {
            httpMethod = template.generateRequestFor(props);
            logger.debug(printableMethodParams(httpMethod));

            if (template.getOutgoing().hasAuthentication()) {
                handleAuth(props, config, template);
            }

            httpStatus = commonsHttpClient.executeMethod(httpMethod);
            httpResponse = httpMethod.getResponseBodyAsString();
        }
        catch (IllegalArgumentException e) {
            // generateRequestFor will thrown that if the URL from the template is invalid
            errorMessage = String.format("Invalid URL in '%s' template? %s", template.getName(), e.toString());
        }
        catch (IOException e) {
            errorMessage = String.format("Problem with '%s' template? %s", template.getName(), e.toString());
        }
        finally {
            if (httpMethod != null) {
                httpMethod.releaseConnection();
            }
        }

        delayProviderAccess(template);

        Response templateResponse = template.getOutgoing().getResponse();
        if (httpStatus == null || !templateResponse.isSuccessStatus(httpStatus)) {
            failureCount = failureCount + 1;
            if (httpStatus == null) {
                String msg = String.format("Delivery to SMS provider failed: %s", errorMessage);
                logger.error(msg);
                motechStatusMessage.alert(msg);
            }
            else {
                errorMessage = templateResponse.extractGeneralFailureMessage(httpResponse);
                if (errorMessage == null) {
                    motechStatusMessage.alert(String.format("Unable to extract failure message for '%s' config: %s",
                            config.getName(), httpResponse));
                    errorMessage = httpResponse;
                }
                logger.error("Delivery to SMS provider failed with HTTP {}: {}", httpStatus, errorMessage);
            }
            for (String recipient : sms.getRecipients()) {
                auditRecords.add(new SmsRecord(config.getName(), OUTBOUND, recipient, sms.getMessage(), now(),
                        config.RetryOrAbortStatus(failureCount), null, sms.getMotechId(), null, errorMessage));
            }
            events.add(outboundEvent(config.RetryOrAbortSubject(failureCount), config.getName(), sms.getRecipients(),
                    sms.getMessage(), sms.getMotechId(), null, sms.getFailureCount()+1, null, null));
        }
        else {
            ResponseHandler handler;
            if (templateResponse.supportsSingleRecipientResponse()) {
                if (sms.getRecipients().size() == 1 && templateResponse.supportsSingleRecipientResponse()) {
                    handler = new MultilineSingleResponseHandler(template, config);
                }
                else {
                    handler = new MultilineResponseHandler(template, config);
                }
            }
            else {
                handler = new GenericResponseHandler(template, config);
            }

            handler.handle(sms, httpResponse, httpMethod.getResponseHeaders());
            events = handler.getEvents();
            auditRecords = handler.getAuditRecords();
        }

        for (MotechEvent event : events) {
            eventRelay.sendEventMessage(event);
        }

        for (SmsRecord smsRecord : auditRecords) {
            smsAuditService.log(smsRecord);
        }
    }
}
