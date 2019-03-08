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
package fr.univlorraine.mondossierweb;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.ui.UI;

import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.ElpDeCollection;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.photo.IPhoto;
import fr.univlorraine.tools.vaadin.IAnalyticsTracker;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * UI générique, pour gérer les attributs communs aux UI desktop ou Mobile sans se soucier de la version de l'UI appelée
 * @author charlie dubois
 *
 */
@SuppressWarnings("serial")
@Slf4j
public class GenericUI  extends UI {

	//Adresse IP du client
	@Setter
	protected String ipClient;

	//Etudiant dont on consulte le dossier
	@Setter
	@Getter
	protected Etudiant etudiant;

	//Vrai si on a réussi à récupérer les inscriptions via le WS.
	@Setter
	@Getter
	protected boolean recuperationWsInscriptionsOk;

	//vrai si on consulte les notes en vue enseignant
	@Setter
	@Getter
	protected boolean vueEnseignantNotesEtResultats;

	@Setter
	@Getter
	private List<String> ListeAnneeInscrits;

	//l'année correspondant à liste des inscrits en cours.
	@Setter
	@Getter
	private String anneeInscrits;

	//l'étape correspondant à la liste des inscrits si c'est une liste d'inscrits à une étape.
	@Setter
	@Getter
	private Etape etapeListeInscrits;

	//l'elp correspondant à la liste des inscrits si c'est une liste d'inscrits à un elp
	@Setter
	@Getter
	private ElementPedagogique elpListeInscrits;

	//la liste des étapes affichées dans la liste des inscrits quand on consulte les inscrits à un ELP
	@Setter
	@Getter
	private List<VersionEtape> listeEtapesInscrits;

	//l'identifiant de l'étape sélectionnée dans la liste des inscrits quand on consulte les inscrits à un ELP
	@Setter
	@Getter
	private String etapeInscrits;

	//la liste des groupes affichés dans la liste des inscrits quand on consulte les inscrits à un ELP
	@Setter
	@Getter
	private List<ElpDeCollection> listeGroupesInscrits;

	//l'identifiant du groupe sélectionné dans la liste des inscrits quand on consulte les inscrits à un ELP
	@Setter
	@Getter
	private String groupeInscrits;

	//annee universitaire en cours
	@Setter
	@Getter
	private String anneeUnivEnCours;

	//code de l'obj dont on affiche la liste des inscrits
	@Setter
	@Getter
	private String codeObjListInscrits;

	//type de l'obj dont on affiche la liste des inscrits
	@Setter
	@Getter
	private String typeObjListInscrits;

	//la liste des inscrits.
	@Setter
	@Getter
	private List<Inscrit> listeInscrits;

	@Getter
	@Setter
	protected IAnalyticsTracker analyticsTracker;



	//Le photo provider
	@Setter
	@Getter
	@Resource(name="${serveurphoto.implementation}")
	private IPhoto photoProvider;

	/*
	@Setter
	@Getter
	@Resource(name="${codetuFromLogin.implementation}")
	protected CodeEtudiantLoginConverterInterface daoCodeLoginEtudiant;*/

	@Override
	protected void init(VaadinRequest request) {
		// TODO Auto-generated method stub
		log.info("init Generic UI");
	}

	/**
	 * @see com.vaadin.ui.UI#getCurrent()
	 */
	public static GenericUI getCurrent() {
		return (GenericUI) UI.getCurrent();
	}


	public String getIpClient() {     

		if(!StringUtils.hasText(ipClient)){

			VaadinRequest vr = VaadinService.getCurrentRequest();

			VaadinServletRequest vsRequest = (VaadinServletRequest)vr;
			HttpServletRequest hsRequest = vsRequest.getHttpServletRequest();

			String ip = hsRequest.getHeader("x-forwarded-for");    
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
				ip = hsRequest.getHeader("X_FORWARDED_FOR");      
			}
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
				ip = hsRequest.getHeader("HTTP_X_FORWARDED_FOR");      
			}
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
				ip = hsRequest.getHeader("Proxy-Client-IP");      
			}   
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
				ip = hsRequest.getHeader("WL-Proxy-Client-IP");      
			}   
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
				ip = hsRequest.getRemoteAddr();     
			} 

			//Si contient plusieurs IP, on prend la deuxième
			if(StringUtils.hasText(ip) && ip.contains(",")){
				ip = ip.split(",")[1];
			}
			
			ipClient = ip;

		}

		return ipClient;
	} 
}
