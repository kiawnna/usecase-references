package com.cloudelements.demo.usecase.connection;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudelements.demo.usecase.authentication.AuthenticationController;
import com.cloudelements.demo.usecase.environment.EnvironmentService;
import com.cloudelements.demo.util.CustomSessionTokenService;
import com.cloudelements.demo.util.HTTPUtil;

@Controller
public class ConnectionViewController {

	@Autowired
	private EnvironmentService envService;
	
	@Autowired
	private AuthenticationController authenticationController;
	
	@Autowired
	private CustomSessionTokenService sessionService;
	
	@RequestMapping(value = {"/connect/{app}"} )
	public String init(Map<String, Object> model, HttpServletRequest request, @PathVariable String app) throws ClientProtocolException, IOException {
		
		return "connection/connect_" + app + ".html";
	}
	
	
	
	@RequestMapping (value = { "/getAuthRedirect/{elementKey}" } )
	public String requestRedirect(Map<String, Object> model, HttpServletRequest request, @PathVariable String elementKey) throws ClientProtocolException, IOException, ParseException {
		
		String url = authenticationController.handleOAuth2Authentication("40", 
				"Q0jMRfsRE7eBrIQGf4YAqolgSau8sCoIKGIXscSuWkGdAeB0PL", 
				"YRwcwXyUhUD0sSPXwXMc9FloU57InLuqSbc8W43zR", 
				envService.getCallbackBaseURL() + "/handleOAuthCallback",
				"&scope=com.intuit.quickbooks.accounting&authentication.type=oauth2");
		
		model.put("redirect_url", url);
		return "connection/connect_" + elementKey + ".html";
	}
	
	
	@RequestMapping(value = {"/connection_successful"} )
	public String connectionSuccessful(Map<String, Object> model, HttpServletRequest request) throws ClientProtocolException, IOException {
		model.put("sessionService", sessionService);
		
		return "connection/connection_successful.html";
	}
	
}
