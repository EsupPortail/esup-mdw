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
package fr.univlorraine.mondossierweb.services.apogee;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.entities.apogee.Anonymat;
import fr.univlorraine.mondossierweb.entities.apogee.Examen;
import fr.univlorraine.mondossierweb.entities.apogee.InfoUsageEtatCivil;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.apogee.Signataire;


public interface MultipleApogeeService {


	public abstract String getAnneeEnCours();
	
	public abstract String getDerniereAnneeOuverteResultats();
	
	public abstract String getLibEtablissementDef();
	
	public abstract List<Examen> getCalendrierExamens(String cod_ind, boolean recupererVet);
	
	public abstract int getDerniereAnneeUniversitaire();

	public abstract List<String> getDixDernieresAnneesUniversitaires();
	
	public abstract List<String> getDernieresAnneesUniversitaires();
	
	public abstract Signataire getSignataireCes(String codeSignataire, String cleApogee);
	
	public abstract Signataire getSignataireRvn(String codeSignataire, String cleApogee);
	
	public abstract InfoUsageEtatCivil getInfoUsageEtatCivilFromCodInd(String cod_ind);
	
	/*public abstract String getCodCivFromCodInd(String cod_ind);
	 
	 public abstract boolean getTemPrUsageFromCodInd(String cod_ind);
	
	public abstract String getCodSexEtaCivFromCodInd(String cod_ind);
	
	public abstract String getLibPrEtaCivFromCodInd(String cod_ind);*/

	public abstract List<Inscrit> getInscritsEtapeJuinSep(Etape e);

	public abstract String getLibelleEtape(Etape e);
	
	public String getLibelleCourtEtape(String codeEtp);
	
	public abstract String getNatureElp(String codElp);

	public abstract List<String> getAnneesFromVetDesc(Etape e, int anneeMaximum);
	
	public abstract List<Anonymat> getNumeroAnonymat(String cod_etu, String cod_anu);

	public abstract int getNbPJnonValides(String cod_ind, String cod_anu);
	
	public abstract boolean isBoursier(String cod_ind, String cod_anu);
	
	public abstract List<BigDecimal> getCodRvn(String cod_ind, String cod_anu, String listeCodesElp);
	
	public abstract String getCodSignataireRvn(BigDecimal cod_rvn);
	
	public abstract List<String> getListeCodeBlocage(String cod_etu);
	
	public abstract String getTemoinEditionCarte(String cod_ind, String cod_anu);

	public abstract boolean isSalarie(String codInd, String codAnu);
	
	public abstract boolean isDossierInscriptionValide(String cod_ind, String cod_anu);

}
