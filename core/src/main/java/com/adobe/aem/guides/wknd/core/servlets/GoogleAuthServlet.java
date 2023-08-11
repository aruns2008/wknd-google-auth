package com.adobe.aem.guides.wknd.core.servlets;


import com.adobe.aem.guides.wknd.core.filters.AuthHandlerFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
        log.debug("inside the get");


        final Resource resource = req.getResource();
        resp.setContentType("text/plain");
        resp.getWriter().write("success");
        HttpSession session = req.getSession();
        session.setAttribute("access_token", "access_token1");

        String code = ((SlingHttpServletRequest) req).getParameter("code");
        log.debug("code = " + code);
        String token = exchangeCodeForAccessToken(code, req, resp);
        log.debug("token"+token);
        if (token != null) {
            Cookie tokenCookie = new Cookie("access_token", token);
            tokenCookie.setMaxAge(36);
            tokenCookie.setPath("/");
            ((SlingHttpServletResponse) resp).addCookie(tokenCookie);

            // Handle the OAuth callback and token exchange
            SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) resp;
            slingResponse.sendRedirect("http://localhost:8080/content/wknd/us/en.html");
            SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) req;
            String callbackUrl = slingRequest.getRequestURL().toString();
            log.debug(callbackUrl);

        }
    }

    private String exchangeCodeForAccessToken(String code, SlingHttpServletRequest req, SlingHttpServletResponse resp) {
        try {
            log.debug("inside methode" + code);

            URL tokenUrl = new URL("https://accounts.google.com/o/oauth2/token");
            HttpURLConnection connection = (HttpURLConnection) tokenUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            String body = "code=" + code +
                    "&client_id=" + CLIENT_ID +
                    "&client_secret=" + CLIENT_SECRET +
                    "&redirect_uri=http://localhost:4503/bin/training/testservlet" +
                    "&grant_type=authorization_code";

            connection.getOutputStream().write(body.getBytes());

            InputStream responseStream = connection.getInputStream();
            java.util.Scanner scanner = new java.util.Scanner(responseStream).useDelimiter("\\A");
            String responseBody = scanner.hasNext() ? scanner.next() : "";
            log.debug("responsebody : " + responseBody);
            scanner.close();
            // Parse the JSON response and extract the access token
            String accessToken = parseAccessToken(responseBody);
//            log.debug("accessToken : "+accessToken);
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



}