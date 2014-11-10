package fr.univlorraine.mondossierweb.services.apogee;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fr.univlorraine.mondossierweb.entities.apogee.Utilisateur;
import fr.univlorraine.mondossierweb.repositories.apogee.UtilisateurApogeeRepository;

@Component
@Transactional("transactionManagerApogee")
@Repository
public class UtilisateurServiceImpl implements UtilisateurService{

	@Resource
	private UtilisateurApogeeRepository utilisateurRepository;

	@PersistenceContext (unitName="entityManagerFactoryApogee")
	private transient EntityManager entityManagerApogee;

	@Override
	public Utilisateur findUtilisateur(String uti) {
		return utilisateurRepository.findOne(uti);
	}



}
