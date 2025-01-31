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

import fr.univlorraine.mondossierweb.entities.apogee.Utilisateur;
import fr.univlorraine.mondossierweb.repositories.apogee.UtilisateurApogeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;

@Component
@org.springframework.transaction.annotation.Transactional("transactionManagerApogee")
@Repository
public class UtilisateurServiceImpl implements UtilisateurService{

	@Resource
	private UtilisateurApogeeRepository utilisateurRepository;

	@PersistenceContext (unitName="entityManagerFactoryApogee")
	private transient EntityManager entityManagerApogee;

	@Override
	public Utilisateur findUtilisateur(String uti) {
		return utilisateurRepository.findById(uti).orElse(null);
	}



}
