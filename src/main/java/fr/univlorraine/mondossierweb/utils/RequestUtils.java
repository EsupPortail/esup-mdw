/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import lombok.Data;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * @author Charlie Dubois
 * 
 * Accès aux porperties du fichier de config des requetes sql
 */
@Component
@Data
public class RequestUtils {


	private Properties properties;

	public RequestUtils(){
		try {
			
			File file = new ClassPathResource("apogeeRequest.xml").getFile();
			FileInputStream fileInput = new FileInputStream(file);
			properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();

//			Enumeration enuKeys = properties.keys();
//			while (enuKeys.hasMoreElements()) {
//				String key = (String) enuKeys.nextElement();
//				String value = properties.getProperty(key);
//				System.out.println(key + ": " + value);
//			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public String getCalendrierDesExamens(){
		return properties.getProperty("calendrierExamen");
	}


	public String getMailFromLogin() {
		return properties.getProperty("mailFromLogin");
	}


	public String getLoginFromCodEtu() {
		return properties.getProperty("loginFromCodEtu");
	}


	public String getMailFromCodEtu() {
		return properties.getProperty("mailFromCodEtu");
	}

}
