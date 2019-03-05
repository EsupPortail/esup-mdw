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

import java.util.Optional;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.Assert;

import com.vaadin.server.VaadinSession;

import lombok.extern.slf4j.Slf4j;

/** A custom {@link SecurityContextHolderStrategy} that stores the {@link SecurityContext} in the Vaadin Session and in an InheritableThreadLocal CONTEXT_HOLDER.
 *
 * @see <a href=
 *      "https://github.com/peholmst/SpringSecurityDemo/blob/master/hybrid-security/src/main/java/org/vaadin/peholmst/samples/springsecurity/hybrid/VaadinSessionSecurityContextHolderStrategy.java">SpringSecurityDemo/.../VaadinSessionSecurityContextHolderStrategy.java</a>
 * @author Adrien Colson
 * @see java.lang.ThreadLocal */
@Slf4j
public class VaadinSecurityContextHolderStrategy implements SecurityContextHolderStrategy {

	/** startup context_holder. */
	private static final ThreadLocal<SecurityContext> STARTUP_CONTEXT_HOLDER = new ThreadLocal<>();
	/** context_holder. */
	private static final ThreadLocal<SecurityContext> CONTEXT_HOLDER = new InheritableThreadLocal<>();

	/** @see org.springframework.security.core.context.SecurityContextHolderStrategy#clearContext() */
	@Override
	public void clearContext() {
		STARTUP_CONTEXT_HOLDER.remove();
		CONTEXT_HOLDER.remove();

		Optional.ofNullable(VaadinSession.getCurrent()).ifPresent(httpSession -> {
			try {
				httpSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, null);
			} catch (final IllegalStateException e) {
				log.trace("Can't clear context in vaadin session.");
			}
		});
	}

	/** @see org.springframework.security.core.context.SecurityContextHolderStrategy#getContext() */
	@Override
	public SecurityContext getContext() {
		if (VaadinSession.getCurrent() != null) {
			Optional.ofNullable(VaadinSession.getCurrent()).ifPresent(httpSession -> {
				try {
					httpSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, STARTUP_CONTEXT_HOLDER.get());
				} catch (final IllegalStateException e) {
					log.trace("Can't clear context in vaadin session.");
				}
			});
			STARTUP_CONTEXT_HOLDER.set(null);
		}
		SecurityContext ctx = (SecurityContext) Optional.ofNullable(VaadinSession.getCurrent()).map(httpSession -> {
			try {
				return httpSession.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
			} catch (final IllegalStateException e) {
				return null;
			}
		}).orElse(CONTEXT_HOLDER.get());

		if (ctx == null) {
			ctx = createEmptyContext();
			setContext(ctx);
		}
		return ctx;
	}

	/** @see org.springframework.security.core.context.SecurityContextHolderStrategy#setContext(org.springframework.security.core.context.SecurityContext) */
	@Override
	public void setContext(final SecurityContext context) {
		Assert.notNull(context, "Only non-null SecurityContext instances are permitted");

		CONTEXT_HOLDER.set(context);

		if (VaadinSession.getCurrent() == null) {
			STARTUP_CONTEXT_HOLDER.set(context);
		} else {
			Optional.ofNullable(VaadinSession.getCurrent()).ifPresent(httpSession -> {
				try {
					httpSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
				} catch (final IllegalStateException e) {
					log.trace("Can't set context attribute in vaadin session.");
				}
			});
		}
	}

	/** @see org.springframework.security.core.context.SecurityContextHolderStrategy#createEmptyContext() */
	@Override
	public SecurityContext createEmptyContext() {
		return new SecurityContextImpl();
	}

}

