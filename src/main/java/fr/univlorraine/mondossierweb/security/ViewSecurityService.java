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
package fr.univlorraine.mondossierweb.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contrôle l'accès aux vues Vaadin en évaluant leurs annotations
 * {@code @Secured}, {@code @RolesAllowed} et {@code @PreAuthorize}.
 */
@Service
@Slf4j
@SuppressWarnings("serial")
public class ViewSecurityService implements VaadinServiceInitListener {

	private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
	private final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

	@Autowired
	private void setBeanFactory(final BeanFactory beanFactory) {
		evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
	}

	/**
	 * Ajoute un listener beforeEnter à chaque UI, qui vérifie l'accès à la vue cible.
	 */
	@Override
	public void serviceInit(final ServiceInitEvent event) {
		event.getSource().addUIInitListener(uiEvent -> {
			uiEvent.getUI().addBeforeEnterListener(beforeEvent -> {
				if (!isAccessGranted(beforeEvent.getUI(), beforeEvent.getNavigationTarget())) {
					throw new AccessDeniedException("Access denied.");
				}
			});
		});
	}

	public boolean isAccessGranted(final UI ui, final Class<?> securedClass) {
		return isAccessGranted(SecurityUtil.getAuthentication(ui).orElse(null), securedClass);
	}

	public boolean isAccessGranted(final Authentication authentication, final Class<?> securedClass) {
		return SecurityUtil.isUserLoggedIn(authentication)
			&& isAccessGrantedForPreAuthorize(authentication, securedClass)
			&& isAccessGrantedForRoleAnnotations(authentication, securedClass);
	}

	private boolean isAccessGrantedForPreAuthorize(final Authentication authentication, final Class<?> securedClass) {
		final PreAuthorize preAuthorize = AnnotationUtils.findAnnotation(securedClass, PreAuthorize.class);
		if (preAuthorize == null) {
			return true;
		}

		final SecurityExpressionRoot securityExpressionRoot = new SecurityExpressionRoot(authentication) {
		};
		securityExpressionRoot.setTrustResolver(new AuthenticationTrustResolverImpl());

		try {
			return spelExpressionParser.parseExpression(preAuthorize.value()).getValue(evaluationContext, securityExpressionRoot, Boolean.class);
		} catch (final EvaluationException e) {
			log.error("Cannot evaluate expression '{}' securing {}.", preAuthorize.value(), securedClass, e);
			return false;
		} catch (final ParseException e) {
			log.error("Cannot parse expression '{}' securing {}.", preAuthorize.value(), securedClass, e);
			return false;
		}
	}

	private boolean isAccessGrantedForRoleAnnotations(final Authentication authentication, final Class<?> securedClass) {
		final Set<String> allowedRoles = Stream.of(
			Optional.ofNullable(AnnotationUtils.findAnnotation(securedClass, Secured.class))
				.map(Secured::value),
			Optional.ofNullable(AnnotationUtils.findAnnotation(securedClass, RolesAllowed.class))
				.map(RolesAllowed::value))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.flatMap(Arrays::stream)
			.collect(Collectors.toSet());

		if (allowedRoles.isEmpty()) {
			return true;
		}

		final Set<String> authoritiesSet = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
		return !Collections.disjoint(allowedRoles, authoritiesSet);
	}
}
