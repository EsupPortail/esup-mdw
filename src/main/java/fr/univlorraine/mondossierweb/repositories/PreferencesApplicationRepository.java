package fr.univlorraine.mondossierweb.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.univlorraine.mondossierweb.entities.PreferencesApplication;

@Repository
public interface PreferencesApplicationRepository extends JpaRepository<PreferencesApplication, String> {

	
}
