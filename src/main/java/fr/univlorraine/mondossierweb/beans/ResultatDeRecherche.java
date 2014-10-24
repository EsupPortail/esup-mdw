package fr.univlorraine.mondossierweb.beans;

import java.util.Map;

import fr.univlorraine.mondossierweb.entities.apogee.VObjSeApogee;
import fr.univlorraine.mondossierweb.entities.solr.ObjSolr;
import lombok.Data;

@Data
public class ResultatDeRecherche {
	
	public String type;
	
	public String code;
	
	public String lib;
	

	
	public ResultatDeRecherche(VObjSeApogee o){
		type=o.getId().getTypObj();
		code=o.getId().getCodObj();
		if(o.getId().getCodVrsObj()!=0)
			code = o.getId().getCodObj()+"/"+o.getId().getCodVrsObj();
		lib=o.getLibObj();
	}
	
	public ResultatDeRecherche(ObjSolr o){
		type=o.getType();
		code=o.getCode();
		if(o.getVersion()!=0)
			code = o.getCode()+"/"+o.getVersion();
		lib=o.getLibelle(); //On prend le libelle et non pas la description
	}
	
	public ResultatDeRecherche(Map<String,Object> o){
		type=(String)o.get("TYP_OBJ");
		code=(String)o.get("COD_OBJ");
		if(((Integer)o.get("COD_VRS_OBJ"))!=0)
			code = o.get("COD_OBJ")+"/"+o.get("COD_VRS_OBJ");
		lib=(String)o.get("LIB_OBJ"); //On prend le libelle et non pas la description
	}
	

	public ResultatDeRecherche() {
		super();
	}
	
	
}
