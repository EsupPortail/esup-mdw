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

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class VaadinInternalRequestMatcher implements RequestMatcher {
    // Chemins utilisés par Vaadin 7 en interne
    private static final String[] VAADIN_INTERNAL_PATHS = {
            "/VAADIN/",
            "/PUSH",
            "/UIDL/",        // Vaadin 7 : les requêtes AJAX passent par /UIDL/
            "/HEARTBEAT/",   // Heartbeat Vaadin
            "/APP/",         // Ressources Vaadin
    };

    private static final String[]   INTERNAL_VAADIN_PARAMETER= {
            "v-", "continue&v-"
    };

    private static final String VAADIN_ROOT_PATH = "/";

    /**
     *
     * @param request the request to check for a match
     * @return TRUE = "doit être ignoré par CSRF"
     */
    @Override
    public boolean matches(HttpServletRequest request) {

        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.isEmpty()) path = "/";

        String queryString = request.getQueryString();
        for (String vaadinPath : VAADIN_INTERNAL_PATHS) {
            if (path.startsWith(vaadinPath)) {
                return true;
            }
        }

        if (path.equals(VAADIN_ROOT_PATH) && queryString != null) {
            for (String param : INTERNAL_VAADIN_PARAMETER) {
                if (queryString.startsWith(param)) {
                    return true;
                }
            }
        }
        return false;
    }
}

