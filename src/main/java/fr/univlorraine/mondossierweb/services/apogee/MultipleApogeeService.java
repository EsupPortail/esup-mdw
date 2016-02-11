/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.services.apogee;

import java.util.List;

import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.entities.apogee.Anonymat;
import fr.univlorraine.mondossierweb.entities.apogee.Examen;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.apogee.Signataire;


public interface MultipleApogeeService {


	public abstract String getAnneeEnCours();
	
	public abstract String getLibEtablissementDef();
	
	public abstract List<Examen> getCalendrierExamens(String cod_ind);
	
	public abstract int getDerniereAnneeUniversitaire();

	public abstract List<String> getDixDernieresAnneesUniversitaires();
	
	public abstract List<String> getDernieresAnneesUniversitaires();
	
	public abstract Signataire getSignataire(String codeSignataire);
	
	public abstract String getCodCivFromCodInd(String cod_ind);

	public abstract List<Inscrit> getInscritsEtapeJuinSep(Etape e);

	public abstract String getLibelleEtape(Etape e);
	
	public abstract String getNatureElp(String codElp);

	public abstract List<String> getAnneesFromVetDesc(Etape e, int anneeMaximum);
	
	public abstract List<Anonymat> getNumeroAnonymat(String cod_etu, String cod_anu);

	public abstract String getCategorieSocioProfessionnelle(String cod_ind, String cod_anu);

	public abstract int getNbPJnonValides(String cod_ind, String cod_anu);
	
	public abstract boolean isBoursier(String cod_ind, String cod_anu);

}
