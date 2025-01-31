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

import jakarta.annotation.Resource;

import lombok.Getter;
import lombok.Setter;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.beans.CollectionDeGroupes;
import fr.univlorraine.mondossierweb.beans.Groupe;
import fr.univlorraine.mondossierweb.entities.apogee.Composante;
import fr.univlorraine.mondossierweb.entities.apogee.ElementPedagogique;
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

	//On utilise soit recupererLesDixDernieresAnneeUniversitaire soit recupererLesDernieresAnneeUniversitaire
	public List<String> recupererLesDixDernieresAnneeUniversitaire(){
		if(lanneeUniv==null)
			lanneeUniv = multipleApogeeService.getDixDernieresAnneesUniversitaires();
		return lanneeUniv;
	}
	
	//On utilise soit recupererLesDixDernieresAnneeUniversitaire soit recupererLesDernieresAnneeUniversitaire
	public List<String> recupererLesDernieresAnneeUniversitaire(){
		if(lanneeUniv==null)
			lanneeUniv = multipleApogeeService.getDernieresAnneesUniversitaires();
		return lanneeUniv;
	}

	/*public List<String> recupererListeDesAnnees(Map<String, String> parameterMap){
		String code = parameterMap.get("code");
		String type = parameterMap.get("type");
		List<String> annees = new LinkedList<String>();


		if (type.equals(Utils.VET)) {
			//On part d'une Etape pour établir une liste d'étudiant
			Etape e = new Etape();
			e.setCode(code.split("/")[0]);
			e.setVersion(code.split("/")[1]);
			annees = multipleApogeeService.getAnneesFromVetDesc(e);

		}
		return annees;
	}*/
	
	public void renseigneObjFromGroupe(ObjetBase obj, Groupe g,String itemId){
		obj.setType(Utils.GRP);
		if(StringUtils.hasText(itemId)){
			obj.setId(Utils.GRP+":"+g.getCleGroupe()+"_"+itemId);
		}else{
			obj.setId(Utils.GRP+":"+g.getCleGroupe());
		}
		obj.setTrueObjectId(g.getCodGroupe());
		obj.setLibelle(g.getLibGroupe());
		obj.setDeplie("true");
		
	}
	
	public void renseigneObjFromCollection(ObjetBase obj, CollectionDeGroupes cdg,String itemId){
		obj.setType(Utils.COL);
		if(StringUtils.hasText(itemId)){
			obj.setId(Utils.COL+":"+cdg.getCodCollection()+"_"+itemId);
		}else{
			obj.setId(Utils.COL+":"+cdg.getCodCollection());
		}
		obj.setTrueObjectId(cdg.getCodCollection());
		obj.setLibelle(cdg.getCodCollection());
		obj.setDeplie("true");
		
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

	public String getTypeObj(String typeObj, String code){
		if(typeObj==null || !typeObj.equals(Utils.ELP)){
			return  Utils.convertTypeToDisplay(typeObj);
		}else{
			return getNatureElp(code);
		}
	}
	
	private String getNatureElp(String codElp){
		return multipleApogeeService.getNatureElp(codElp);
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

	/*
	public List<String> recupererListeAnnees(String code, String type){

		List<String> annees = new LinkedList<String>();
		if(type!=null){
			if (type.equals(Utils.VET)) {
				annees = recupererLesDernieresAnneeUniversitaire();
			}
			if (type.equals(Utils.CMP) || type.equals(Utils.ELP)) {
				annees = recupererLesDixDernieresAnneeUniversitaire();
			}
		}

		return annees;
	}*/

}
