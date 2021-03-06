package com.kolystyle.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.kolystyle.service.impl.UserSecurityService;
import com.kolystyle.utility.SecurityUtility;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private Environment env;
	

	@Autowired
	private UserSecurityService userSecurityService;

	private BCryptPasswordEncoder passwordEncoder() {
		
		return SecurityUtility.passwordEncoder();
	}

	private static final String[] PUBLIC_MATCHERS = { "/css/**", "/js/**", "/image/**","/rest/cart/**", "/","/index", "/newUser","/forgetPassword","/forgotpassword","/login","/register","/fonts/**","/home","/productshelf","/productDetail","/public/**","/searchByCategory","/searchBySubCategory","/searchProduct","/shoppingCart/**","/paypal/**","/checkouts/**","/cart/guestcheckout","/customize/**","/chargeguest","/thankyou/**","/newsletter/**","/ordertracking/**" };

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().
		/* antMatchers("/**"). */
				antMatchers(PUBLIC_MATCHERS).permitAll().anyRequest().authenticated();

		http.csrf().disable().cors().disable().formLogin().failureUrl("/login?error").defaultSuccessUrl("/customer/account")
				.loginPage("/userlogin").permitAll().and().logout()
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/login?logout")
				.deleteCookies("remember-me").permitAll().and().rememberMe();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userSecurityService).passwordEncoder(passwordEncoder());
	}

}
