$().ready(function(){
	$( "#payment-form" ).validate({
        rules: {
        	firstName:"required",
        	lastName:"required",
        	shippingAddressStreet1: "required",
        	shippingAddressState: "required",
        	shippingAddressZipcode: "required",
            email: {
                required: true,
                email:true
            },
            phoneNumber:{
            	required:true,
            	minlength:10
            },
            shippingAddressCity:"required"
        },
        messages:{
        	firstName:"First Name Required",
        	lastName:"Last Name Required",
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
            phoneNumber:{
            	required: "Phone Number Required",
            	minlength: "Invalid Phone Number"
            },
            shippingAddressCity:"City Required"
        }
    });
});


$('.tab').click(function(){
    var a=$(this).attr('id');
    $('#sample').html('Active Tab is'+a);
});

