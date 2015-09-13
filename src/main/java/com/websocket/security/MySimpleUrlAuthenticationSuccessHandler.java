package com.websocket.security;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.websocket.log.Log;

public class MySimpleUrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	 
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
    		Authentication authentication) throws IOException {
    	Log.info("Log in successful");
        handle(request, response, authentication);
        clearAuthenticationAttributes(request);
    }
 
    protected void handle(HttpServletRequest request, 
      HttpServletResponse response, Authentication authentication) throws IOException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().equals("USER")) {
            	Log.info("user is uses!!");
            	response.getWriter().print("{\"success\":\"true\", \"targetUrl\" : \"websocket" + "\"}");
                break;
            } else if (grantedAuthority.getAuthority().equals("ADMIN")) {
            	Log.info("User is admin");
            	response.getWriter().print("{\"success\":\"true\", \"targetUrl\" : \"websocket" + "\"}");
                break;
            }else{
            	response.getWriter().print("{\"success\":\"false\", \"targetUrl\" : \"" + "\"}");
                break;
            }
        }
        response.getWriter().flush();
    }
 
    protected void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
 
}
