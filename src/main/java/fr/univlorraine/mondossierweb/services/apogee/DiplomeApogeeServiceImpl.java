/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.services.apogee;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fr.univlorraine.mondossierweb.entities.apogee.DiplomeApogee;
import fr.univlorraine.mondossierweb.repositories.apogee.DiplomeApogeeRepository;

@Component
@Transactional("transactionManagerApogee")
@Repository
public class DiplomeApogeeServiceImpl implements DiplomeApogeeService{

	@Resource
	private DiplomeApogeeRepository dipRepository;

	@PersistenceContext (unitName="entityManagerFactoryApogee")
	private transient EntityManager entityManagerApogee;

	@Override
	public DiplomeApogee findDiplome(String codDip) {
		return dipRepository.findOne(codDip);
	}


	

}
