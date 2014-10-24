package fr.univlorraine.mondossierweb.controllers.rest;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.univlorraine.mondossierweb.entities.Structure;
import fr.univlorraine.mondossierweb.repositories.StructureRepository;

/**
 * Contrôleur REST pour la gestion de l'entité Structure
 */
@Controller
@RequestMapping("/structure")
@PreAuthorize("hasRole('ROLE_' + @environment.getRequiredProperty('role.admin'))")
public class StructureRest {

	@Resource
	private StructureRepository structureRepository;

	/**
	 * @return la liste des structures au format JSON
	 */
	@RequestMapping
	public @ResponseBody List<Structure> getStructures() {
		return structureRepository.findAll();
	}

	/**
	 * @param codStr
	 * @return la structure recherchée au format JSON
	 */
	@RequestMapping(value="/{codStr}", method=RequestMethod.GET)
	public @ResponseBody Structure getStructure(@PathVariable String codStr) {
		return structureRepository.findOne(codStr);
	}

	/**
	 * Enregistre une structure
	 * @param structure
	 * @return la structure enregistrée
	 */
	@RequestMapping(method=RequestMethod.PUT)
	public @ResponseBody Structure saveStructure(@RequestBody Structure structure) {
		return structureRepository.saveAndFlush(structure);
	}

	/**
	 * Supprime une structure
	 * @param codStr
	 */
	@RequestMapping(value="/{codStr}", method=RequestMethod.DELETE)
	public void deleteStructure(@PathVariable String codStr) {
		structureRepository.delete(codStr);
	}

}
