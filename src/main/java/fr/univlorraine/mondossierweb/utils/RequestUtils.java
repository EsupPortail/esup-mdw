/**
 *
 *  ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
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
