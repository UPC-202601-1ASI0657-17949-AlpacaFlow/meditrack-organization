package com.alpacaflow.meditrack.organization;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "authorization.jwt.secret=WriteHereYourSecretStringForTokenSigningCredentials"
})
class OrganizationApplicationTests {

    @Test
    void contextLoads() {
    }
}
