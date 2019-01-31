package com.docuvantage.atlassian.seraph;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.component.ComponentAccessor;
import org.apache.log4j.Category;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.config.SecurityConfig;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.google.common.base.Strings;

import java.security.Principal;
import java.util.Base64;
import java.util.Map;
import java.util.Random;

/**
 * Extension of DefaultAuthenticator that uses third-party code to determine if
 * a user is logged in, given a HTTPRequest object. Third-party code will
 * typically check for the existence of a special cookie.
 *
 * In SSO scenarios where this authenticator is used, one typically configures
 * Seraph to use an external login page as well:
 *
 * <init-param>
 * <param-name>login.url</param-name>
 * <param-value>http://mycompany.com/globallogin?target=${originalurl}</param-value>
 * </init-param>
 *
 *
 *
 */
public class JiraJwtAuthenticator extends com.atlassian.jira.security.login.JiraSeraphAuthenticator {

    private static final Category log = Category.getInstance(JiraJwtAuthenticator.class);
    String sharedSecret = null;
    String tokenParamName = "jwt";
    private String issuer;
    private int leeway = 0;

    @Override
    public void init(Map<String, String> params, SecurityConfig config) {
        super.init(params, config);

        sharedSecret = params.get("jwt.shared.secret");
        tokenParamName = params.get("jwt.token.param.name");
        if (!Strings.isNullOrEmpty(params.get("jwt.verify.issuer"))) {
            issuer = params.get("jwt.verify.issuer");
        }
        if (!Strings.isNullOrEmpty(params.get("jwt.leeway"))) {
            leeway = Integer.parseInt(params.get("jwt.leeway"));
        }
    }

    public Principal getUser(HttpServletRequest request, HttpServletResponse response) {
        log.debug("getUser() requestURL=" + request.getRequestURL() + " " + request.getQueryString());

        Principal princ = null;
        if (request.getSession() != null && request.getSession().getAttribute(DefaultAuthenticator.LOGGED_IN_KEY) != null) {
            log.debug("Session found; user already logged in");
            princ = (Principal) request.getSession().getAttribute(DefaultAuthenticator.LOGGED_IN_KEY);
        } else {
            // TODO check path of request and only work on service desk portal
            if (Strings.isNullOrEmpty(sharedSecret)) {
                log.debug("The shared secret is not configured so not trying JWT auth");
            } else {
                try {
                    log.debug("Trying verifyToken");
                    String token = request.getParameter(tokenParamName);
                    if (Strings.isNullOrEmpty(token)) {
                        log.debug("No request parameter " + tokenParamName + " so not trying JWT auth");
                    } else {
                        MyUser verifiedUser = verifyToken(token);
                        log.debug("verifiedLogin=" + verifiedUser);
                        // get the user from jira
                        princ = getUser(verifiedUser.email);

                        if (princ == null) {
                            // need to create user
                            CrowdService crowdService = ComponentAccessor.getCrowdService();
                            Random prng = new Random();
                            byte[] randomPass = new byte[16];
                            prng.nextBytes(randomPass);
                            ImmutableUser.Builder userBuilder = new ImmutableUser.Builder();
                            userBuilder.active(true);
                            userBuilder.directoryId(0);
                            userBuilder.displayName(verifiedUser.fullName);
                            userBuilder.emailAddress(verifiedUser.email);
                            userBuilder.name(verifiedUser.email);
                            crowdService.addUser(userBuilder.toUser(), Base64.getEncoder().encodeToString(randomPass));
                            princ = getUser(verifiedUser.email);
                        }

                        log.debug("returning princ=" + princ);
                        log.info("Logged in via JWT, with User " + princ);
                        request.getSession().setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, princ);
                        request.getSession().setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
                        return princ;
                    }
                } catch (Exception ex) {
                    log.error("Error", ex);
                }
            }
        }
        log.debug("calling super.getUser");
        Principal superGetUser = super.getUser(request, response);
        log.debug("superGetUser=" + superGetUser);
        return superGetUser;

    }

    MyUser verifyToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(sharedSecret);
        Verification verification = JWT.require(algorithm).acceptLeeway(leeway);
        if (!Strings.isNullOrEmpty(issuer)) {
            verification = verification.withIssuer(issuer);
        }
        JWTVerifier verifier = verification.build(); //Reusable verifier instance
        DecodedJWT jwt = verifier.verify(token);
        log.debug("subject=" + jwt.getSubject());
        log.debug("iat=" + jwt.getIssuedAt());
        Claim name = jwt.getClaim("name");
        final String email = jwt.getSubject();
        return new MyUser(name.asString(), email);
    }

    class MyUser {
        String fullName;
        String email;
        public MyUser(String fullName, String email) {
            this.fullName = fullName;
            this.email = email;
        }
    }
}
