package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;


@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@TestPropertySource(locations={"classpath:/application_test.properties"})
public class UserTest {

  public static int TEST_USERPROFILE_ID = -42;

  @MockitoSpyBean
  private UserQueryManager userQueryManager;

  @MockitoSpyBean
  private DafoEngineUserManager dafoEngineUserManager;

  @MockitoSpyBean
  private TokenVerifier tokenVerifier;

  @Test
  public void testUnconfiguredDatabase() throws InvalidTokenException {
    String tokenData = "lVdbl6I6E333V7icR1c3ICDimvZ8IKgooIAX8A0hXBRBSZDLrz9ot07bcznzvZFKZdeuSp"
        + "HsfP+"
        + "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"
        + "4EbzpvH0szzyewo6+Mn8s+VTytiXd9jgXw==";

    // Skip date checks on the token
    doNothing().when(tokenVerifier).verifyTokenAge(any());
    doReturn(true).when(tokenVerifier).checkNotOnOrafter(any());

    // Mock up userprofile info instead of fetching it from the database
    doReturn(TEST_USERPROFILE_ID).when(userQueryManager).getUserProfileIdByName(any());
    doReturn(Arrays.asList("ReadDemoService")).when(userQueryManager).getSystemRoleNamesByUserProfileId(TEST_USERPROFILE_ID);
    doReturn(Arrays.asList(AreaRestriction.lookup("Demo:Cardinal directions:North"),
            AreaRestriction.lookup("Demo:Cardinal directions:East"))).when(userQueryManager).getAreaRestrictionsByUserProfileId(TEST_USERPROFILE_ID);

    SamlDafoUserDetails samlDafoUserDetails = dafoEngineUserManager.getSamlUserDetailsFromToken(
        tokenData
    );

    assertThat(samlDafoUserDetails.getUserProfiles()).isNotEmpty();
    assertThat(samlDafoUserDetails.getSystemRoles()).isNotEmpty();
    assertThat(samlDafoUserDetails.getAreaRestrictionsForRole("ReadDemoService")).isNotEmpty();
  }

}
