/**
 *
 *  ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package fr.univlorraine.mondossierweb.services.apogee;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import lombok.Data;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.utils.RequestUtils;



@Component
@Transactional("transactionManagerApogee")
@Data
public class AnnuMelLoginApogeeServiceImpl implements AnnuMelLoginApogeeService {


	@PersistenceContext (unitName="entityManagerFactoryApogee")
	private transient EntityManager entityManagerApogee;


	@Resource
	private RequestUtils requestUtils;
	
	@Override
	public String findMailFromCodEtu(String cod_etu) {
		try{
			//Si on a une requête SQL pour surcharger la requête livrée avec l'application
			if(StringUtils.hasText(requestUtils.getMailFromCodEtu())){
				
				//On utilise la requête indiquée dans le fichier XML
				return (String) entityManagerApogee.createNativeQuery(requestUtils.getMailFromCodEtu().replaceAll("#COD_ETU#", cod_etu)).getSingleResult();
			}else{
				return (String) entityManagerApogee.createNativeQuery("select MAIL FROM ANNU_MEL_LOGIN WHERE COD_ETU="+cod_etu).getSingleResult();
			}
		}catch(NoResultException e){
			return null;
		}catch(EmptyResultDataAccessException e){
			return null;
		}
	}

	@Override
	public String findMailFromLogin(String login) {
		
		//Si on a une requête SQL pour surcharger la requête livrée avec l'application
		if(StringUtils.hasText(requestUtils.getMailFromLogin())){
			
			//On utilise la requête indiquée dans le fichier XML
			return (String) entityManagerApogee.createNativeQuery(requestUtils.getMailFromLogin().replaceAll("#LOGIN#", login)).getSingleResult();

		}else{
		
			return (String) entityManagerApogee.createNativeQuery("select MAIL FROM ANNU_MEL_LOGIN WHERE LOGIN="+login).getSingleResult();
		}
	}

	@Override
	public String findLoginFromCodEtu(String cod_etu) {
		try{
			if(StringUtils.hasText(requestUtils.getLoginFromCodEtu())){
				
				//On utilise la requête indiquée dans le fichier XML
				return (String) entityManagerApogee.createNativeQuery(requestUtils.getLoginFromCodEtu().replaceAll("#COD_ETU#", cod_etu)).getSingleResult();

			}else{
				return (String) entityManagerApogee.createNativeQuery("select LOGIN FROM ANNU_MEL_LOGIN WHERE COD_ETU="+cod_etu).getSingleResult();
				
			}
		}catch(NoResultException e){
			return null;
		}catch(EmptyResultDataAccessException e){
			return null;
		}
	}




}
