package fr.univlorraine.mondossierweb.services.apogee;



public interface InscriptionService {

	public String getProfil(String codAnu, String codInd);
	
	public String getCgeFromCodIndIAE(String codAnu, String codInd, String codEtp, String vrsVet);
	
	public String getCmpFromCodIndIAE(String codAnu, String codInd, String codEtp, String vrsVet);

}
