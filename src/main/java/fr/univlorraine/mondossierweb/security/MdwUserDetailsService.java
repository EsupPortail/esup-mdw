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
package fr.univlorraine.mondossierweb.security;

import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.converters.CodeEtudiantLoginConverterInterface;
import fr.univlorraine.mondossierweb.entities.apogee.Utilisateur;
import fr.univlorraine.mondossierweb.entities.mdw.Administrateurs;
import fr.univlorraine.mondossierweb.entities.mdw.UtilisateurSwap;
import fr.univlorraine.mondossierweb.repositories.mdw.AdministrateursRepository;
import fr.univlorraine.mondossierweb.repositories.mdw.UtilisateurSwapRepository;
import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeService;
import fr.univlorraine.mondossierweb.services.apogee.UtilisateurService;
import fr.univlorraine.mondossierweb.services.apogee.UtilisateurServiceImpl;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.esupportail.portal.ws.client.PortalGroup;
import org.esupportail.portal.ws.client.PortalUser;
import org.esupportail.portal.ws.client.support.uportal.CachingUportalServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;



@Component
//@Service("userDetailsService")
@Slf4j
public class MdwUserDetailsService implements UserDetailsService {




	public static final String CONSULT_DOSSIER_AUTORISE = "consultation_dossier";

	public static final String CONSULT_ADMINVIEW_AUTORISE = "consultation_adminView";


	@Resource
	private transient ConfigController configController;

	@Resource
	private MultipleApogeeService multipleApogeeService;

	@Resource
	private AdministrateursRepository administrateursRepository;

	@Resource
	private UtilisateurSwapRepository utilisateurSwapRepository;

	@Resource
	private transient LdapUserSearch ldapUserSearch;

	@Resource
	private transient CachingUportalServiceImpl portalService;

	/** {@link UtilisateurServiceImpl} */
	@Resource
	private UtilisateurService utilisateurService;

	@Resource(name="${codetuFromLogin.implementation}")
	private CodeEtudiantLoginConverterInterface daoCodeLoginEtudiant;

	/**
	 * Context http request
	 */
	@Autowired
	private HttpServletRequest request;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		//Au cas où on ait un swap d'utilisateur
		String finalusername = getCurrentUserName(username);

		log.info("loadUserByUsername : "+finalusername+" ("+username+")" );

		//Si le login utilisé est admin
		if(isAdmin(finalusername)){
			return new MdwUserDetails(finalusername,new String[]{Utils.ADMIN_USER}, true, getIP(request));
		}

		boolean canAccessAdminView = false;

		//Si il y un swap et que le login d'origne est admin
		if(!finalusername.equals(username) && isAdmin(username)){
			//On doit donner acces à la vue admin
			canAccessAdminView= true;
		}

		return new MdwUserDetails(finalusername,determineTypeUser(finalusername), canAccessAdminView, getIP(request));
	}



	private String getIP(HttpServletRequest hsRequest) {

		String ip = hsRequest.getHeader("x-forwarded-for");    
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
			ip = hsRequest.getHeader("X_FORWARDED_FOR");      
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
			ip = hsRequest.getHeader("HTTP_X_FORWARDED_FOR");      
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
			ip = hsRequest.getHeader("Proxy-Client-IP");      
		}   
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
			ip = hsRequest.getHeader("WL-Proxy-Client-IP");      
		}   
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
			ip = hsRequest.getRemoteAddr();     
		} 

		//Si contient plusieurs IP, on prend la deuxième
		if(StringUtils.hasText(ip) && ip.contains(",")){
			ip = ip.split(",")[1];
		}

		return ip;
	}



	private boolean isAdmin(String login) {
		DirContextOperations dco = getLdapEntry(login);
		// Si l'utilisateur est présent dans le ldap
		if(dco!=null) {
			//on recupère la liste de groupes ldap donnant le droit admin
			List<String> listegroupes = PropertyUtils.getListeGroupesLdapAdmin();
			// Si appartient au groupe ldap admin
			if(estDansLeGroupe(login, dco, PropertyUtils.getAttributGroupeLdap(), listegroupes)) {
				return true;
			}
			// Si le login est dans la table admin
			Administrateurs adm = administrateursRepository.findById(login).orElse(null);
			if(adm!=null && adm.getLogin()!=null && adm.getLogin().equals(login)){
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param login de l'utilisateur
	 * @return vrai si le compte est dans le ldap le type retourné par ldap.
	 */
	public DirContextOperations getLdapEntry(final String login) {
		try {
			return ldapUserSearch.searchForUser(login);
			/*if(ldapUserSearch.searchForUser(login)!=null){
				return true;
			}
			return false;*/
		} catch (Exception e) {
			log.error("Probleme à la recuperation de l'utilisateur : "+login+" dans le LDAP",e);
			return null;
		}
	}


	public String[] determineTypeUser(String username) {
		log.info("   determineTypeUser "+username);

		List<String> llogins=configController.getListeLoginsBloques();
		if(llogins!=null && llogins.contains(username)){
			log.info("utilisateur "+username+" bloqué car il a été exclu de l'application");
			return new String[]{Utils.UNAUTHORIZED_USER};

		}else{

			List<String> profisLdap = getLdapProfiles(username);

			boolean doctorantNonEnseignant = false;
			boolean doctorantNonGestionnaire = false;

			//Si un des profils du compte ldap correspond au profil gestionnaire (potentiellement "enseignant" en priorité)
			if (profisLdap != null && !profisLdap.isEmpty() && profisLdap.contains(Utils.LDAP_GEST)) { 
				return new String[]{Utils.GEST_USER};
			}
			
			//Si un des profils du compte ldap correspond au profil doctorant (potentiellement "enseignant" en priorité)
			if (profisLdap != null && !profisLdap.isEmpty() && profisLdap.contains(Utils.LDAP_DOCTORANT)) { 
				// On cherche d'abord à savoir si c'est un gestionnaire
				if(determineGestionnaire(username)) {
					return new String[]{Utils.GEST_USER};
				}
				doctorantNonGestionnaire = true;
				// Puis si c'est un enseignant
				if(determineEnseignant(username)) {
					return new String[]{Utils.TEACHER_USER};
				}
				doctorantNonEnseignant = true;
				
			}

			//Si un des profils du compte ldap correspond au profil étudiant
			if (profisLdap != null && !profisLdap.isEmpty() && profisLdap.contains(Utils.LDAP_ETUDIANT)) {

				//On vérifie si il y a un blocage sur l'étudiant
				String codetu = daoCodeLoginEtudiant.getCodEtuFromLogin(username);
				//On vérifie si l'étudiant est interdit de consultation de l'application
				List<String> lcodesBloquant = configController.getListeCodesBlocageAccesApplication();
				//Si on a paramétré des codes bloquant
				if(lcodesBloquant!=null && !lcodesBloquant.isEmpty()){
					//Récupération des éventuels blocage pour l'étudiant
					List<String> lblo = multipleApogeeService.getListeCodeBlocage(codetu);
					// Si l'étudiant a des blocages
					if(lblo!=null && !lblo.isEmpty()){
						//Parcours des blocage
						for(String codblo : lblo){
							//Si le blocage est dans la liste des blocages configurés comme bloquant
							if(codblo != null && lcodesBloquant.contains(codblo)){
								//étudiant non autorise a consulter ses notes
								log.info("utilisateur "+username+" bloqué car il possède des blocages dans Apogée");
								return new String[]{Utils.UNAUTHORIZED_USER};

							}
						}
					}
				}
				return new String[]{Utils.STUDENT_USER,codetu};
			}

			// Si c'est un gestionnaire (si c'est un doctorantNonGestionnaire on ne refait pas le test pour rien)
			if(!doctorantNonGestionnaire && determineGestionnaire(username)) {
				return new String[]{Utils.GEST_USER};
			}
			
			// Si c'est un enseignant (si c'est un doctorantNonEnseignant on ne refait pas le test pour rien)
			if(!doctorantNonEnseignant && determineEnseignant(username)) {
				return new String[]{Utils.TEACHER_USER};
			}

			log.info("utilisateur "+username+" n' est pas dans le LDAP en tant qu' etudiant, n'appartient à aucun groupe uportal, et n'est pas dans la table utilisateur d'APOGEE -> UTILISATEUR NON AUTORISE !");
			return new String[]{Utils.UNAUTHORIZED_USER};

		}
	}


	private boolean determineGestionnaire(String username) {
		// Inutile de rechercher dans le ldap car cette étape a été effectuée dans getLdapProfiles, appelée par determineTypeUser
		// On regarde si on doit chercher un utilisateur au profil GEST dans Apogee
		if(PropertyUtils.isLoginApogee() && PropertyUtils.getProfilUtilisateurApogee().equals(Utils.PROFIL_GEST)){
			return estUtilisateurApogeeValide(username);
		}
		return false;
	}


	private boolean estUtilisateurApogeeValide(String username) {
		//Test de la présence dans la table utilisateur d'Apogee
		//on regarde si il est dans la table utilisateur 
		try {
			Utilisateur uti = utilisateurService.findUtilisateur(username.toUpperCase());

			// Si l'utilisateur a été trouvé et qu'il est en service (si on doit tester le témoin En_SERVICE)
			if (uti != null && (uti.isTemEnService() || !PropertyUtils.isCheckTesUtilisateurApogee())) {
				log.info("USER "+username+" GESTIONNAIRE VIA APOGEE.UTILISATEUR. Profil : "+PropertyUtils.getProfilUtilisateurApogee());
				return true;
			} else {
				log.info("utilisateur "+username+" n'est pas dans la table utilisateur d'APOGEE ");
			}
		} catch (Exception ex) {
			log.error("Probleme lors de la vérification de l'existence de l'utilisateur "+username+" dans la table Utilisateur de Apogee",ex);
		}
		return false;
	}



	private boolean determineEnseignant(String username) {
		//on cherche a savoir si l'employé a acces (ex: c'est un enseignant)
		//si il est autorisé type=enseignant, sinon type=non-autorise

		boolean useruportal = false;
		try {
			//on reucupère la liste de groupes mis dans le bean security
			List<String> listegroupes = PropertyUtils.getListeGroupesUportalAutorises();

			//on test si on est en portlet
			if (listegroupes != null && !listegroupes.isEmpty()) {

				//recupère l'utilisateur uportal
				PortalUser portaluser = portalService.getUser(username);

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
			log.info("PROBLEME DE CONNEXION AUX GROUPES UPORTAL");
		}

		boolean userldap = false;
		//Si pas user uportal on va chercher dans le ldap si mdw est configuré pour cela
		if(!useruportal){
			//on recupère la liste de groupes ldap mis dans le bean security
			List<String> listegroupes = PropertyUtils.getListeGroupesLdapAutorises();

			//test si on a des groupes renseignes
			if (StringUtils.hasText(PropertyUtils.getAttributGroupeLdap()) && listegroupes != null && !listegroupes.isEmpty()) {
				//on recupère l'utilisateur ldap
				DirContextOperations dco = ldapUserSearch.searchForUser(username);
				userldap = estDansLeGroupe(username, dco, PropertyUtils.getAttributGroupeLdap(), listegroupes);
				if(!userldap){
					log.info("utilisateur "+username+" n'appartient à aucun groupe ldap autorises");
				}					
			}
		}

		if (useruportal || userldap) {
			//c'est un utilisateur uportal il est donc autorisé en tant qu'enseignant
			log.info("USER "+username+" ENSEIGNANT VIA UPORTAL OU GROUPE LDAP");
			return true;

		} else {
			//va voir dans apogée
			log.info("USER "+username+" NON ENSEIGNANT VIA UPORTAL OU GROUPES LDAP -> Recherche Apogée");

			//On test si on doit chercher un utilisateur au profil ENS dans Apogee
			if(PropertyUtils.isLoginApogee() && PropertyUtils.getProfilUtilisateurApogee().equals(Utils.PROFIL_ENS)){
				if(estUtilisateurApogeeValide(username)) {
					return true;
				}
			}else{
				log.info("Utilisateur "+username+" n'appartient à aucun groupe uportal ou  ldap");
			}
		}
		return false;
	}



	private boolean estDansLeGroupe(String username, DirContextOperations dco, String attributGroupeLdap, List<String> listegroupes) {
		if(dco != null && StringUtils.hasText(attributGroupeLdap) && listegroupes != null && !listegroupes.isEmpty()){
			String[] vals= dco.getStringAttributes(attributGroupeLdap);
			if(vals!=null){
				List<String> lmemberof = Arrays.asList(vals);
				//Si le compte LDAP possede des groupes
				if (!lmemberof.isEmpty()) {
					//on regarde si il appartient a un des groupes
					for (String groupe : listegroupes) {
						//on cherche le groupe	
						if (lmemberof.contains(groupe)) {
							log.info("Utilisateur "+username+" autorisé via groupe LDAP : "+ groupe);
							return true;
						} 
					}
				}
			}
		}
		return false;
	}



	/**
	 * 
	 * @param login de l'utilisateur
	 * @return les profils calculés à partir du compte ldap de l'utilisateur.
	 */
	public List<String> getLdapProfiles(final String login) {
		try {
			log.info("     getLdapProfiles searchForUser : "+login);
			DirContextOperations dco = ldapUserSearch.searchForUser(login);
			if(dco != null){
				List<String> profilsLdap = new LinkedList<String> ();
				// Test si étudiant
				if(compteLdapMatch(dco, PropertyUtils.getAttributLdapEtudiant(), PropertyUtils.getTypeEtudiantLdap())) {
					profilsLdap.add(Utils.LDAP_ETUDIANT);
				}
				// Test si doctorant
				if(compteLdapMatch(dco, PropertyUtils.getAttributLdapDoctorant(), PropertyUtils.getValeursAttributLdapDoctorant())) {
					profilsLdap.add(Utils.LDAP_DOCTORANT);
				}
				// Test si gestionnaire
				if(compteLdapMatch(dco, PropertyUtils.getAttributLdapGestionnaire(), PropertyUtils.getValeursAttributLdapGestionnaire())) {
					profilsLdap.add(Utils.LDAP_GEST);
				}
				log.info("Profils LDAP "+login+" : "+profilsLdap);
				return profilsLdap;
			}
			log.info("Compte LDAP "+login+" non trouve");
			return null;
		} catch (Exception e) {
			log.error("Probleme à la recuperation de l'utilisateur : "+login+" dans le LDAP",e);
			return null;
		}
	}


	/**
	 * 
	 * @param dco
	 * @param attributLdap
	 * @param valeurs
	 * @return vrai si l'entrée ldap dco contient une des valeurs en paramètres dans l'attributLdap en paramètre
	 */
	private boolean compteLdapMatch(DirContextOperations dco, String attributLdap, List<String> valeursCible) {
		if(StringUtils.hasText(attributLdap) && valeursCible!=null && !valeursCible.isEmpty()) {
			// récupération de la liste des valeurs de l'attribut
			String[] vals= dco.getStringAttributes(attributLdap);
			if(vals!=null){
				// liste des valeurs de l'attribut sous forme de liste
				List<String> listeValeurs = Arrays.asList(vals);

				//Si une des valeurs cible fait partie des valeurs de l'attribut pour le compte ldap
				if (Utils.listHaveCommonValue(listeValeurs, valeursCible)) { 
					return true;
				}
			}
		}
		return false;
	}



	/**
	 * @return username de l'utilisateur courant
	 */
	public String getCurrentUserName(String username) {
		//return "toto54";

		UtilisateurSwap us= utilisateurSwapRepository.findById(username).orElse(null);
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
}
