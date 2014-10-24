package fr.univlorraine.mondossierweb.repositories.apogee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.univlorraine.mondossierweb.entities.apogee.ElementPedagogique;

@Repository
public interface ElementPedagogiqueApogeeRepository extends JpaRepository<ElementPedagogique, String> {

	

}
