package fr.univlorraine.mondossierweb.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.utils.Utils;

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
	private transient UserDetailsService userDetailsService;
	@Resource
	private transient EtudiantController etudiantController;





	public void accessToDetail(String code, String type) {
		//System.out.println("acceder au "+type+" -> "+code);
		Map<String, String> parameterMap = new HashMap<>();
		parameterMap.put("code",code);
		parameterMap.put("type",type);
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
				System.out.println("sessionController.setEtudiant : "+MainUI.getCurrent().getEtudiant().getCod_etu());
				etudiantController.recupererEtatCivil();
				/*etudiantController.recupererInscriptions();
				etudiantController.recupererCalendrierExamens();*/
				MainUI.getCurrent().navigateToDossierEtudiant(parameterMap);
		}
	}

	
	
}
