package fr.univlorraine.mondossierweb.controllers;

import java.util.Collection;

import javax.annotation.Resource;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.util.MethodInvocationUtils;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 * Gestion de l'utilisateur
 */
@Component
public class UserController {

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient UserDetailsService userDetailsService;
	@Resource
	private transient MethodSecurityInterceptor methodSecurityInterceptor;

	/**
	 * @return l'authentification courante
	 */
	public Authentication getCurrentAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	/**
	 * @param viewClass
	 * @return true si l'utilisateur peut accéder à la vue
	 */
	public boolean canCurrentUserAccessView(Class<? extends View> viewClass) {
		MethodInvocation methodInvocation = MethodInvocationUtils.createFromClass(viewClass, "enter");
		Collection<ConfigAttribute> configAttributes = methodSecurityInterceptor.obtainSecurityMetadataSource().getAttributes(methodInvocation);
		/* Renvoie true si la vue n'est pas sécurisée */
		if (configAttributes.isEmpty()) {
			return true;
		}
		/* Vérifie que l'utilisateur a les droits requis */
		try {
			methodSecurityInterceptor.getAccessDecisionManager().decide(getCurrentAuthentication(), methodInvocation, configAttributes);
		} catch (InsufficientAuthenticationException | AccessDeniedException e) {
			return false;
		}
		return true;
	}

	/**
	 * @return user utilisateur courant
	 */
	public UserDetails getCurrentUser() {
		return (UserDetails) getCurrentAuthentication().getPrincipal();
	}

	/**
	 * @return username de l'utilisateur courant
	 */
	public String getCurrentUserName() {
		return getCurrentAuthentication().getName();
	}

	/**
	 * @return true si l'utilisateur a pris le rôle d'un autre utilisateur
	 */
	public boolean isUserSwitched() {
		for (GrantedAuthority ga : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
			if (SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR.equals(ga.getAuthority())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Change le rôle de l'utilisateur courant
	 * @param username
	 */
	public void switchToUser(String username) {
		if (!StringUtils.hasText(username)) {
			throw new IllegalArgumentException("username ne peut être vide.");
		}

		/* Vérifie que l'utilisateur existe */
		try {
			userDetailsService.loadUserByUsername(username);
		} catch (UsernameNotFoundException unfe) {
			Notification.show(applicationContext.getMessage("admin.switchUser.usernameNotFound", new Object[] {username}, UI.getCurrent().getLocale()), Notification.Type.WARNING_MESSAGE);
			return;
		}

		String switchToUserUrl = environment.getRequiredProperty("switchUser.switchUrl") + "?" + SwitchUserFilter.SPRING_SECURITY_SWITCH_USERNAME_KEY + "=" + username;
		Page.getCurrent().open(switchToUserUrl, null);
	}

	/**
	 * Rétabli le rôle original de l'utilisateur
	 */
	public void switchBackToPreviousUser() {
		Page.getCurrent().open(environment.getRequiredProperty("switchUser.exitUrl"), null);
	}

	public boolean isEnseignant() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isEtudiant() {
		// TODO Auto-generated method stub
		return true;
	}

}
