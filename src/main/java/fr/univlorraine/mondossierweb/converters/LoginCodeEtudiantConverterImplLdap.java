/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2007 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.converters;




import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;


import org.springframework.stereotype.Component;

import fr.univlorraine.mondossierweb.utils.PropertyUtils;




/**
 * Classe qui sait récupérer le login depuis le code étudiant.
 * @author Charlie Dubois
 *
 */
@Component(value="loginFromCodetuLdapImpl")
public class LoginCodeEtudiantConverterImplLdap implements LoginCodeEtudiantConverterInterface {

	private Logger LOG = LoggerFactory.getLogger(LoginCodeEtudiantConverterImplLdap.class);
	
	@Resource
	private transient LdapUserSearch ldapUserSearch;
	
	@Resource
	private transient FilterBasedLdapUserSearch filterBasedLdapUserSearch;


	public LoginCodeEtudiantConverterImplLdap() {
		super();
	}


	public String getLoginFromCodEtu(final String codetu) {
		
		
		try {
			if(ldapUserSearch.searchForUser(codetu)!=null){
				String[] vals= ldapUserSearch.searchForUser(codetu).getStringAttributes(PropertyUtils.getAttributLdapCodEtu());
				if(vals!=null){
					LOG.debug("login via codetu pour "+codetu+" => "+vals[0]);
					return vals[0];
				}
			}
			return null;
		} catch (Exception e) {
			LOG.error("probleme de récupération du login depuis le codetu via le ldap. ",e);
			return null;
		}
	}
}
