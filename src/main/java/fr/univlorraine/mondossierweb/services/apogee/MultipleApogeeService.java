package fr.univlorraine.mondossierweb.services.apogee;

import java.util.List;

import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.entities.apogee.Examen;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.apogee.Signataire;


public interface MultipleApogeeService {


	public abstract String getAnneeEnCours();
	
	public abstract String getLibEtablissementDef();
	
	public abstract List<Examen> getCalendrierExamens(String cod_ind);

	public abstract List<String> getCinqDernieresAnneesUniversitaires();
	
	public abstract Signataire getSignataire(String codeSignataire);
	
	public abstract String getCodCivFromCodInd(String cod_ind);

	public abstract List<Inscrit> getInscritsEtapeJuinSep(Etape e);

	public abstract String getLibelleEtape(Etape e);

	public abstract List<String> getAnneesFromVetDesc(Etape e);
	


}
