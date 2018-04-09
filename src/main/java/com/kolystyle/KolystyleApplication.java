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

		/*String PATH = "src/main/resources/";
	    
		String folderName =  PATH.concat(DEFAULT_CONFIG_FILENAME);
		System.out.println(folderName);
		
		File configFile = new File(folderName);
		System.out.println(configFile);*/

		
		File configFile = new File(DEFAULT_CONFIG_FILENAME);

        try {
        	
        	gateway = new BraintreeGateway(
          		  "sandbox",
          		  "hw92n9y8n4f4mqmj",
          		  "87ztjfwf97f9587k",
          		  "2333de127f1aedd0af27b84f7f2f4dcd"
          		);
        	if(gateway != null) {
        		System.out.println("Braintree API used From hardcode value Env");
        	}else {
        		System.out.println("Braintree API Unsucessfull");
        	}
        	
           /* if(configFile.exists() && !configFile.isDirectory()) {
                gateway = BraintreeGatewayFactory.fromConfigFile(configFile);

                System.out.println("Braintree API use From Config File");
            } else {
                gateway = BraintreeGatewayFactory.fromConfigMapping(System.getenv());
                System.out.println("Braintree configuration from SYSTEM ENVIRONMENT loaded");

            }*/
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

/*package com.kolystyle;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.braintreegateway.Environment;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.CustomerRequest;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionOptionsRequest;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.ValidationError;
import com.braintreegateway.ValidationErrors;

@SpringBootApplication
public class KolystyleApplication implements CommandLineRunner {
	
	
	private static final Logger logger = LoggerFactory.getLogger(KolystyleApplication.class.getName());
	// Below are the Braintree sandbox credentials
	public static BraintreeGateway gateway = null;
	private static String publicKey = "5xy4v43ygg8xxyhp";
	private static String privateKey = "8e5958a0973f2b3d6c383c9f02402dab";
	private static String merchantId= "pdqt6nyrzthmy3jh"; 
    
	
	
	
	public static void main(String[] args) {

		 // Initialize Braintree Connection
	    gateway = connectBraintreeGateway();
	    braintreeProcessing();
		SpringApplication.run(KolystyleApplication.class, args);
	}
	
	public static void braintreeProcessing() {

	    System.out.println(" ----- BrainTree Implementation Starts --- ");

	    // Generate client Token
	    String clientToken = generateClientToken();
	    System.out.println(" Client Token : " +clientToken);

	    // Receive payment method nonce
	    String nonceFromTheClient = receivePaymentMethodNonce();

	    // Do payment transactions
	    BigDecimal amount = new BigDecimal("5.10");
	    doPaymentTransaction(nonceFromTheClient, amount);
	}

	// Connect to Braintree Gateway.
	public static BraintreeGateway connectBraintreeGateway() {
	    BraintreeGateway braintreeGateway = new BraintreeGateway(
	            Environment.SANDBOX, merchantId, publicKey, privateKey);
	    return braintreeGateway;
	}

	// Make an endpoint which return client token.
	public static String generateClientToken() {
	    // client token will be generated at server side and return to client
	    String clientToken = gateway.clientToken().generate();
	    return clientToken;
	}

	// Make an endpoint which receive payment method nonce from client and do payment.
	public static String receivePaymentMethodNonce() {
	     String nonceFromTheClient =  "fake-valid-mastercard-nonce";
	     return nonceFromTheClient;
	}

	// Make payment 
	public static void doPaymentTransaction(String paymentMethodNonce, BigDecimal amount) {

	    TransactionRequest request = new TransactionRequest();
	    request.amount(amount);
	    request.paymentMethodNonce(paymentMethodNonce);

	    CustomerRequest customerRequest = request.customer();
	    customerRequest.email("cpatel@gmail.com");
	    customerRequest.firstName("Chirag");
	    customerRequest.lastName("Patel");

	    TransactionOptionsRequest options = request.options();
	    options.submitForSettlement(true);

	    // Done the transaction request
	    options.done();

	    // Create transaction ...
	    Result<Transaction> result = gateway.transaction().sale(request);
	    boolean isSuccess = result.isSuccess();

	    if (isSuccess) {
	        Transaction transaction = result.getTarget();
	        displayTransactionInfo(transaction);
	    } else {
	        ValidationErrors errors = result.getErrors();
	        validationError(errors);
	    }
	}

	private static void displayTransactionInfo(Transaction transaction) {
	    System.out.println(" ------ Transaction Info ------ ");
	    System.out.println(" Transaction Id  : " +transaction.getId());
	    System.out.println(" Processor Response Text : " +transaction.getProcessorResponseText());
	}

	private static void validationError(ValidationErrors errors) {
	    List<ValidationError> error = errors.getAllDeepValidationErrors();
	    for (ValidationError er : error) {
	        System.out.println(" error code : " + er.getCode());
	        System.out.println(" error message  : " + er.getMessage());
	    }
	}

	@Override
	public void run(String... arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}
}*/