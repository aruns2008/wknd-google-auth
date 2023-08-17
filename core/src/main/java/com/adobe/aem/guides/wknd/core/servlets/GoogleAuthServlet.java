package com.adobe.aem.guides.wknd.core.servlets;

import com.adobe.aem.guides.wknd.core.filters.AuthHandlerFilter;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(
        service = Servlet.class,immediate = true,
        property = {
                "sling.servlet.methods=" + HttpConstants.METHOD_POST,
                "sling.servlet.paths="+"/bin/training/testservlet"
        }
)
public class GoogleAuthServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(AuthHandlerFilter.class);
    private static final String CLIENT_ID = "537884037165-vc1uv1kbkm67o98te1d8vsqhlditsg8g.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";

    @Override
    protected void doGet(SlingHttpServletRequest req, SlingHttpServletResponse resp) throws ServletException, IOException {
        log.debug("inside the auth servlett");
        String googleOAuthUrl = "https://accounts.google.com/signin/oauth/identifier?response_type=code&client_id=537884037165-vc1uv1kbkm67o98te1d8vsqhlditsg8g.apps.googleusercontent.com&redirect_uri=http://localhost:4503/bin/training/login&scope=https://www.googleapis.com/auth/userinfo.profile&include_granted_scopes=true&state=pass-through-value";
        SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) resp;

        SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) req;
        final Resource resource = req.getResource();
        slingResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        slingResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        slingResponse.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        slingResponse.setHeader("Access-Control-Allow-Credentials", "true");


        log.debug("Resource Path" + resource.getPath());
        resp.setContentType("text/plain");
        resp.getWriter().write("success");

        slingResponse.sendRedirect(googleOAuthUrl);
    }
}