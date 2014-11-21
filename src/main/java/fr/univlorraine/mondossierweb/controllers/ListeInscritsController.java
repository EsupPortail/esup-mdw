package fr.univlorraine.mondossierweb.controllers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import lombok.Getter;
import lombok.Setter;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.converters.EmailConverterInterface;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeService;
import fr.univlorraine.mondossierweb.utils.Utils;

/**
 * Gestion de la recherche
 */
@Component
public class ListeInscritsController {

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient UserDetailsService userDetailsService;
	@Resource
	private transient UiController uiController;
	@Resource
	private MultipleApogeeService multipleApogeeService;
	@Resource(name="emailConverter")
	private transient EmailConverterInterface emailConverter;


	
	
	public void recupererLaListeDesInscrits(Map<String, String> parameterMap){
		String code = parameterMap.get("code");
		String type = parameterMap.get("type");
		
		if(MainUI.getCurrent().getListeInscrits()!=null){
			MainUI.getCurrent().getListeInscrits().clear();
		}
		MainUI.getCurrent().setCodeObjListInscrits(code);
		MainUI.getCurrent().setTypeObjListInscrits(type);
		List<Inscrit> listeInscrits = null;
		

		if (type.equals(Utils.TYPE_VET)) {
			//On part d'une Etape pour établir une liste d'étudiant
			Etape e = new Etape();
			e.setCode(code.split("/")[0]);
			e.setVersion(code.split("/")[1]);
			System.out.println("code : "+e.getCode()+" version : "+e.getVersion());
			List<String> annee = multipleApogeeService.getAnneesFromVetDesc(e);
			//On prend l'année la plus récente (la premiere de la liste)
			e.setAnnee(annee.get(0));
			System.out.println("annee : "+e.getAnnee());
			e.setLibelle(multipleApogeeService.getLibelleEtape(e));
			System.out.println("libellé : "+e.getLibelle());
			MainUI.getCurrent().setEtapeListeInscrits(e);
			listeInscrits = (List<Inscrit>) multipleApogeeService.getInscritsEtapeJuinSep(e);
			
		} else {
			/*if (type.equals("GRP")) {
				//On part d'un Groupe pour établir une liste d'étudiant
				ObjetRecherche r = new ObjetRecherche();
				r.setAnneeencours(annee);
				r.setCode(code);
				listeInscrits = (ArrayList<Inscrit>) service.getInscritsGroupe(r);
				recherchecodegroupe=true;
			}else{
				//On part d'un ELP pour établir une liste d'étudiant
				libelle = service.getLibelleElementPedagogique(code);
				ElementPedagogique e = new ElementPedagogique();
				e.setCode(code);
				e.setAnnee(annee);
		
				listeInscrits = (ArrayList<Inscrit>) service.getInscritsElementPedagogiqueJuinSepEtape(e);
				
			}*/
		}

		
		//setLoginInscrits(listeInscrits);
		setMailInscrits(listeInscrits);

		//on vérifie que les photo sont récupérées pour savoir si on peut afficher le lien vers le trombinoscope:
		/*if(listeInscrits != null && listeInscrits.size() > 0) {
			listeInscrits.get(0).setUrlphoto(photo.getUrlPhoto(listeInscrits.get(0).getCod_ind(), listeInscrits.get(0).getCod_etu()));
			if (listeInscrits.get(0).getUrlphoto() != null && !listeInscrits.get(0).getUrlphoto().equals("")) {
				photosValides = true;
			}
		}*/
		
		
		MainUI.getCurrent().setListeInscrits(listeInscrits);
		
	}
	
	/**
	 * renseigne les logins de chaque inscrit.
	 *
	 */
/*	private void setLoginInscrits( List<Inscrit> listeInscrits) {
		for (Inscrit i : listeInscrits) {
			if(i.getCod_etu()!=null)
			i.setLogin(service.getLoginFromCodEtu(i.getCod_etu()));
		}
	}*/
	
	/**
	 * renseigne les emails de chaque inscrit.
	 *
	 */
	private void setMailInscrits(List<Inscrit> listeInscrits) {
		for (Inscrit i : listeInscrits) {
			if(i.getCod_etu()!=null)
				i.setEmail(emailConverter.getMail(null,i.getCod_etu()));
		}
	}
	

}
