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
//        {
//  "iss":"DVOD-data1",
//  "sub": "sareljbotha@gmail.com",
//  "iat": 1516239022
//}
        JiraJwtAuthenticator instance = new JiraJwtAuthenticator();
        instance.sharedSecret = "your-256-bit-secret";

        try {
            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJEVk9ELWRhdGExIiwic3ViIjoic2FyZWxqYm90aGFAZ21haWwuY29tIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyMzkwMjJ9.1oLW7_yEsaDyy2R2hFCZ9U0icUOJO-PFY2YmlVodM6A";
            String expResult = "sareljbotha@gmail.com";
            MyUser result = instance.verifyToken(token);
            Assert.assertEquals(expResult, result.email);
            fail("should have failed this token");
        } catch (TokenExpiredException ex) {
            // pass
            System.out.println("Threw error for expired token - pass");
        }

        String expResult = "sareljbotha@gmail.com";
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJEVk9ELWRhdGExIiwic3ViIjoic2FyZWxqYm90aGFAZ21haWwuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.8qf1IuHwAbH8eDm4Y5I5NtUY3KSBXvnhj1burP6XlR0";
        MyUser result = instance.verifyToken(token);
        Assert.assertEquals(expResult, result.email);
        System.out.println("Token verified - pass");
    }

}
