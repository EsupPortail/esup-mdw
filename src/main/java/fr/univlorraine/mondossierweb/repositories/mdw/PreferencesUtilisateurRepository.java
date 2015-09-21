/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.repositories.mdw;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fr.univlorraine.mondossierweb.entities.mdw.PreferencesUtilisateur;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesUtilisateurPK;

@Repository
public interface PreferencesUtilisateurRepository extends JpaRepository<PreferencesUtilisateur, PreferencesUtilisateurPK> {

	
	
	@Query(name="PreferencesUtilisateur.findOnePrefFromLoginAndPrefid", value="SELECT p " +
			"FROM PreferencesUtilisateur p " +
			"WHERE p.id.login = ?1 "+
			"AND p.id.prefid = ?2")
	public PreferencesUtilisateur findOnePrefFromLoginAndPrefid(String login, String prefid);
}
