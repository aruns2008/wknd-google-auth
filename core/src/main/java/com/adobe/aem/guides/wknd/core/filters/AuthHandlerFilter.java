package com.adobe.aem.guides.wknd.core.filters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Component(service = Filter.class, property = {
        "sling.filter.scope=REQUEST",
        "service.ranking:Integer=1000"
})
public class AuthHandlerFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(AuthHandlerFilter.class);
    private static final String CLIENT_ID = "537884037165-vc1uv1kbkm67o98te1d8vsqhlditsg8g.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (request instanceof SlingHttpServletRequest) {
            SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;

            String homePageUrl = "http://localhost:8080/index.html";
            String currentUrl = slingRequest.getRequestURL().toString();
            String googleOAuthUrl = "https://accounts.google.com/signin/oauth/identifier?response_type=code&client_id=537884037165-vc1uv1kbkm67o98te1d8vsqhlditsg8g.apps.googleusercontent.com&redirect_uri=http://localhost:8080/content/wknd/us/en.html&scope=https://www.googleapis.com/auth/userinfo.profile&include_granted_scopes=true&state=pass-through-value";

            // Check if the user is hitting the root URL
            if (homePageUrl.equals(currentUrl)) {
                // Construct the Google OAuth URL
                // Send a redirect to the Google OAuth URL
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                httpServletResponse.sendRedirect(googleOAuthUrl);

                return;
            } else {

                // Proceed with authentication logic
                String userId = slingRequest.getResourceResolver().getUserID();
                if (userId != null && !userId.isEmpty()) {
                    // User is authenticated, allow the request to proceed
                    boolean isAllowedDomain = isAllowedDomain(userId);
                    log.debug(userId);

                    chain.doFilter(request, response);}
                 else {
                    // User is not authenticated, redirect to a login page
                    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                    httpServletResponse.sendRedirect(googleOAuthUrl);
                }
            }
            // Handle the OAuth callback and token exchange
            String callbackUrl = slingRequest.getRequestURL().toString();
            if (callbackUrl.startsWith("http://localhost:8080/content/wknd/us/en.html")) {
                String code = slingRequest.getParameter("code");
                log.debug("codes = " + code);
                if (code != null) {
                    // Perform token exchange using the code
                    String accessToken = exchangeCodeForAccessToken(code);

                    if (accessToken != null) {

                        HttpSession session = slingRequest.getSession(true);
                        session.setAttribute("access_token", accessToken);

                        // Create a new cookie to store the access token
                        Cookie tokenCookie = new Cookie("access_token", accessToken);
                        tokenCookie.setMaxAge(3600); // Set cookie expiration time (in seconds)
                        tokenCookie.setPath("/"); // Set cookie path to root ("/") to make it accessible across the site

                        // Add the cookie to the response
                        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                        httpServletResponse.addCookie(tokenCookie);
                        // Use the access token for further API requests
                        String profileData = fetchUserProfileData(accessToken);
                        log.debug("User profile data: " + profileData);
                    } else {
                        // Handle token exchange failure
                        log.debug("Token exchange failed");
                    }
                }
            }
        }
    }

    private String exchangeCodeForAccessToken(String code) {
        try {
            URL tokenUrl = new URL("https://accounts.google.com/o/oauth2/token");
            HttpURLConnection connection = (HttpURLConnection) tokenUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            String body = "code=" + code +
                    "&client_id=" + CLIENT_ID +
                    "&client_secret=" + CLIENT_SECRET +
                    "&redirect_uri=http://localhost:8080/content/wknd/us/en.html" +
                    "&grant_type=authorization_code";

            connection.getOutputStream().write(body.getBytes());

            InputStream responseStream = connection.getInputStream();
            java.util.Scanner scanner = new java.util.Scanner(responseStream).useDelimiter("\\A");
            String responseBody = scanner.hasNext() ? scanner.next() : "";
            log.debug("responsebody" + responseBody);
            scanner.close();
            // Parse the JSON response and extract the access token
            String accessToken = parseAccessToken(responseBody);

//            fetchUserEmail(accessToken);
            return accessToken;


        } catch (Exception e) {
            log.error("Error exchanging code for access token: " + e.getMessage());
            return null;
        }
    }
    private String parseAccessToken(String responseBody) {
        try {
            // Parse the JSON response using Jackson
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String accessToken = jsonNode.get("access_token").asText();
            log.debug(accessToken);
            return accessToken;
        } catch (Exception e) {
            log.error("Error parsing access token: " + e.getMessage());
            return null;
        }
    }
    private String fetchUserProfileData(String accessToken) {
        try {
            URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            InputStream responseStream = connection.getInputStream();
            java.util.Scanner scanner = new java.util.Scanner(responseStream).useDelimiter("\\A");
            String responseBody = scanner.hasNext() ? scanner.next() : "";
            scanner.close();

            return responseBody;
        } catch (Exception e) {
            log.error("Error fetching user profile data: " + e.getMessage());
            return null;
        }
    }


    private boolean isAllowedDomain(String userEmail) {
        // Extract the domain from the email address
        int atIndex = userEmail.indexOf('@');
        if (atIndex != -1) {
            String domain = userEmail.substring(atIndex + 1);
            // Check if the domain is allowed
            return "auki.com".equals(domain);
        }
        return false;
    }

//    private String fetchUserEmail(String accessToken) {
//        try {
//            // Use the discovery document to get the correct userinfo endpoint URL
//            URL discoveryUrl = new URL("https://accounts.google.com/.well-known/openid-configuration");
//            HttpURLConnection discoveryConnection = (HttpURLConnection) discoveryUrl.openConnection();
//            InputStream discoveryStream = discoveryConnection.getInputStream();
//            java.util.Scanner discoveryScanner = new java.util.Scanner(discoveryStream).useDelimiter("\\A");
//            String discoveryResponse = discoveryScanner.hasNext() ? discoveryScanner.next() : "";
//            discoveryScanner.close();
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode discoveryNode = objectMapper.readTree(discoveryResponse);
//            String userinfoEndpoint = discoveryNode.get("userinfo_endpoint").asText();
//
//            // Fetch user profile using the userinfo endpoint
//            URL userinfoUrl = new URL(userinfoEndpoint);
//            HttpURLConnection connection = (HttpURLConnection) userinfoUrl.openConnection();
//            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
//
//            InputStream responseStream = connection.getInputStream();
//            java.util.Scanner scanner = new java.util.Scanner(responseStream).useDelimiter("\\A");
//            String responseBody = scanner.hasNext() ? scanner.next() : "";
//            log.debug("responseBody new"+responseBody);
//            scanner.close();
//            ObjectMapper objectMapper1 = new ObjectMapper();
//            JsonNode jsonNode = objectMapper1.readTree(responseBody);
//            String email = jsonNode.get("sub").asText();
//            // Parse the JSON response and extract the email address
//
//            return email;
//        } catch (Exception e) {
//            log.error("Error fetching user email: " + e.getMessage());
//            return null;
//        }
//
//    }

    @Override
    public void destroy() {

    }
}