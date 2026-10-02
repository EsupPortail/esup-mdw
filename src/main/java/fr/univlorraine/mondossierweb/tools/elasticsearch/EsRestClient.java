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
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Client HTTP minimal vers Elasticsearch (indépendant de la version du serveur). */
public class EsRestClient {

	private final HttpClient http;
	private final String baseUrl;

	public EsRestClient(final String url, final int port) {
		this.baseUrl = "http://" + url + ":" + port;
		this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
	}

	/** `GET /` : renvoie la réponse racine (contient `version.number`). */
	public String getRoot() throws IOException, InterruptedException {
		return send(HttpRequest.newBuilder(URI.create(baseUrl + "/"))
			.timeout(Duration.ofSeconds(5))
			.GET()
			.build());
	}

	/** `POST {path}` avec un corps JSON. */
	public String search(final String path, final String body) throws IOException, InterruptedException {
		final HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
			.timeout(Duration.ofSeconds(5))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
			.build();
		return send(request);
	}

	private String send(final HttpRequest request) throws IOException, InterruptedException {
		final HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		if (response.statusCode() >= 300) {
			throw new IOException("Elasticsearch HTTP " + response.statusCode() + " sur " + request.uri());
		}
		return response.body();
	}
}
