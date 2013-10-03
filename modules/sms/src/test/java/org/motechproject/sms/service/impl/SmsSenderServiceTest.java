package org.motechproject.sms.service.impl;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.sms.service.SmsService;
import org.motechproject.sms.service.SmsServiceImpl;

import static org.mockito.MockitoAnnotations.initMocks;

public class SmsSenderServiceTest {

    @Mock
    private SettingsFacade settings;

    @InjectMocks
    private SmsService smsSender = new SmsServiceImpl(settings);

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