package com.kolystyle;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import com.braintreegateway.BraintreeGateway;
import com.kolystyle.domain.User;
import com.kolystyle.domain.security.Role;
import com.kolystyle.domain.security.UserRole;
import com.kolystyle.service.UserService;
import com.kolystyle.utility.SecurityUtility;

@SpringBootApplication
public class KolystyleApplication implements CommandLineRunner {

	public static String DEFAULT_CONFIG_FILENAME = "config.properties";
    public static BraintreeGateway gateway;
    
	@Autowired
	private UserService userService;
	
	
	public static void main(String[] args) {

/*		String PATH = "src/main/resources/";
	    
		String folderName =  PATH.concat(DEFAULT_CONFIG_FILENAME);
		System.out.println(folderName);
		
		File configFile = new File(folderName);
		System.out.println(configFile);
*/
		
		File configFile = new File(DEFAULT_CONFIG_FILENAME);

        try {
        	
        	/*gateway = new BraintreeGateway(
          		  "sandbox",
          		  "hw92n9y8n4f4mqmj",
          		  "87ztjfwf97f9587k",
          		  "2333de127f1aedd0af27b84f7f2f4dcd"
          		);
        	System.out.println("Braintree API used From hardcode value Env");*/
            if(configFile.exists() && !configFile.isDirectory()) {
                gateway = BraintreeGatewayFactory.fromConfigFile(configFile);

                System.out.println("Braintree API use From Config File");
            } else {
                gateway = BraintreeGatewayFactory.fromConfigMapping(System.getenv());
                System.out.println("Braintree configuration from SYSTEM ENVIRONMENT loaded");

            }
        } catch (NullPointerException e) {
            System.err.println("Could not load Braintree configuration from config file or system environment.");
            System.exit(1);
        }
		SpringApplication.run(KolystyleApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception{
		User user1 = new User();
		user1.setFirstName("John");
		user1.setLastName("Adams");
		user1.setUsername("j");
		user1.setPassword(SecurityUtility.passwordEncoder().encode("p"));
		user1.setEmail("deepakkhanal190@gmail.com");
		Set<UserRole> userRoles = new HashSet<>();
		Role role1 = new Role();
		role1.setRoleId(1);
		role1.setName("ROLE_USER");
		userRoles.add(new UserRole(user1, role1));
		
		userService.createUser(user1, userRoles);
		
	}
}