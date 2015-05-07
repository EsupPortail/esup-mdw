package fr.univlorraine.mondossierweb.repositories.mdw;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.univlorraine.mondossierweb.entities.mdw.Administrateurs;

@Repository
public interface AdministrateursRepository extends JpaRepository<Administrateurs, String> {


}
