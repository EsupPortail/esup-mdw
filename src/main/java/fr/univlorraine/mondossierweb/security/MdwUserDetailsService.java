package fr.univlorraine.mondossierweb.security;

import javax.annotation.Resource;

import org.esupportail.portal.ws.client.support.uportal.CachingUportalServiceImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.stereotype.Component;

import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.repositories.AdministrateursRepository;
import fr.univlorraine.mondossierweb.services.apogee.UtilisateurService;
import fr.univlorraine.mondossierweb.services.apogee.UtilisateurServiceImpl;



@Component
public class MdwUserDetailsService implements UserDetailsService {


	/**
	 * type utilisateur admin.
	 */
	public static final String ADMIN_USER = "admin";
	
	public static final String CONSULT_DOSSIER_AUTORISE = "consultation_dossier";



	@Resource
	private transient CachingUportalServiceImpl portalService;
	/** {@link UtilisateurServiceImpl} */
	@Resource
	private UtilisateurService utilisateurService;
	@Resource
	private transient LdapUserSearch ldapUserSearch;
	@Resource
	private transient UserController userController;
	@Resource
	private AdministrateursRepository administrateursRepository;


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		//Au cas où on ait un swap d'utilisateur
		username = userController.getCurrentUserName(username);
		
		if(userController.isAdmin(username)){
			return new MdwUserDetails(username,ADMIN_USER);
		}
		return new MdwUserDetails(username,userController.determineTypeUser(username));
	}


}
