package com.adobe.aem.guides.wknd.core.listeners;

import com.day.cq.wcm.api.PageEvent;
import com.day.cq.wcm.api.PageModification;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.adobe.aem.guides.wknd.core.filters.AuthHandlerFilter;
import org.apache.sling.event.jobs.Job;

import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;


@Component(service = EventHandler.class,
        immediate = true,
        property = {
                EventConstants.EVENT_TOPIC + "=" + PageEvent.EVENT_TOPIC,

        })
public class PageRefreshListener implements EventHandler {
    private static final Logger log = LoggerFactory.getLogger(AuthHandlerFilter.class);

    public void handleEvent(final Event event)  {

        try {
            Iterator<PageModification> pageInfo=PageEvent.fromEvent(event).getModifications();
            while (pageInfo.hasNext()) {
                PageModification pageModification = pageInfo.next();
                String path = pageModification.getPath();
                log.debug(path);
            }
        }catch (Exception e){
//                LOG.debug("\n Error while Activating/Deactivating - {} " , e.getMessage());
        }

    }
}
