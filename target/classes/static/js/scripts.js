/**
 * 
 */
//Apply Promo
//Add New Category
$(function() {
	$('#applyPromoNow').click(
	        function() {
	            $.post("/shoppingCart/applyPromoCode", {
	            	id : $('#cartId').val(),
	            	promocode : $('#enterPromoCode').val(),
	                ajax : 'true'
	            }, function(data) {
	                var htmls = '';
	              	var discount = 0;
	              	var shipping =0;
	              	var total =0;
	              	var code = $('#enterPromoCode').val();
		                if(data.promoCode != null){
		                	if(data.promoCode == code){
		                		 $('#applyPromoNow').hide();
		                		 $('#removePromoNow').show();
		                		 $("#enterPromoCode").prop("disabled",true);
		                		htmls += '<p class="text-success"><span>You saved $</span><span>'+data.discountedAmount+'</span> using <span>'+data.promoCode+'</span>';
		                	
		                	}else{
		                		htmls += '<p style="color:red">'+code+' is not a valid code</p>'
		                	}
		                	discount = data.discountedAmount;
		                    shipping = data.shippingCost;
		                    total = data.orderTotal;
		                }else{
		                	htmls += '<p style="color:red">'+code+' is not a valid code</p>';
		                	discount = data.discountedAmount;
		                    shipping = data.shippingCost;
		                    total = data.orderTotal;
		                }
		                
		                    
		            $('#applyPromoError').show();
	                $('#applyPromoError').html(htmls);
	                $('#shippingcost').html(shipping);
	                $('#discountamount').html(discount.toFixed(2));
	                $('#ordertotal').html(total.toFixed(2));
	                
	            });
	            return false;
	        });
  });

$(document).ready(function(){
	var code = $('#enterPromoCode').val();
	if(code !=""){
		 $('#applyPromoNow').hide();
		 $('#removePromoNow').show();
		 $("#enterPromoCode").prop("disabled",true);
	}
});

//Remove Promo
$(function() {
	$('#removePromoNow').click(
	        function() {
	        	var cart = $('#cartId').val();
	        	var bagId =$('#bagId').val();
	            $.post("/shoppingCart/removePromoCode/"+cart+"/"+bagId, {
	            	cartId : $('#cartId').val(),
	            	bagId : $('#bagId').val(),
	            	//promocode : $('#enterPromoCode').val(),
	                ajax : 'true'
	            }, function(data) {
	                var htmls = '';
	              	var discount = 0;
	              	var shipping =0;
	              	var total =0;
	              	var code = $('#enterPromoCode').val();
	              	if($.isEmptyObject(data)){
	              		
	              		htmls += '<p style="color:red">Something is not right</p>'
	              	}else{
	              		discount = data.discountedAmount;
	                    shipping = data.shippingCost;
	                    total = data.orderTotal;
	              		$('#removePromoNow').hide();
	              		$("#enterPromoCode").val("");
	              		$("#enterPromoCode").prop("disabled",false);	
	              	}
	                        
		            $('#applyPromoError').show();
	                $('#applyPromoError').html(htmls);
	                $('#shippingcost').html(shipping);
	                $('#discountamount').html(discount.toFixed(2));
	                $('#ordertotal').html(total.toFixed(2));
	                
	                
	            });
	            return false;
	        });
  });
//Update Mini Cart Category.

$(function() {
	$('#cart-total').click(
	        function() {
	            $.getJSON("/shoppingCart/minicart", {
	               // id : $(this).val(),
	            	id : 2,
	                ajax : 'true'
	            }, function(data) {
	                var html = '';
	                var htmls ='';
	                var len = data.length;
	                if(len < 1 ){
	                	html += '<td>You have no items in your shopping bag.</td>';
	                }else{
	                for ( var i = 0; i < len; i++) {
	                	
	                	 html +='<tr><td class="image"><a href="/productDetail?id='+data[i].product.id+'"><img src="http://localhost:8089/adminportal/image/product/'+data[i].product.id+'/'+data[i].product.coverImageName+'" alt="'+data[i].product.title+'" title="'+data[i].product.title+'" /></a></td><td class="name"><a href="/productDetail?id='+data[i].product.id+'">'+data[i].product.title+'</a></td><td class="quantity">x&nbsp;'+data[i].qty+'</td><td class="total">$'+data[i].product.ourPrice * data[i].qty+'</td></tr>';
	                	 
	                
	                }	
	                if(data[len-1].shoppingCart ==null){
	                	htmls= '<tr><td class="right"><b>Your Bag Total:</b></td><td class="right">$'+data[len-1].guestShoppingCart.grandTotal+'</td></tr>';
	                }else{
	                	htmls= '<tr><td class="right"><b>Your Bag Total:</b></td><td class="right">$'+data[len-1].shoppingCart.grandTotal+'</td></tr>';
	                	
	                }
	                var checkBtn = '<a class="button" href="/shoppingCart/cart">Checkout</a>';
	                }    
	                $('#cart-item-list').html(html);
	                $('#mini-cart-total').html(htmls);
	                $('#mini-cart-checkout').html(checkBtn);
	                });
	                
	                
	               
	            });
	        });
 


function checkBillingAddress(){
	if($("#theSameAsShippingAddress").is(":checked")){
		$(".billingAddress").prop("disabled",true);
	}else{
		$(".billingAddress").prop("disabled",false);
	}
}

function checkPasswordMatch(){
	var password = $("#txtNewPassword").val();
	var currentPassword = $("#currentPassword").val();
	var confirmPassword = $("#txtConfirmPassword").val();
	var userEmail = $("#userEmail").val();
	var email = $("#email").val();
	
	if(email != userEmail){
		$("#emailError").attr('class', 'text-primary');
		$("#emailError").html("<i class='fa fa-info-circle' aria-hidden='true'></i> Please enter curent password to change email");
		if(currentPassword != ''){
			$("#currentPassword").removeClass('input-error')
			$("#updateUserInfoButton").prop('disabled',false);	
		}else{
			$("#updateUserInfoButton").prop('disabled',true);
			$("#currentPassword").addClass('input-error');
		}
		
	}else if(password == "" && confirmPassword == ""){
		$("#checkPasswordMatch").html("");
		$("#updateUserInfoButton").prop('disabled',false);
	}else{
		if(password.length < 6){
			$("#checkPasswordMatch").html("Passwords must be atleast 6 characters!");
			$("#PasswordMatched").hide();
			$("#checkPasswordMatch").show();
			$("#updateUserInfoButton").prop('disabled',true);
		}else if(password != confirmPassword){
			$("#PasswordMatched").hide();
			$("#checkPasswordMatch").show();
			$("#checkPasswordMatch").html("Passwords do not match!");
			$("#updateUserInfoButton").prop('disabled',true);
		}else{
			$("#checkPasswordMatch").hide();
			$("#PasswordMatched").show();
			$("#PasswordMatched").html("<i class='fa fa-check-circle' aria-hidden='true'></i> Passwords matched");
			$("#updateUserInfoButton").prop('disabled',false);
		}
	}
}
function enableButton(){
	$('#saveNow').css('display','inline-block');	
}
$(document).ready(function(){
	$("#saveNow").hide();
});
$(document).ready(function(){
$(".cartItemQty").on('change',function(){
	var id=this.id;
	if(this.value!=''){
	$('#update-item-'+id).css('display','inline-block');
	}else{
	$('#update-item-'+id).css('display','none');
	}
});
});

$(document).ready(function(){
$("#theSameAsShippingAddress").on('click',checkBillingAddress);
	$("#txtConfirmPassword").keyup(checkPasswordMatch);
	$("#txtNewPassword").keyup(checkPasswordMatch);
	$("#email").keyup(checkPasswordMatch);
	$("#currentPassword").keyup(checkPasswordMatch);
});

//Validate Promo
$(document).ready(function(){
	$('#enterPromoCode').keyup(function(){
		var id=this.id;
		if(this.value!=''){
			$('#applyPromoNow').css('display','inline-block');
			}else{
			$('#applyPromoNow').css('display','none');
			}	
		$('#applyPromoError').css('display','none');
	});
	$('#enterPromoCode').on('focusout',function(){
		
		if(this.value!=''){
			$('#applyPromoNow').css('display','inline-block');
			}else{
			$('#applyPromoNow').css('display','none');
			}
	});
	
	
});

//Add Product to WishList
$(function() {
    $(".addingtowishlist").click(function() {
    	var id=this.id;
      // validate and process form here
    	var ids = $("#id-"+id).val();
    	
    	var dataString = 'id='+ ids;
    	$.ajax({
    	    type: "POST",
    	    url: "/customer/addtolist?id="+ids,
    	    data: dataString,
    	    success: function() {
    	    $(".id-"+id).html("<a href='/customer/wishlist'>Added to Wishlist</a>").show();
    	    $(".id-"+id).fadeOut(10000);
    	    //  alert("Submitted"+id);
    	    }
    	  });
    	  return false;
    });
  });




//Add Product to cart
$(function() {
    $(".cart").click(function() {
    	var id=this.id;
      // validate and process form here
    	var ids = $("#id-"+id).val();
    	var qty = 1;
    	var dataString = 'id='+ ids + '&qty=' + qty;
    	$.ajax({
    	    type: "POST",
    	    url: "/shoppingCart/addItem",
    	    data: dataString,
    	    success: function() {
    	    $(".id-"+id).html("<a href='/shoppingCart/cart' class='addedtocart'>Added to cart</a>").show();
    	    $(".id-"+id).fadeOut(10000);
    	    //  alert("Submitted"+id);
    	    }
    	  });
    	  return false;
    });
  });

//Display or hide SIZE dropdown on product details page
$(document).ready(function(){
	$(".sizecontainer").hide();
});


$(document).ready(function(){
	$("#stitching").on('change',function(){
		var id=this.value;
		if(this.value != 'unstiched'){	
			$('.sizecontainer').css('display','block');
		}else{
			$('.sizecontainer').css('display','none');
		}
	});
	});

$(document).ready(function(){
	$("#readytowear").on('change',function(){
		var id=this.value;
		if(this.value != 'readytowear'){	
			$('.sizecontainer').css('display','none');
		}else{
			$('.sizecontainer').css('display','block');
		}
	});
	});

/*$(function(){
	$('#applyPromoNow').click(function(){
		var promocode = $('#enterPromoCode').val();
		if(promocode ==""){
			$('#applyPromoError').html("Please enter a valid promo code");
			$('#applyPromoError').css('display','inline-block');
		}else{
			$.ajax({
				type:"POST",
				url:"/shoppingCart/applyPromoCode",
				data:promocode,
				success:function(){
					$('#applyPromoError').html("Promo Code Applied");
					$('#applyPromoError').css('display','inline-block');
				},
				error: function(e){
					$('#applyPromoError').html("Promo Code Applied Failed");

					$('#applyPromoError').css('display','inline-block');
				}
			});
		}
	});
});*/

