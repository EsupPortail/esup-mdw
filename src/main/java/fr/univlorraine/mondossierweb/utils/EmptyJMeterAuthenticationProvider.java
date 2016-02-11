/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

public class EmptyJMeterAuthenticationProvider implements AuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		// String name = authentication.getName();
	       // String password = authentication.getCredentials().toString();
	 
	        // use the credentials to try to authenticate against the third party system

	          List<GrantedAuthority> grantedAuths = new ArrayList<>();
	            return new UsernamePasswordAuthenticationToken("toto54", "", grantedAuths);
	
	}

	 @Override
	    public boolean supports(Class<?> authentication) {
		 return true;
	       // return authentication.equals(UsernamePasswordAuthenticationToken.class);
	    }

}
