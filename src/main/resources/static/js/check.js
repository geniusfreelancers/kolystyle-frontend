$().ready(function(){
	$( "#payment-form" ).validate({
        rules: {
        	firstName:"required",
        	shippingAddressStreet1: "required",
        	shippingAddressState: "required",
        	shippingAddressZipcode: "required",
        	username:{
        		required: true,
        		minlength:5
        	},
            email: {
                required: true,
                email:true
            },
            password:{
            	required:true,
            	minlength:7
            	/*equalTo:"#password"*/
            },
            phone:{
            	required:true,
            	minlength:10
            },
            shippingAddressCity:"required"
        },
        messages:{
        	firstName:"First Name Required",
        	shippingAddressStreet1:"Street Address Required",
        	shippingAddressState:"State Required",
        	shippingAddressZipcode:"Zipcode Required",
        	username:{
        		required: "Username Required",
        		minlength:"Username must be atleast 5 characters"
        	},
        	email: {
                required: "Email Required",
            },
            password:{
            	required: "Password Required",
            	minlength: "Password must be atleast 7 characters"
            	/*equalTo:"Please enter same password"*/
            },
            phone:{
            	required: "Phone Number Required",
            	minlength: "Please enter a valid Phone Number"
            },
            shippingAddressCity:"City Required"
        }
    });
});