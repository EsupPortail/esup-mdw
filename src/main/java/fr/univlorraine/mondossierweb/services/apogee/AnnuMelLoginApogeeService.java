package fr.univlorraine.mondossierweb.services.apogee;


public interface AnnuMelLoginApogeeService {


	public abstract String findMailFromCodEtu(String cod_etu);

	public abstract String findMailFromLogin(String login);
	
	public abstract String findLoginFromCodEtu(String cod_etu);
	
}
