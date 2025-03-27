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
package fr.univlorraine.mondossierweb.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/AppExceptionHandler")
public class AppExceptionHandler extends HttpServlet {
	private static final String TOO_MANY_SESSION_EXCEPTION = "TooManyActiveSessionsException";

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processError(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processError(request, response);
	}

	private void processError(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		// Analyze the servlet exception
		Throwable throwable = (Throwable) request
				.getAttribute("jakarta.servlet.error.exception");
		Integer statusCode = (Integer) request
				.getAttribute("jakarta.servlet.error.status_code");
		String servletName = (String) request
				.getAttribute("jakarta.servlet.error.servlet_name");
		if (servletName == null) {
			servletName = "Unknown";
		}
		String requestUri = (String) request
				.getAttribute("jakarta.servlet.error.request_uri");
		if (requestUri == null) {
			requestUri = "Unknown";
		}
		
		
		// Set response content type
		response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		out.write("<html><head><title>Erreur</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head><body><div style=\"background-color: rgba(85, 96, 164, 0.12); width: 100%; height: 100%; margin:auto;text-align: center;font-size: x-large;display: flex;\"><div style=\"margin: auto;\">");
		// Si c'est une erreur du au nombre de session max atteint
		if(statusCode != null && statusCode == 500 && throwable!=null && throwable.getClass() !=null &&
				throwable.getClass().getName() !=null &&  throwable.getClass().getName().contains(TOO_MANY_SESSION_EXCEPTION)){
			out.write("<h5>Le service demandé est momentanément indisponible, nous mettons tout en oeuvre pour rétablir son fonctionnement.</h5>");
		}else{
			out.write("<h5>Une erreur est survenue</h5>");
		}
		out.write("<h5>Merci de réessayer ultérieurement.<h5>");
		out.write("</div></div></body></html>");
	}
}
