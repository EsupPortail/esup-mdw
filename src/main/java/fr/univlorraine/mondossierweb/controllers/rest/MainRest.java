/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.controllers.rest;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.univlorraine.mondossierweb.controllers.UserController;

/**
 * Contrôleur REST principal
 */
@Controller
public class MainRest {

	@Resource
	private UserController userController;

	/**
	 * Affiche un message par défaut
	 * @return un message donnant la liste des services REST disponibles
	 */
	@RequestMapping
	public @ResponseBody String getRoot() {
		return "Services REST disponibles : /user et /structure";
	}

	/**
	 * Renvoie l'utilisateur courant
	 * @return une réponse au format JSON
	 */
	@RequestMapping("/user")
	public @ResponseBody Map<String, String> getUser() {
		Map<String, String> currentUser = new HashMap<>();
		currentUser.put("username", userController.getCurrentUserName());
		currentUser.put("roles", userController.getCurrentAuthentication().getAuthorities().toString());
		return currentUser;
	}

}
