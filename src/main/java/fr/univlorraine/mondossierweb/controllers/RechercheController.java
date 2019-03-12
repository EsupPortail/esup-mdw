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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.FavorisMobileView;
import fr.univlorraine.mondossierweb.views.windows.HelpMobileWindow;
import lombok.extern.slf4j.Slf4j;

/**
 * Gestion de la recherche
 */
@Component
@Slf4j
public class RechercheController {



	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource(name="${resultat.implementation}")
	private transient ResultatController resultatController;
	@Resource
	private transient UserController userController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient ObjectFactory<HelpMobileWindow> helpMobileWindowFactory;


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
		MainUI.getCurrent().navigateToRechercheArborescente(parameterMap);
	}


	/**
	 * 
	 * @param fragment
	 * @param type
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
			Notification.show(applicationContext.getMessage("deepLinking.codetuNotFound",null, UI.getCurrent().getLocale()), Notification.Type.WARNING_MESSAGE);
		}

	}


	public void accessToDetail(String code, String type, String annee) {
		Map<String, String> parameterMap = new HashMap<>();
		parameterMap.put("code",code);
		parameterMap.put("type",type);
		parameterMap.put("annee",annee);
		if(type.equals(Utils.TYPE_CMP) || type.equals(Utils.CMP)){
			if(userController.isEnseignant()){
				parameterMap.replace("type",Utils.CMP);
				MainUI.getCurrent().navigateToRechercheArborescente(parameterMap);
			}
		}

		if(type.equals(Utils.TYPE_VET) || type.equals(Utils.VET) || type.equals(Utils.ELP) ||  type.equals(Utils.TYPE_ELP) ){
			if(userController.isEnseignant()){
				if(type.equals(Utils.TYPE_VET))
					parameterMap.replace("type",Utils.VET);
				if(type.equals(Utils.TYPE_ELP))
					parameterMap.replace("type",Utils.ELP);
				MainUI.getCurrent().navigateToListeInscrits(parameterMap);	
			}
		}

		if(type.equals(Utils.TYPE_ETU) || type.equals(Utils.ETU)){
			//Si l'utilisateur est enseignant ou si il s'agit bien de l'étudiant concerné
			//if(userController.isEnseignant() || ( userController.isEtudiant() && userController.getCodetu().equals(code))){
				parameterMap.replace("type",Utils.ETU);
				MainUI.getCurrent().setEtudiant(new Etudiant(code));
				etudiantController.recupererEtatCivil();
				//Si l'étudiant n'existe pas
				if(MainUI.getCurrent().getEtudiant()==null){
					MainUI.getCurrent().afficherErreurView();
				}else{
					MainUI.getCurrent().navigateToDossierEtudiant(parameterMap);
				}
			//}
		}
	}

	public void accessToMobileDetail(String code, String type, boolean fromSearch) {

		Map<String, String> parameterMap = new HashMap<>();
		parameterMap.put("code",code);
		parameterMap.put("type",type);

		//Si on vient de la recherche rapide, il faut que le bouton 'retour' de la recherche rapide arrive sur les favoris
		//Sinon boucle possible dans la navigation
		if(fromSearch){
			MdwTouchkitUI.getCurrent().setRechercheFromView(FavorisMobileView.NAME);
		}

		if(type.equals(Utils.TYPE_VET) || type.equals(Utils.VET) || type.equals(Utils.ELP) ||  type.equals(Utils.TYPE_ELP) ){
			if(type.equals(Utils.TYPE_VET))
				parameterMap.replace("type",Utils.VET);
			if(type.equals(Utils.TYPE_ELP))
				parameterMap.replace("type",Utils.ELP);
			if(fromSearch){
				MdwTouchkitUI.getCurrent().navigateToListeInscritsFromSearch(parameterMap);	
			}else{
				MdwTouchkitUI.getCurrent().navigateToListeInscritsFromFavoris(parameterMap);	
			}
		}

		if(type.equals(Utils.TYPE_ETU) || type.equals(Utils.ETU)){
				parameterMap.replace("type",Utils.ETU);

				if(MdwTouchkitUI.getCurrent().getEtudiant()==null || !MdwTouchkitUI.getCurrent().getEtudiant().getCod_etu().equals(code)){
					MdwTouchkitUI.getCurrent().setEtudiant(new Etudiant(code));
					etudiantController.recupererEtatCivil();
					if((userController.isEtudiant() && configController.isAffCalendrierEpreuvesEtudiants()) || configController.isAffCalendrierEpreuvesEnseignants()){
						etudiantController.recupererCalendrierExamens();
					}
					resultatController.recupererNotesEtResultatsEnseignant(MdwTouchkitUI.getCurrent().getEtudiant());
				}
				if(fromSearch){
					MdwTouchkitUI.getCurrent().navigateToDossierEtudiantFromSearch();
				}else{
					MdwTouchkitUI.getCurrent().navigateToDossierEtudiantFromListeInscrits();
				}
		}
	}


	public void accessToMobileNotesDetail(Etape etape) {


		MdwTouchkitUI.getCurrent().navigateToDetailNotes(etape);


		//Recuperer dans la base si l'utilisateur a demandé à ne plus afficher le message
		boolean afficherMessage = true;
		//Si on a paramétré l'application pour ne pas affiche le message
		if(!configController.isAffMessageNotesInformatives()){
			//On bloque l'affichage
			afficherMessage = false;
		}else{
			//Si l'utilisateur n'est pas un étudiant
			if(!userController.isEtudiant()){
				//On vérifie s'il a demandé à ne plus afficher le message
				String val  = userController.getPreference(Utils.SHOW_MESSAGE_NOTES_MOBILE_PREFERENCE);
				if(StringUtils.hasText(val)){
					afficherMessage = Boolean.valueOf(val);
				}
			}
		}

		if(afficherMessage){
			String message =applicationContext.getMessage("notesDetailMobileView.window.message.info", null, null);
			HelpMobileWindow hbw = helpMobileWindowFactory.getObject();
			hbw.init(message,applicationContext.getMessage("helpWindow.defaultTitle", null, null),!userController.isEtudiant());
			hbw.addCloseListener(g->{
				if(!userController.isEtudiant()){
					boolean choix = hbw.getCheckBox().getValue();
					//Test si l'utilisateur a coché la case pour ne plus afficher le message
					if(choix){
						//mettre a jour dans la base de données
						userController.updatePreference(Utils.SHOW_MESSAGE_NOTES_MOBILE_PREFERENCE, "false");
					}
				}
			});
			UI.getCurrent().addWindow(hbw);
		}

	}


}
