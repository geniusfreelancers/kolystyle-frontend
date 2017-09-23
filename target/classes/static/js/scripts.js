/**
 * 
 */
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
    	    $(".id-"+id).html("Added to cart").show();
    	    $(".id-"+id).fadeOut(10000);
    	    //  alert("Submitted"+id);
    	    }
    	  });
    	  return false;
    });
  });



$(function(){
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
});

