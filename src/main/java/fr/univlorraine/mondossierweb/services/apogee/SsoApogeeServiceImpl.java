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
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.config.ResultType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.entities.apogee.DiplomeApogee;
import fr.univlorraine.mondossierweb.entities.apogee.Examen;
import fr.univlorraine.mondossierweb.repositories.apogee.DiplomeApogeeRepository;
import fr.univlorraine.mondossierweb.utils.RequestUtils;

@Component
@Transactional("transactionManagerApogee")
@Repository
public class SsoApogeeServiceImpl implements SsoApogeeService{


	@PersistenceContext (unitName="entityManagerFactoryApogee")
	private transient EntityManager entityManagerApogee;

	@Resource
	private RequestUtils requestUtils;

	@Override
	public String getMutuelle(String codAnu, String codInd) {

		String mutuelle ="";
		String requeteSQL ="";

		//Si on a une requête SQL pour surcharger la requête livrée avec l'application
		if(StringUtils.hasText(requestUtils.getMutuelle())){

			//On utilise la requête indiquée dans le fichier XML
			requeteSQL = requestUtils.getMutuelle().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

		}else{
			//Préparation de la requête
			requeteSQL = "SELECT decode(mut.lib_nom_mut,NULL,'* * * * * * * *',drt.lic_drt)  "+
					"FROM ins_adm_anu iaa, mutuelle mut, droit drt "+
					"WHERE iaa.cod_anu = '"+codAnu+"' "+
					"AND iaa.cod_ind = "+codInd+" "+
					"AND iaa.cod_drt_mut = drt.cod_drt AND drt.cod_mut = mut.cod_mut "+
					"AND ((EXISTS "+ //contrôle une quittance ou un remboursement valide sur ce droit mutuelle
					"(SELECT 1 FROM iaa_iae_dim iid, situation_quittance_rmb sqr "+
					"WHERE iid.cod_ind = iaa.cod_ind AND iid.cod_anu = iaa.cod_anu AND iid.cod_drt = iaa.cod_drt_mut AND sqr.cod_ind = iid.cod_ind "+
					"AND sqr.cod_anu = iid.cod_anu AND sqr.num_occ_sqr = iid.num_occ_sqr AND sqr.eta_qut = 'V') "+
					"AND NOT EXISTS "+ //contrôle pas d'attente de paiement sur ce droit
					"(SELECT 1 FROM iaa_iae_dim iid "+
					"WHERE iid.cod_ind = iaa.cod_ind AND iid.cod_anu = iaa.cod_anu AND iid.cod_drt = iaa.cod_drt_mut "+
					"AND iid.cod_typ_iad = 'N' AND iid.tem_exo_iad IS NULL AND iid.num_occ_sqr IS NULL)) "+
					"OR EXISTS "+ //prise en compte des étudiants exonérés du droit mutuelle
					"(SELECT 1 FROM iaa_iae_dim iid "+
					"WHERE iid.cod_ind = iaa.cod_ind AND iid.cod_anu = iaa.cod_anu AND iid.cod_drt = iaa.cod_drt_mut AND iid.cod_typ_iad = 'E'))";

		}
		try{
			mutuelle = (String)entityManagerApogee.createNativeQuery(requeteSQL).getSingleResult();
		}catch(EmptyResultDataAccessException ex){
			mutuelle = null;
		}

		if(!StringUtils.hasText(mutuelle)){
			mutuelle = "* * * * * * * *";
		}

		return mutuelle;
	}

	@Override
	public Map<String,String> getCentrePayeur(String codAnu, String codInd, boolean affilie) {

		String requeteSQL="";

		//Si étudiant affilé
		if(affilie){
			//Si on a une requête SQL pour surcharger la requête livrée avec l'application
			if(StringUtils.hasText(requestUtils.getCentrePayeurPourAffilie())){

				//On utilise la requête indiquée dans le fichier XML
				requeteSQL = requestUtils.getCentrePayeurPourAffilie().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

			}else{
				//Préparation de la requête
				requeteSQL = "SELECT ctp.lic_ctp LIC_CTP, TO_CHAR(iaa.dat_afl_sso,'DD/MM/YYYY') DAT_AFL_SSO "+
						"FROM ins_adm_anu iaa, centre_payeur ctp "+
						"WHERE iaa.cod_anu = '"+codAnu+"' "+
						"AND iaa.cod_ind = "+codInd+" AND iaa.tem_afl_sso = 'O' AND iaa.cod_ctp = ctp.cod_ctp (+) "+
						"AND ((EXISTS "+ //contrôle une quittance ou un remboursement valide  sur un droit de type SS
						"(SELECT 1 FROM iaa_iae_dim iid, situation_quittance_rmb sqr, droit drt "+
						"WHERE iid.cod_ind = iaa.cod_ind "+
						"AND iid.cod_anu = iaa.cod_anu AND iid.cod_drt = drt.cod_drt "+
						"AND drt.cod_tdr = 'SS' AND sqr.cod_ind = iid.cod_ind "+
						"AND sqr.cod_anu = iid.cod_anu AND sqr.num_occ_sqr = iid.num_occ_sqr "+
						"AND sqr.eta_qut = 'V') "+ //contrôle pas d'attente de paiement sur ce droit
						"AND NOT EXISTS "+
						"(SELECT 1 FROM iaa_iae_dim iid, droit drt "+
						"WHERE iid.cod_ind = iaa.cod_ind "+
						"AND iid.cod_anu = iaa.cod_anu "+
						"AND iid.cod_drt = drt.cod_drt "+
						"AND drt.cod_tdr = 'SS' "+
						"AND iid.cod_typ_iad = 'N' "+
						"AND iid.tem_exo_iad IS NULL "+
						"AND iid.num_occ_sqr IS NULL)) "+
						"OR EXISTS "+ //prise en compte des étudiants exonérés du droit SS
						"(SELECT 1 FROM iaa_iae_dim iid, droit drt "+
						"WHERE iid.cod_ind = iaa.cod_ind "+
						"AND iid.cod_anu = iaa.cod_anu "+
						"AND iid.cod_drt =drt.cod_drt "+
						"AND drt.cod_tdr = 'SS' "+
						"AND iid.cod_typ_iad = 'E'))";
			}
		}else{
			//étudiant non affilé

			//Si on a une requête SQL pour surcharger la requête livrée avec l'application
			if(StringUtils.hasText(requestUtils.getCentrePayeurPourNonAffilie())){

				//On utilise la requête indiquée dans le fichier XML
				requeteSQL = requestUtils.getCentrePayeurPourNonAffilie().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

			}else{
				//Préparation de la requête
				requeteSQL = "SELECT ctp.lic_ctp LIC_CTP, TO_CHAR(iaa.dat_afl_sso,'DD/MM/YYYY') DAT_AFL_SSO "+
						"FROM ins_adm_anu iaa, centre_payeur ctp "+
						"WHERE iaa.cod_anu = '"+codAnu+"' "+
						"AND iaa.cod_ind = "+codInd+" "+  
						"AND iaa.tem_afl_sso = 'N' "+
						"AND iaa.cod_ctp = ctp.cod_ctp (+)";
			}

		}

		//String[] r = (String[]) entityManagerApogee.createNativeQuery(requeteSQL).getSingleResult();

		Query query = entityManagerApogee.createNativeQuery(requeteSQL);

		query.setHint(QueryHints.RESULT_TYPE, ResultType.Map);

		@SuppressWarnings("unchecked")
		Map<String,String> r = (Map<String,String>) query.getSingleResult();



		//centre_payeur = (String) query.getSingleResult();

		if(!StringUtils.hasText(r.get("LIC_CTP"))){
			r.put("LIC_CTP", "***");
		}

		return r;
	}

	@Override
	public String getDateCotisation(String codAnu, String codInd) {
		String requeteSQL="";
		//Si on a une requête SQL pour surcharger la requête livrée avec l'application
		if(StringUtils.hasText(requestUtils.getDateCotisation())){

			//On utilise la requête indiquée dans le fichier XML
			requeteSQL = requestUtils.getDateCotisation().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

		}else{
			requeteSQL="SELECT TO_CHAR(sqr.dat_sqr,'DD/MM/YYYY') "+
					"FROM situation_quittance_rmb sqr, centre_gestion cge "+
					"WHERE sqr.cod_anu = '"+codAnu+"' AND sqr.cod_ind ="+codInd+" "+
					"AND sqr.cod_typ_sqr = 'Q' AND sqr.eta_qut = 'V' AND cge.cod_cge = sqr.cod_cge "+
					"AND sqr.num_occ_sqr = 1 ";
		}
		
		Query query = entityManagerApogee.createNativeQuery(requeteSQL);
		
		String dateCotisation = (String) query.getSingleResult();
		
		return dateCotisation;
	}


	@Override
	public boolean isAffilieSso(String codAnu, String codInd) {

		String requeteSQL = "SELECT iaa.tem_afl_sso FROM ins_adm_anu iaa "+
				"WHERE iaa.cod_anu = '"+codAnu+"' "+
				" AND iaa.cod_ind = "+codInd;

		Query query = entityManagerApogee.createNativeQuery(requeteSQL);

		String tem_afl_sso = (String) query.getSingleResult();

		if(tem_afl_sso!=null && tem_afl_sso.equals("O")){
			return true;
		}

		return false;
	}





}
