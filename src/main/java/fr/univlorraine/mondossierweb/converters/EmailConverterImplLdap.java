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

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.stereotype.Component;



/**
 * la classe qui permet d'obtenir l'e-mail d'un etudiant connaissant son codetu.
 * @author Charlie Dubois
 */
@Component(value="emailConverterLdapImpl")
@Slf4j
public class EmailConverterImplLdap implements EmailConverterInterface{

	@Resource
	private transient LdapUserSearch ldapEtudiantSearch;
	
	/**
	 * le constructeur.
	 */
	public EmailConverterImplLdap() {
		super();
	}
	
	/**
	 * @param codetu
	 * @return l'adresse mail.
	 */
	public String getMail( String cod_etu) {

		try {
			if(ldapEtudiantSearch.searchForUser(cod_etu)!=null){
				String[] vals= ldapEtudiantSearch.searchForUser(cod_etu).getStringAttributes("mail");
				if(vals!=null){
					log.debug("mail via codetu pour "+cod_etu+" => "+vals[0]);
					return vals[0];
				}
			}
			return null;
		} catch (Exception e) {
			log.error("probleme de récupération du mail depuis le codetu via le ldap. ",e);
			return null;
		}	
	}
	

}
