/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.repositories.mdw;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import fr.univlorraine.mondossierweb.entities.mdw.FavorisPK;

@Repository
public interface FavorisRepository extends JpaRepository<Favoris, FavorisPK> {

	
	
	@Query(name="Favoris.findFavorisFromLogin", value="SELECT f " +
			"FROM Favoris f " +
			"WHERE f.id.login = ?1")
	public List<Favoris> findFavorisFromLogin(String login);
}
