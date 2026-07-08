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

import com.vaadin.flow.component.UI;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesUtilisateur;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesUtilisateurPK;
import fr.univlorraine.mondossierweb.repositories.mdw.PreferencesUtilisateurRepository;
import fr.univlorraine.mondossierweb.security.MdwUserDetails;
import fr.univlorraine.mondossierweb.security.MdwUserDetailsService;
import fr.univlorraine.mondossierweb.security.SecurityUtil;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Gestion de l'utilisateur
 */
@Component
public class UserController {

	@Resource
	private PreferencesUtilisateurRepository preferencesUtilisateurRepository;

	/**
	 * Récupère le securityContext dans la session.
	 *
	 * @return securityContext associé à la session
	 */
	public SecurityContext getSecurityContextFromSession() {
		return SecurityUtil.getSecurityContext(UI.getCurrent()).orElse(null);
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
		// un gestionnaire est un enseignant ++, il a tous les droits des enseignants
		return getCurrentUser().isEnseignant() || getCurrentUser().isGestionnaire() ;
	}

	public boolean isGestionnaire() {
		return getCurrentUser().isGestionnaire();
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
		UI ui = UI.getCurrent();
		if (ui != null && ui.getSession() != null) {
			if (ui.getSession().getSession() != null) {
				ui.getSession().getSession().setAttribute(org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
			}
			ui.getSession().close();
		}
	}


}
