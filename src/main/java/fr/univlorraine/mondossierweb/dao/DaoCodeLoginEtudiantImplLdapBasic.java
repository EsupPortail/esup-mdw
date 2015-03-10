/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2007 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.dao;




import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.ldap.search.LdapUserSearch;


import org.springframework.stereotype.Component;

import fr.univlorraine.mondossierweb.utils.PropertyUtils;




/**
 * Classe qui sait récupérer le cod_etu depuis le login Etudiant.
 * @author Charlie Dubois
 *
 */
@Component(value="codetuFromLoginDao")
public class DaoCodeLoginEtudiantImplLdapBasic implements IDaoCodeLoginEtudiant {

	private Logger LOG = LoggerFactory.getLogger(DaoCodeLoginEtudiantImplLdapBasic.class);
	
	@Resource
	private transient LdapUserSearch ldapUserSearch;


	public DaoCodeLoginEtudiantImplLdapBasic() {
		super();
	}


	public String getCodEtuFromLogin(final String login) {
		
		
		try {
			if(ldapUserSearch.searchForUser(login)!=null){
				String[] vals= ldapUserSearch.searchForUser(login).getStringAttributes(PropertyUtils.getAttributLdapCodEtu());
				if(vals!=null){
					LOG.debug("Codetu via LDAP pour "+login+" => "+vals[0]);
					return vals[0];
				}
			}
			return null;
		} catch (Exception e) {
			LOG.error("probleme de récupération du cod_etu depuis le login via le ldap. ",e);
			return null;
		}
	}
}
