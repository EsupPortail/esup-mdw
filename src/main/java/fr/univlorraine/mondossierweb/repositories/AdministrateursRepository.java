package fr.univlorraine.mondossierweb.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.univlorraine.mondossierweb.entities.Administrateurs;

@Repository
public interface AdministrateursRepository extends JpaRepository<Administrateurs, String> {


}
