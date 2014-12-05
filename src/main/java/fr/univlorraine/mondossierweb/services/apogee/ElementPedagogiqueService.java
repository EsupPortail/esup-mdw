package fr.univlorraine.mondossierweb.services.apogee;

import java.util.List;

import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;


public interface ElementPedagogiqueService {

	
	public String getLibelleElp(String codElp);

	public List<Inscrit> getInscritsFromElp(String code, String annee);
	
	public List<String> getCodIndInscritsFromGroupe(String code, String annee);
	
	
}
