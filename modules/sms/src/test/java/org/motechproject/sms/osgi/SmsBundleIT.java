package org.motechproject.sms.osgi;

import org.junit.After;
import org.junit.Before;
import org.motechproject.testing.osgi.BaseOsgiIT;

import java.util.List;

import static java.util.Arrays.asList;

public class SmsBundleIT extends BaseOsgiIT {



    @Before
    public void onSetUp() {
    }
/*
    public void testSmsService() throws IOException, Exception {

        SmsService smsService = (SmsService) applicationContext.getBean("smsSenderService");

        smsService.send(new OutgoingSms(Arrays.asList(new String[]{"+12065551212"}), "test message"));

        //do test the service, dude!!! assertEquals("test", actualText.trim());

    }
*/
    @After
    public void onTearDown() {
    }


    @Override
    protected List<String> getImports() {
        return asList(
                "org.motechproject.event.listener"
        );
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[]{"/META-INF/spring/testblueprint.xml"};
    }
}
