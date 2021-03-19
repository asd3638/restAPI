package restAPI.restAPI.config;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import restAPI.restAPI.domian.Account;
import restAPI.restAPI.domian.AccountRole;
import restAPI.restAPI.service.AccountService;

import java.util.Set;

@Configuration
public class AppConfig {

    //얘는 의존성 주입은 아니고 그냥 빈 등록만 해준다.
    //어디선가 autowired 로 선언해서 등록하기 애매한 애들은 config 파일 안에서 수동 등록해줄 수 있다.
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return new ApplicationRunner() {

            @Autowired
            AccountService accountService;

            @Override
            public void run(ApplicationArguments args) throws Exception {
                Account jihye = Account.builder()
                        .email("jihye@naver.com")
                        .password("123")
                        .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                        .build();
                accountService.saveAccount(jihye);
            }
        };
    }

}
