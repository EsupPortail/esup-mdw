/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.services.apogee;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fr.univlorraine.mondossierweb.entities.apogee.ElementPedagogique;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
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


	@Override
	public List<Inscrit> getInscritsFromElp(String code, String annee) {
		@SuppressWarnings("unchecked")
		List<Inscrit> linscrits = (List<Inscrit>)entityManagerApogee.createNativeQuery("select i.cod_ind,i.cod_etu, decode(I.LIB_NOM_USU_IND,null,i.lib_nom_pat_ind,I.LIB_NOM_USU_IND) NOM, i.lib_pr1_ind, "+
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
				" where ice.cod_elp = '"+code+"' "+
				" and ice.cod_anu = "+annee +" "+
				" and ice.tem_prc_ice = 'N'  "+
				" and i.cod_ind = ice.cod_ind  "+
				" and rj.cod_ind = ice.cod_ind  "+
				" and e.cod_etp = ice.cod_etp "+
				" and rj.cod_elp = ice.cod_elp and rj.cod_anu = ice.cod_anu  "+
				" and rj.cod_ses <= '1' and rj.cod_adm = '1'  "+
				" and ve.cod_etp = e.cod_etp "+
				" and ve.cod_vrs_vet = ice.cod_vrs_vet "+
				" and i.cod_etu is not null "+
				" order by NOM,i.lib_pr1_ind,i.date_nai_ind", Inscrit.class).getResultList();
		return linscrits;
	}


	@Override
	public List<BigDecimal> getCodIndInscritsFromGroupe(String code, String annee) {
		@SuppressWarnings("unchecked")
		List<BigDecimal> lCodindInscrits = (List<BigDecimal>)entityManagerApogee.createNativeQuery("select distinct ind.cod_ind "+
    "from IND_AFFECTE_GPE ind, GROUPE g "+
    "where ind.COD_GPE = g.cod_gpe  "+
    "and g.COD_GPE = "+code+ " "+
    "and ind.COD_ANU= "+annee+ " ").getResultList();
    		
		return lCodindInscrits;
	}




}
