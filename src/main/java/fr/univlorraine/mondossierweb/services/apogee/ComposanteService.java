/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.services.apogee;

import java.util.List;

import fr.univlorraine.mondossierweb.entities.apogee.Composante;
import fr.univlorraine.mondossierweb.entities.apogee.ElementPedagogique;
import fr.univlorraine.mondossierweb.entities.apogee.VersionDiplome;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;

public interface ComposanteService {
	
	public List<Composante> findComposantesEnService();
	
	public String getLibelleComposante(String codCmp);
	
	public List<VersionDiplome> findVdiFromComposante(String annee, String cod_cmp);
	
	public List<VersionEtape> findVetFromVdiAndCmp(String annee,String cod_vdi, String vrs_vdi, String codcmp);
	
	public List<ElementPedagogique> findElpFromVet(String codEtp, String vrsEtp);
	
	public List<ElementPedagogique> findElpFromElp(String codElp);
}
