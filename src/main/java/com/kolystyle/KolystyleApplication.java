/*package com.kolystyle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class KolystyleApplication extends SpringBootServletInitializer {
	 
	 public static void main(String[] args) throws Exception {
	        SpringApplication.run(KolystyleApplication.class, args);
	    }
	 
	 @Override
	 protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
	        return builder.sources(KolystyleApplication.class);
	    }

}*/
package com.kolystyle;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KolystyleApplication implements CommandLineRunner  {
	 
	 public static void main(String[] args) throws Exception {
	        SpringApplication.run(KolystyleApplication.class, args);
	    }
	 
	
	 @Override
		public void run(String... args) throws Exception{
			
			
		}

}