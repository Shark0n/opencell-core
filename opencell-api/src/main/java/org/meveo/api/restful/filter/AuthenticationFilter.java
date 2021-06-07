package org.meveo.api.restful.filter;

import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@WebFilter(filterName = "AuthenticationFilter", urlPatterns = { "/api/rest/v1/*" })
public class AuthenticationFilter implements Filter {

    public static ResteasyClient httpClient;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        httpClient = new ResteasyClientBuilder().build();
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        if ( httpServletRequest.getHeader("Authorization") != null ) {
            String base64Credentials = httpServletRequest.getHeader("Authorization").substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            // credentials = username:password
            final String[] values = credentials.split(":", 2);

            BasicAuthentication basicAuthentication = new BasicAuthentication( values[0], values[1] );
            httpClient.register(basicAuthentication);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
