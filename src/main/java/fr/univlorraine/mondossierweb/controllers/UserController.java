/**
 *
 *  ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package fr.univlorraine.mondossierweb.controllers;

import java.util.Collection;

import javax.annotation.Resource;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.util.MethodInvocationUtils;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import com.vaadin.navigator.View;
import com.vaadin.ui.UI;

import fr.univlorraine.mondossierweb.entities.mdw.PreferencesUtilisateur;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesUtilisateurPK;
import fr.univlorraine.mondossierweb.repositories.mdw.PreferencesUtilisateurRepository;
import fr.univlorraine.mondossierweb.security.MdwUserDetails;
import fr.univlorraine.mondossierweb.security.MdwUserDetailsService;
import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeService;

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
	private transient MethodSecurityInterceptor methodSecurityInterceptor;

	@Resource
	private PreferencesUtilisateurRepository preferencesUtilisateurRepository;

	@Resource
	private transient ConfigController configController;
	
	@Resource
	private MultipleApogeeService multipleApogeeService;

	/**
	 * Récupère le securityContext dans la session.
	 *
	 * @return securityContext associé à la session
	 */
	public SecurityContext getSecurityContextFromSession() {
		if (UI.getCurrent() != null && UI.getCurrent().getSession() != null
				&& UI.getCurrent().getSession().getSession() != null) {
			return (SecurityContext) UI.getCurrent().getSession().getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
		}
		return null;
}

	/** @return l'authentification courante */
	public Authentication getCurrentAuthentication() {
		SecurityContext securityContext = getSecurityContextFromSession();
		if (securityContext == null) {
			return SecurityContextHolder.getContext().getAuthentication();
		} else {
			return securityContext.getAuthentication();
		}
	}

	/**
	 * @param viewClass
	 * @return true si l'utilisateur peut accéder à la vue
	 */
	public boolean canCurrentUserAccessView(Class<? extends View> viewClass) {
		MethodInvocation methodInvocation = MethodInvocationUtils.createFromClass(viewClass, "enter");
		Collection<ConfigAttribute> configAttributes = methodSecurityInterceptor.obtainSecurityMetadataSource().getAttributes(methodInvocation);
		// Renvoie true si la vue n'est pas sécurisée 
		if (configAttributes==null || configAttributes.isEmpty()) {
			return true;
		}
		// Vérifie que l'utilisateur a les droits requis 
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
	public MdwUserDetails getCurrentUser() {
		if (getCurrentAuthentication() != null) {
			return (MdwUserDetails) getCurrentAuthentication().getPrincipal();
		}
		return null;
	}

	/**
	 * @return username de l'utilisateur courant
	 */
	public String getCurrentUserName() {
		return getCurrentUser().getUsername();
	}
	
	public String getCodetu(){
		return getCurrentUser().getCodetu();
	}
	
	public boolean isAdmin() {
		return getCurrentUser().isAdmin();
	}

	public boolean isEnseignant() {
		return getCurrentUser().isEnseignant();
	}

	public boolean isEtudiant() {		
		return getCurrentUser().isEtudiant();
	}


	/**
	 * @param preference
	 * @return la valeur pour l'utilisateur de la préférence en parametre
	 */
	public String getPreference(String preference) {

		PreferencesUtilisateur pu = preferencesUtilisateurRepository.findOnePrefFromLoginAndPrefid(getCurrentUserName(), preference);

		if(pu!=null && pu.getId()!=null && pu.getValeur()!=null){
			return pu.getValeur();
		}

		return null;
	}

	/**
	 * Met à jour la valeur pour l'utilisateur de la préférence en parametre avec la valeur en parametre
	 * @param showMessageNotesPreference
	 * @param valeur
	 */
	@Procedure
	public void updatePreference(String showMessageNotesPreference, String valeur) {
		PreferencesUtilisateur pu = new PreferencesUtilisateur();
		PreferencesUtilisateurPK pupk = new PreferencesUtilisateurPK();
		pupk.setLogin(getCurrentUserName());
		pupk.setPrefid(showMessageNotesPreference);
		pu.setId(pupk);
		pu.setValeur(valeur);
		preferencesUtilisateurRepository.save(pu);

	}


	public boolean userCanAccessAdminView() {
		//On parcourt les droits
		for (GrantedAuthority ga : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
			//Si a l'autorisation de consulter la vue adminView
			if (MdwUserDetailsService.CONSULT_ADMINVIEW_AUTORISE.equals(ga.getAuthority())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Vérifie que l'utilisateur courant a un rôle dans ceux listés dans une propriété.
	 * @param propertyName propriété contenant les rôles
	 * @return true si l'utilisateur a un rôle
	 */
	public boolean hasRoleInProperty(final String propertyName) {

		return getCurrentAuthentication().getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch(propertyName::contains);
	}

	/** Nettoie la session */
	public void disconnectUser() {
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		SecurityContextHolder.setContext(context);
		UI.getCurrent().getSession().getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
		UI.getCurrent().getSession().close();
	}


}
