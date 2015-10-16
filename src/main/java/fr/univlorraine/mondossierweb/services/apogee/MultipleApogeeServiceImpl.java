/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.services.apogee;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import lombok.Data;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.entities.apogee.Anonymat;
import fr.univlorraine.mondossierweb.entities.apogee.Examen;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.apogee.NatureElp;
import fr.univlorraine.mondossierweb.entities.apogee.Signataire;



@Component
@Transactional("transactionManagerApogee")
@Data
public class MultipleApogeeServiceImpl implements MultipleApogeeService {

	private Logger LOG = LoggerFactory.getLogger(MultipleApogeeServiceImpl.class);

	@PersistenceContext (unitName="entityManagerFactoryApogee")
	private transient EntityManager entityManagerApogee;

	@Override
	public String getAnneeEnCours() {
		return (String) entityManagerApogee.createNativeQuery("select cod_anu from annee_uni where eta_anu_iae = 'O'").getSingleResult();

	}

	@Override
	public String getLibEtablissementDef() {
		return (String) entityManagerApogee.createNativeQuery("select e.lib_web_etb from apogee.variable_appli va, etablissement e where COD_VAP = 'ETB_COD' and va.PAR_VAP = e.COD_ETB").getSingleResult();

	}

	@Override
	public List<Examen> getCalendrierExamens(String cod_ind) {
		@SuppressWarnings("unchecked")
		List<Examen> lins = (List<Examen>)entityManagerApogee.createNativeQuery(
				"SELECT DISTINCT  rownum ID,to_char(PESA.DAT_DEB_PES,'DD/MM/YYYY') datedeb, "+
						"DECODE(SUBSTR(TO_CHAR(PESA.DHH_DEB_PES),1,1),'1', "+
						"TO_CHAR(PESA.DHH_DEB_PES),'0'||TO_CHAR(PESA.DHH_DEB_PES)) ||':'|| "+
						"DECODE(TO_CHAR(PESA.DMM_DEB_PES),'0','00',TO_CHAR(PESA.DMM_DEB_PES)) heure, "+
						"PESA.DUR_EXA_EPR_PES duree, "+
						"PESA.COD_SAL salle, SAL.LIB_SAL libsalle, "+
						"NVL(TO_CHAR(PI.NUM_PLC_AFF_PSI),' ') place, "+
						"BAT.LIB_BAT BATIMENT,BAT.LIB_LOC_BAT localisation, E.LIB_EPR epreuve, "+
						"'' codcin "+
						"FROM APOGEE.PRD_EPR_SAL_ANU PESA,APOGEE.EPREUVE E,APOGEE.PES_IND PI,APOGEE.BATIMENT BAT, "+
						"APOGEE.SALLE SAL,APOGEE.PERIODE_EXA PEX  "+
						"WHERE  PI.COD_IND="+cod_ind+" "+
						"AND PI.COD_PES=PESA.COD_PES  "+
						"AND  PESA.COD_EPR=E.COD_EPR AND  PESA.COD_PXA = PEX.COD_PXA  "+
						"AND  PEX.LIB_PXA LIKE '@%' AND  SAL.COD_SAL = PESA.COD_SAL  "+
						"AND  BAT.COD_BAT = SAL.COD_BAT  "+
						"ORDER BY DATEDEB,2", Examen.class).getResultList();

		return lins;
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
	public Signataire getSignataire(String codeSignataire) {
		@SuppressWarnings("unchecked")
		Signataire signataire = (Signataire)entityManagerApogee.createNativeQuery("select sig.COD_SIG, sig.NOM_SIG, sig.QUA_SIG, "+
				"PKB_CRY1.decryptLob(decode(std.TEM_CES_STD,'T',std.IMG_TAM_STD,std.IMG_SIG_STD), "+
				" UTL_RAW.cast_to_raw('CLEFAPOGEE123456')) as IMG_SIG_STD "+
				" from APOGEE.SIGNATAIRE sig, APOGEE.SIGN_TAMP_DIGITALISE std "+
				" where sig.COD_SIG = std.COD_SIG (+) "+
				" and sig.COD_SIG = '"+codeSignataire+"'", Signataire.class).getSingleResult();
		return signataire;
	}

	@Override
	public String getCodCivFromCodInd(String cod_ind) {
		@SuppressWarnings("unchecked")
		String codCiv = (String)entityManagerApogee.createNativeQuery("select i.cod_civ "+
				" from apogee.individu i  "+
				" where i.cod_ind ="+cod_ind).getSingleResult();
		return codCiv;
	}

	@Override
	public List<Inscrit> getInscritsEtapeJuinSep(Etape e) {
		@SuppressWarnings("unchecked")
		List<Inscrit> linscrits = (List<Inscrit>)entityManagerApogee.createNativeQuery("select i.cod_ind,i.cod_etu, i.lib_pr1_ind, decode(I.LIB_NOM_USU_IND,null,i.lib_nom_pat_ind,I.LIB_NOM_USU_IND) NOM, "+
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
		@SuppressWarnings("unchecked")
		String libelle = (String)entityManagerApogee.createNativeQuery("select lib_web_vet "+
				" from version_etape "+
				" where cod_etp = '"+e.getCode()+"' "+
				" and cod_vrs_vet = "+e.getVersion()).getSingleResult();
		return libelle;
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
			@SuppressWarnings("unchecked")
			NatureElp nature = (NatureElp)entityManagerApogee.createNativeQuery("select nel.COD_NEL, nel.LIB_NEL, nel.LIC_NEL, NEL.TEM_EN_SVE_NEL "+
					"from ELEMENT_PEDAGOGI elp, NATURE_ELP  nel "+
					"where nel.COD_NEL=elp.COD_NEL "+
					"and elp.COD_ELP='"+codElp+"'", NatureElp.class).getSingleResult();
			return nature.getLib_nel();
		}
		return null;
	}


	@Override
	public String getCategorieSocioProfessionnelle(String cod_ind, String cod_anu) {
		if(StringUtils.hasText(cod_ind) && StringUtils.hasText(cod_anu)){
			@SuppressWarnings("unchecked")
			String codPcsEtu = (String)entityManagerApogee.createNativeQuery("select COD_PCS_ETUDIANT from ins_adm_anu "
					+ " where cod_ind='"+cod_ind+"' and cod_anu="+cod_anu).getSingleResult();
			return codPcsEtu;
		}
		return null;
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







}
