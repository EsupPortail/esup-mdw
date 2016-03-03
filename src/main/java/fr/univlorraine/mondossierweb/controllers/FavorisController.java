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
package fr.univlorraine.mondossierweb.controllers;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import fr.univlorraine.mondossierweb.entities.mdw.FavorisPK;
import fr.univlorraine.mondossierweb.repositories.mdw.FavorisRepository;
import fr.univlorraine.mondossierweb.services.apogee.ComposanteService;
import fr.univlorraine.mondossierweb.services.apogee.ComposanteServiceImpl;
import fr.univlorraine.mondossierweb.services.apogee.ElementPedagogiqueService;
import fr.univlorraine.mondossierweb.services.apogee.ElementPedagogiqueServiceImpl;
import fr.univlorraine.mondossierweb.services.apogee.VersionEtapeService;
import fr.univlorraine.mondossierweb.services.apogee.VersionEtapeServiceImpl;

/**
 * Gestion des favoris
 */
@Component
public class FavorisController {



	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private FavorisRepository favorisRepository;
	/** {@link ComposanteServiceImpl} */
	@Resource
	private ComposanteService composanteService;
	/** {@link ElementPedagogiqueServiceImpl} */
	@Resource
	private ElementPedagogiqueService elpService;
	/** {@link VersionEtapeServiceImpl} */
	@Resource
	private VersionEtapeService versionEtapeService;
	@Resource
	private transient UserController userController;

	public List<Favoris> getFavoris() {
		return getFavorisFromLogin(userController.getCurrentUserName());
	}
	
	public List<Favoris> getFavorisFromLogin(String login) {
		return favorisRepository.findFavorisFromLogin(login);
	}

	public void removeFavori(FavorisPK favori) {
		favorisRepository.delete(favori);
	}

	public void saveFavori(Favoris favori) {
		favorisRepository.save(favori);
	}
	
	public String getLibObjFavori(String typeObj,String idObj){
		
		if(typeObj.equals("CMP")){
			return getLibelleComposante(idObj);
		}
		if(typeObj.equals("VET")){
			return getLibelleVet(idObj);
		}
		//cas ELP
		return elpService.getLibelleElp(idObj);
		
	}

	public String getLibelleComposante(String codCmp){
		return composanteService.getLibelleComposante(codCmp);
	}
	
	public String getLibelleElp(String codElp){
		return elpService.getLibelleElp(codElp);
	}

	public String getLibelleVet(String idObj){
		if(StringUtils.hasText(idObj)){
			String[] s = idObj.split("/");
			String codvet=s[0];
			String versVet=s[1];
			return versionEtapeService.getLibelleVet(codvet, versVet);
		}
		return null;
	}

}
