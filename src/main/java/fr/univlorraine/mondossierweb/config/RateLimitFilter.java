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

import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filter pour limiter le taux de requêtes sur /login/cas
 * afin d'empêcher les attaques par force brute sur les tickets CAS.
 */
@Slf4j
public class RateLimitFilter implements Filter {


    private final int maxRequests;
    private static final long TIME_WINDOW_MS = 60_000;

    private final Map<String, List<Long>> requestTimestamps = new ConcurrentHashMap<>();

    public RateLimitFilter(int maxRequests) {
        super();
        this.maxRequests = maxRequests;
        log.info("RateLimitFilter actif pour " + this.maxRequests + " requêtes toutes les " + TIME_WINDOW_MS + "ms");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String ip = httpRequest.getRemoteAddr();
        String path = httpRequest.getRequestURI();

        // Applique uniquement à /login/cas
        if (!path.startsWith(Utils.CAS_RETURN_URL)) {
            chain.doFilter(request, response);
            return;
        }

        // Nettoyage des anciens timestamps
        List<Long> timestamps = requestTimestamps.computeIfAbsent(ip, k -> new ArrayList<>());
        long now = System.currentTimeMillis();
        timestamps.removeIf(ts -> ts < now - TIME_WINDOW_MS);

        // Vérification du seuil
        if (maxRequests > 0 && timestamps.size() >= maxRequests) {
            log.warn("Too Many Requests for - " + ip);
            ((HttpServletResponse) response).sendError(429, "Too Many Requests");
            return;
        }
        timestamps.add(now);
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialisation non nécessaire
    }

    @Override
    public void destroy() {
        // Nettoyage non nécessaire (ConcurrentHashMap gère la mémoire)
    }
}
