package dk.magenta.datafordeler.core.security;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.user.TokenVerifier;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;


@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TokenTest {

  private class TokenHeaderInjector implements ClientHttpRequestInterceptor {

    private String tokenString;

    public TokenHeaderInjector() {
      this.tokenString = "";
    }

    public String getTokenString() {
      return tokenString;
    }

    public void setTokenString(String tokenString) {
      this.tokenString = tokenString;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
        ClientHttpRequestExecution execution) throws IOException {
      if (tokenString != null && !tokenString.equals("")) {
        request.getHeaders().set("Authorization", "SAML " + tokenString);
      }

      return execution.execute(request, body);
    }
  }

  @Autowired
  private TestRestTemplate template;
  @MockitoSpyBean
  TokenVerifier tokenVerifier;
  private TokenHeaderInjector tokenHeaderInjector;

  @PostConstruct
  public void postContruct() {
    tokenHeaderInjector = new TokenHeaderInjector();
    template.getRestTemplate().setInterceptors(Collections.singletonList(tokenHeaderInjector));
  }


  @Test
  public void testRestTemplateSetup() {
    assertThat(this.template).isNotNull();
  }

  @Test
  public void testParseInvalidToken() throws Exception {
    // Make token string invalid
    tokenHeaderInjector.setTokenString("Invalid token string");
    ResponseEntity<String> result = template.getForEntity(
        "/demo/postnummer/1/rest/search?postnr=*",
        String.class
    );
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void testParseValidToken() throws Exception {
    // Skip checks for token age
    doNothing().when(tokenVerifier).verifyTokenAge(any());
    doReturn(true).when(tokenVerifier).checkNotOnOrafter(any());

    // Make token string invalid
    tokenHeaderInjector.setTokenString(
        "lVfZlqLKEn33K1z2o6sKEBVxdXkuo6KgAiriG0IyKIOSiQxff9Aq7bJ6OH3fzMjIHTsiw8jN93+KKGxeQAqDJ"
            + "H5rEa94qwliO3GC2HtrrVfiy6D1z6jxHVpR2BkyEIIU1Z5NCcIMSDFEVozeWh2coF7w/ksHXxHksEsM"
            + "O53XAU7sWs3NHblzRa5jxXB4w3prZWk8TCwYwGFsRQAOkT3UGUUe1p5D6x7ofqaAby0fodMQw/I8f83"
            + "J1yT1sA6OE9hWkXXbB5HVGn3QvJFLR7zlJi/6Sv+OPZm/O3CoB15soSwFH/DO7+BxDKex2seBgfet9T"
            + "gLHCl2k9uSs+IkDmwrDCrrylgByE+cJhN6SRogP/otbwK/Ar+Awn6xiW78rYU9U/trILx7Z/gSJSn4l"
            + "kLrBfpWp9f/gNSAC9L6VkFzrUlvrfc0VqkVQzdJI/i8/M+ATyUB8QWEyQk4L/DO+yPo3wP+ohSj78Ae"
            + "SrEdZjC4gPm1P06WDWBzmQI3KOQA1l1XwHt3APv/qjL2md6X5Xs1+MADEP3lDTwVpC488VGBd5CNFWZ"
            + "gxDsQBkLum9tOcHJsb3YK05jGGf8weLsR+Ox8Mzwu7X35peseXfJ+giC46mCvjM5pXEXsyj11+DBVt9"
            + "L6bHEpTYy1qhecwmwaR6U2tzL74omk4evg7BiZTIxjjpO34vbY5s+2CxMrNi69cl2VNE+q2oEMACvFb"
            + "d4ZTDhd9Bll6YQlwk+8YtrHsFi6jlwhNN0y2oxUFV6mPWql9P3NqS74KvSmTm4P1B7nCJIoCi6IheCw"
            + "9NGMmCIh8nEs4NPTebnuaYSsU8pBOLbz8Bz1aduI5bnXl9x4Mj3L1cRcm/6BogoeJ6pLthmv6B7u7o"
            + "86vRgf6MiLqx2P2QruqavZRY1WVp+EeHFi8u55Qm6JabAolvq62E6Wh03UMTp8nze2+4J2C6ShfSCJ"
            + "ayMKBpfdebHYs5ornAXv7e1R+k+1vpZ/BsrHVWx7OM1byHosuOvocuuRgMBIkSSuXXEcAxYek0ss40"
            + "lTZqa4okxNuELcKQw+5vTzWJf2JK8KLJevGYVVC+HAqKw337CMonBGcTE7ImxYY7pyeAaIOV7MKwZX"
            + "Dut8sVKr+cqxalv5blMett8BXXEaDl+z0qWcV83pLNlJ/sWeMzUDVmV4zxOWzHVfTbj6N8ss1I5VFq"
            + "68xfeG4AuhMSBjA4Yz9iCb5qBo4BeinV5oH41hmhh2eMlomPKU1jszNEXvJ3ixJV0J13cy45URZS7k"
            + "vTBWjIOxm/QMsotmazbv+GwmWM60wSzydXcytngxh5i7ysLQrHg+UNfkXA9jKc19PTKm5sFbk/T8OM"
            + "7wc8IeT1RBpVbg4H5+Xhy7+KK30fQObFDn/nbblvbVyu+JZeLgU6Sng8DaZ/IYdQrD2c/NwSFMKPao"
            + "BDkovZjECLh0qrUamWRaJqtY7h26Bj5Lpg3pfM6664mmUYJBa50SlXq6R6zf623sRJK5PVsu2122nQ"
            + "zofrbS2hUGdp4o8xIwSNkqT2MZHnOKJBZTjGrghKOE8yPH5ALDWHNVEbr5RDX5jYYvWNYUxIVxrJbW"
            + "Qd2JPNA08TiPNZMucTzU6psdjBli7Ui5aiqsxTT+6MzkN2chV0Wl7i7GHXy9dfnj1llmZkcNE9rsz"
            + "jcPAjIquwcdY+xcpIE3LT3TF/XdNGtX7RWt5X0ddGXWnTLLbpBZZsovIhZDGpx0mfCSC70pqWuN9h"
            + "HrnHZdbS7LyWlSee1JyC/jY76dno94Oy39BZ/WpykoHHWvz3JC4gvT3eXQXUwySyhMr6ooTR+TXZm"
            + "OGz25j1wqvJSKsJ2N0cVHpWIp3XDCLXtVBe0LIfXOvaVFhbBQic2g2CjWrioFvnCJesCFh7ZFioy2"
            + "xY7+pdF358w5dycBcjaUa6yT+aoSrUyLPVffbKnNpAiP0S5cbsgUTeR6Hs/X7EoLg9heEh06nibiv"
            + "IxnbZin+KbR3wubQ5gH872yp876yswwMuHohXRe7z1uRaG+8j5Nvk6Ih/F9hmCfp8vT9LmrGz3bH4"
            + "CN7svr4yjxTbF+uyz0e1VFvBI3S+C8uDfXYRbDE7BrGsBpNa8oalaLmHqZvrVuoM7D0BpZUf0T/K9"
            + "WZZfABpmzLx2QvnrhXVu9s/hCkUtiN7jGqmXRH7auiTfnCVrEi5Rx0TX+s6Ds/RCU19f7DzGwX5ao"
            + "9nGCqwO8hmFBXQDwJ9H6l1zu8EzmBNeHWqsf8DSwP2d73xpdhQOslYNT51qXDbszfTh8NTyBYV/z+"
            + "IGP/FhHdR9FIEbN2/K/VfnT6RoUgQL9ysaFtQ6v+370R6luD+2rX23+1FE/0vkF3K82n42PlB60UF"
            + "2NfYbA73duPfwu0T5XuiYXRBDL6tY9pYkbhKD109Hbw/740gj++1PjJbgV2Qa1AoXBEJUncNWjw+u"
            + "dxV5rxDPioqk//Vke+T0FHf1k/tnyKWXsy3fY6F8="
    );
    ResponseEntity<String> result = template.getForEntity(
        "/demo/postnummer/1/rest/search?postnr=*",
        String.class
    );

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}
