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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
import lombok.extern.slf4j.Slf4j;



//@Component
@Service("userDetailsService")
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

		//Si le login utilisé est admin
		if(isAdmin(finalusername)){
			return new MdwUserDetails(finalusername,new String[]{Utils.ADMIN_USER}, true, request.getRemoteAddr());
		}
		
		log.info("loadUserByUsername "+username);
		
		boolean canAccessAdminView = false;
		
		//Si il y un swap et que le login d'origne est admin
		if(!finalusername.equals(username) && isAdmin(username)){
			//On doit donner acces à la vue admin
			canAccessAdminView= true;
		}

		return new MdwUserDetails(finalusername,determineTypeUser(finalusername), canAccessAdminView, request.getRemoteAddr());
	}



	private boolean isAdmin(String login) {
		Administrateurs adm = administrateursRepository.findOne(login);
		//Si le login est dans la table admin
		if(adm!=null && adm.getLogin()!=null && adm.getLogin().equals(login)){
			//On vérifie quand même que l'utilisateur est présent dans le ldap
			if(estDansLeLdap(login)){
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
	public boolean estDansLeLdap(final String login) {
		try {
			if(ldapUserSearch.searchForUser(login)!=null){
				return true;
			}
			return false;
		} catch (Exception e) {
			log.error("Probleme à la recuperation de l'utilisateur : "+login+" dans le LDAP",e);
			return false;
		}
	}


	public String[] determineTypeUser(String username) {
		log.debug("   determineTypeUser "+username);

		List<String> llogins=configController.getListeLoginsBloques();
		if(llogins!=null && llogins.contains(username)){
			log.debug("utilisateur "+username+" bloqué car il a été exclu de l'application");
			return new String[]{Utils.UNAUTHORIZED_USER};

		}else{
			
			List<String> type = typeLdap(username);
			List<String> typeEtudiant = PropertyUtils.getTypeEtudiantLdap();

			//Si un des types du compte ldap correspond à un type étudiant
			if (typeEtudiant!=null && typeEtudiant.size()>0 && type!=null && Utils.listHaveCommonValue(type, typeEtudiant)) { 
				
				//vérifier si il y a un blocage sur l'étudiant
				String codetu = daoCodeLoginEtudiant.getCodEtuFromLogin(username);
				//On vérifie si l'étudiant est interdit de consultation de l'application
				List<String> lcodesBloquant = configController.getListeCodesBlocageAccesApplication();
				//Si on a paramétré des codes bloquant
				if(lcodesBloquant!=null && lcodesBloquant.size()>0){
					//Récupération des éventuels blocage pour l'étudiant
					List<String> lblo = multipleApogeeService.getListeCodeBlocage(codetu);
					// Si l'étudiant a des blocages
					if(lblo!=null && lblo.size()>0){
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
					if (StringUtils.hasText(PropertyUtils.getAttributGroupeLdap()) && listegroupes != null && listegroupes.size()>0) {
						//on recupère l'utilisateur ldap
						DirContextOperations dco = ldapUserSearch.searchForUser(username);
						if(dco!=null){
							String[] vals= dco.getStringAttributes(PropertyUtils.getAttributGroupeLdap());
							if(vals!=null){
								List<String> lmemberof = Arrays.asList(vals);
								//Si le compte LDAP possede des groupes
								if (lmemberof != null && lmemberof.size()>0) {
									//on regarde si il appartient a un des groupes
									for (String groupe : listegroupes) {
										// on cherche le groupe si il n'est pas déjà trouvé
										if (!userldap) {
											//on cherche le groupe	
											if (lmemberof.contains(groupe)) {
												userldap = true;
											} 
										}
									}
								}
							}
						}
						if(!userldap){
							log.debug("utilisateur "+username+" n'appartient à aucun groupe ldap autorises");
						}					
					}
				}

				if (useruportal || userldap) {
					//c'est un utilisateur uportal il est donc autorisé en tant qu'enseignant
					log.debug("USER "+username+" ENSEIGNANT VIA UPORTAL");
					return new String[]{Utils.TEACHER_USER};

				} else {
					//va voir dans apogée
					log.debug("USER "+username+" NON ENSEIGNANT VIA UPORTAL OU GROUPES LDAP -> Recherche Apogée");


					//On test si on doit chercher l'utilisateur dans Apogee
					if(PropertyUtils.isLoginApogee()){
						//Test de la présence dans la table utilisateur d'Apogee
						//on regarde si il est dans la table utilisateur 
						try {
							Utilisateur uti = utilisateurService.findUtilisateur(username.toUpperCase());

							if (uti != null) {
								log.debug("USER "+username+" ENSEIGNANT VIA APOGEE.UTILISATEUR");
								return new String[]{Utils.TEACHER_USER};
							} else {
								log.debug("utilisateur "+username+" n' est pas dans le LDAP en tant qu' etudiant, n'appartient à aucun groupe uportal, et n'est pas dans la table utilisateur d'APOGEE -> UTILISATEUR NON AUTORISE !");
								return new String[]{Utils.UNAUTHORIZED_USER};
							}
						} catch (Exception ex) {
							log.error("Probleme lors de la vérification de l'existence de l'utilisateur "+username+" dans la table Utilisateur de Apogee",ex);
						}
					}else{
						log.info("Utilisateur "+username+" n' est pas dans le LDAP en tant qu' etudiant, n'appartient à aucun groupe uportal -> UTILISATEUR NON AUTORISE !");
						return new String[]{Utils.UNAUTHORIZED_USER};

					}


				}
			}

			return new String[]{Utils.UNAUTHORIZED_USER};
		}
	}





	/**
	 * 
	 * @param login de l'utilisateur
	 * @return le type retourné par ldap.
	 */
	public List<String> typeLdap(final String login) {
		try {
			log.debug("     typeLdap searchForUser : "+login);
			if(ldapUserSearch.searchForUser(login)!=null){
				DirContextOperations dco = ldapUserSearch.searchForUser(login);
				if(dco!=null){
					String[] vals= dco.getStringAttributes(PropertyUtils.getAttributLdapEtudiant());
					if(vals!=null){
						List<String> listeValeurs = Arrays.asList(vals);
						return listeValeurs;
					}
				}
			}
			return null;
		} catch (Exception e) {
			log.error("Probleme à la recuperation de l'utilisateur : "+login+" dans le LDAP",e);
			return null;
		}
	}


	/**
	 * @return username de l'utilisateur courant
	 */
	public String getCurrentUserName(String username) {
		//return "toto54";

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
}
