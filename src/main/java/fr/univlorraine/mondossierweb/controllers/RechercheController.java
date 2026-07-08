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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.QueryParameters;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestion de la recherche
 */
@Component
@Slf4j
public class RechercheController {

	/** Routes des vues Flow (mêmes noms que les anciennes @SpringView) */
	public static final String ROUTE_RECHERCHE_ARBORESCENTE = "rechercheArborescenteView";
	public static final String ROUTE_LISTE_INSCRITS = "listeInscritsView";
	public static final String ROUTE_ETAT_CIVIL = "etatCivilView";
	public static final String ROUTE_ERREUR = "erreurView";

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient UserController userController;
	/** État de la session utilisateur (injection lazy : bean de portée vaadin-session) */
	@Autowired
	@Lazy
	private transient SessionController sessionController;


	public void accessToRechercheArborescente(String code, String type) {
		Map<String, String> parameterMap = new HashMap<>();
		parameterMap.put("code",code);
		parameterMap.put("type",type);
		if(type.equals(Utils.TYPE_CMP))
			parameterMap.replace("type",Utils.CMP);
		if(type.equals(Utils.TYPE_VET))
			parameterMap.replace("type",Utils.VET);
		if(type.equals(Utils.TYPE_ELP))
			parameterMap.replace("type",Utils.ELP);
		navigate(ROUTE_RECHERCHE_ARBORESCENTE, parameterMap);
	}


	/**
	 * Accès à un dossier étudiant par deep linking
	 * @param fragment
	 */
	public void accessToDossierEtudiantDeepLinking(String fragment) {

		String fragmentpart[] =fragment.split("/");

		int rangCode = 0;
		//parcours du fragment pour trouver le codetu. Obligatoire en cas de "polution" de l'url.
		for(int i=0 ; i < fragmentpart.length ; i++){
			if(fragmentpart[i]!=null && fragmentpart[i].contains(Utils.FRAGMENT_ACCES_DOSSIER_ETUDIANT)){
				rangCode=i+1;
			}
		}
		String code= fragmentpart[rangCode];

		//On vérifie que l'étudiant avec ce code existe
		if(etudiantController.isEtudiantExiste(code)){
			//On accède au dossier
			accessToDetail(code,Utils.ETU, null);
		}else{
			Notification notification = Notification.show(applicationContext.getMessage("deepLinking.codetuNotFound",null, UI.getCurrent().getLocale()));
			notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
		}

	}


	public void accessToDetail(String code, String type, String annee) {
		Map<String, String> parameterMap = new HashMap<>();
		parameterMap.put("code", code);
		parameterMap.put("type", type);
		if (annee != null) {
			parameterMap.put("annee", annee);
		}
		if(type.equals(Utils.TYPE_CMP) || type.equals(Utils.CMP)){
			if(userController.isEnseignant()){
				parameterMap.replace("type",Utils.CMP);
				navigate(ROUTE_RECHERCHE_ARBORESCENTE, parameterMap);
			}
		}

		if(type.equals(Utils.TYPE_VET) || type.equals(Utils.VET) || type.equals(Utils.ELP) ||  type.equals(Utils.TYPE_ELP) ){
			if(userController.isEnseignant()){
				if(type.equals(Utils.TYPE_VET))
					parameterMap.replace("type",Utils.VET);
				if(type.equals(Utils.TYPE_ELP))
					parameterMap.replace("type",Utils.ELP);
				navigate(ROUTE_LISTE_INSCRITS, parameterMap);
			}
		}

		if(type.equals(Utils.TYPE_ETU) || type.equals(Utils.ETU)){
			parameterMap.replace("type",Utils.ETU);
			sessionController.setEtudiant(new Etudiant(code));
			etudiantController.recupererEtatCivil();
			//Si l'étudiant n'existe pas
			if(sessionController.getEtudiant()==null){
				UI.getCurrent().navigate(ROUTE_ERREUR);
			}else{
				//par défaut on affiche la vue état-civil du dossier
				navigate(ROUTE_ETAT_CIVIL, parameterMap);
			}
		}
	}

	/**
	 * Navigation vers une route Flow en transmettant les paramètres en query parameters.
	 */
	private void navigate(String route, Map<String, String> parameterMap) {
		Map<String, String> params = new HashMap<>();
		parameterMap.forEach((k, v) -> {
			if (v != null) {
				params.put(k, v);
			}
		});
		UI.getCurrent().navigate(route, QueryParameters.simple(params));
	}

}
