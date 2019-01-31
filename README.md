This is the home of the JWT Authenticator for Atlassian Seraph


# Building and Installing

    Download the code from here:

    Use maven or NetBeans to build the project with this command:
    mvn package
    Copy the jars in target and target/deps to /opt/jira-core/atlassian-jira/WEB-INF/lib

    Edit /opt/jira-core/atlassian-jira/WEB-INF/classes/seraph-config.xml and comment out the authenticator tag and add our own authenticator instead like this:
    <authenticator class="com.docuvantage.atlassian.seraph.JiraJwtAuthenticator">
        <init-param>
            <param-name>jwt.shared.secret</param-name>
            <param-value>your-256-bit-secret</param-value>
        </init-param>
        <init-param>
            <param-name>jwt.token.param.name</param-name>
            <param-value>dvjwt</param-value>
       </init-param>
        <!-- set this to require a specific issuer -->
        <init-param>
            <param-name>jwt.verify.issuer</param-name>
            <param-value></param-value>
        </init-param>
        <!-- set the leeway in number of seconds -->
        <init-param>
            <param-name>jwt.leeway</param-name>
            <param-value>dvjwt</param-value>
        </init-param>
    </authenticator>
    Stop and start the Jira service
    Change the secret and make sure the same secret is used on the DVOD server in config.ini

# Testing

Generate a test token from this page: https://jwt.io

# Generating Token

Write the code on your platform of choice to generate the token. Here is an example written in Java:

    Algorithm algorithm = Algorithm.HMAC256("your-256-bit-secret");
    String token = JWT.create()
        .withIssuer("DVOD-data1")
        .withClaim("email", user.getEmailAddress())
        .withSubject(user.getFullName())
        .withExpiresAt(DateTime.now().plusSeconds(60).toDate())
        .sign(algorithm);

The java sample uses this dependency:

       <dependency>
           <groupId>com.auth0</groupId>
           <artifactId>java-jwt</artifactId>
           <version>3.6.0</version>
       </dependency>


# Troubleshooting

Enable logging by editing /opt/jira-core/atlassian-jira/WEB-INF/classes/log4j.properties and add:

    log4j.logger.com.docuvantage = DEBUG, console, filelog
    log4j.additivity.com.docuvantage = false

View the log file here:

    less /opt/jira-core/logs/catalina.out

# CREDITS
https://docs.atlassian.com/atlassian-seraph/2.6.1-m1/sso.html?_ga=2.253651081.1448579665.1548708514-509546532.1547576944

Derek Jarvis' write-up here: https://www.jarvispowered.com/single-sign-on-to-jira-with-siteminder/

The Auth0 java-jwt project here: https://github.com/auth0/java-jwt

Example CrowdService code here: https://www.programcreek.com/java-api-examples/?api=com.atlassian.crowd.embedded.api.CrowdService

