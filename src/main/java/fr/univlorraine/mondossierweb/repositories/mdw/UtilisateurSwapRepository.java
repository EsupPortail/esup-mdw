/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.repositories.mdw;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.univlorraine.mondossierweb.entities.mdw.UtilisateurSwap;

@Repository
public interface UtilisateurSwapRepository extends JpaRepository<UtilisateurSwap, String> {

	
}
