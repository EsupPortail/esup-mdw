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

import fr.univlorraine.mondossierweb.utils.RequestUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.config.ResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
@org.springframework.transaction.annotation.Transactional("transactionManagerApogee")
@Repository
public class SsoApogeeServiceImpl implements SsoApogeeService{

	private Logger LOG = LoggerFactory.getLogger(SsoApogeeServiceImpl.class);

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
		}catch(NoResultException | EmptyResultDataAccessException ex){
			mutuelle = null;
		}

		if(!StringUtils.hasText(mutuelle)){
			mutuelle = "* * * * * * * *";
		}

		return mutuelle;
	}

	@Override
	public List<Map<String,String>> getQuittances(String codAnu, String codInd){

		String requeteSQL = "";
		//Si on a une requête SQL pour surcharger la requête livrée avec l'application
		if(StringUtils.hasText(requestUtils.getQuittances())){

			//On utilise la requête indiquée dans le fichier XML
			requeteSQL = requestUtils.getQuittances().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

		}else{
			requeteSQL=" SELECT TO_CHAR(sqr.num_occ_qut) numoccqut, TO_CHAR(sqr.dat_sqr,'DD/MM/YYYY') datsqr, cge.lic_cge liccge, TO_CHAR(sqr.num_occ_sqr) numoccsqr "+
				"FROM    situation_quittance_rmb sqr, centre_gestion cge "+
				"WHERE sqr.cod_anu = '"+codAnu+"' "+
				"AND sqr.cod_ind = "+codInd+" "+
				"AND sqr.cod_typ_sqr = 'Q' "+
				"AND sqr.eta_qut = 'V' "+
				"AND cge.cod_cge = sqr.cod_cge "+
				"ORDER BY sqr.num_occ_qut DESC";
		}
		Query query = entityManagerApogee.createNativeQuery(requeteSQL);

		query.setHint(QueryHints.RESULT_TYPE, ResultType.Map);

		@SuppressWarnings("unchecked")
		List<Map<String,String>> r = (List<Map<String,String>>) query.getResultList();

		return r;
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
					"AND iid.cod_typ_iad = 'E') "+
					"OR  iaa.cod_rss ='450')";
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
				"FROM situation_quittance_rmb sqr "+
				"WHERE sqr.cod_anu = '"+codAnu+"' AND sqr.cod_ind ="+codInd+" "+
				"AND sqr.cod_typ_sqr = 'Q' AND sqr.eta_qut = 'V' "+
				"AND sqr.num_occ_qut = (select MAX(sqr2.num_occ_qut) FROM situation_quittance_rmb sqr2 "+
				"WHERE sqr2.cod_anu = '"+codAnu+"' AND sqr2.cod_ind ="+codInd+" "+
				"AND sqr2.cod_typ_sqr = 'Q' AND sqr2.eta_qut = 'V') ";
		}

		Query query = entityManagerApogee.createNativeQuery(requeteSQL);

		try {
			String dateCotisation = (String) query.getSingleResult();
			return dateCotisation;
		}catch(NoResultException nre) {
			LOG.info("getDateCotisation - Aucune date de cotisation pour " + codInd + " en " + codAnu);
		}

		return null;
	}


	/*@Override
	public boolean isAffilieSso(String codAnu, String codInd) {
		String requeteSQL = "";

		if(StringUtils.hasText(requestUtils.isAffilieSso())){

			//On utilise la requête indiquée dans le fichier XML
			requeteSQL = requestUtils.isAffilieSso().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

		}else{
			requeteSQL = "SELECT iaa.tem_afl_sso FROM ins_adm_anu iaa "+
					"WHERE iaa.cod_anu = '"+codAnu+"' "+
					" AND iaa.cod_ind = "+codInd;
		}

		Query query = entityManagerApogee.createNativeQuery(requeteSQL);

		String tem_afl_sso = (String) query.getSingleResult();

		if(tem_afl_sso!=null && tem_afl_sso.equals("O")){
			return true;
		}

		return false;
	}*/

	@Override
	public List<String> getMoyensDePaiement(String codAnu,  String codInd, String NumOccSqr) {
		String requeteSQL = "";

		if(StringUtils.hasText(requestUtils.getMoyensDePaiement())){

			//On utilise la requête indiquée dans le fichier XML
			requeteSQL = requestUtils.getMoyensDePaiement().replaceAll("#NUM_OCC_SQR#", NumOccSqr).replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

		}else{
			requeteSQL = "SELECT mdp.lic_mdp "+
				"FROM paiement pmt, mode_paiement mdp "+
				"WHERE pmt.cod_anu = '"+codAnu+"' "+
				"AND pmt.cod_ind = "+codInd+" "+
				"AND pmt.num_occ_sqr = "+NumOccSqr+ " "+
				"AND mdp.cod_mdp = pmt.cod_mdp order by pmt.num_occ_pmt";
		}

		Query query = entityManagerApogee.createNativeQuery(requeteSQL);
		try{
			List<String> mdps = (List<String>) query.getResultList();

			if(mdps!=null && mdps.size()>0 && mdps.get(0)!=null && StringUtils.hasText(mdps.get(0))){
				return mdps;
			}
		}catch(NoResultException | EmptyResultDataAccessException e){
			return null;
		}

		return null;
	}

	@Override
	public boolean isPaiement3X(String codAnu,  String codInd){
		String requeteSQL = "";

		if(StringUtils.hasText(requestUtils.isPaiement3X())){

			//On utilise la requête indiquée dans le fichier XML
			requeteSQL = requestUtils.isPaiement3X().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

		}else{
			requeteSQL= "SELECT tem_pmt_3f "+
				"FROM ins_adm_anu "+
				"WHERE cod_ind="+codInd+" "+
				"AND cod_anu='"+codAnu+"' ";
		}

		Query query = entityManagerApogee.createNativeQuery(requeteSQL);

		String tem_pmt_3xf = (String) query.getSingleResult();

		if(tem_pmt_3xf!=null && tem_pmt_3xf.equals("O")){
			return true;
		}

		return false;
	}

	@Override
	public String getDate1erPaiement(String codAnu,  String codInd){
		String requeteSQL = "";

		if(StringUtils.hasText(requestUtils.getDate1erPaiement())){

			//On utilise la requête indiquée dans le fichier XML
			requeteSQL = requestUtils.getDate1erPaiement().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

		}else{
			requeteSQL="SELECT TO_CHAR(dat_ech1,'DD/MM') "+
				"FROM paiement "+
				"WHERE cod_ind="+codInd+" "+
				"AND cod_anu='"+codAnu+"' "+
				"AND dat_ech1 IS NOT NULL "+
				"ORDER BY dat_ech1";
		}

		Query query = entityManagerApogee.createNativeQuery(requeteSQL);
		try{
			String date = (String) query.getSingleResult();
			return date;
		}catch(NoResultException | EmptyResultDataAccessException e){
			return null;
		}
	}

	@Override
	public String getDate2emPaiement(String codAnu,  String codInd){
		String requeteSQL = "";

		if(StringUtils.hasText(requestUtils.getDate2emPaiement())){

			//On utilise la requête indiquée dans le fichier XML
			requeteSQL = requestUtils.getDate2emPaiement().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

		}else{
			requeteSQL="SELECT TO_CHAR(dat_ech2,'DD/MM') "+
				"FROM paiement "+
				"WHERE cod_ind="+codInd+" "+
				"AND cod_anu='"+codAnu+"' "+
				"AND dat_ech2 IS NOT NULL "+
				"ORDER BY dat_ech2";
		}

		Query query = entityManagerApogee.createNativeQuery(requeteSQL);
		try{
			String date = (String) query.getSingleResult();
			return date;
		}catch(NoResultException | EmptyResultDataAccessException e){
			return null;
		}
	}

	@Override
	public String getDate3emPaiement(String codAnu,  String codInd){
		String requeteSQL = "";

		if(StringUtils.hasText(requestUtils.getDate3emPaiement())){

			//On utilise la requête indiquée dans le fichier XML
			requeteSQL = requestUtils.getDate3emPaiement().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

		}else{
			requeteSQL="SELECT TO_CHAR(dat_ech3,'DD/MM') "+
				"FROM paiement "+
				"WHERE cod_ind="+codInd+" "+
				"AND cod_anu='"+codAnu+"' "+
				"AND dat_ech3 IS NOT NULL "+
				"ORDER BY dat_ech3";
		}

		Query query = entityManagerApogee.createNativeQuery(requeteSQL);
		try{
			String date = (String) query.getSingleResult();
			return date;
		}catch(NoResultException | EmptyResultDataAccessException e){
			return null;
		}
	}



	@Override
	public String getMontant1erPaiement(String codAnu,  String codInd){
		String requeteSQL = "";

		if(StringUtils.hasText(requestUtils.getMontant1erPaiement())){

			//On utilise la requête indiquée dans le fichier XML
			requeteSQL = requestUtils.getMontant1erPaiement().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

		}else{
			requeteSQL="SELECT to_char(SUM(mnt_pmt_ech1)) "+
				"FROM paiement "+
				"WHERE cod_ind= "+codInd+" "+
				"AND cod_anu= '"+codAnu+"' ";
		}

		Query query = entityManagerApogee.createNativeQuery(requeteSQL);
		try{
			String date = (String) query.getSingleResult();
			return date;
		}catch(NoResultException | EmptyResultDataAccessException e){
			return null;
		}
	}

	@Override
	public String getMontant2emPaiement(String codAnu,  String codInd){
		String requeteSQL = "";

		if(StringUtils.hasText(requestUtils.getMontant2emPaiement())){

			//On utilise la requête indiquée dans le fichier XML
			requeteSQL = requestUtils.getMontant2emPaiement().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

		}else{
			requeteSQL="SELECT to_char(SUM(mnt_pmt_ech2)) "+
				"FROM paiement "+
				"WHERE cod_ind= "+codInd+" "+
				"AND cod_anu= '"+codAnu+"' ";
		}

		Query query = entityManagerApogee.createNativeQuery(requeteSQL);
		try{
			String date = (String) query.getSingleResult();
			return date;
		}catch(NoResultException | EmptyResultDataAccessException e){
			return null;
		}
	}

	@Override
	public String getMontant3emPaiement(String codAnu,  String codInd){
		String requeteSQL = "";

		if(StringUtils.hasText(requestUtils.getMontant3emPaiement())){

			//On utilise la requête indiquée dans le fichier XML
			requeteSQL = requestUtils.getMontant3emPaiement().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

		}else{
			requeteSQL="SELECT to_char(SUM(mnt_pmt_ech3)) "+
				"FROM paiement "+
				"WHERE cod_ind= "+codInd+" "+
				"AND cod_anu= '"+codAnu+"' ";
		}

		Query query = entityManagerApogee.createNativeQuery(requeteSQL);
		try{
			String date = (String) query.getSingleResult();
			return date;
		}catch(NoResultException | EmptyResultDataAccessException e){
			return null;
		}
	}



	@Override
	public String getMontantTotalPaye(String codAnu,  String codInd){
		String requeteSQL = "";

		if(StringUtils.hasText(requestUtils.getMontantTotalPaye())){

			//On utilise la requête indiquée dans le fichier XML
			requeteSQL = requestUtils.getMontantTotalPaye().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

		}else{
			requeteSQL="SELECT to_char(sum( nvl(iid.mnt_pai_iad,0) - nvl(iid.mnt_rmb_iad,0) )) "+
				"FROM    situation_quittance_rmb sqr,  iaa_iae_dim iid "+
				"WHERE sqr.cod_anu = '"+codAnu+"' "+
				"AND sqr.cod_ind ="+codInd+" "+
				"AND sqr.eta_qut IN ( 'V','T','S','C') "+
				"AND iid.cod_anu = sqr.cod_anu "+
				"AND iid.cod_ind = sqr.cod_ind "+
				"AND iid.num_occ_sqr = sqr.num_occ_sqr "+
				"GROUP BY sqr.cod_ind ";
		}

		Query query = entityManagerApogee.createNativeQuery(requeteSQL);
		try{
			String montant = (String) query.getSingleResult();
			return montant;
		}catch(NoResultException | EmptyResultDataAccessException e){
			return null;
		}
	}

	@Override
	public List<Map<String,String>> getMontantsPayes(String codAnu,  String codInd){
		String requeteSQL = "";

		if(StringUtils.hasText(requestUtils.getMontantsPayes())){

			//On utilise la requête indiquée dans le fichier XML
			requeteSQL = requestUtils.getMontantsPayes().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

		}else{
			requeteSQL="SELECT nrg.lic_nrg LIC_NRG, to_char(sum( nvl(iid.mnt_pai_iad,0) - nvl(iid.mnt_rmb_iad,0))) MONTANT "+
				"FROM  situation_quittance_rmb sqr,  iaa_iae_dim iid, droit drt, niv_regroup nrg "+
				"WHERE sqr.cod_anu = '"+codAnu+"' "+
				"AND sqr.cod_ind ="+codInd+" "+
				"AND sqr.eta_qut IN ( 'V','T','S','C') "+
				"AND iid.cod_anu = sqr.cod_anu "+
				"AND iid.cod_ind = sqr.cod_ind "+
				"AND iid.num_occ_sqr = sqr.num_occ_sqr "+
				"AND iid.cod_drt = drt.cod_drt "+
				"AND drt.COD_CAT_EXO_EXT = iid.COD_CAT_EXO_EXT "+
				"AND nrg.cod_nrg = drt.cod_nrg "+
				"GROUP BY nrg.lic_nrg ";
		}

		Query query = entityManagerApogee.createNativeQuery(requeteSQL);

		query.setHint(QueryHints.RESULT_TYPE, ResultType.Map);

		@SuppressWarnings("unchecked")
		List<Map<String,String>> r = (List<Map<String,String>>) query.getResultList();

		return r;
	}



}
