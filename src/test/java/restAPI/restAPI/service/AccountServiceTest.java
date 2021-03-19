package restAPI.restAPI.service;

import org.aspectj.bridge.Message;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.platform.engine.support.discovery.SelectorResolver;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import restAPI.restAPI.RestApiApplication;
import restAPI.restAPI.domian.Account;
import restAPI.restAPI.domian.AccountRole;
import restAPI.restAPI.repository.AccountRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = RestApiApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class AccountServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void findByUserName() throws Exception{

        //given
        String username = "jihye@naver.com";
        String password = "jihye";
        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        accountService.saveAccount(account);
        //when
        UserDetailsService userDetailsService = (UserDetailsService) accountService;
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        //then
        assertThat(passwordEncoder.matches(password, userDetails.getPassword()));

    }

    @Test
    public void findByUserNameFail() throws Exception{
        //어떤 오류가 예상되는지 먼저 적어줘야 한다.
        String username = "random@naver.com";
        expectedException.expect(UsernameNotFoundException.class);
        expectedException.expectMessage(Matchers.containsString(username));
        //given
        accountService.loadUserByUsername(username);
        //when

        //then

    }

}