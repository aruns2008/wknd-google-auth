package com.adobe.aem.guides.wknd.core.filters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.wrappers.SlingHttpServletResponseWrapper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TimerTask;

@Component(service = Filter.class, property = {
        "sling.filter.scope=REQUEST",
        "service.ranking:Integer=1000"
})

public class AuthHandlerFilter implements Filter {

    @Reference
    private ResourceResolverFactory resolverFactory;
    private static final Logger log = LoggerFactory.getLogger(AuthHandlerFilter.class);
    private static final String CLIENT_ID = "537884037165-vc1uv1kbkm67o98te1d8vsqhlditsg8g.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (request instanceof SlingHttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
            SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) response;
            String homePageUrl = "http://localhost:8080/index.html";
            String currentUrl = slingRequest.getRequestURL().toString();
//                String googleOAuthUrl = "https://accounts.google.com/signin/oauth/identifier?response_type=code&client_id=537884037165-vc1uv1kbkm67o98te1d8vsqhlditsg8g.apps.googleusercontent.com&redirect_uri=http://localhost:8080/content/wknd/us/en.html&scope=https://www.googleapis.com/auth/userinfo.profile&include_granted_scopes=true&state=pass-through-value";
            String googleOAuthUrl = "https://accounts.google.com/signin/oauth/identifier?response_type=code&client_id=537884037165-vc1uv1kbkm67o98te1d8vsqhlditsg8g.apps.googleusercontent.com&redirect_uri=http://localhost:4503/bin/training/testservlet&scope=https://www.googleapis.com/auth/userinfo.profile&include_granted_scopes=true&state=pass-through-value";

            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            SlingHttpServletResponseWrapper responseWrapper = new SlingHttpServletResponseWrapper((SlingHttpServletResponse) response);
            String requestUrl = slingRequest.getRequestURL().toString();

            // Check if the user is hitting the root URL
            if (requestUrl.startsWith("http://localhost:8080/") || requestUrl.startsWith("http://localhost:4503/bin/training/testservlet"))
//                if (homePageUrl.equals(currentUrl))
            {
                Cookie tokenCookie = slingRequest.getCookie("access_token");

                if(tokenCookie != null){
                    log.debug("Starts with host");
                    chain.doFilter(request, response);
                }else {
                    log.debug("inside the else");


                    request.getRequestDispatcher("/bin/training/testservlet").forward(request, response);
                }
                return;
            } else if (requestUrl.startsWith("https://accounts.google.com/")) {

            } else {

                chain.doFilter(request, response);
                // Proceed with authentication logic
                String userId = slingRequest.getResourceResolver().getUserID();
                if (userId != null && !userId.isEmpty()) {
                    // User is authenticated, allow the request to proceed

                    log.debug(userId);

                }
            }
            // Handle the OAuth callback and token exchange
            String callbackUrl = slingRequest.getRequestURL().toString();

            if (callbackUrl.startsWith("http://localhost:4503/")) {
                String code = slingRequest.getParameter("code");
                Cookie tokenCookie = slingRequest.getCookie("access_token");

                if(tokenCookie != null){
                    log.debug("Starts with host");
                    chain.doFilter(request, response);
                }else {
                    log.debug("inside the else");
                        request.getRequestDispatcher("/bin/training/testservlet").forward(request, response);}
                return;
            }
        }
    }

    @Override
    public void destroy() {

    }
}