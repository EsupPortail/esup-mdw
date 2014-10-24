package fr.univlorraine.mondossierweb.repositories.apogee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtapePK;

@Repository
public interface VersionEtapeApogeeRepository extends JpaRepository<VersionEtape, VersionEtapePK> {
	

	
	

}
