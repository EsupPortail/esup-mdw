package fr.univlorraine.mondossierweb.services.apogee;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import lombok.Data;

import org.jfree.util.Log;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fr.univlorraine.mondossierweb.entities.apogee.Examen;
import fr.univlorraine.mondossierweb.entities.apogee.Signataire;



@Component
@Transactional("transactionManagerApogee")
@Data
public class MultipleApogeeServiceImpl implements MultipleApogeeService {


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
	public List<String> getCinqDernieresAnneesUniversitaires() {
		@SuppressWarnings("unchecked")
		List<String> lannee = (List<String>)entityManagerApogee.createNativeQuery("select cod_anu from annee_uni order by cod_anu DESC").getResultList();

		//On garde 5 annee maxi
		if(lannee!=null && lannee.size()>5){
			for(int i=(lannee.size()-1);i>4;i--){
				lannee.remove(i);
			}
		}

		return lannee;
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






}
