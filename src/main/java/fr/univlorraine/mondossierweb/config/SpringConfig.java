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

import fr.univlorraine.mondossierweb.tools.elasticsearch.ElasticSearchApogeeService;
import fr.univlorraine.mondossierweb.tools.elasticsearch.ElasticSearchApogeeServiceImpl;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.Resource;
import net.sf.ehcache.CacheManager;
import org.esupportail.portal.ws.client.support.uportal.CachingUportalServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration Spring
 */
@Configuration
public class SpringConfig {

	@Resource
	private Environment environment;

	@Bean
	public LdapContextSource ldapServer() {
		LdapContextSource ldapContextSource = new LdapContextSource();
		String urls = environment.getRequiredProperty("ldap.url");
		if (urls != null && urls.contains(";")) {
			ldapContextSource.setUrls(urls.split(";"));
		} else {
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
		FilterBasedLdapUserSearch fbus = new FilterBasedLdapUserSearch(environment.getProperty("ldap.ou.people"), environment.getProperty("ldap.uid.attribute") + "={0}", ldapServer());
		fbus.setReturningAttributes(getLdapAttributes());
		return fbus;
	}

	@Bean
	public LdapUserSearch ldapEtudiantSearch() {
		FilterBasedLdapUserSearch fbus = new FilterBasedLdapUserSearch(environment.getProperty("ldap.ou.student"), environment.getProperty("attributLdapCodEtu") + "={0}", ldapServer());
		fbus.setReturningAttributes(getLdapAttributes());
		return fbus;
	}

	private String[] getLdapAttributes() {
		List<String> lattributes = new LinkedList<>();
		lattributes.add("uid");
		if (!environment.getProperty("ldap.uid.attribute").equals("uid")) {
			lattributes.add(environment.getProperty("ldap.uid.attribute"));
		}
		lattributes.add("mail");
		if (environment.getProperty("param.esupsgc.urlphoto") != null) {
			lattributes.add("eduPersonPrincipalName");
		}
		lattributes.add(PropertyUtils.getAttributLdapEtudiant());
		lattributes.add(PropertyUtils.getAttributLdapCodEtu());
		if (StringUtils.hasText(PropertyUtils.getAttributGroupeLdap())) {
			lattributes.add(PropertyUtils.getAttributGroupeLdap());
		}
		if (StringUtils.hasText(PropertyUtils.getAttributLdapDoctorant())) {
			lattributes.add(PropertyUtils.getAttributLdapDoctorant());
		}
		String[] tat = new String[lattributes.size()];
		lattributes.toArray(tat);
		return tat;
	}

	/* Uportal service */
	@Bean
	public CachingUportalServiceImpl cachingUportalServiceImpl() {
		final CachingUportalServiceImpl cachingUportalService = new CachingUportalServiceImpl();
		cachingUportalService.setUrl(environment.getRequiredProperty("uportal.ws"));
		cachingUportalService.setCacheManager(new CacheManager());
		return cachingUportalService;
	}

	@Bean
	public ElasticSearchApogeeService ElasticSearchApogeeService() {
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
		esas.setElasticSearchChampTypeObjet(PropertyUtils.getElasticSearchChampTypeObjet());

		return esas;
	}
}
