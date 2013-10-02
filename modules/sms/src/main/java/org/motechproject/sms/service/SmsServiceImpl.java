package org.motechproject.sms.service;

import org.motechproject.server.config.SettingsFacade;
import org.motechproject.sms.model.OutgoingSms;
import org.motechproject.sms.model.Settings;
import org.motechproject.sms.model.SettingsDto;
import org.motechproject.sms.model.Templates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service("smsService")
public class SmsServiceImpl implements SmsService {

    private SettingsDto settings;
    private Templates templates;
    private static Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Autowired
    public SmsServiceImpl(@Qualifier("smsSettings") final SettingsFacade settingsFacade) {
        this.templates = new Templates(settingsFacade);
        this.settings = new Settings(settingsFacade).getSettingsDto();
    }

    @Override
    /**
     * TODO
     */
    public void send(final OutgoingSms outgoingSms){
        String config = outgoingSms.getConfig();
        String template;
        Properties templateProperties;
/*
        if (config == null) {
            logger.info("No config specified, using default config.");
            config = settings.getDefaultConfig();
        }
        else if (!settings.getConfigs().) {
            throw new IllegalArgumentException(String.format("The configuration '%s' is invalid or does not exist.", config));
        }
        template = config.get
        logger.info("Using config: " + config);
*/
        //throw new SmsDeliveryFailureException("Hello, world!");

        //TODO: send sms here!
    }
}
