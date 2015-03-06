package fr.univlorraine.mondossierweb.controllers;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.aopalliance.intercept.MethodInvocation;
import org.esupportail.portal.ws.client.PortalGroup;
import org.esupportail.portal.ws.client.PortalUser;
import org.esupportail.portal.ws.client.support.uportal.CachingUportalServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.query.Procedure;
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
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.util.MethodInvocationUtils;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

import fr.univlorraine.mondossierweb.GenericUI;
import fr.univlorraine.mondossierweb.entities.Administrateurs;
import fr.univlorraine.mondossierweb.entities.PreferencesUtilisateur;
import fr.univlorraine.mondossierweb.entities.PreferencesUtilisateurPK;
import fr.univlorraine.mondossierweb.entities.UtilisateurSwap;
import fr.univlorraine.mondossierweb.entities.apogee.Utilisateur;
import fr.univlorraine.mondossierweb.repositories.AdministrateursRepository;
import fr.univlorraine.mondossierweb.repositories.PreferencesUtilisateurRepository;
import fr.univlorraine.mondossierweb.repositories.UtilisateurSwapRepository;
import fr.univlorraine.mondossierweb.services.apogee.UtilisateurService;
import fr.univlorraine.mondossierweb.services.apogee.UtilisateurServiceImpl;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;

/**
 * Gestion de l'utilisateur
 */
@Component
public class UserController {

	private Logger LOG = LoggerFactory.getLogger(UserController.class);

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient UserDetailsService userDetailsService;
	@Resource
	private transient LdapUserSearch ldapUserSearch;
	@Resource
	private transient MethodSecurityInterceptor methodSecurityInterceptor;
	@Resource
	private transient CachingUportalServiceImpl portalService;
	/** {@link UtilisateurServiceImpl} */
	@Resource
	private UtilisateurService utilisateurService;
	@Resource
	private PreferencesUtilisateurRepository preferencesUtilisateurRepository;
	@Resource
	private AdministrateursRepository administrateursRepository;
	@Resource
	private UtilisateurSwapRepository utilisateurSwapRepository;




	/**
	 * type utilisateur étudiant.
	 */
	public static final String STUDENT_USER = "student";
	/**
	 * type correspondant à un utilisateur dont le login doit être exclu de l'application.
	 */
	public static final String LOGIN_EXCLU = "exclu";

	/**
	 * type utilisateur enseignant.
	 */
	public static final String TEACHER_USER = "teacher";

	/**
	 * type utilisateur non-autorisé.
	 */
	public static final String UNAUTHORIZED_USER = "unauthorized";




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
		if (configAttributes==null || configAttributes.isEmpty()) {
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
		String username= getCurrentAuthentication().getName();
		UtilisateurSwap us= utilisateurSwapRepository.findOne(username);
		if(us!=null && us.getLoginCible()!=null){
			//test si la date de création du swap utilisateur n'est pas plus vieille que 1 heure
			Calendar dateButoire = Calendar.getInstance(); 
			dateButoire.setTime(us.getDatCre());
			dateButoire.add(Calendar.HOUR_OF_DAY, Utils.NB_HEURE_DUREE_SWAP_USER);
			Date d = new Date();
			if(d.compareTo(dateButoire.getTime())<0){
				//swap autorise
				return us.getLoginCible();
			}
		}
		return username;
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



	public boolean isEnseignant() {
		//Un admin a les droits d'un enseignant
		if(isAdmin()){
			return true;
		}
		if(GenericUI.getCurrent().getTypeUser()==null){
			determineTypeUser();
		}
		if(GenericUI.getCurrent().getTypeUser()!=null && GenericUI.getCurrent().getTypeUser().equals(TEACHER_USER)){
			return true;
		}
		return false;
	}

	public boolean isEtudiant() {
		if(GenericUI.getCurrent().getTypeUser()==null){
			determineTypeUser();
		}
		if(GenericUI.getCurrent().getTypeUser()!=null && GenericUI.getCurrent().getTypeUser().equals(STUDENT_USER)){
			return true;
		}
		return false;
	}




	public void determineTypeUser() {

		GenericUI.getCurrent().setTypeUser(null);
		
		List<String> type = typeLdap(getCurrentUserName());

		if (StringUtils.hasText(PropertyUtils.getTypeEtudiantLdap()) && type!=null &&
				type.contains(PropertyUtils.getTypeEtudiantLdap())) { 
			GenericUI.getCurrent().setTypeUser(STUDENT_USER);
			GenericUI.getCurrent().getGoogleAnalyticsTracker().trackEvent(getClass().getSimpleName(), "Identification_etudiant","Authentification d'un étudiant");
		} else {

			//on cherche a savoir si l'employé a acces (ex: c'est un enseignant)
			//si il est autorisé type=enseignant, sinon type=non-autorise



			boolean useruportal = false;
			try {
				//on reucupère la liste de groupes mis dans le bean security
				List<String> listegroupes = PropertyUtils.getListeGroupesUportalAutorises();

				//on test si on est en portlet
				if (listegroupes != null && listegroupes.size()>0) {

					//recupère l'utilisateur uportal
					PortalUser portaluser = portalService.getUser(getCurrentUserName());

					//on cherche si il appartient a un groupe
					useruportal = false;



					//on regarde si il appartient a un des groupes
					for (String nomgroupe : listegroupes) {
						//si on est pas déjà sur qu'il appartient a un groupe:
						if(!useruportal) {
							//on cherche le groupe
							PortalGroup pgroup = portalService.getGroupByName(nomgroupe);
							if (pgroup != null) {
								//on regarde si l'utilisateur appartient a ce groupe
								if (portalService.isUserMemberOfGroup(portaluser, pgroup)) {
									//c'est un utilisateur uportal
									useruportal = true;
								}
							} 
						}
					}
				}
			} catch (Exception e) {
				//Test présence dans la table utilisateur de Apogee
				LOG.info("PROBLEME DE CONNEXION AUX GROUPES UPORTAL");
			}

			if (useruportal) {
				//c'est un utilisateur uportal il est donc autorisé en tant qu'enseignant
				LOG.info("USER "+getCurrentUserName()+" ENSEIGNANT VIA UPORTAL");
				GenericUI.getCurrent().setTypeUser(TEACHER_USER);
				GenericUI.getCurrent().getGoogleAnalyticsTracker().trackEvent(getClass().getSimpleName(), "Identification_enseignant", "Authentification d'un enseignant");


			} else {
				//va voir dans apogée
				LOG.info("USER "+getCurrentUserName()+" NON ENSEIGNANT VIA UPORTAL -> Recherche Apogée");


				//On test si on doit chercher l'utilisateur dans Apogee
				if(PropertyUtils.isLoginApogee()){
					//Test de la présence dans la table utilisateur d'Apogee
					//on regarde si il est dans la table utilisateur 
					try {
						Utilisateur uti = utilisateurService.findUtilisateur(getCurrentUserName().toUpperCase());

						if (uti != null) {
							LOG.info("USER "+getCurrentUserName()+" ENSEIGNANT VIA APOGEE.UTILISATEUR");
							GenericUI.getCurrent().setTypeUser(TEACHER_USER);
							GenericUI.getCurrent().getGoogleAnalyticsTracker().trackEvent(getClass().getSimpleName(), "Identification_enseignant","Authentification d'un enseignant");
						} else {
							GenericUI.getCurrent().setTypeUser(UNAUTHORIZED_USER);
							LOG.info("utilisateur "+getCurrentUserName()+" n' est pas dans le LDAP en tant qu' etudiant, n'appartient à aucun groupe uportal, et n'est pas dans la table utilisateur d'APOGEE -> UTILISATEUR NON AUTORISE !");

						}
					} catch (Exception ex) {
						LOG.error("Probleme lors de la vérification de l'existence de l'utilisateur dans la table Utilisateur de Apogee",ex);
					}
				}else{
					GenericUI.getCurrent().setTypeUser(UNAUTHORIZED_USER);
					LOG.info("Utilisateur "+getCurrentUserName()+" n' est pas dans le LDAP en tant qu' etudiant, n'appartient à aucun groupe uportal -> UTILISATEUR NON AUTORISE !");
				}


			}
		}

	}

	/**
	 * 
	 * @param login de l'utilisateur
	 * @return le type retourné par ldap.
	 */
	public List<String> typeLdap(final String login) {
		try {
			if(ldapUserSearch.searchForUser(getCurrentUserName())!=null){
				String[] vals= ldapUserSearch.searchForUser(getCurrentUserName()).getStringAttributes(PropertyUtils.getAttributLdapEtudiant());
				if(vals!=null){
					List<String> listeValeurs = Arrays.asList(vals);
					return listeValeurs;
				}
			}
			return null;
		} catch (Exception e) {
			LOG.error("Probleme à la recuperation de l'utilisateur : "+login+" dans le LDAP",e);
			return null;
		}
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

	public boolean isAdmin() {
		String login = getCurrentUserName();
		Administrateurs adm = administrateursRepository.findOne(login);
		if(adm!=null && adm.getLogin()!=null && adm.getLogin().equals(login)){
			
			//GenericUI.getCurrent().getGoogleAnalyticsTracker().trackEvent(getClass().getSimpleName(), "Identification_admin","Authentification d'un admin");
			
			return true;
		}
		return false;
	}

}
