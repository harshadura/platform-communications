package org.motechproject.sms.web;

import org.motechproject.event.listener.EventRelay;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.sms.audit.SmsRecord;
import org.motechproject.sms.configs.Config;
import org.motechproject.sms.configs.ConfigReader;
import org.motechproject.sms.configs.Configs;
import org.motechproject.sms.service.SmsAuditService;
import org.motechproject.sms.templates.Status;
import org.motechproject.sms.templates.Template;
import org.motechproject.sms.templates.TemplateReader;
import org.motechproject.sms.templates.Templates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.motechproject.commons.date.util.DateUtil.now;
import static org.motechproject.sms.audit.SmsDeliveryStatus.DELIVERY_CONFIRMED;
import static org.motechproject.sms.audit.SmsDeliveryStatus.FAILURE_CONFIRMED;
import static org.motechproject.sms.audit.SmsType.OUTBOUND;
import static org.motechproject.sms.event.SmsEvents.makeOutboundSmsFailureEvent;
import static org.motechproject.sms.event.SmsEvents.makeOutboundSmsSuccessEvent;

/**
 * todo
 */
@Controller
@RequestMapping(value = "/status")
public class StatusController {

    private Logger logger = LoggerFactory.getLogger(StatusController.class);
    private ConfigReader configReader;
    private Configs configs;
    private Templates templates;
    private EventRelay eventRelay;
    private SmsAuditService smsAuditService;

    @Autowired
    public StatusController(@Qualifier("smsSettings") SettingsFacade settingsFacade, EventRelay eventRelay,
                            TemplateReader templateReader, SmsAuditService smsAuditService) {
        this.eventRelay = eventRelay;
        configReader = new ConfigReader(settingsFacade);
        configs = configReader.getConfigs();
        templates = templateReader.getTemplates();
        this.smsAuditService = smsAuditService;
    }

    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @RequestMapping(value = "/{configName}")
    public String handle(@PathVariable String configName, @RequestParam Map<String, String> params) {
        logger.info("Status, configName = {}, params = {}", configName, params);

        Config config;
        if (configs.hasConfig(configName)) {
            config = configs.getConfig(configName);
        }
        else {
            //todo: do we really want to do that?
            config = configs.getDefaultConfig();
        }
        Template template = templates.getTemplate(config.getTemplateName());
        logger.debug("template = {}", template);
        Status status = template.getStatus();
        logger.debug("status = {}", status);

        if (status.hasMessageIdKey() && params != null && params.containsKey(status.getMessageIdKey())) {
            String providerId = params.get(status.getMessageIdKey());

            if (status.hasStatusKey() && status.hasStatusSuccess()) {
                String statusString = params.get(status.getStatusKey());

                if (statusString != null && statusString.matches(status.getStatusSuccess())) {
                    //todo: matchup the messageid from the log and fetch the phone number
                    eventRelay.sendEventMessage(makeOutboundSmsSuccessEvent(configName, null, null, null, providerId,
                        now(), null));
                    smsAuditService.log(new SmsRecord(configName, OUTBOUND, null, null, now(), DELIVERY_CONFIRMED, null,
                        providerId, null));
                }
                else {
                    eventRelay.sendEventMessage(makeOutboundSmsFailureEvent(configName, null, null, null, providerId,
                            now(), null));
                    smsAuditService.log(new SmsRecord(configName, OUTBOUND, null, null, now(), FAILURE_CONFIRMED, null,
                            providerId, null));
                }
                return "Thanks!";
            }
        }

        logger.error("Status message received from provider, but no template support! Config: {}, Parameters: {}",
                configName, params);

        return "Thanks!";
    }
}
