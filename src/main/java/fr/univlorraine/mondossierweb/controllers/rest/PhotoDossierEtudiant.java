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
package fr.univlorraine.mondossierweb.controllers.rest;



import java.util.Base64;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.photo.IPhoto;
import fr.univlorraine.mondossierweb.utils.Utils;
import lombok.Getter;
import lombok.Setter;


/**
 * Contrôleur REST pour récupérer les photos de manière parallélisée depuis le navigateur client
 */
@Scope(value="session", proxyMode=ScopedProxyMode.DEFAULT)
@Controller
@RequestMapping("/"+Utils.FRAGMENT_ACCES_PHOTO)
public class PhotoDossierEtudiant {

	private Logger LOG = LoggerFactory.getLogger(PhotoDossierEtudiant.class);

	@Resource
	private Environment environment;

	@Resource
	private transient UserController userController;

	//Le photo provider
	@Setter
	@Getter
	@Resource(name="${serveurphoto.implementation}")
	private IPhoto photoProvider;

	/**
	 * retourne la photo
	 */
	@RequestMapping(value="/{codEtu}", method=RequestMethod.GET)
	public ResponseEntity<byte[]>  getPhoto(@PathVariable String codEtu) {
		// Controle d'acces
		if( userController.isEnseignant() ||  userController.getCodetu().equals(codEtu)) {

			// Récupération de la photo
			String photo = photoProvider.getUrlPhoto(null, codEtu, userController.isEnseignant(),userController.getCurrentUserName());

			LOG.debug("photo "+codEtu+" : "+photo);
			
			if(photo == null) {
				return null;
			}
			
			String type = photo.startsWith(Utils.DATA_IMAGE) ? photo.split(",")[0] : null;
			String value = type != null ? photo.split(",")[1] : photo;

			// Si la photo est en base64
			if(type != null && type.endsWith("base64")) {
				return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(Base64.getMimeDecoder().decode(value));
			} else {
				return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(value.getBytes());
			}
		}
		LOG.warn("Acces non autorisé sur la photo de "+codEtu+ " par "+userController.getCurrentUserName());
		return null;
	}
}

