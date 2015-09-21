/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.services.apogee;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import lombok.Data;

import org.springframework.dao.EmptyResultDataAccessException;
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
		try{
			String mail=(String) entityManagerApogee.createNativeQuery("select MAIL FROM ANNU_MEL_LOGIN WHERE COD_ETU="+cod_etu).getSingleResult();
			return mail;
		}catch(NoResultException e){
			return null;
		}catch(EmptyResultDataAccessException e){
			return null;
		}
	}

	@Override
	public String findMailFromLogin(String login) {
		return (String) entityManagerApogee.createNativeQuery("select MAIL FROM ANNU_MEL_LOGIN WHERE LOGIN="+login).getSingleResult();

	}

	@Override
	public String findLoginFromCodEtu(String cod_etu) {
		try{
			String login=(String) entityManagerApogee.createNativeQuery("select LOGIN FROM ANNU_MEL_LOGIN WHERE COD_ETU="+cod_etu).getSingleResult();
			return login;
		}catch(NoResultException e){
			return null;
		}catch(EmptyResultDataAccessException e){
			return null;
		}
	}




}
