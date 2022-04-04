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

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import fr.univlorraine.mondossierweb.entities.apogee.Composante;
import fr.univlorraine.mondossierweb.entities.apogee.ElementPedagogique;
import fr.univlorraine.mondossierweb.entities.apogee.VersionDiplome;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.repositories.apogee.ComposanteApogeeRepository;

@Component
@org.springframework.transaction.annotation.Transactional("transactionManagerApogee")
@Repository
public class ComposanteServiceImpl implements ComposanteService{

	@Resource
	private ComposanteApogeeRepository composanteRepository;

	@PersistenceContext (unitName="entityManagerFactoryApogee")
	private transient EntityManager entityManagerApogee;

	@Override
	public List<Composante> findComposantesEnService() {
		return composanteRepository.findComposantesEnService();
	}



	@Override
	public List<VersionDiplome> findVdiFromComposante(String annee,String cod_cmp) {

		@SuppressWarnings("unchecked")
		List<VersionDiplome> lvdi = (List<VersionDiplome>)entityManagerApogee.createNativeQuery(
				"select cod_dip, cod_vrs_vdi, lib_web_vdi, cod_tpd_etb from "+
						"(select distinct chv.cod_cmp, lib_cmp, vfv.cod_dip, vfv.cod_vrs_vdi, vdi.lib_web_vdi, dip.cod_tpd_etb "+
						"from cmp_habiliter_vdi chv, vdi_fractionner_vet vfv, version_diplome vdi, composante, diplome dip   "+
						"where ("+annee+" >= vfv.DAA_DEB_RCT_VET and vfv.DAA_FIN_RCT_VET >= "+annee+")  "+
						"and chv.cod_dip = vfv.cod_dip and chv.cod_vrs_vdi = vfv.cod_vrs_vdi  "+
						"and chv.TEM_EN_SVE_CVD = 'O' and vdi.cod_dip = vfv.cod_dip "+ 
						"and vdi.cod_vrs_vdi = vfv.cod_vrs_vdi and chv.cod_cmp = '"+cod_cmp+"' and chv.cod_cmp = composante.cod_cmp "+
						"and vfv.cod_dip = dip.cod_dip "+
						"order by vdi.lib_web_vdi )", VersionDiplome.class).getResultList(); 

		
		return lvdi;


	}


	@Override
	public List<VersionEtape> findVetFromVdiAndCmp(String annee,String cod_vdi, String vrs_vdi, String codcmp) {

		@SuppressWarnings("unchecked")
		List<VersionEtape> lvdi = (List<VersionEtape>)entityManagerApogee.createNativeQuery(
				"select vfv.cod_etp, vfv.cod_vrs_vet, ve.lib_web_vet "+
						"from vdi_fractionner_vet vfv, etape e,version_etape ve, version_diplome vdi   "+
						"where ("+annee+" >= vfv.DAA_DEB_RCT_VET and vfv.DAA_FIN_RCT_VET >= "+annee+")  "+
						"and e.cod_etp = vfv.cod_etp  "+
						"and ve.cod_etp = e.cod_etp "+
						"and ve.cod_cmp= '" +codcmp+"' "+
						"and ve.cod_vrs_vet = vfv.cod_vrs_vet "+
						"and vdi.cod_dip = vfv.cod_dip and vdi.cod_vrs_vdi = vfv.cod_vrs_vdi  "+
						"and vdi.cod_dip = '"+cod_vdi+"' and vdi.cod_vrs_vdi = '"+vrs_vdi+"'  "+
						"order by ve.lib_web_vet ", VersionEtape.class).getResultList(); 
		return lvdi;


	}

	@Override
	public List<ElementPedagogique> findElpFromVet(String codEtp, String vrsEtp) {
		@SuppressWarnings("unchecked")
		List<ElementPedagogique> lelp = (List<ElementPedagogique>)entityManagerApogee.createNativeQuery(
				"select e.cod_elp, e.lib_elp "+
						" from vet_regroupe_lse vrl, liste_elp le, elp_regroupe_elp ere, element_pedagogi e   "+
						" where vrl.cod_etp = '"+codEtp+"'  "+
						" and vrl.cod_vrs_vet = '"+vrsEtp+"'  "+
						" and vrl.dat_frm_rel_lse_vet is null  "+
						" and le.cod_lse = vrl.cod_lse  "+
						" and le.eta_lse = 'O'  "+
						" and ere.cod_lse = le.cod_lse  "+
						" and ere.eta_lse = 'O'  "+
						" and ere.date_fermeture_lien is null   "+
						" and ere.eta_elp_fils = 'O'  "+
						" and ere.tem_sus_elp_fils = 'N'  "+
						" and e.cod_elp = ere.cod_elp_fils "+
						" order by e.lib_elp", ElementPedagogique.class).getResultList(); 
		return lelp;
	}

	@Override
	public List<ElementPedagogique> findElpFromElp(String codElp) {
		@SuppressWarnings("unchecked")
		List<ElementPedagogique> lelp = (List<ElementPedagogique>)entityManagerApogee.createNativeQuery(
				"select e.cod_elp, e.lib_elp "+
						"from elp_regroupe_elp ere, element_pedagogi e  "+
						"where ere.cod_elp_pere = '"+codElp+"'  "+
						"and ere.eta_lse = 'O'  "+
						"and ere.date_fermeture_lien is null  "+
						"and ere.eta_elp_fils = 'O'  "+
						"and ere.tem_sus_elp_fils = 'N'  "+
						"and ere.eta_elp_pere = 'O'  "+
						"and ere.tem_sus_elp_pere = 'N'  "+
						"and e.cod_elp = ere.cod_elp_fils  "+
						"order by e.lib_elp", ElementPedagogique.class).getResultList(); 
		return lelp;
	}



	@Override
	public String getLibelleComposante(String codCmp) {
		Composante c = composanteRepository.findById(codCmp).orElse(null);
		if(c!=null){
			return c.getLibCmp();
		}
		return null;
	}



}
