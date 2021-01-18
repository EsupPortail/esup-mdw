package fr.univlorraine.mondossierweb.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FragmentErrorFilter implements Filter {

	private Logger LOG = LoggerFactory.getLogger(FragmentErrorFilter.class);



	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest) request);
		String location = wrapper.getParameter("v-loc");
		
		if(location != null && location.contains("#")) {

			String[] locations = location.split("#");
			if(locations.length>1) {
				//wrapper.getParameterMap().remove("v-loc");
				String newlocation = locations[0] + "#" + locations [1];
				
				LOG.info("Wrong location :  "+ location);
				LOG.info("New location :  "+ newlocation);

				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.sendRedirect(newlocation);
				//return;
			}
		} 
		chain.doFilter(request, response);

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}



}
