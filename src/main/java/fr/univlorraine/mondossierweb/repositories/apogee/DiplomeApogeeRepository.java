/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.repositories.apogee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.univlorraine.mondossierweb.entities.apogee.DiplomeApogee;

@Repository
public interface DiplomeApogeeRepository extends JpaRepository<DiplomeApogee, String> {

	

}
