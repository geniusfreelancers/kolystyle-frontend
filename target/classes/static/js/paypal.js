/*	var CREATE_PAYMENT_URL  = 'http://localhost:8083/paybypaypal';
    var EXECUTE_PAYMENT_URL = 'http://localhost:8083/executepayment';
    paypal.Button.render({

        env: 'sandbox', // sandbox | production

        // PayPal Client IDs - replace with your own
        // Create a PayPal app: https://developer.paypal.com/developer/applications/create
        client: {
            sandbox:    'AbTOmn8uV0EThC4P4W6VAxQBKqj6IrHKS7eu4KHci7l2a5TENaruOVVznYdn4aFhdYXeMVjrJO-WrPYK',
            production: 'AXDiy6MenxmZLxzqhfl4YFBpLdORV677hacEd8FGGjONLMcUU8KQ3MeBXoZAbExqK5naY13rXXlr9zSX'
        },

        // Show the buyer a 'Pay Now' button in the checkout flow
        commit: true,

        // payment() is called when the button is clicked
        payment: function() {
            return paypal.request.post(CREATE_PAYMENT_URL).then(function(data) {
                return data.id;
            });
        },

        payment: function () {

            // Set up a url on your server to create the payment
            var CREATE_URL = CREATE_PAYMENT_URL;

            // Make a call to your server to set up the payment
            return paypal.request.post(CREATE_URL)
                .then(function (res) {
                    console.log("res");
                    console.log(res);//this is always empty
                    return res.paymentID;
                }).catch(function (err) {
                    reject("err");
                    reject(err);
                });
        },
        onAuthorize: function (data, actions) {

            // Set up a url on your server to execute the payment
            var EXECUTE_URL = EXECUTE_PAYMENT_URL;

            // Set up the data you need to pass to your server
            var data = {
                paymentID: data.paymentID,
                payerID: data.payerID
            };
            console.log(data);
            // Make a call to your server to execute the payment
            return paypal.request.post(EXECUTE_URL, data)
                .then(function (res) {
                    window.alert('Payment Complete!');
                    console.log(res);
                });
        }
        
        onAuthorize: function(data) {
            return paypal.request.post(EXECUTE_PAYMENT_URL, {
                paymentID: data.paymentID,
                payerID:   data.payerID
            }).then(function() {
            	window.alert('Payment Complete!');
                // The payment is complete!
                // You can now show a confirmation message to the customer
            });
        }
 		},'#paypal-button');*/


/*paypal.Button.render({

        env: 'sandbox', // Or 'sandbox'
        style: {
            label: 'paypal',
            size:  'medium',    // small | medium | large | responsive
            shape: 'rect',     // pill | rect
            color: 'blue',     // gold | blue | silver | black
            tagline: false    
        },

        client: {
            sandbox:    'AbTOmn8uV0EThC4P4W6VAxQBKqj6IrHKS7eu4KHci7l2a5TENaruOVVznYdn4aFhdYXeMVjrJO-WrPYK',
            production: 'AXDiy6MenxmZLxzqhfl4YFBpLdORV677hacEd8FGGjONLMcUU8KQ3MeBXoZAbExqK5naY13rXXlr9zSX'
        },

        commit: true, // Show a 'Pay Now' button

        payment: function(data, actions) {
            return actions.payment.create({
                payment: {
                    transactions: [
                        {
                            amount: { total: '150.95', currency: 'USD' }
                        }
                    ]
                }
            });
        },

        onAuthorize: function(data, actions) {
            return actions.payment.execute().then(function(payment) {

                // The payment is complete!
                // You can now show a confirmation message to the customer
            	console.log("Paypal Payment Successful");
            	alert("Paypal Payment Successful");
            });
        },
        onCancel: function(data, actions) {
             
             * Buyer cancelled the payment 
             
        	console.log("Buyer cancelled the payment");
        	alert("Buyer cancelled the payment");
        },

        onError: function(err) {
             
             * An error occurred during the transaction 
             
        	console.log("An error occurred during the transaction");
        	alert("An error occurred during the transaction");
        }

    }, '#paypal-button');*/

        // Render the PayPal button

        paypal.Button.render({

            // Pass in the Braintree SDK

            braintree: braintree,

            // Pass in your Braintree authorization key

            client: {
            	sandbox:    'AbTOmn8uV0EThC4P4W6VAxQBKqj6IrHKS7eu4KHci7l2a5TENaruOVVznYdn4aFhdYXeMVjrJO-WrPYK',
                production: 'AXDiy6MenxmZLxzqhfl4YFBpLdORV677hacEd8FGGjONLMcUU8KQ3MeBXoZAbExqK5naY13rXXlr9zSX'
            },

            // Set your environment

            env: 'sandbox', // sandbox | production

            // Wait for the PayPal button to be clicked

            payment: function(data, actions) {

                // Make a call to create the payment

                return actions.payment.create({
                    payment: {
                        transactions: [
                            {
                                amount: { total: '0.01', currency: 'USD' }
                            }
                        ]
                    }
                });
            },

            // Wait for the payment to be authorized by the customer

            onAuthorize: function(data, actions) {

                // Call your server with data.nonce to finalize the payment

                console.log('Braintree nonce:', data.nonce);

                // Get the payment and buyer details

                return actions.payment.get().then(function(payment) {
                    console.log('Payment details:', payment);
                });
            }

        }, '#paypal-button');