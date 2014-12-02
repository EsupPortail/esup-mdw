package fr.univlorraine.mondossierweb.controllers;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import lombok.Getter;
import lombok.Setter;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.entities.apogee.Composante;
import fr.univlorraine.mondossierweb.entities.apogee.ElementPedagogique;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtapePK;
import fr.univlorraine.mondossierweb.entities.vaadin.ObjetBase;
import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeService;
import fr.univlorraine.mondossierweb.utils.Utils;

/**
 * Gestion de la recherche
 */
@Component
public class RechercheArborescenteController {

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
	private transient EtudiantController etudiantController;
	@Resource
	private transient FavorisController favorisController;


	@Resource
	private MultipleApogeeService multipleApogeeService;

	@Getter
	@Setter
	private String code;

	@Getter
	@Setter
	private String type;


	private List<String> lanneeUniv;

	public List<String> recupererLesDixDernieresAnneeUniversitaire(){
		if(lanneeUniv==null)
			lanneeUniv = multipleApogeeService.getDixDernieresAnneesUniversitaires();
		return lanneeUniv;
	}

	public List<String> recupererListeDesAnnees(Map<String, String> parameterMap){
		String code = parameterMap.get("code");
		String type = parameterMap.get("type");
		List<String> annees = new LinkedList<String>();


		if (type.equals(Utils.VET)) {
			//On part d'une Etape pour établir une liste d'étudiant
			Etape e = new Etape();
			e.setCode(code.split("/")[0]);
			e.setVersion(code.split("/")[1]);
			annees = multipleApogeeService.getAnneesFromVetDesc(e,Integer.parseInt(etudiantController.getAnneeUnivEnCours()));

		}
		return annees;
	}

	public void renseigneObjFromElp(ObjetBase obj, ElementPedagogique elp,String itemId) {
		obj.setType(Utils.ELP);
		if(StringUtils.hasText(itemId)){
			obj.setId(Utils.ELP+":"+elp.getCod_elp()+"_"+itemId);
		}else{
			obj.setId(Utils.ELP+":"+elp.getCod_elp());
		}
		obj.setTrueObjectId(elp.getCod_elp());
		obj.setLibelle(elp.getLib_elp());
		obj.setDeplie("false");

	}

	public void renseigneObjFromVet(ObjetBase obj, VersionEtape vet, String itemId) {
		obj.setType(Utils.VET);
		if(StringUtils.hasText(itemId)){
			obj.setId(Utils.VET+":"+vet.getId().getCod_etp()+"/"+vet.getId().getCod_vrs_vet()+"_"+itemId);
		}else{
			obj.setId(Utils.VET+":"+vet.getId().getCod_etp()+"/"+vet.getId().getCod_vrs_vet());
		}
		obj.setTrueObjectId(vet.getId().getCod_etp()+"/"+vet.getId().getCod_vrs_vet());
		obj.setLibelle(vet.getLib_web_vet());
		obj.setDeplie("false");

	}

	public void renseigneObjFromCmp(ObjetBase obj, Composante comp) {
		obj.setType(Utils.CMP);
		obj.setId(Utils.CMP+":"+comp.getCodCmp());
		obj.setTrueObjectId(comp.getCodCmp());
		obj.setLibelle(comp.getLibCmp());
		obj.setDeplie("false");
	}

	public ObjetBase getObj(String code, String type) {
		ObjetBase obj = new ObjetBase();

		if(type!=null){
			if(type.equals(Utils.VET)){

				VersionEtape vet = new VersionEtape();
				VersionEtapePK vetpk = new VersionEtapePK();
				String[] codevers = code.split("/");
				vetpk.setCod_etp(codevers[0]);
				vetpk.setCod_vrs_vet(codevers[1]);
				vet.setId(vetpk);
				vet.setLib_web_vet(favorisController.getLibelleVet(code));
				renseigneObjFromVet(obj, vet, null);

			}
			if(type.equals(Utils.ELP)){
				ElementPedagogique elp = new ElementPedagogique();
				elp.setCod_elp(code);
				elp.setLib_elp(favorisController.getLibelleElp(code));
				renseigneObjFromElp(obj, elp, null);

			}
			if(type.equals(Utils.CMP)){
				Composante cmp = new Composante();
				cmp.setCodCmp(code);
				cmp.setLibCmp(favorisController.getLibelleComposante(code));
				renseigneObjFromCmp(obj, cmp);
			}


		}
		return obj;
	}

	public List<String> recupererListeAnnees(String code, String type){

		List<String> annees = new LinkedList<String>();
		if(type!=null){
			if (type.equals(Utils.VET)) {
				annees = recupererLesDixDernieresAnneeUniversitaire();
				/*Etape e = new Etape();
				e.setCode(code.split("/")[0]);
				e.setVersion(code.split("/")[1]);
				annees = multipleApogeeService.getAnneesFromVetDesc(e,Integer.parseInt(etudiantController.getAnneeUnivEnCours()));
			*/
			}
			if (type.equals(Utils.CMP) || type.equals(Utils.ELP)) {
				annees = recupererLesDixDernieresAnneeUniversitaire();
			}
		}

		return annees;
	}

}
