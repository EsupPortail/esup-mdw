package fr.univlorraine.mondossierweb.controllers;

import javax.annotation.Resource;

import lombok.Getter;
import lombok.Setter;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * Gestion de la recherche
 */
@Component
public class ListeInscritsController {

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient UserDetailsService userDetailsService;
	@Resource
	private transient UiController uiController;


	@Getter
	@Setter
	private String code;
	
	@Getter
	@Setter
	private String type;
	

}
