package fr.univlorraine.mondossierweb.services.apogee;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import lombok.Data;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;



@Component
@Transactional("transactionManagerApogee")
@Data
public class AnnuMelLoginApogeeServiceImpl implements AnnuMelLoginApogeeService {

	
	@PersistenceContext (unitName="entityManagerFactoryApogee")
	private transient EntityManager entityManagerApogee;

	@Override
	public String findMailFromCodEtu(String cod_etu) {
		return (String) entityManagerApogee.createNativeQuery("select MAIL FROM ANNU_MEL_LOGIN WHERE COD_ETU="+cod_etu).getSingleResult();
		
	}

	
	

}
