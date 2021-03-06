package com.cloudelements.demo.usecase.authentication;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudelements.demo.model.Element;
import com.cloudelements.demo.usecase.environment.EnvironmentService;
import com.cloudelements.demo.util.AuthenticationUtil;
import com.cloudelements.demo.util.HTTPUtil;

/*
 * This controller is launched from the main ViewController after it gathered the data.
 * It has methods to handle types of authentication, currently supported: basic and oauth2
 * 
 */

@RestController
public class AuthenticationController {

	@Autowired
	private HttpSession httpSession;
	
	@Autowired
	private EnvironmentService envService;
	
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
	/*
	 * Create the expected CE JSON Structure based upon incoming data from the viewController when handling BASIC authentication
	 * Returns the CE instancetoken if available, null if not 
	 */
	public String handleBasicAuthentication (Element el, HashMap<String, String> configMap) {
		JSONObject authObject = AuthenticationUtil.createAuthConfiguration(el.getKey(), "myInstance", new String[] {"javaApp"}, configMap);
		logger.debug( authObject.toJSONString() );
		JSONObject instanceObj = HTTPUtil.doPost(null, "/elements/api-v2/instances", authObject.toJSONString());

		String token = instanceObj.get("token").toString();
		if ( token == null ) {
			return null;
		}
		return "/createInvoice?token=" + token;
	}
	
	/*
	 * Calls CE to get the redirect URL for the given element.
	 * Returns the redirect url if provided by CE, otherwise null
	 */
	public String handleOAuth2Authentication (String elementKey, String apiKey, String apiSecret, String callbackUrl, String extra) throws ParseException {
		JSONObject redirectObj = HTTPUtil.doGet(null, "/elements/api-v2/elements/" + elementKey + "/oauth/url?apiKey=" + apiKey + 
				"&apiSecret=" + apiSecret + "&callbackUrl=" + callbackUrl + extra);

		if (redirectObj.containsKey("message")) {
			logger.debug("**** EXCEPTION >>> " + redirectObj.get("message"));
			return null;
		} else {
			String redirectURL = redirectObj.get("oauthUrl") != null ? redirectObj.get("oauthUrl").toString() : "something_went_wrong"; //TODO: should handle incorrect responses better
			
			return redirectURL;
		}
	}
	
	/*
	 * Exposes the https://your_url:8080/handleOAuthCallback that is to be used upon using the oauth2 authentication flow
	 * as callback URL.  This method captures the auth tokens and triggers the instance creation in CE.
	 * That instance can be set to session and user redirected to the oauth page
	 * 
	 */
	@RequestMapping ("/handleOAuthCallback")
	public void handleOAuthCallback (HttpServletRequest request, HttpServletResponse response) {
		try {
			// These params might be different per app we speak to?
			String authCode = (String) request.getParameter("code");
			String appState = (String) request.getParameter("state");
			String realmId  = (String) request.getParameter("realmId");
			
			//qboInstanceCreation(authCode, realmId);
					/*
					 * Look for expected responses qbo vs freshbooks. They are so different  
					//https://docs.cloud-elements.com/home/quickbooks-online-authenticate-snippettermelementucarticle
					 * https://docs.cloud-elements.com/home/freshbooks-cloud-accounting-authenticate-snippettermelementucarticle
					 */
			
			response.sendRedirect("http://localhost:8080/connection_successful/" + envService.getURLFriendlyQBO());
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("Full url received = " + request.getRequestURL().toString());
		}
	}
}
