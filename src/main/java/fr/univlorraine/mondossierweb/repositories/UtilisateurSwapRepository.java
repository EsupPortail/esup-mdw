package fr.univlorraine.mondossierweb.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.univlorraine.mondossierweb.entities.UtilisateurSwap;

@Repository
public interface UtilisateurSwapRepository extends JpaRepository<UtilisateurSwap, String> {

	
}
