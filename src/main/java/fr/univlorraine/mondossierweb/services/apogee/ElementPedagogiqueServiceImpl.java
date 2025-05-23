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

import fr.univlorraine.mondossierweb.entities.apogee.ElementPedagogique;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.repositories.apogee.ElementPedagogiqueApogeeRepository;
import fr.univlorraine.mondossierweb.utils.RequestUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Component
@org.springframework.transaction.annotation.Transactional("transactionManagerApogee")
@Repository
@Slf4j
public class ElementPedagogiqueServiceImpl implements ElementPedagogiqueService{

	@Resource
	private ElementPedagogiqueApogeeRepository elpRepository;

	@PersistenceContext (unitName="entityManagerFactoryApogee")
	private transient EntityManager entityManagerApogee;

	@Resource
	private RequestUtils requestUtils;


	@Override
	public String getLibelleElp(String codElp) {
		ElementPedagogique elp = elpRepository.findById(codElp).orElse(null);
		if(elp!=null){
			return elp.getLib_elp();
		}
		return null;
	}


	@Override
	public List<Inscrit> getInscritsFromElp(String codElp, String codAnu) {
		@SuppressWarnings("unchecked")
		String requeteSQL="";

		//Si on a une requête SQL pour surcharger la requête livrée avec l'application
		if(StringUtils.hasText(requestUtils.getInscritsFromElp())){
			//On utilise la requête indiquée dans le fichier XML
			log.info("getInscritsFromElp => Utilisation de la requête du fichier apogeeRequest.xml");
			requeteSQL = requestUtils.getInscritsFromElp().replaceAll("#COD_ELP#", codElp).replaceAll("#COD_ANU#", codAnu);
		}else{
			log.info("getInscritsFromElp => Utilisation de la requête intégrée à MDW");
			requeteSQL = "select rownum, i.cod_ind,i.cod_etu, I.lib_nom_pat_ind NOM, I.LIB_NOM_USU_IND NOM_USUEL, i.lib_pr1_ind, "+
				" to_char(i.date_nai_ind,'DD/MM/YYYY') date_nai_ind,  "+
				" decode(avc.ETA_ANO_OBJ_AOA,'V',' ',nvl(decode(to_char(rj.not_elp),null,rj.not_sub_elp,to_char(rj.not_elp)),' ')) notej,  "+
				" decode(avc.ETA_ANO_OBJ_AOA,'V',' ',nvl(rj.cod_tre,' ')) resj, "+
				" decode(avc2.ETA_ANO_OBJ_AOA,'V',' ',nvl(decode(to_char(rs.not_elp),null,rs.not_sub_elp,to_char(rs.not_elp)),' ')) notes,  "+
				" decode(avc2.ETA_ANO_OBJ_AOA,'V',' ',nvl(rs.cod_tre,' ')) ress, "+
				" ice.cod_etp cod_etp,ice.cod_vrs_vet cod_vrs_vet,ve.lib_web_vet lib_etp"+
				" from apogee.individu i, apogee.ind_contrat_elp ice "+
				" left outer join apogee.resultat_elp rs on ( rs.cod_ind = ice.cod_ind "+
				" and rs.cod_elp = ice.cod_elp and rs.cod_anu = ice.cod_anu "+
				" and rs.cod_ses = '2' and rs.cod_adm = '1' ),  "+
				" apogee.resultat_elp rj left outer join AVCT_OBJ_ANO avc on (avc.COD_ANU=rj.COD_ANU "+
				" and avc.COD_OBJ_AOA=rj.cod_elp "+
				" and avc.COD_SES_OBJ_AOA=rj.COD_SES "+
				" and avc.COD_ADM_OBJ_AOA=rj.COD_ADM "+
				" and avc.TYP_OBJ_AOA='ELP' "+
				" and avc.COD_VRS_OBJ_AOA=0 "+
				" and avc.ETA_ANO_OBJ_AOA='V' ) "+
				" left outer join AVCT_OBJ_ANO avc2 on (avc2.COD_ANU=rj.COD_ANU "+
				" and avc2.COD_OBJ_AOA=rj.cod_elp "+
				" and avc2.COD_SES_OBJ_AOA='2' "+
				" and avc2.COD_ADM_OBJ_AOA='1' "+
				" and avc2.TYP_OBJ_AOA='ELP' "+
				" and avc2.COD_VRS_OBJ_AOA=0 "+
				" and avc2.ETA_ANO_OBJ_AOA='V' ), "+
				" apogee.etape e, version_etape ve "+
				" where ice.cod_elp = '"+codElp+"' "+
				" and ice.cod_anu = "+codAnu +" "+
				" and ice.tem_prc_ice = 'N'  "+
				" and i.cod_ind = ice.cod_ind  "+
				" and rj.cod_ind = ice.cod_ind  "+
				" and e.cod_etp = ice.cod_etp "+
				" and rj.cod_elp = ice.cod_elp and rj.cod_anu = ice.cod_anu  "+
				" and rj.cod_ses <= '1' and rj.cod_adm = '1'  "+
				" and ve.cod_etp = e.cod_etp "+
				" and ve.cod_vrs_vet = ice.cod_vrs_vet "+
				" and i.cod_etu is not null "+
				" order by NOM,i.lib_pr1_ind,i.date_nai_ind";
		}
		List<Inscrit> linscrits = (List<Inscrit>)entityManagerApogee.createNativeQuery(requeteSQL, Inscrit.class).getResultList();
		return linscrits;
	}


	@Override
	public List<BigDecimal> getCodIndInscritsFromGroupe(String codGpe, String codAnnu) {
		@SuppressWarnings("unchecked")

		String requeteSQL="";

		//Si on a une requête SQL pour surcharger la requête livrée avec l'application
		if(StringUtils.hasText(requestUtils.getCodIndInscritsFromGroupe())){
			//On utilise la requête indiquée dans le fichier XML
			log.info("getCodIndInscritsFromGroupe => Utilisation de la requête du fichier apogeeRequest.xml");
			requeteSQL = requestUtils.getCodIndInscritsFromGroupe().replaceAll("#COD_GPE#", codGpe).replaceAll("#COD_ANU#", codAnnu);
		}else{
			log.info("getCodIndInscritsFromGroupe => Utilisation de la requête intégrée à MDW");
			requeteSQL = "select distinct ind.cod_ind "+
				"from IND_AFFECTE_GPE ind, GROUPE g "+
				"where ind.COD_GPE = g.cod_gpe  "+
				"and g.COD_GPE = "+codGpe+ " "+
				"and ind.COD_ANU= "+codAnnu+ " ";
		}
		List<BigDecimal> lCodindInscrits = (List<BigDecimal>)entityManagerApogee.createNativeQuery(requeteSQL).getResultList();

		return lCodindInscrits;
	}

}
