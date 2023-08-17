package com.adobe.aem.guides.wknd.core.schedulers;

import com.adobe.aem.guides.wknd.core.filters.AuthHandlerFilter;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;

@Component(immediate = true, service = Runnable.class)
public class AuthCheckScheduler implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(AuthHandlerFilter.class);
    private static final String SCHEDULER_NAME = "authCheckScheduler";
    @Reference
    private Scheduler scheduler;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Activate
    protected void activate() throws LoginException {
        log.debug("QuotesScheduler activated");
        log.debug("cronExpression");
        try {
            ScheduleOptions scheduleOptions = scheduler.EXPR("0/5 * * * * ?");
            scheduleOptions.name(SCHEDULER_NAME);
            scheduleOptions.canRunConcurrently(false);
            scheduler.schedule(this, scheduleOptions);

        } catch (Exception e) {
            log.debug("Error scheduling QuotesScheduler", e);
        }
    }

    @Override
    public void run() {
        String serviceUser = "google-auth-user";
        try {
            log.debug("working");
            Map<String, Object> param = new HashMap<String, Object>();
            param.put(ResourceResolverFactory.SUBSERVICE, serviceUser);
            ResourceResolver resolver = resolverFactory.getServiceResourceResolver(param);

            String pagePath = "/content/wknd/us/en.html"; // Replace with your actual page path
            Resource pageResource = resolver.getResource(pagePath);
            String targetPath = "/content/index.html"; // Replace with your target page path
            pageResource.getResourceMetadata().put("sling:target", targetPath);
            pageResource.getResourceMetadata().put("sling:status", 302); // Optional: Set the HTTP status code (e.g., 301 or 302)
            HttpCookie request = resolver.adaptTo(HttpCookie.class);
        log.debug(String.valueOf(request));
        } catch (Exception e) {
            log.debug("Error : "+e);
            throw new RuntimeException(e);

        }
    }
}