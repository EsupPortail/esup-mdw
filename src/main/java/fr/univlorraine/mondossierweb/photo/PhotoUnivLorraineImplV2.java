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

import java.security.Key;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.flywaydb.core.internal.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import fr.univlorraine.mondossierweb.converters.LoginCodeEtudiantConverterInterface;
import fr.univlorraine.mondossierweb.utils.Utils;
import lombok.Synchronized;


@Scope(value="session", proxyMode=ScopedProxyMode.DEFAULT)
@Component(value="photoUnivLorraineImplV2")
public class PhotoUnivLorraineImplV2 implements IPhoto {

	private static final String UTF_8 = "UTF-8";

	private Logger LOG = LoggerFactory.getLogger(PhotoUnivLorraineImplV2.class);

	@Resource(name="${loginFromCodetu.implementation}")
	private LoginCodeEtudiantConverterInterface loginCodeEtudiantConverter;

	/**
	 * le token JWT du user
	 */
	private String userTokenJWT;
	
	/**
	 * Date d'expiration du token
	 */
	private Date userTokenJWTExpirationDate;

	private String photoUrl;

	private String tokenUrl;

	private String avatarUrl;

	private String clientIdHeader;

	private String  apiKeyHeader;

	private String  loginHeader;

	private String clientId;

	private String clientSecret;

	private String displayNameHeader;

	private String cypherAlgo;

	private String keyAlgo;

	private byte[] cypherKey;

	private Cipher cypher;


	public String getPhotoUrl() {
		if(photoUrl==null){
			photoUrl=System.getProperty("context.param.photoserver.urlphoto");
		}
		return photoUrl;
	}

	public String getAvatarUrl() {
		if(avatarUrl==null){
			avatarUrl=System.getProperty("context.param.photoserver.urlavatar");
		}
		return avatarUrl;
	}

	public String getTokenUrl() {
		if(tokenUrl==null){
			tokenUrl=System.getProperty("context.param.photoserver.urltoken");
		}
		return tokenUrl;
	}

	public String getClientIdHeader() {
		if(clientIdHeader==null){
			clientIdHeader=System.getProperty("context.param.photoserver.clientidheader");
		}
		return clientIdHeader;
	}

	public String getApiKeyHeader() {
		if(apiKeyHeader==null){
			apiKeyHeader=System.getProperty("context.param.photoserver.apikeyheader");
		}
		return apiKeyHeader;
	}

	public String getLoginHeader() {
		if(loginHeader==null){
			loginHeader=System.getProperty("context.param.photoserver.loginheader");
		}
		return loginHeader;
	}

	public String getDisplayNameHeader() {
		if(displayNameHeader==null){
			displayNameHeader=System.getProperty("context.param.photoserver.displaynameheader");
		}
		return displayNameHeader;
	}

	public String getClientId() {
		if(clientId==null){
			clientId=System.getProperty("context.param.photoserver.clientid");
		}
		return clientId;
	}

	public String getClientSecret() {
		if(clientSecret==null){
			clientSecret=System.getProperty("context.param.photoserver.clientsecret");
		}
		return clientSecret;
	}
	
	private String getCypherAlgo() {
		if(cypherAlgo==null){
			cypherAlgo=System.getProperty("context.param.photoserver.cypheralgo");
		}
		return cypherAlgo;
	}

	private String getKeyAlgo() {
		if(keyAlgo==null){
			keyAlgo=System.getProperty("context.param.photoserver.cypherkeyalgo");
		}
		return keyAlgo;
	}

	private byte[] getCypherKey() {
		if(cypherKey==null){
			String key=System.getProperty("context.param.photoserver.cypherkey");
			cypherKey = key.getBytes();
		}
		return cypherKey;
	}

	@Override
	public String getUrlPhoto(String cod_ind, String cod_etu, boolean isUtilisateurEnseignant, String loginUser) {
		return getBase64(cod_etu, loginUser);
	}


	@Override
	public String getUrlPhotoTrombinoscopePdf(String cod_ind, String cod_etu, boolean isUtilisateurEnseignant, String loginUser) {
		return getBase64(cod_etu, loginUser);
		//return getUrl(cod_etu, loginUser);
	}

	private String getUrl(String cod_etu,String loginUser) {
		checkTokenForUser(loginUser);

		// Récupération de l'url de la photo
		return getEncryptedPhotoUrl(userTokenJWT, loginCodeEtudiantConverter.getLoginFromCodEtu(cod_etu));

	}

	private String getEncryptedPhotoUrl(String token, String login) {
		return getPhotoUrl() + "/" + encrypt(login) + "?token=" + token;
	}

	//@Synchronized
	private String getBase64(String cod_etu,String loginUser) {

		checkTokenForUser(loginUser);

		// Récupération de la photo
		String photo = getPhoto(userTokenJWT, loginCodeEtudiantConverter.getLoginFromCodEtu(cod_etu));
		if(photo == null) {
			LOG.warn("Photo null, récupération de l'avatar pour "+cod_etu);
			photo =  getAvatar(userTokenJWT, "");
		}
		return photo;
	}

	@Synchronized
	private void checkTokenForUser(String loginUser) {
		// Si le token du user est null ou expiré
		if(userTokenJWT==null || isTokenExpired()) {
			userTokenJWT = getToken(loginUser);
			userTokenJWTExpirationDate = getExpirationDate(userTokenJWT);
		}
	}


	private String getToken(String login) {
		String url = getTokenUrl();

		// Headers
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add(getClientIdHeader(), getClientId());
		requestHeaders.add(getApiKeyHeader(), getClientSecret());
		if(login != null) {
			requestHeaders.add(getLoginHeader(), login);
		}

		//Body
		LinkedMultiValueMap<String, Object>  params = new LinkedMultiValueMap<>();

		// Request
		HttpEntity<?> request = new HttpEntity<>(params , requestHeaders);

		LOG.debug("GET TOKEN : "+url+" request : "+request);

		// Http Call 
		RestTemplate rt = new RestTemplate();
		try {
			ResponseEntity<String> response = rt.exchange(url, HttpMethod.GET, request, String.class);
			// Si appel OK
			if(response !=null && response.getStatusCode().equals(HttpStatus.OK)) {
				return response.getBody();
			} else {
				LOG.warn("Une erreur est survenue lors de la récupération du token JWT du serveur photo "+login+" Error Response => " + ( response == null ? "null" : response.getStatusCode().toString()));
			}
		} catch (Exception e) {
			LOG.error("Une erreur est survenue lors de la récupération du token JWT du serveur photo "+login,e);
		}
		return null;
	}




	private String getPhoto(String token, String login) {
		if(login == null) {
			LOG.warn("Récupération de la photo pour login null");
			return null;
		}
		String url = getPhotoUrl() + "/" + login;

		// Headers
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Authorization", "Bearer "+token);
		//requestHeaders.add("response-type", "image");

		//Body
		LinkedMultiValueMap<String, Object>  params = new LinkedMultiValueMap<>();

		// Request
		HttpEntity<?> request = new HttpEntity<>(params , requestHeaders);

		LOG.debug("GET PHOTO : "+url+" request : "+request);

		// Http Call 
		RestTemplate rt = new RestTemplate();
		try {
			ResponseEntity<String> response = rt.exchange(url, HttpMethod.GET, request, String.class);
			// Si appel OK
			if(response !=null && response.getStatusCode().equals(HttpStatus.OK)) {
				return Utils.DATA_IMAGE + "/jpg;base64, "+ response.getBody();
				//return Utils.DATA_IMAGE + "/jpg, "+ response.getBody();
			} else {
				LOG.warn("Une erreur est survenue lors de la récupération de la photo de "+login+" Error Response => " + ( response == null ? "null" : response.getStatusCode().toString()));
			}
		} catch(HttpClientErrorException he) { 
			LOG.warn("Récupération de la photo de "+login+" non autorisée Erreur HTTP "+he.getStatusCode()+". Il est probable que l'étudiant ne soit plus présent dans le ldap");
		}catch (Exception e) {
			LOG.error("Une erreur est survenue lors de la récupération de la photo de "+login,e);
		}
		return null;
	}

	private String getAvatar(String token, String displayName) {
		String url = getAvatarUrl();

		// Headers
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Authorization", "Bearer "+token);
		requestHeaders.add(getDisplayNameHeader(), displayName);

		//Body
		LinkedMultiValueMap<String, Object>  params = new LinkedMultiValueMap<>();

		// Request
		HttpEntity<?> request = new HttpEntity<>(params , requestHeaders);

		LOG.debug("GET AVATAR : "+url+" request : "+request);

		// Http Call 
		RestTemplate rt = new RestTemplate();
		try {
			ResponseEntity<String> response = rt.exchange(url, HttpMethod.GET, request, String.class);
			// Si appel OK
			if(response !=null && response.getStatusCode().equals(HttpStatus.OK)) {
				return Utils.DATA_IMAGE + "/png;base64, "+ response.getBody();
			} else {
				LOG.warn("Une erreur est survenue lors de la récupération de l'avatar Error Response => " + ( response == null ? "null" : response.getStatusCode().toString()));
			}
		} catch (Exception e) {
			LOG.error("Une erreur est survenue lors de la récupération de la photo de l'avatar ",e);
		}
		return null;
	}



	@Override
	public boolean isOperationnel() {
		return true;
	}

	private Boolean isTokenExpired() {
		/*DecodedJWT decodedToken  = JWT.decode(token);
		if (decodedToken != null) {
			LOG.debug("token expires at : "+decodedToken.getExpiresAt());
			return decodedToken.getExpiresAt().before(new Date());
		}
		LOG.debug("token null");
		return true;*/
		return userTokenJWTExpirationDate.before(new Date());
	}
	
	private Date getExpirationDate(String token) {
		DecodedJWT decodedToken  = JWT.decode(token);
		if (decodedToken != null) {
			LOG.debug("token expires at : "+decodedToken.getExpiresAt());
			Calendar c = Calendar.getInstance();
	        c.setTime(decodedToken.getExpiresAt());
	        // On prend 30 secondes de marge sur la fin du token
	        c.add(Calendar.SECOND, -30);
			return c.getTime();
		}
		LOG.debug("token null");
		return null;
	}

	
	private String encrypt(String chaine) {
		try {
			if(cypher==null) {
				Key key = new SecretKeySpec(getCypherKey(), getKeyAlgo());
				final Cipher c = Cipher.getInstance(getCypherAlgo());
				c.init(Cipher.ENCRYPT_MODE, key);
			}
			return new String(Base64.encode(cypher.doFinal(chaine.getBytes(UTF_8))), UTF_8);
		} catch (Exception e) {
			LOG.error("Erreur lors du cryptage de la chaine "+chaine,e);
			return null;
		}
	}

	

}
