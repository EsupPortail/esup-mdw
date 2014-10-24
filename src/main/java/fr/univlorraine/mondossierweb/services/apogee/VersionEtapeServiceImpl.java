package fr.univlorraine.mondossierweb.services.apogee;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtapePK;
import fr.univlorraine.mondossierweb.repositories.apogee.VersionEtapeApogeeRepository;

@Component
@Transactional("transactionManagerApogee")
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
