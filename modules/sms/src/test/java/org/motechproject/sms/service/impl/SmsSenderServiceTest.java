package org.motechproject.sms.service.impl;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.scheduler.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.sms.service.SmsService;
import org.motechproject.sms.service.SmsServiceImpl;
import org.motechproject.sms.templates.TemplateReader;

import static org.mockito.MockitoAnnotations.initMocks;

public class SmsSenderServiceTest {

    @Mock
    private SettingsFacade settings;
    @Mock
    private EventRelay eventRelay;
    @Mock
    MotechSchedulerService schedulerService;
    @Mock
    TemplateReader templateReader;

    @InjectMocks
    private SmsService smsSender = new SmsServiceImpl(settings, eventRelay, schedulerService, templateReader);

    @Before
    public void setUp() {
        initMocks(this);
    }
/*
    @Test
    public void shouldSendCriticalNotification() throws Exception {
        OutgoingSms outgoingSms = new OutgoingSms(Arrays.asList(new String[]{"+12065551212"}), "sample message");

        smsSender.send(outgoingSms);
    }
*/
}
