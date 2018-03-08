package de.hw4.binance.marketmaker;


import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import de.hw4.binance.marketmaker.persistence.Authority;
import de.hw4.binance.marketmaker.persistence.AuthorityRepository;
import de.hw4.binance.marketmaker.persistence.User;
import de.hw4.binance.marketmaker.persistence.UserRepository;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private AuthorityRepository authorityRepo;
	
	@Value("${db.sa.password}")
	private String defaultSaPwd;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/register", "/img/**", "/css/**", "/js/**", "/*.js").permitAll().anyRequest().authenticated().and()
				.formLogin().loginPage("/login").permitAll().and().logout().permitAll();
		
		http.authorizeRequests().antMatchers("/public/**").permitAll();

		// for h2 console
		http.authorizeRequests().antMatchers("/console/**").hasRole("SA_ROLE");
		http.csrf().ignoringAntMatchers("/console/**");
		http.headers().frameOptions().disable();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		
		User sa = userRepo.findOne("sa");
		if (sa == null) {
			sa = new User("sa");
			sa.setPassword(defaultSaPwd);
			sa.setEnabled(true);
			userRepo.save(sa);
			
			Authority authority = new Authority("sa", "SA_ROLE");
			authorityRepo.save(authority);
			
		}

		auth.jdbcAuthentication().dataSource(dataSource)
			.usersByUsernameQuery("select username, password, enabled from user where username=?")
			.authoritiesByUsernameQuery("select username, authority from authority where username=?")
			.passwordEncoder(new BCryptPasswordEncoder());


		//auth.inMemoryAuthentication().withUser("sa").password("sapwd").roles("USER");
	}

}
