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
package fr.univlorraine.mondossierweb.config;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;

import com.vaadin.spring.annotation.EnableVaadin;

import fr.univlorraine.mondossierweb.Initializer;
import fr.univlorraine.mondossierweb.tools.elasticsearch.ElasticSearchApogeeService;
import fr.univlorraine.mondossierweb.tools.elasticsearch.ElasticSearchApogeeServiceImpl;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;

/**
 * Configuration Spring
 * 
 * @author Adrien Colson
 */
@Configuration
@EnableSpringConfigured
@ComponentScan(basePackageClasses=Initializer.class)
@EnableAspectJAutoProxy(proxyTargetClass=true)
@EnableVaadin
@PropertySource("classpath:/app.properties")
public class SpringConfig {
	
	@Resource
	private Environment environment;

	/**
	 * Ajoute les paramètres de contexte aux propriétés Spring
	 * @return
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	/**
	 * Messages de l'application
	 * @return
	 */
	@Bean
	public ResourceBundleMessageSource messageSource() {
		ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
		
		List<String> filesToLoad=new LinkedList<String>();
		
		
		//Ajout du fichier de conf optionnel
		ClassPathResource res = new ClassPathResource("i18n/messages.properties");
		if(res.exists()){
			filesToLoad.add("i18n/messages");
		}
		//Ajout du fichier de conf par défaut
		filesToLoad.add("i18n/messages-default");
		
		//Ajout du fichier de conf optionnel
		ClassPathResource resv = new ClassPathResource("i18n/vaadin-messages.properties");
		if(resv.exists()){
			filesToLoad.add("i18n/vaadin-messages");
		}
		//Ajout du fichier de conf par défaut
		filesToLoad.add("i18n/vaadin-messages-default");
		
		resourceBundleMessageSource.setBasenames(filesToLoad.toArray(new String[filesToLoad.size()]));
		return resourceBundleMessageSource;
	}
	
	@Bean
	public LdapContextSource ldapServer() {
		LdapContextSource ldapContextSource = new LdapContextSource();
		String urls= environment.getRequiredProperty("ldap.url");
		if(urls!=null && urls.contains(";")){
			ldapContextSource.setUrls(urls.split(";"));
		}else{
			ldapContextSource.setUrl(urls);
		}

		String userDn = environment.getProperty("ldap.userDn");
		if (userDn instanceof String && !userDn.isEmpty()) {
			ldapContextSource.setUserDn(userDn);
		}

		String password = environment.getProperty("ldap.password");
		if (password instanceof String && !password.isEmpty()) {
			ldapContextSource.setPassword(password);
		}

		return ldapContextSource;
	}
	
	@Bean
	public LdapUserSearch ldapUserSearch() {
		FilterBasedLdapUserSearch fbus = new FilterBasedLdapUserSearch(environment.getProperty("ldap.ou.people"), environment.getProperty("ldap.uid.attribute")+"={0}", ldapServer());
		fbus.setReturningAttributes(getLdapAttributes());
		return fbus;
	}
	
	@Bean
	public LdapUserSearch ldapEtudiantSearch() {
		FilterBasedLdapUserSearch fbus = new FilterBasedLdapUserSearch(environment.getProperty("ldap.ou.student"), environment.getProperty("attributLdapCodEtu")+"={0}", ldapServer());
		fbus.setReturningAttributes(getLdapAttributes());
		return fbus;
	}

	private String[] getLdapAttributes(){
		List<String> lattributes = new LinkedList<>();
		lattributes.add("uid");
		if(!environment.getProperty("ldap.uid.attribute").equals("uid")){
			lattributes.add(environment.getProperty("ldap.uid.attribute"));
		}
		lattributes.add("mail");
		if(environment.getProperty("param.esupsgc.urlphoto")!=null) {
			lattributes.add("eduPersonPrincipalName");
		}
		lattributes.add(PropertyUtils.getAttributLdapEtudiant());
		lattributes.add(PropertyUtils.getAttributLdapCodEtu());
		if(StringUtils.hasText(PropertyUtils.getAttributGroupeLdap())){
			lattributes.add(PropertyUtils.getAttributGroupeLdap());
		}
		if(StringUtils.hasText(PropertyUtils.getAttributLdapDoctorant())){
			lattributes.add(PropertyUtils.getAttributLdapDoctorant());
		}
		String[] tat =new String[lattributes.size()];
		lattributes.toArray(tat);
		return tat;
	}
	
	@Bean
	public ElasticSearchApogeeService ElasticSearchApogeeService(){
		ElasticSearchApogeeServiceImpl esas = new ElasticSearchApogeeServiceImpl();
		
		esas.setTypCmp(Utils.CMP);
		esas.setTypVet(Utils.VET);
		esas.setTypElp(Utils.ELP);
		esas.setTypEtu(Utils.ETU);
		esas.setEsTyp(Utils.ES_TYPE);
		esas.setElasticSearchCluster(PropertyUtils.getElasticSearchCluster());
		esas.setElasticSearchUrl(PropertyUtils.getElasticSearchUrl());
		esas.setElasticSearchPort(PropertyUtils.getElasticSearchPort());
		esas.setElasticSearchIndex(PropertyUtils.getElasticSearchIndex());
		esas.setElasticSearchChampCodeObjet(PropertyUtils.getElasticSearchChampCodeObjet());
		esas.setElasticSearchChampRecherche(PropertyUtils.getElasticSearchChampRecherche());
		esas.setElasticSearchChampVersionObjet(PropertyUtils.getElasticSearchChampVersionObjet());
		
		return esas;
		
	}
}
