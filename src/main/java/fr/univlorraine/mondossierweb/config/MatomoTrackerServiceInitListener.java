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

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Statistiques d'utilisation Matomo/Piwik (remplace l'ancien PiwikAnalyticsTracker Vaadin 7) :
 * injecte le snippet Matomo dans la page et trace les navigations (SPA).
 */
@SuppressWarnings("serial")
@Component
public class MatomoTrackerServiceInitListener implements VaadinServiceInitListener {

	@Value("${piwik.tracker.url:}")
	private String trackerUrl;

	@Value("${piwik.site.id:}")
	private String siteId;

	@Override
	public void serviceInit(final ServiceInitEvent event) {
		if (!StringUtils.hasText(trackerUrl) || !StringUtils.hasText(siteId)) {
			return;
		}
		final String baseUrl = "https://" + trackerUrl + "/";

		/* Injection du snippet Matomo dans le head de la page */
		event.addIndexHtmlRequestListener(response -> {
			final String script = "var _paq = window._paq = window._paq || [];\n"
				+ "_paq.push(['trackPageView']);\n"
				+ "_paq.push(['enableLinkTracking']);\n"
				+ "(function() {\n"
				+ "  var u='" + baseUrl + "';\n"
				+ "  _paq.push(['setTrackerUrl', u+'matomo.php']);\n"
				+ "  _paq.push(['setSiteId', '" + siteId + "']);\n"
				+ "  var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];\n"
				+ "  g.async=true; g.src=u+'matomo.js'; s.parentNode.insertBefore(g,s);\n"
				+ "})();";
			response.getDocument().head().appendElement("script").text(script);
		});

		/* Trace les navigations internes (application monopage) */
		event.getSource().addUIInitListener(uiEvent ->
			uiEvent.getUI().addAfterNavigationListener(navEvent ->
				uiEvent.getUI().getPage().executeJs(
					"if (window._paq) {"
						+ " _paq.push(['setCustomUrl', '/' + $0]);"
						+ " _paq.push(['trackPageView']);"
						+ "}",
					navEvent.getLocation().getPath())));
	}
}
