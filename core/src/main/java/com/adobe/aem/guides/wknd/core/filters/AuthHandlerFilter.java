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
    //            Session session = slingRequest.getResourceResolver().adaptTo(Session.class);
                String homePageUrl = "http://localhost:8080/index.html";
                String currentUrl = slingRequest.getRequestURL().toString();
//                String googleOAuthUrl = "https://accounts.google.com/signin/oauth/identifier?response_type=code&client_id=537884037165-vc1uv1kbkm67o98te1d8vsqhlditsg8g.apps.googleusercontent.com&redirect_uri=http://localhost:8080/content/wknd/us/en.html&scope=https://www.googleapis.com/auth/userinfo.profile&include_granted_scopes=true&state=pass-through-value";
                  String googleOAuthUrl = "https://accounts.google.com/signin/oauth/identifier?response_type=code&client_id=537884037165-vc1uv1kbkm67o98te1d8vsqhlditsg8g.apps.googleusercontent.com&redirect_uri=http://localhost:4503/bin/training/testservlet&scope=https://www.googleapis.com/auth/userinfo.profile&include_granted_scopes=true&state=pass-through-value";

                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                SlingHttpServletResponseWrapper responseWrapper = new SlingHttpServletResponseWrapper((SlingHttpServletResponse) response);
                String requestUrl = slingRequest.getRequestURL().toString();
                // Check if the user is hitting the root URL
                log.debug("request url is : " + requestUrl);
                if (requestUrl.startsWith("http://localhost:8080/") )
//                if (homePageUrl.equals(currentUrl))
                    {
                        Cookie tokenCookie = slingRequest.getCookie("access_token");
                        log.debug(String.valueOf(tokenCookie));
                        if(tokenCookie != null){
                            log.debug("Starts with host");
                            chain.doFilter(request, response);
                        }else {
                            log.debug("inside the else");
                            httpServletResponse.sendRedirect(googleOAuthUrl);
                        }

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

                if (callbackUrl.startsWith("http://localhost:8080/content/wknd/us/en.html")) {
                    String code = slingRequest.getParameter("code");

//                    if (code != null) {
//                        // Perform token exchange using the code
//                        String accessToken = exchangeCodeForAccessToken(code,httpServletResponse);
//
//                        if (accessToken != null) {
//                            log.debug("inside if");
//                            log.debug(accessToken);
//
//                            Cookie tokenCookie1 = new Cookie("access_token1", "access_token1");
//                            tokenCookie1.setMaxAge(36); // Set cookie expiration time (in seconds)
//                            tokenCookie1.setHttpOnly(true); // Make the cookie accessible only through HTTP
//
//                            // Add the new cookie to the captured response
//                            responseWrapper.addCookie(tokenCookie1);
//
//                            // Now, get the captured response and write it to the original response
//                            SlingHttpServletResponse capturedResponse = (SlingHttpServletResponse) responseWrapper.getResponse();
//                            capturedResponse.flushBuffer();
//
//
//                            scheduleTokenRevocation(accessToken, 10);
//
//                            // Use the access token for further API requests
//                            String profileData = fetchUserProfileData(accessToken);
//
//                        } else {
//                            // Handle token exchange failure
//                            log.debug("Token exchange failed");
//                        }
//                    }
                }
            }
        }

//        private String exchangeCodeForAccessToken(String code,HttpServletResponse httpServletResponse) {
//            try {
//                URL tokenUrl = new URL("https://accounts.google.com/o/oauth2/token");
//                HttpURLConnection connection = (HttpURLConnection) tokenUrl.openConnection();
//                connection.setRequestMethod("POST");
//                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                connection.setDoOutput(true);
//
//                String body = "code=" + code +
//                        "&client_id=" + CLIENT_ID +
//                        "&client_secret=" + CLIENT_SECRET +
//                        "&redirect_uri=http://localhost:8080/content/wknd/us/en.html" +
//                        "&grant_type=authorization_code";
//
//                connection.getOutputStream().write(body.getBytes());
//
//                InputStream responseStream = connection.getInputStream();
//                java.util.Scanner scanner = new java.util.Scanner(responseStream).useDelimiter("\\A");
//                String responseBody = scanner.hasNext() ? scanner.next() : "";
//    //            log.debug("responsebody" + responseBody);
//                scanner.close();
//                // Parse the JSON response and extract the access token
//                String accessToken = parseAccessToken(responseBody,httpServletResponse);
//
//    //            fetchUserEmail(accessToken);
//                return accessToken;
//
//
//            } catch (Exception e) {
//                log.error("Error exchanging code for access token: " + e.getMessage());
//                return null;
//            }
//        }
        private String parseAccessToken(String responseBody,HttpServletResponse httpServletResponse) {
            try {
                // Parse the JSON response using Jackson
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String accessToken = jsonNode.get("access_token").asText();
    //            log.debug(accessToken);

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


        private void scheduleTokenRevocation(String token, int delayInSeconds) {

            TimerTask tokenRevocationTask = new TimerTask() {
                @Override
                public void run() {
                    String accessToken = token;
                    if (accessToken != null) {
                        // Perform token revocation logic
                        revokeAccessToken(accessToken);
                        log.debug("Access token revoked");
                    }
                }
            };
        }


     private void revokeAccessToken(String accessToken) {
                try {
                    // Construct the token revocation URL
                    URL tokenRevocationUrl = new URL("https://accounts.google.com/o/oauth2/revoke");
                    HttpURLConnection connection = (HttpURLConnection) tokenRevocationUrl.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setDoOutput(true);

                    // Construct the request body
                    String requestBody = "token=" + accessToken;

                    // Write the request body to the connection's output stream
                    try (OutputStream outputStream = connection.getOutputStream()) {
                        outputStream.write(requestBody.getBytes());
                    }

                    // Get the response code
                    int responseCode = connection.getResponseCode();
                        log.debug("responseCode" + responseCode);
                    // Check if the token revocation was successful
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        log.debug("Access token revoked successfully");
                    } else {
                        log.debug("Failed to revoke access token");
                    }

                    connection.disconnect();
                } catch (Exception e) {
                    log.error("Error revoking access token: " + e.getMessage());
                }
            }

        // Method to check if a cookie is present in the request
        private boolean isCookiePresent(SlingHttpServletRequest request, String cookieName) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(cookieName)) {
                        return true;
                    }
                }
            }
            return false;
        }
        @Override
        public void destroy() {

        }
    }