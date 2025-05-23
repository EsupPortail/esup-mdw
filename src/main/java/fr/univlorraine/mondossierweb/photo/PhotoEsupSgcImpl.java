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
package fr.univlorraine.mondossierweb.photo;

import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component(value="photoEsupSgc")
@Slf4j
public class PhotoEsupSgcImpl implements IPhoto {

	@Value("${param.esupsgc.urlphoto:}")
	private String esupSgcPhotoUrl;

	@Resource
	private transient LdapUserSearch ldapEtudiantSearch;

	RestTemplate rt = new RestTemplate();

	@Override
	public String getUrlPhoto(String cod_ind, String cod_etu, boolean isUtilisateurEnseignant, String loginUser) {
		String photoAsBase64 = getPhotoAsBase64(cod_etu);
		return String.format("%s/png;base64,%s", Utils.DATA_IMAGE, photoAsBase64);
	}

	@Override
	public String getUrlPhotoTrombinoscopePdf(String cod_ind, String cod_etu, boolean isUtilisateurEnseignant, String loginUser) {
		return getUrlPhoto(cod_ind, cod_etu, isUtilisateurEnseignant, loginUser);
	}

	@Override
	public boolean isOperationnel() {
		return esupSgcPhotoUrl != null;
	}

	byte[] getPhoto(String cod_etu) {
		String eppn = getEppnFromCodEtu(cod_etu);
		String url = String.format(esupSgcPhotoUrl, eppn);
		log.debug("GET PHOTO : " + url);
		try {
			ResponseEntity<byte[]> response = rt.getForEntity(url, byte[].class);
			return response.getBody();
		} catch(HttpClientErrorException he) { 
			log.warn("Récupération de la photo de "+eppn+" en erreur Erreur HTTP "+he.getStatusCode());
			return he.getResponseBodyAsByteArray();
		} catch (Exception e) {
			log.error("Une erreur est survenue lors de la récupération de la photo de "+eppn,e);
		}
		return null;
	}

	String getPhotoAsBase64(String cod_etu) {
		byte[] photo = getPhoto(cod_etu);
		if(photo!=null) {
			return StringUtils.newStringUtf8(Base64.encodeBase64(photo, false));
		}
		return null;
	}

	String getEppnFromCodEtu(String codetu) {
		try {
			String[] vals = ldapEtudiantSearch.searchForUser(codetu).getStringAttributes("eduPersonPrincipalName");
			if (vals != null) {
				log.debug("login via codetu pour " + codetu + " => " + vals[0]);
				return vals[0];
			} else {
				log.warn("No eduPersonPrincipalName  in LDAP for " + codetu);
			}
			return null;
		} catch (Exception e) {
			log.error("probleme de récupération de l'eppn depuis le codetu via le ldap. ",e);
			return null;
		}
	}


}
