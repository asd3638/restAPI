package restAPI.restAPI.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import restAPI.restAPI.RestApiApplication;
import restAPI.restAPI.common.BaseTest;
import restAPI.restAPI.common.TestDescription;
import restAPI.restAPI.domian.Account;
import restAPI.restAPI.domian.AccountRole;
import restAPI.restAPI.repository.EventRepository;
import restAPI.restAPI.service.AccountService;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RestApiApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class AuthServerConfigTest {

    @Autowired
    AccountService accountService;

    @Autowired
    protected MockMvc mockMvc;

    @Test
    @TestDescription("인증 토큰을 발급받는 테스트")
    public void getAuthToken() throws Exception {

        String username = "jihye@email.com";
        String password = "jihye";
        Account jihye = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        accountService.saveAccount(jihye);

        String clientId = "myApp";
        String clientSecret = "pass";

        this.mockMvc.perform(post("/oauth/token")
        .with(httpBasic(clientId, clientSecret))
        .param("username", username)
        .param("password", password)
        .param("grant_type", "password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists());
    }

}
