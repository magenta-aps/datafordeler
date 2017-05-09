package dk.magenta.dafosts.saml;

import dk.magenta.dafosts.library.DafoTokenGenerator;
import dk.magenta.dafosts.library.DatabaseQueryManager;
import dk.magenta.dafosts.saml.controller.PassiveGetTokenController;
import dk.magenta.dafosts.saml.users.DafoAssertionVerifier;
import dk.magenta.dafosts.library.users.DafoPasswordUserDetails;
import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DafoStsBySamlApplicationTests {

    static String USERNAME = "amalie@serviceydbyder.gl";
    static String PASSWORD = "amalie";
    static String ENCRYPTED_PASSWORD = "9QTHekiEX59S7GqF5Za0X7ezZ1E4rXq2/1cvIRbTqfE=";
    static String PASSWORD_SALT = "eAxwVKDSNpyXsoi/WIJKTA==";
    static int MOCK_USERID = -42;
    static int VALID_USER_STATUS = DatabaseQueryManager.ACCESS_ACCOUNT_STATUS_ACTIVE;
    static int INVALID_USER_STATUS = DatabaseQueryManager.ACCESS_ACCOUNT_STATUS_ACTIVE - 42;
    static String correctPasswordURI = "/get_token_passive?username=" + USERNAME + "&password=" + PASSWORD;
    static String wrongPasswordURI = "/get_token_passive?username=" + USERNAME + "&password=" + "wrong";
    static String BOOTSTRAP_TOKEN = "nVZZk6LMEn33V3Q4jx3KIq7RbdxiFRUUxfWNpQQUQasKUX/9LbR1uvvOzDfffSIq6+ThZEJlnT" +
            "fs7GO+AzCGiERp8nLexwnu3KLv5QwlndTBEe4kzh7iDvE6U2AMO3yV7TiPlPJHTo7Jezkk5NBhGOyFcO/gKt3BqXOopihgcszwLF" +
            "tnWJ4hKMPkkVcAYHJ65uZ5Xs1rtxSKrzE0pYBUKAbG6QGWX3T5Li3LIr9TEzipVlcFrg4aIltTOJ4T2gJfr/G1Ft9oUjTGGdQTTJ" +
            "yECuRZrllh6xW2ZXNCh2c7tVaVFxrr8sscIkzLoZAqW+6+3RtzS0bdQhqm2nxnk1Yi/1DdOwFMiFP1d2/MF+TbNAoSh2QI3sv7TV" +
            "ksw7YZCvBxFPwod0u3NOjrySalC8lJ0iTynDi6OkWLDUjC1H8BcZCiiIT735ByDMcWpBV49ioeJyQ/yswH9U3RX9J80YawU8Ghw92Y" +
            "JnADEUw8+DKb6O/lH3/9EWiujZwEb1K0x58X/07M4xfwK/hR003X39P9pkXMF3VyFEBM/p9uPTt1p5g7cQa7bYbgaZS1myc1mvWEH" +
            "dtGNeV4XQpD4f2N+YykQp4tLhZffornZ/zAgu25KfvipmVxDYVxI0lBkaYzKNRgQzTJch72QMPl90k0TdyLuxkI28MYrveX/Znbad" +
            "yRuFwPX8Bag4tpSW44ivHqrNfq6LhsMYz46jdPPTLt1w79xiXZcDXmNWGmRz1yj5PllWebp6UcRfXx4WLEfZsJtfBMlt4MooFf4hAPr" +
            "PZS0FvzuTlTJHspvH9U87mANx93BvBSlPcxCvy/PC5F5rLOtmWHOM+FVIyjDT01BHYNXZdMW5LAuhGAXBdBoCvTmlALtsAUg90x3EVa" +
            "O2dFYM1UIItzw8K5ZK3kuWVpSt6fz67K0AA7DXAzpSQBuscqZ3ULZmJgzkXgGfKMO/mLOrtaxJmurg/rpXeWbTC876eGqPq2fdXPyhZ" +
            "Y95hhSIvzqbTiVexo7asvA6jm7NmQwcWwd7l5pc/t2qGxq2kXMeMZM9TZWbqC/p1oZYN4bheKSx+SdRn0ZctWFoZo3SSLeW7aizbnJp" +
            "PQWQjBfB/H/lUxDYBv+1KQK3POHBtbZWqI4F4myPO+q7W3q8U5dWsma+jaxgCsJk2P2lR3a7KlFP0CQNBMIEtiZA3EwJJmByadc6eF12" +
            "LsnTWNwMlOsxI2riNDcLDIRzFBvDVIr0lUd+Zq1uJaxmE0XOuoNTQS+7qI4/7p5ID6BAD/4CqS+yq01K0rQjbEcOab9VLvkJ+SHiM" +
            "vWmvP0067ek1vjNCxGQ6lE7fukWwkpbsgvDLARuFlfHy1dsTYLJPJVOihgda3r2dH8mbNEe1UCVhATM96rshgVDS0Z7VEsGnR2gxJ" +
            "HINctlb9QbrWw5NnAktRRQvIwUoBi3o+njQ9xA2Bf3zV0UQQopIVT7S6bkvr5aod6uz4EgzHPOoBM3wdrzb2/uxmo+SyM7VLaDTUr" +
            "Tp0F3y27ln2itsaaHxA2xHaXwZ1vO1PSiPB52So9Wxmlehb3O6nwWRwEpY9RcqHRJ/7wNX9RU8h83X/Ykx3xLtcsOqdsnBsDVqWxy" +
            "jM4tjKelMppXPlF6fiGbyfG+bn+et+Op2Pq2+auVvokcfSpA5Al19UOiMd8ntrwFW5WyTyK5sbtEONQBQD30cQ43JXBjZQRxNZGSoT" +
            "xtnTKw7+hxqJU+TBzHcvPkTVIH7cqfd3fhMkpckmKpgLu3If0n92Kt6+40IHQUQn84P5W3GU048KQvxipkSEVDr8k1egoFEyQmBD" +
            "IPofXP2Je9CDzI+KmT6hkx5FXvGi71vdm7XABFd8uHGymFC/QyJyoW7jofkJ/R74Qst8r+jnm0iYTAn9EfaU+uW2/Gdb9CWbkhJ4" +
            "Jr+KSTE1hPTy6v7RM3odr8DR8Jg+8hR9Ku4XXL/a/Bp81vMz/PCl3f8CQSmWqQ==";

	@MockBean
	private DatabaseQueryManager dbManager;

	@MockBean
    DafoAssertionVerifier dafoAssertionVerifier;

    @Autowired
    private PassiveGetTokenController passiveGetTokenController;

    @Autowired
    DafoTokenGenerator dafoTokenGenerator;

    @Autowired
    private MockMvc mockMvc;

    public DafoPasswordUserDetails mockPasswordUser(int activeStatus) {
        return new DafoPasswordUserDetails(
                MOCK_USERID, // UserId
                activeStatus, // Status
                "Test", // givenName
                "User", // lastName
                USERNAME, // email
                "Test Organisation", // organisation
                ENCRYPTED_PASSWORD, // encrypted password
                PASSWORD_SALT, // salt
                dbManager
        );
    }

    org.hamcrest.Matcher<java.lang.String> decodedAndInflatedContains(String matchingString) {

        return new org.hamcrest.TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String item) {
                try {
                    String decodedMessage = dafoTokenGenerator.decodeAndInflate(item);
                    return decodedMessage.contains(matchingString);
                }
                catch(MessageDecodingException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a string contains ")
                        .appendValue(matchingString);
            }
        };
    }


	@Test
	public void contextLoads() {
	    assertThat(passiveGetTokenController).isNotNull();
	}

	@Test
    public void frontpageLoads() throws Exception {
        String requestURI = "/get_token_passive?username=" + USERNAME + "&password=" + PASSWORD;
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("usernamepasswordform")))
                .andExpect(content().string(containsString("bootstraptokenform")));
    }


	@Test
    public void issueTokenByUsernamePassword() throws Exception {

        when(dbManager.getDafoPasswordUserByUsername(USERNAME)).thenReturn(mockPasswordUser(VALID_USER_STATUS));

        mockMvc.perform(get(correctPasswordURI))
                .andExpect(status().isOk())
                // An assertion is expected to have the xmlns for SAML2 assertions somewhere
                .andExpect(content().string(decodedAndInflatedContains(
                        "urn:oasis:names:tc:SAML:2.0:assertion"
                )))
                // And we expect the username to be somewhere in the token as well
                .andExpect(content().string(decodedAndInflatedContains(USERNAME)));
    }

    @Test
    public void rejectWrongUsernameAndPassword() throws Exception {
        when(dbManager.getDafoPasswordUserByUsername(USERNAME)).thenReturn(mockPasswordUser(VALID_USER_STATUS));

        mockMvc.perform(get(wrongPasswordURI))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void rejectInactiveUsers() throws Exception {
        when(dbManager.getDafoPasswordUserByUsername(USERNAME)).thenReturn(mockPasswordUser(INVALID_USER_STATUS));

        mockMvc.perform(get(correctPasswordURI)).andExpect(status().isForbidden());
    }

    @Test
    public void tokenContainsUserProfiles() throws Exception {
        String[] userProfiles = new String[] {"MockUserProfile1", "MockUserProfile2"};
        when(dbManager.getUserProfiles(MOCK_USERID)).thenReturn(new ArrayList<String>(Arrays.asList(userProfiles)));
        when(dbManager.getDafoPasswordUserByUsername(USERNAME)).thenReturn(mockPasswordUser(VALID_USER_STATUS));

        mockMvc.perform(get(correctPasswordURI))
                .andExpect(status().isOk())
                .andExpect(content().string(decodedAndInflatedContains(">" + userProfiles[0] + "<")))
                .andExpect(content().string(decodedAndInflatedContains(">" + userProfiles[1] + "<")));

    }

    @Test
    public void issueTokenByBootstrapToken() throws Exception {
        // Do not actually verify the token, just parse it so we can get the username
        when(dafoAssertionVerifier.parseAssertion(anyString())).thenCallRealMethod();
        Assertion assertion = dafoAssertionVerifier.parseAssertion(BOOTSTRAP_TOKEN);
        when(dafoAssertionVerifier.verifyAssertion(
                anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class))
        ).thenReturn(assertion);
        when(dbManager.getDafoPasswordUserByUsername(anyString())).thenReturn(mockPasswordUser(VALID_USER_STATUS));

        mockMvc.perform(get("/get_token_passive?bootstrap_token=" + BOOTSTRAP_TOKEN))
                .andExpect(status().isOk())
                // An assertion is expected to have the xmlns for SAML2 assertions somewhere
                .andExpect(content().string(decodedAndInflatedContains(
                        "urn:oasis:names:tc:SAML:2.0:assertion"
                )))
                // And we expect the username to be somewhere in the token as well
                .andExpect(content().string(decodedAndInflatedContains(USERNAME)));
    }

}
