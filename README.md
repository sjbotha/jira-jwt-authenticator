This is the home of the JWT Authenticator for Atlassian Seraph

With this you can setup SSO between another application and Jira applications. On the other system you generate a JSON Web Token, then redirect users to the Jira application where they are logged in automatically. This Authenticator will also automatically create a user if a user does not already exist. This makes sense for use with the Service Desk application.

# Building and Installing

Use maven or NetBeans to build the project with this command:

    mvn package

1. Copy the jars in target and target/deps to /opt/jira-core/atlassian-jira/WEB-INF/lib

2. Edit /opt/jira-core/atlassian-jira/WEB-INF/classes/seraph-config.xml and comment out the authenticator tag and add our own authenticator instead like this:

        <authenticator class="com.docuvantage.atlassian.seraph.JiraJwtAuthenticator">
            <init-param>
                <param-name>jwt.shared.secret</param-name>
                <param-value>your-256-bit-secret</param-value>
            </init-param>
            <init-param>
                <param-name>jwt.token.param.name</param-name>
                <param-value>jwt</param-value>
        </init-param>
            <!-- set this to require a specific issuer -->
            <init-param>
                <param-name>jwt.verify.issuer</param-name>
                <param-value></param-value>
            </init-param>
            <!-- set the leeway in number of seconds -->
            <init-param>
                <param-name>jwt.leeway</param-name>
                <param-value>120</param-value>
            </init-param>
        </authenticator>

3. Stop and start the Jira service

4. Change the secret and make sure the same secret is used on the DVOD server in config.ini

# Testing

To test that you have configured the Authenticator correctly generate a test token from this page: https://jwt.io

And then try to use it like this: https://jira.example.com/jira/servicedesk/customer/portal/1?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJEVk9ELWRhdGExIiwic3ViIjoidGVzdDJAZG9jdXZhbnRhZ2UuY29tIiwiaWF0IjoxNTE2MjM5MDIyLCJuYW1lIjoiVGVzdDIgVGVzdDIifQ.7JIu6QSp2s3PFPVQoGC6nujFbCqrgcBNahQQI762MYc

# Troubleshooting

Enable logging by editing /opt/jira-core/atlassian-jira/WEB-INF/classes/log4j.properties and add:

    log4j.logger.com.docuvantage = DEBUG, console, filelog
    log4j.additivity.com.docuvantage = false

View the log file here:

    less /opt/jira-core/logs/catalina.out

# Generating Token Yourself

Write the code on your platform of choice to generate the token. Here is an example written in Java:

    Algorithm algorithm = Algorithm.HMAC256("your-256-bit-secret");
    String token = JWT.create()
        .withIssuer("DVOD-data1")
        .withClaim("email", user.getEmailAddress())
        .withSubject(user.getFullName())
        .withExpiresAt(DateTime.now().plusSeconds(60).toDate())
        .sign(algorithm);
    response.sendRedirect("https://jira.example.com/jira/servicedesk/customer/portal/1?jwt="+token);

The java sample uses this dependency:

       <dependency>
           <groupId>com.auth0</groupId>
           <artifactId>java-jwt</artifactId>
           <version>3.6.0</version>
       </dependency>

# Credits

https://docs.atlassian.com/atlassian-seraph/2.6.1-m1/sso.html?_ga=2.253651081.1448579665.1548708514-509546532.1547576944

Derek Jarvis' write-up here: https://www.jarvispowered.com/single-sign-on-to-jira-with-siteminder/

The Auth0 java-jwt project here: https://github.com/auth0/java-jwt

Example CrowdService code here: https://www.programcreek.com/java-api-examples/?api=com.atlassian.crowd.embedded.api.CrowdService

