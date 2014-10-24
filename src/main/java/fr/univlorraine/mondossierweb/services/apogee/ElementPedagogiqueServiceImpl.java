package fr.univlorraine.mondossierweb.services.apogee;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fr.univlorraine.mondossierweb.entities.apogee.ElementPedagogique;
import fr.univlorraine.mondossierweb.repositories.apogee.ElementPedagogiqueApogeeRepository;

@Component
@Transactional("transactionManagerApogee")
@Repository
public class ElementPedagogiqueServiceImpl implements ElementPedagogiqueService{

	@Resource
	private ElementPedagogiqueApogeeRepository elpRepository;

	@PersistenceContext (unitName="entityManagerFactoryApogee")
	private transient EntityManager entityManagerApogee;


	@Override
	public String getLibelleElp(String codElp) {
		ElementPedagogique elp = elpRepository.findOne(codElp);
		if(elp!=null){
			return elp.getLib_elp();
		}
		return null;
	}


}
