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

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtapePK;
import fr.univlorraine.mondossierweb.repositories.apogee.VersionEtapeApogeeRepository;

@Component
@org.springframework.transaction.annotation.Transactional("transactionManagerApogee")
@Repository
public class VersionEtapeServiceImpl implements VersionEtapeService{

	@Resource
	private VersionEtapeApogeeRepository versionEtapeRepository;




	@Override
	public String getLibelleVet(String codvet, String versVet) {
		VersionEtapePK vepk = new VersionEtapePK();
		vepk.setCod_etp(codvet);
		vepk.setCod_vrs_vet(versVet);
		VersionEtape vet = versionEtapeRepository.findOne(vepk);
		if(vet!=null){
			return vet.getLib_web_vet();
		}
		return null;
	}


}
