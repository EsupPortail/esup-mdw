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
package fr.univlorraine.mondossierweb.converters;




import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private transient LdapUserSearch ldapEtudiantSearch;
	
	


	public LoginCodeEtudiantConverterImplLdap() {
		super();
	}


	public String getLoginFromCodEtu(final String codetu) {
		
		
		try {
			if(ldapEtudiantSearch.searchForUser(codetu)!=null){
				String[] vals= ldapEtudiantSearch.searchForUser(codetu).getStringAttributes(PropertyUtils.getAttributLdapUid());
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
