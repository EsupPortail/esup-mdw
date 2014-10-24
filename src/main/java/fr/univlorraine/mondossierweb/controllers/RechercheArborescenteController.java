package fr.univlorraine.mondossierweb.controllers;

import java.util.List;

import javax.annotation.Resource;

import lombok.Getter;
import lombok.Setter;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeService;

/**
 * Gestion de la recherche
 */
@Component
public class RechercheArborescenteController {

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient UserDetailsService userDetailsService;
	@Resource
	private transient UiController uiController;


	@Resource
	private MultipleApogeeService multipleApogeeService;
	
	@Getter
	@Setter
	private String code;
	
	@Getter
	@Setter
	private String type;
	
	
	private List<String> lanneeUniv;
	
	public List<String> recupererLesCinqDernieresAnneeUniversitaire(){
		if(lanneeUniv==null)
			lanneeUniv = multipleApogeeService.getCinqDernieresAnneesUniversitaires();
		return lanneeUniv;
	}
	

}
