/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.beans;

import java.io.Serializable;
import java.util.Map;

import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.entities.apogee.VObjSeApogee;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import lombok.Data;

@Data
public class ResultatDeRecherche implements Serializable {
	
	
	public String code;
	
	public String lib;
	
	public String type;
	

	
	public ResultatDeRecherche(VObjSeApogee o){
		type=o.getId().getTypObj();
		code=o.getId().getCodObj();
		if(o.getId().getCodVrsObj()!=0)
			code = o.getId().getCodObj()+"/"+o.getId().getCodVrsObj();
		lib=o.getLibObj();
	}
	
	
	public ResultatDeRecherche(Map<String,Object> o){
		type=(String)o.get(Utils.ES_TYPE);
		code=(String)o.get(PropertyUtils.getElasticSearchChampCodeObjet());
		if(((Integer)o.get(PropertyUtils.getElasticSearchChampVersionObjet()))!=0)
			code = o.get(PropertyUtils.getElasticSearchChampCodeObjet())+"/"+o.get(PropertyUtils.getElasticSearchChampVersionObjet());
		lib="["+code+"] "+(String)o.get(PropertyUtils.getElasticSearchChampLibelleObjet()); //On prend le libelle
	}
	

	public ResultatDeRecherche() {
		super();
	}
	
	
	
	@Override
	public String toString() {
		return lib;
	}

	public ResultatDeRecherche(String code, String lib, String type) {
		super();
		this.type = type;
		this.code = code;
		this.lib = lib;
	}
}
