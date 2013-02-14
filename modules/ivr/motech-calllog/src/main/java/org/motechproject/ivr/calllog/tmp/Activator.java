package org.motechproject.ivr.calllog.tmp;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Activator implements BundleActivator {
    private static Logger logger = LoggerFactory.getLogger(Activator.class);
    private HttpServiceTracker tracker;

    @Override
    public void start(BundleContext context) {
        this.tracker = new HttpServiceTracker(context, resourceMappings());
        tracker.start();
        logger.debug(String.format("Started bundle: [%d] %s", context.getBundle().getBundleId(),
                context.getBundle().getSymbolicName()));
    }

    public void stop(BundleContext context) {
        this.tracker.close();
        tracker.unregister();
    }

    protected Map<String, String> resourceMappings() {
        return null;
    }
}
