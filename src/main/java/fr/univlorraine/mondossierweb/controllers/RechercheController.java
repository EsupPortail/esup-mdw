package fr.univlorraine.mondossierweb.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

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

/**
 * Gestion de la recherche
 */
@Component
public class RechercheController {



	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient UserController userController;


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
		String code= fragmentpart[fragmentpart.length-1];
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
			parameterMap.replace("type",Utils.CMP);
			MainUI.getCurrent().navigateToRechercheArborescente(parameterMap);
		}

		if(type.equals(Utils.TYPE_VET) || type.equals(Utils.VET) || type.equals(Utils.ELP) ||  type.equals(Utils.TYPE_ELP) ){
			if(type.equals(Utils.TYPE_VET))
				parameterMap.replace("type",Utils.VET);
			if(type.equals(Utils.TYPE_ELP))
				parameterMap.replace("type",Utils.ELP);
			MainUI.getCurrent().navigateToListeInscrits(parameterMap);	
		}

		if(type.equals(Utils.TYPE_ETU) || type.equals(Utils.ETU)){
			parameterMap.replace("type",Utils.ETU);
			MainUI.getCurrent().setEtudiant(new Etudiant(code));
			etudiantController.recupererEtatCivil();
			//Si l'étudiant n'existe pas
			if(MainUI.getCurrent().getEtudiant()==null){
				MainUI.getCurrent().afficherErreurView();
			}else{
				MainUI.getCurrent().navigateToDossierEtudiant(parameterMap);
			}
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
				etudiantController.recupererCalendrierExamens();
				etudiantController.recupererNotesEtResultatsEnseignant(MdwTouchkitUI.getCurrent().getEtudiant());
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
		if(!userController.isEtudiant()){
			String val  = userController.getPreference(Utils.SHOW_MESSAGE_NOTES_MOBILE_PREFERENCE);

			if(StringUtils.hasText(val)){
				afficherMessage = Boolean.valueOf(val);
			}
		}
		
		if(afficherMessage){
			String message =applicationContext.getMessage("notesDetailMobileView.window.message.info", null, null);
			HelpMobileWindow hbw = new HelpMobileWindow(message,applicationContext.getMessage("helpWindow.defaultTitle", null, null),!userController.isEtudiant());
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
