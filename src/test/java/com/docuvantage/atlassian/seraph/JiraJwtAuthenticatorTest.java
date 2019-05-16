/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.docuvantage.atlassian.seraph;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.docuvantage.atlassian.seraph.JiraJwtAuthenticator.MyUser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author Sarel
 */
public class JiraJwtAuthenticatorTest {

    public JiraJwtAuthenticatorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @org.junit.Test
    public void testVerifyToken() {
        System.out.println("verifyToken");

        JiraJwtAuthenticator instance = new JiraJwtAuthenticator();
        instance.sharedSecret = "your-256-bit-secret";

        try {
            // this token expired long ago so should fail
            // { "iss": "DVOD-data1", "sub": "asdf@example.com", "iat":1516239022, "exp": 1516239022 }
            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJEVk9ELWRhdGExIiwic3ViIjoiYXNkZkBleGFtcGxlLmNvbSIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyfQ.UguAAtEh3wSmzixzGxYR3EsxGYWvSpgJBwCUu0THIeg";
            String expResult = "asdf@example.com";
            MyUser result = instance.verifyToken(token);
            Assert.assertEquals(expResult, result.email);
            fail("should have failed this token");
        } catch (TokenExpiredException ex) {
            // pass
            System.out.println("Threw error for expired token - pass");
        }

        // {"iss": "DVOD-data1","sub": "asdf@example.com","email": "asdf@example.com","name": "John Doe","iat":1516239022}
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJEVk9ELWRhdGExIiwic3ViIjoiYXNkZkBleGFtcGxlLmNvbSIsImVtYWlsIjoiYXNkZkBleGFtcGxlLmNvbSIsIm5hbWUiOiJKb2huIERvZSIsImlhdCI6MTUxNjIzOTAyMn0.cpcfFaLl3LfcRrcpDYtFKZcFmDeuTYPtB5iqiommELI";
        MyUser result = instance.verifyToken(token);
        Assert.assertEquals("asdf@example.com", result.email);
        Assert.assertEquals("John Doe", result.fullName);
        System.out.println("Token verified - pass");
    }

}
