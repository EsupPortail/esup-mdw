package fr.univlorraine.mondossierweb.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.univlorraine.mondossierweb.entities.Structure;

@Repository
public interface StructureRepository extends JpaRepository<Structure, String> {

}
