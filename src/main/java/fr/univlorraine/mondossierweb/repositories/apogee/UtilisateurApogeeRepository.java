/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.repositories.apogee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.univlorraine.mondossierweb.entities.apogee.Utilisateur;

@Repository
public interface UtilisateurApogeeRepository extends JpaRepository<Utilisateur, String> {
	
	

}
