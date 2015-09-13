package com.websocket.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.websocket.log.Log;


@Configuration
@EnableWebMvcSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	@Qualifier("myUserDetailsService")
	UserDetailsService myUserDetailsService;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable()
        .authorizeRequests()
            .antMatchers("/resources/**").permitAll()
            .anyRequest().authenticated()
            .and()
        .formLogin()
            .loginPage("/")
            .loginProcessingUrl("/j_spring_security_check")
            .usernameParameter("j_username").passwordParameter("j_password")
            .successHandler(new MySimpleUrlAuthenticationSuccessHandler())
            .permitAll()
            .and()
        .logout()
        	.logoutSuccessUrl("/")
            .permitAll();
    }

    @Override
	@Autowired
    protected void configure(final AuthenticationManagerBuilder auth)throws Exception {
    	System.err.println("authorize!!");
        auth.userDetailsService(myUserDetailsService);
    }
    @RequestMapping(value="logout", method=RequestMethod.POST)
    protected @ResponseBody String logout(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
		securityContextLogoutHandler.logout(request, response, null);
		HttpSession session = request.getSession(false);
		if(request.isRequestedSessionIdValid()){
			session.invalidate();
		}
		Log.info("Logged out!!");
		return "{\"success\":\"true\", \"targetUrl\" : \"" + "\"}";
    }
}
