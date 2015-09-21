/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.repositories.apogee;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fr.univlorraine.mondossierweb.entities.apogee.Composante;

@Repository
public interface ComposanteApogeeRepository extends JpaRepository<Composante, String> {
	
	@Query(name="Composante.findComposantesEnService", value="SELECT c " +
			"FROM Composante c " +
			"WHERE c.temEnSveCmp = 'O' order by c.libCmp")
	public List<Composante> findComposantesEnService();
	
	

}
