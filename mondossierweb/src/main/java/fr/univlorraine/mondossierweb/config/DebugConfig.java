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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.springframework.aop.Advisor;
import org.springframework.aop.interceptor.CustomizableTraceInterceptor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import fr.univlorraine.mondossierweb.Initializer;
import fr.univlorraine.mondossierweb.controllers.UserController;

/**
 * Configuration mode debug
 * 
 * @author Adrien Colson
 */
@Configuration @Profile(Initializer.DEBUG_PROFILE)
public class DebugConfig {

	/**
	 * Interceptor permettant de logger les appels aux méthodes
	 * @return
	 */
	@Bean
	public CustomizableTraceInterceptor customizableTraceInterceptor() {
		CustomizableTraceInterceptor customizableTraceInterceptor = new CustomizableTraceInterceptor();
		customizableTraceInterceptor.setUseDynamicLogger(true);
		customizableTraceInterceptor.setEnterMessage("Entering $[methodName]($[arguments])");
		customizableTraceInterceptor.setExitMessage("Leaving  $[methodName](), returned $[returnValue]");
		return customizableTraceInterceptor;
	}

	/**
	 * Branche customizableTraceInterceptor sur les méthodes public des classes du package controllers
	 * @return
	 */
	@Bean
	public Advisor controllersAdvisor() {
		return new StaticMethodMatcherPointcutAdvisor(customizableTraceInterceptor()) {
			private static final long serialVersionUID = 5897279987213542868L;

			@Override
			public boolean matches(Method method, Class<?> clazz) {
				return Modifier.isPublic(method.getModifiers()) && clazz.getPackage() != null && clazz.getPackage().getName().startsWith(UserController.class.getPackage().getName());
			}
		};
	}

	/**
	 * Branche customizableTraceInterceptor sur les méthodes enter des vues
	 * @return
	 */
/*	@Bean
	public Advisor viewsEnterAdvisor() {
		return new StaticMethodMatcherPointcutAdvisor(customizableTraceInterceptor()) {
			private static final long serialVersionUID = -7297125641462899887L;

			@Override
			public boolean matches(Method method, Class<?> clazz) {
				return clazz.isAnnotationPresent(VaadinView.class) && "enter".equals(method.getName());
			}
		};
	}*/

}
