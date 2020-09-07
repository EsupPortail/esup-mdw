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

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.config.ResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.entities.apogee.Anonymat;
import fr.univlorraine.mondossierweb.entities.apogee.Examen;
import fr.univlorraine.mondossierweb.entities.apogee.InfoUsageEtatCivil;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.apogee.NatureElp;
import fr.univlorraine.mondossierweb.entities.apogee.Signataire;
import fr.univlorraine.mondossierweb.utils.RequestUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import lombok.Data;



@Component
@org.springframework.transaction.annotation.Transactional("transactionManagerApogee")
@Data
public class MultipleApogeeServiceImpl implements MultipleApogeeService {

	private Logger LOG = LoggerFactory.getLogger(MultipleApogeeServiceImpl.class);


	@PersistenceContext (unitName="entityManagerFactoryApogee")
	private transient EntityManager entityManagerApogee;

	@Resource
	private RequestUtils requestUtils;

	@Override
	public String getAnneeEnCours() {
		return (String) entityManagerApogee.createNativeQuery("select cod_anu from annee_uni where eta_anu_iae = 'O'").getSingleResult();

	}

	@Override
	public String getLibEtablissementDef() {
		return (String) entityManagerApogee.createNativeQuery("select e.lib_web_etb from apogee.variable_appli va, etablissement e where COD_VAP = 'ETB_COD' and va.PAR_VAP = e.COD_ETB").getSingleResult();

	}

	@Override
	public List<Examen> getCalendrierExamens(String cod_ind, boolean recupererVet) {

		//Si on a une requête SQL pour surcharger la requête livrée avec l'application
		if(StringUtils.hasText(requestUtils.getCalendrierDesExamens())){

			//On utilise la requête indiquée dans le fichier XML
			@SuppressWarnings("unchecked")
			List<Examen> lins = (List<Examen>)entityManagerApogee.createNativeQuery(
					requestUtils.getCalendrierDesExamens().replaceAll("#COD_IND#", cod_ind), Examen.class).getResultList();

			return lins;

		}else{
			//Préparation de la requête
			String requeteSQL = "SELECT rownum ID, t.* from (SELECT DISTINCT PESA.DAT_DEB_PES datedeb, "+
					"DECODE(SUBSTR(TO_CHAR(PESA.DHH_DEB_PES),1,1),'1', "+
					"TO_CHAR(PESA.DHH_DEB_PES),'0'||TO_CHAR(PESA.DHH_DEB_PES)) ||':'|| "+
					"DECODE(TO_CHAR(PESA.DMM_DEB_PES),'0','00',TO_CHAR(PESA.DMM_DEB_PES)) heure, "+
					"PESA.DUR_EXA_EPR_PES duree, "+
					"PESA.COD_SAL salle, SAL.LIB_SAL libsalle, "+
					"NVL(TO_CHAR(PI.NUM_PLC_AFF_PSI),' ') place, "+
					"BAT.LIB_BAT BATIMENT,BAT.LIB_LOC_BAT localisation, E.LIB_EPR epreuve, "+
					"'' codcin,  E.COD_EPR codeepreuve, REPLACE(PEX.LIB_PXA ,'@' ) libsession , ICE.COD_ETP codeetape, ICE.COD_VRS_VET versionetape "+
					"FROM APOGEE.PRD_EPR_SAL_ANU PESA,APOGEE.EPREUVE E,APOGEE.PES_IND PI,APOGEE.BATIMENT BAT, IND_CONTRAT_ELP ICE,apogee.EPR_SANCTIONNE_ELP ESE, "+
					"APOGEE.SALLE SAL,APOGEE.PERIODE_EXA PEX  "+
					"WHERE  PI.COD_IND="+cod_ind+" "+
					"AND PI.COD_PES=PESA.COD_PES  "+
					"AND ICE.COD_IND = PI.COD_IND AND ICE.COD_ANU = PESA.COD_ANU and ice.cod_elp=ESE.COD_ELP and ese.cod_epr= E.COD_EPR " +
					"AND  PESA.COD_EPR=E.COD_EPR AND  PESA.COD_PXA = PEX.COD_PXA  "+
					"AND  PEX.LIB_PXA LIKE '@%' AND  SAL.COD_SAL = PESA.COD_SAL  "+
					"AND  BAT.COD_BAT = SAL.COD_BAT  "+
					"ORDER BY DATEDEB,2) t";

			//Si on n'a pas besoin de récupérer la VET
			if(!recupererVet){
				//On ne recupere pas la VET, ce qui évite les lignes en doublons.
				requeteSQL = requeteSQL.replaceFirst("ICE.COD_ETP codeetape, ICE.COD_VRS_VET versionetape", "'' codeetape, '' versionetape");
			}

			@SuppressWarnings("unchecked")
			List<Examen> lins = (List<Examen>)entityManagerApogee.createNativeQuery(requeteSQL, Examen.class).getResultList();

			return lins;

		}
	}

	@Override
	public List<String> getDixDernieresAnneesUniversitaires() {
		@SuppressWarnings("unchecked")
		List<String> lannee = getDernieresAnneesUniversitaires();

		//On garde 10 annees maxi
		if(lannee!=null && lannee.size()>10){
			for(int i=(lannee.size()-1);i>9;i--){
				lannee.remove(i);
			}
		}

		return lannee;
	}

	@Override
	public List<String> getDernieresAnneesUniversitaires() {
		@SuppressWarnings("unchecked")
		List<String> lannee = (List<String>)entityManagerApogee.createNativeQuery("select cod_anu from annee_uni order by cod_anu DESC").getResultList();

		return lannee;
	}

	@Override
	public int getDerniereAnneeUniversitaire() {
		@SuppressWarnings("unchecked")
		String annee = (String)entityManagerApogee.createNativeQuery("select max(cod_anu) from annee_uni").getSingleResult();

		return Integer.parseInt(annee);
	}

	@Override
	public Signataire getSignataire(String codeSignataire, String cleApogee) {
		@SuppressWarnings("unchecked")
		Signataire signataire = (Signataire)entityManagerApogee.createNativeQuery("select sig.COD_SIG, sig.NOM_SIG, sig.QUA_SIG, "+
				"PKB_CRY1.decryptLob(decode(std.TEM_CES_STD,'T',std.IMG_TAM_STD,std.IMG_SIG_STD), "+
				" UTL_RAW.cast_to_raw('"+cleApogee+"')) as IMG_SIG_STD "+
				" from APOGEE.SIGNATAIRE sig, APOGEE.SIGN_TAMP_DIGITALISE std "+
				" where sig.COD_SIG = std.COD_SIG (+) "+
				" and sig.COD_SIG = '"+codeSignataire+"'", Signataire.class).getSingleResult();
		return signataire;
	}

	
	
	@Override
	public InfoUsageEtatCivil getInfoUsageEtatCivilFromCodInd(String cod_ind) {
		@SuppressWarnings("unchecked")
		InfoUsageEtatCivil info = (InfoUsageEtatCivil) entityManagerApogee.createNativeQuery("select i.cod_ind codInd, i.cod_civ codCiv,DECODE(i.tem_pr_usage,'O',1,0) temPrUsage, "+
				" i.cod_sex_eta_civ codSexEtatCiv, "+
				" i.lib_pr_eta_civ libPrEtaCiv "+
				" from apogee.individu i  "+
				" where i.cod_ind ="+cod_ind, InfoUsageEtatCivil.class ).getSingleResult();
		return info;
	}

/*
	@Override
	public String getCodCivFromCodInd(String cod_ind) {
		@SuppressWarnings("unchecked")
		String codCiv = (String)entityManagerApogee.createNativeQuery("select i.cod_civ "+
				" from apogee.individu i  "+
				" where i.cod_ind ="+cod_ind).getSingleResult();
		return codCiv;
	}
	
	@Override
	public boolean getTemPrUsageFromCodInd(String cod_ind) {
		@SuppressWarnings("unchecked")
		String tem = (String) entityManagerApogee.createNativeQuery("select i.tem_pr_usage "+
				" from apogee.individu i  "+
				" where i.cod_ind ="+cod_ind).getSingleResult();
		return (tem!=null && tem.equals("O"));
	}

	@Override
	public String getCodSexEtaCivFromCodInd(String cod_ind) {
		@SuppressWarnings("unchecked")
		String codSexEtaCiv = (String)entityManagerApogee.createNativeQuery("select i.cod_sex_eta_civ "+
				" from apogee.individu i  "+
				" where i.cod_ind ="+cod_ind).getSingleResult();
		return codSexEtaCiv;
	}

	@Override
	public String getLibPrEtaCivFromCodInd(String cod_ind) {
		@SuppressWarnings("unchecked")
		String libPrEtaCiv = (String)entityManagerApogee.createNativeQuery("select i.lib_pr_eta_civ "+
				" from apogee.individu i  "+
				" where i.cod_ind ="+cod_ind).getSingleResult();
		return libPrEtaCiv;
	}*/

	@Override
	public List<Inscrit> getInscritsEtapeJuinSep(Etape e) {
		@SuppressWarnings("unchecked")
		List<Inscrit> linscrits = (List<Inscrit>)entityManagerApogee.createNativeQuery("select i.cod_ind,i.cod_etu, i.lib_pr1_ind, I.lib_nom_pat_ind NOM, I.LIB_NOM_USU_IND NOM_USUEL, "+
				" to_char(i.date_nai_ind,'DD/MM/YYYY') date_nai_ind,   "+
				" decode(rj.tem_iae_ko_vet,0,'O','N') iae, "+
				" decode(avc.ETA_ANO_OBJ_AOA,'V',' ',nvl(decode(to_char(rj.not_vet),null,rj.not_sub_vet,to_char(rj.not_vet)),' ')) notej ,  "+
				" decode(avc.ETA_ANO_OBJ_AOA,'V',' ',nvl(rj.cod_tre,' ')) resj , "+
				" decode(avc2.ETA_ANO_OBJ_AOA,'V',' ',nvl(decode(to_char(rs.not_vet),null,rs.not_sub_vet,to_char(rs.not_vet)),' ')) notes , "+
				" decode(avc2.ETA_ANO_OBJ_AOA,'V',' ',nvl(rs.cod_tre,' ')) ress  "+
				" from apogee.individu i , apogee.resultat_vet rj  "+
				" left outer join apogee.resultat_vet rs on ( rs.cod_ind = rj.cod_ind   "+
				" and rs.tem_iae_ko_vet in ('0','2')  "+
				" and rs.cod_etp = rj.cod_etp  "+
				" and rs.cod_vrs_vet = rj.cod_vrs_vet  "+
				" and rs.cod_anu = rj.cod_anu  "+
				" and rs.cod_ses = '2'  "+
				" and rs.cod_adm = '1')  "+
				" left outer join AVCT_OBJ_ANO avc on (avc.COD_ANU=rj.COD_ANU "+
				" and avc.COD_OBJ_AOA=rj.cod_etp "+
				" and avc.COD_SES_OBJ_AOA=rj.COD_SES "+
				" and avc.COD_ADM_OBJ_AOA=rj.COD_ADM "+
				" and avc.TYP_OBJ_AOA='VET' "+
				" and avc.COD_VRS_OBJ_AOA=rj.cod_vrs_vet "+
				" and avc.ETA_ANO_OBJ_AOA='V' ) "+
				" left outer join AVCT_OBJ_ANO avc2 on (avc2.COD_ANU=rj.COD_ANU "+
				" and avc2.COD_OBJ_AOA=rj.cod_etp "+
				" and avc2.COD_SES_OBJ_AOA='2' "+
				" and avc2.COD_ADM_OBJ_AOA='1' "+
				" and avc2.TYP_OBJ_AOA='VET' "+
				" and avc2.COD_VRS_OBJ_AOA=rj.cod_vrs_vet "+
				" and avc2.ETA_ANO_OBJ_AOA='V')     "+    
				" where rj.tem_iae_ko_vet in ('0','2')  "+
				" and rj.cod_etp = '"+e.getCode()+"' "+
				" and rj.cod_vrs_vet = "+e.getVersion()+" "+
				" and rj.cod_anu = "+e.getAnnee()+ " "+
				" and rj.cod_ses in ('0','1') and rj.cod_adm = '1'  "+
				" and i.cod_ind = rj.cod_ind  "+
				" order by NOM,i.lib_pr1_ind,i.date_nai_ind ", Inscrit.class).getResultList();

		return linscrits;

	}


	@Override
	public String getLibelleEtape(Etape e) {
		try{
			@SuppressWarnings("unchecked")
			String libelle = (String)entityManagerApogee.createNativeQuery("select lib_web_vet "+
					" from version_etape "+
					" where cod_etp = '"+e.getCode()+"' "+
					" and cod_vrs_vet = "+e.getVersion()).getSingleResult();
			return libelle;
		}catch(NoResultException | EmptyResultDataAccessException ex){
			LOG.info("Aucun lib_web_vet trouvé pour étape : "+e.getCode()+"/"+e.getVersion());
		}
		return null;
	}

	@Override
	public String getLibelleCourtEtape(String codeEtp) {
		try{
			@SuppressWarnings("unchecked")
			String libelle = (String)entityManagerApogee.createNativeQuery("select lic_etp "+
					" from etape "+
					" where cod_etp = '"+codeEtp+"'").getSingleResult();
			return libelle;
		}catch(NoResultException | EmptyResultDataAccessException ex){
			LOG.info("Aucun lic_etp trouvé pour étape : "+codeEtp);
		}
		return null;
	}

	@Override
	public List<String> getAnneesFromVetDesc(Etape e, int anneeMaximum) {
		List<String> lannee = new LinkedList<String>();
		try{
			@SuppressWarnings("unchecked")
			int anneeMin = Integer.parseInt((String)entityManagerApogee.createNativeQuery(" select MIN(DAA_DEB_RCT_VET) "+
					" from vdi_fractionner_vet vfv "+
					" where VFV.COD_ETP='"+e.getCode()+"' "+
					" and VFV.COD_VRS_VET="+e.getVersion()).getSingleResult());
			int anneeMax = Integer.parseInt((String)entityManagerApogee.createNativeQuery(" select MAX(DAA_FIN_RCT_VET) "+
					" from vdi_fractionner_vet vfv "+
					" where VFV.COD_ETP='"+e.getCode()+"' "+
					" and VFV.COD_VRS_VET="+e.getVersion()).getSingleResult());


			for(int i=anneeMax; i>=anneeMin;i--){
				if(i<=anneeMaximum){
					lannee.add(""+i);
				}
			}

		}catch(NumberFormatException nfe){
			LOG.debug("Aucune année valide trouvée pour cette vet : "+e.getCode()+"/"+e.getVersion(), nfe);
		}
		return lannee;

	}




	@Override
	public List<Anonymat> getNumeroAnonymat(String cod_etu, String cod_anu) {
		if(StringUtils.hasText(cod_etu) && StringUtils.hasText(cod_anu)){
			@SuppressWarnings("unchecked")
			List<Anonymat> lano = (List<Anonymat>)entityManagerApogee.createNativeQuery("select rownum id, t.COD_ETU_ANO, t.LIB_MAN from (select distinct COD_ETU_ANO, LIB_MAN from v_ind_ano,MAQ_ANO, MAQ_OBJ_ANO "+
					" where v_ind_ano.cod_anu = '"+cod_anu+"' and v_ind_ano.cod_etu = "+cod_etu+" "+
					" and MAQ_ANO.COD_MAN = MAQ_OBJ_ANO.COD_MAN "+
					" AND concat(MAQ_OBJ_ANO.COD_OBJ_MOA, MAQ_OBJ_ANO.COD_VRS_OBJ_MOA )= concat(V_IND_ANO.COD_OBJ,  V_IND_ANO.COD_VRS_OBJ ) "+
					" and COD_ETU_ANO is not null)t ", Anonymat.class).getResultList();
			return lano;
		}
		return null;
	}

	@Override
	public String getNatureElp(String codElp) {
		if(StringUtils.hasText(codElp) && StringUtils.hasText(codElp)){
			try{
				@SuppressWarnings("unchecked")
				NatureElp nature = (NatureElp)entityManagerApogee.createNativeQuery("select nel.COD_NEL, nel.LIB_NEL, nel.LIC_NEL, NEL.TEM_EN_SVE_NEL "+
						"from ELEMENT_PEDAGOGI elp, NATURE_ELP  nel "+
						"where nel.COD_NEL=elp.COD_NEL "+
						"and elp.COD_ELP='"+codElp+"'", NatureElp.class).getSingleResult();
				return nature.getLib_nel();
			}catch(NoResultException | EmptyResultDataAccessException e){
				LOG.info("Aucune nature trouvee pour ELP : "+codElp);
			}
		}
		return null;
	}


	@Override
	public boolean isSalarie(String codInd, String codAnu) {
		if(StringUtils.hasText(codInd) && StringUtils.hasText(codAnu)){
			String requeteSQL="";
			//Si on a une requête SQL pour surcharger la requête livrée avec l'application
			if(StringUtils.hasText(requestUtils.getCodPcsSalarie())){

				//On utilise la requête indiquée dans le fichier XML
				requeteSQL = requestUtils.getCodPcsSalarie().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);

			}else{
				//requeteSQL = "select COD_PCS_ETUDIANT from ins_adm_anu where cod_ind='"+codInd+"' and cod_anu="+codAnu+" and COD_PCS_ETUDIANT not in ('81', '82','99','A','84')";
				requeteSQL = "select iaa.COD_PCS_ETUDIANT from ins_adm_anu iaa, CAT_SOC_PFL csp where csp.COD_PCS = iaa.COD_PCS_ETUDIANT and csp.TEM_SAI_QTR='O' and iaa.cod_ind="+codInd+" and iaa.cod_anu="+codAnu;
			}
			
			try{
				Query query = entityManagerApogee.createNativeQuery(requeteSQL);

				query.setHint(QueryHints.RESULT_TYPE, ResultType.Value);

				@SuppressWarnings("unchecked")
				String codPcsSalarie = (String) query.getSingleResult();

				if(StringUtils.hasText(codPcsSalarie)){
					return true;
				}
			}catch (NoResultException | EmptyResultDataAccessException nre){
				return false;
			}
		}
		return false;
	}
	
	@Override
	public boolean isBoursier(String codInd, String codAnu) {
		if(StringUtils.hasText(codInd) && StringUtils.hasText(codAnu)){
			
			String requeteSQL="";
			
			//Si on a une requête SQL pour surcharger la requête livrée avec l'application
			if(StringUtils.hasText(requestUtils.getTemBoursierIaa())){
				//On utilise la requête indiquée dans le fichier XML
				requeteSQL = requestUtils.getTemBoursierIaa().replaceAll("#COD_IND#", codInd).replaceAll("#COD_ANU#", codAnu);
			}else{
				requeteSQL = "select iaa.TEM_BRS_IAA from ins_adm_anu iaa where iaa.cod_ind="+codInd+" and iaa.cod_anu="+codAnu+" and iaa.TEM_BRS_IAA = 'O'";
			}
			
			try{
				Query query = entityManagerApogee.createNativeQuery(requeteSQL);

				query.setHint(QueryHints.RESULT_TYPE, ResultType.Value);

				@SuppressWarnings("unchecked")
				String temBrsIaa = (String) query.getSingleResult();

				if(StringUtils.hasText(temBrsIaa)){
					return true;
				}
			}catch (NoResultException | EmptyResultDataAccessException nre){
				return false;
			}
		}
		return false;
	}

	@Override
	public int getNbPJnonValides(String cod_ind, String cod_anu) {
		if(StringUtils.hasText(cod_ind) && StringUtils.hasText(cod_anu)){
			@SuppressWarnings("unchecked")
			BigDecimal nbPJnonValides = (BigDecimal)entityManagerApogee.createNativeQuery("select count(*) from TELEM_IAA_TPJ tit, INS_ADM_ANU iaa "+
					"where iaa.COD_ANU = "+cod_anu+" "+
					"and iaa.cod_ind = tit.cod_ind "+
					"and iaa.ETA_IAA = 'E' "+
					"and tit.cod_anu= iaa.COD_ANU "+
					"and tit.STATUT_PJ != 'V' "+
					"and tit.cod_ind = "+cod_ind).getSingleResult();
			return nbPJnonValides.intValue();
		}
		return 0;
	}


	@Override
	public List<String> getListeCodeBlocage(String cod_etu) {
		if(StringUtils.hasText(cod_etu) ){
			@SuppressWarnings("unchecked")
			List<String> lblo = (List<String>)entityManagerApogee.createNativeQuery("select COD_BLO "+
					"from IND_SANCTIONNE_BLO "+
					"where COD_ETU="+cod_etu+ " "+
					"and DAT_DEB_BLO < SYSDATE and (DAT_FIN_BLO is null or DAT_FIN_BLO > SYSDATE)").getResultList();
			return lblo;
		}
		return null;
	}


	@Override
	public List<BigDecimal> getCodRvn(String cod_ind, String cod_anu, String listeCodesElp) {
		@SuppressWarnings("unchecked")
		List<BigDecimal> codRvn = (List<BigDecimal>)entityManagerApogee.createNativeQuery("select distinct cod_rvn "+
				" from trav_ext_rvm_res"+
				" where cod_ind = "+cod_ind+
				" and cod_obj_mnp in ("+listeCodesElp+") "+
				" and cod_anu = "+cod_anu).getResultList();
		return codRvn;
	}

	@Override
	public String getCodSignataireRvn(BigDecimal cod_rvn) {
		@SuppressWarnings("unchecked")
		String codSig = (String)entityManagerApogee.createNativeQuery("select distinct s.cod_sig "+
				" from trav_ext_rvm_res t, releve_note rn, signataire s"+
				" where t.cod_rvn=rn.cod_rvn and rn.cod_sig=s.cod_sig "+
				"and t.cod_rvn="+cod_rvn ).getSingleResult();
		return codSig;
	}

	@Override
	public String getTemoinEditionCarte(String cod_ind, String cod_anu) {
		@SuppressWarnings("unchecked")
		String etaEdtCrt = (String)entityManagerApogee.createNativeQuery("select iaa.eta_edt_crt "+
				" from apogee.ins_adm_anu iaa"+
				" where iaa.cod_anu="+cod_anu+
				" and iaa.cod_ind="+cod_ind ).getSingleResult();
		return etaEdtCrt;
	}



}
