/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.services.apogee;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional("transactionManagerApogee")
@Repository
public class InscriptionServiceImpl implements InscriptionService{



	@PersistenceContext (unitName="entityManagerFactoryApogee")
	private transient EntityManager entityManagerApogee;


	@Override
	public String getProfil(String codAnu, String codInd) {
		@SuppressWarnings("unchecked")
		String  codPru= (String)entityManagerApogee.createNativeQuery("select cod_pru"+
				" from apogee.ins_adm_anu "+
				" where cod_anu = "+codAnu+" "+
				" and cod_ind = '"+codInd+"' ").getSingleResult();
		return codPru;
	}


	@Override
	public String getCgeFromCodIndIAE(String codAnu, String codInd,String codEtp, String vrsVet) {
		@SuppressWarnings("unchecked")
		String  codCge= (String)entityManagerApogee.createNativeQuery("select iae.cod_cge "+
				" from apogee.ins_adm_etp iae "+
				" where iae.cod_anu = "+codAnu+"  "+
				" and iae.cod_ind = '"+codInd+"'  "+
				" and iae.cod_etp = '"+codEtp+"'  "+
				" and iae.cod_vrs_vet = "+vrsVet+"  "+
				" and iae.eta_iae = 'E' ").getSingleResult();
		return codCge;
	}


	@Override
	public String getCmpFromCodIndIAE(String codAnu, String codInd,String codEtp, String vrsVet) {
		@SuppressWarnings("unchecked")
		String  codCmp= (String)entityManagerApogee.createNativeQuery("select iae.cod_cmp "+
				" from apogee.ins_adm_etp iae  "+
				" where iae.cod_anu = "+codAnu+"  "+
				" and iae.cod_ind = '"+codInd+"'  "+
				" and iae.cod_etp = '"+codEtp+"'  "+
				" and iae.cod_vrs_vet = "+vrsVet+"  "+
				" and iae.eta_iae = 'E' ").getSingleResult();
		return codCmp;
	}


	@Override
	public String getFormationEnCours(String codetu) {
		@SuppressWarnings("unchecked")
		List<String>  llib = (List<String>) entityManagerApogee.createNativeQuery("select LIB_WEB_VET "+
				"from ins_adm_etp ins, etape e, version_etape ve, individu ind "+
				"where e.cod_etp = ins.cod_etp  "+
				"and ve.cod_etp = e.cod_etp "+
				"and ve.cod_vrs_vet = ins.cod_vrs_vet "+
				"and ind.cod_etu = "+codetu+" "+
				"and ins.cod_ind = ind.cod_ind "+
				"and   eta_iae = 'E' "+
				"ORDER BY COD_ANU DESC").getResultList();

		if(llib!=null && llib.size()>0){
			return llib.get(0);
		}

		return null;

	}




}
