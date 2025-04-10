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


import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.stereotype.Component;




/**
 * Classe qui sait récupérer le cod_etu depuis le login Etudiant.
 * @author Charlie Dubois
 *
 */
@Component(value="codetuFromLoginLdapImpl")
@Slf4j
public class CodeEtudiantLoginConverterImplLdap implements CodeEtudiantLoginConverterInterface {

	@Resource
	private transient LdapUserSearch ldapUserSearch;


	public CodeEtudiantLoginConverterImplLdap() {
		super();
	}


	public String getCodEtuFromLogin(final String login) {
		
		
		try {
			DirContextOperations dco = ldapUserSearch.searchForUser(login);
			if(dco!=null){
				String[] vals= dco.getStringAttributes(PropertyUtils.getAttributLdapCodEtu());
				if(vals!=null){
					log.debug("Codetu via LDAP pour "+login+" => "+vals[0]);
					return vals[0];
				}
			}
			return null;
		} catch (Exception e) {
			log.error("probleme de récupération du cod_etu depuis le login via le ldap. ",e);
			return null;
		}
	}
}
