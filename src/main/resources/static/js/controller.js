/**
 * Created by Samima on 1/22/2017.
 */

var cartApp = angular.module("cartApp", []);

cartApp.controller("cartCtrl", function ($scope, $http) {

    $scope.refreshCart = function () {
        $http.get('/rest/cart/'+$scope.cartId).success(function (data) {
            $scope.shoppingCart = data;
            $scope.shoppingCart.shippingCost = $scope.calShipping(data);
        });
    };

    $scope.clearCart = function () {
        $http.delete('/rest/cart/'+$scope.cartId).success($scope.refreshCart());

    };

    $scope.initCartId = function (cartId) {
        $scope.cartId = cartId;
        $scope.refreshCart(cartId);
    };

    $scope.addToCart = function (productId) {
        $http.put('/rest/cart/add/'+productId).success(function () {
            $(function()
            {
                $('.overlay_'+productId).show();
                setTimeout(function() {
                    $('.overlay_'+productId).fadeOut('fast');
                }, 3000);
            });
        });
    };
    ////
    $scope.updateCartItem = function (cartItemId) {
    	$('.updateCartitem').hide();
    	$('.spinnerspin').ploading({
    	    action: 'show', 
    	    spinner: 'wave'
    	  })
    	var qty = $('#'+cartItemId).val();
        $http.put('/shoppingCart/updateCartItem/'+cartItemId+'/'+qty).success(function (data) {
        	$scope.shoppingCart = data;
        	 $scope.shoppingCart.shippingCost = $scope.calShipping(data);
             $('.updateCartitem').show();
             $('.spinnerspin').ploading({
            	    action: 'hide'
            })
        });
    };
////
    $scope.removeFromCart = function (cartItemId) {
    	
        $http.post('/rest/cart/remove/'+cartItemId).success(function (data) {
        	$scope.shoppingCart = data;
       	 	$scope.shoppingCart.shippingCost = $scope.calShipping(data);
            
        });
    };


   
    
    $scope.calShipping = function (data) {
    	$scope.shoppingCart = data;
        var shippingTotal = $scope.shoppingCart.shippingCost;
        if (shippingTotal == 0){
        	shippingTotal = 'FREE';
        }
        return shippingTotal;

    };
    
   
    
    
    //Promo code logic
    $scope.applyPromo = function () {
    	
    	  var id = $('#cartId').val();
	      var promocode = $('#enterPromoCode').val();
	      if(promocode ==""){
	    	  	$('#applyPromoError').hide();
				$('.applyPromoError').html("Please enter a valid promo code");
				//$('#applyPromoError').css('display','inline-block');
			}else{
				$('.applyPromoError').html("");
				$('.spinnerspin').ploading({
		    	    action: 'show', 
		    	    spinner: 'wave'
		    	  })
		        $http.put('/shoppingCart/applyPromoCode/'+id+'/'+promocode,{ ajax : 'true'}).success(function (data) {
		        	$scope.shoppingCart = data;
		        	$scope.shoppingCart.shippingCost = $scope.calShipping(data);
		        	 if($scope.shoppingCart.errors == null){
		        		 $('.applyPromoError').html('<p class="text-success"><span>You saved $</span><span>'+$scope.shoppingCart.discountedAmount.toFixed(2)+'</span> using promo code <span>'+$scope.shoppingCart.promoCode.toUpperCase()+'</span>');
		        		 $('#applyPromoNow').hide();
                		 $('#removePromoNow').show();
		        		 $("#enterPromoCode").prop("disabled",true);
		        	 }
		        	$('.spinnerspin').ploading({
		        	    action: 'hide'
		        })
		        $('#applyPromoError').show();
		           /* $(function()
		            {
		            //    $('#applyPromoError').show();
		                setTimeout(function() {
		                    $('#applyPromoError').fadeOut('fast');
		                }, 3000);
		            });*/
		        	
		            
		        });
    }
    };

    $scope.removePromo = function () {
    	var cart = $('#cartId').val();
    	var bagId =$('#bagId').val();
    	$('.applyPromoError').html("");
		$('.spinnerspin').ploading({
    	    action: 'show', 
    	    spinner: 'wave'
    	  })
        $http.post('/shoppingCart/removePromoCode/'+cart+'/'+bagId).success(function (data) {
        	$scope.shoppingCart = data;
        	$scope.shoppingCart.shippingCost = $scope.calShipping(data);
        	$("#enterPromoCode").val("");
        	$("#enterPromoCode").prop("disabled",false);
        	$('#removePromoNow').hide();
        	$('#applyPromoNow').show();
        	$('.spinnerspin').ploading({
        	    action: 'hide'
        	})
   		 	
          //  $scope.refreshCart();
        });
    };
    
    
    

});

/*if($('input[name=stitching]:checked').val() != "readytowear"){
	  size : "unstiched";
}else{
	  size : $('#size').val();
},*/
$('.shopping-cart').mouseenter(function(){
    $('.b-content').show(); 
 });
 $('.shopping-cart').mouseleave(function(){
    $('.b-content').hide(); 
 });
 $('.b-content').mouseenter(function(){
	    $('.b-content').show(); 
	 });
	 $('.b-content').mouseleave(function(){
	    $('.b-content').hide(); 
	 });
 
$(document).ready(function(){
	$.get("/rest/cart/mycart", {
        ajax : 'true'
    }, function(data) {
    	//console.log(data);
    	 var item =0;
    	 var items="";
    	 if(data==""){
    		 $('.b-content').hide(); 
    		 $('.b-content').html('<p class="text-center"><span>Your cart is empty</span></p>');
    	 }else{
    	for (i = 0; i < data.cartItemList.length; i++) {
           item += data.cartItemList[i].qty;
           items += '<tr class="border-bottom"><td><img class="img-responsive product-shelf" style="width:70px;" src="https://s3.us-east-2.amazonaws.com/kolystyle/'+data.cartItemList[i].product.coverImageName+'" /></td><td><span class="col-md-12"><strong>'+data.cartItemList[i].product.title+'</strong></span><span class="col-md-12">Size: '+data.cartItemList[i].productSize+' | '+data.cartItemList[i].qty+' x $'+data.cartItemList[i].product.ourPrice+'</span></td><td> $'+data.cartItemList[i].subtotal+'</td></tr>';
       	
        }
    	   
    	$('.count').html(item+' items');
    	$('.amount').html('$'+data.orderTotal);
    	$('#cart-item-list').html(items);	
    	$('.mini-Subtotal').html('$'+data.grandTotal);
    	$('.mini-Discount').html('$'+data.discountedAmount);
    	$('.mini-Shipping').html('$'+data.shippingCost);
    	$('.mini-Ordertotal').html('$'+data.orderTotal);
    	 }
    });
});
$(function() {
	var sizes = "";
	var option = "";
	$('#addthisproduct').click(
	        function() {
	        	option = $('#stitching').val();
	        	
	        	if(option != "readytowear"){
	        		  sizes = "unstiched";
	        	}else{
	        		  sizes = $('#size').val();
	        	}	
	        	
	            $.post("/shoppingCart/addItem", {
	            	id : $('#id').val(),
	            	size : sizes,
	                qty : $('#qty').val(),
	                ajax : 'true'
	            }, function(data) {
	                var html = '';
	                var len = data.length;
	                if(len != 0){
	              //  console.log(data);
	                var cartListing = '';
	                var i;
	                var item =0;
	                var items="";
	                for (i = 0; i < data.cartItemList.length; i++) {
	                    cartListing += '<tr><th scope="row">'+data.cartItemList[i].product.id+'</th><td>'+data.cartItemList[i].product.title+'</td><td>'+data.cartItemList[i].productSize+'</td><td>'+data.cartItemList[i].qty+'</td></tr>';
	                    item += data.cartItemList[i].qty;
	                    items += '<tr class="border-bottom"><td><img class="img-responsive product-shelf" style="width:70px;" src="https://s3.us-east-2.amazonaws.com/kolystyle/'+data.cartItemList[i].product.coverImageName+'" /></td><td><span class="col-md-12"><strong>'+data.cartItemList[i].product.title+'</strong></span><span class="col-md-12">Size: '+data.cartItemList[i].productSize+' | '+data.cartItemList[i].qty+' x $'+data.cartItemList[i].product.ourPrice+'</span></td><td> $'+data.cartItemList[i].subtotal+'</td></tr>';
	                	
	                }
	                $('.modal-title').show();
	                $('.modal-title').html('Item Added To Your Bag');
	                $('#modal-body').hide();
	                $('#mod-table').show();
	                $('#modal-table').html(cartListing);
	                $('#mymodal').modal('show');	                
	                
	                //Update mini cart
	                $('.count').html(item+' items');
	            	$('.amount').html('$'+data.orderTotal);
	            	$('#cart-item-list').html(items);	
	            	$('.mini-Subtotal').html('$'+data.grandTotal);
	            	$('.mini-Discount').html('$'+data.discountedAmount);
	            	$('.mini-Shipping').html('$'+data.shippingCost);
	            	$('.mini-Ordertotal').html('$'+data.orderTotal);
	                
	                }else{	
	                	$('.modal-title').show();
	                	$('.modal-title').html('Item cannot be added to your Bag');	
	                	$('#modal-body').show();
		                $('#modal-body').html('<i class="fa fa-exclamation-circle text-danger" aria-hidden="true"></i> <span class="text-danger">Not Enough Quantity.</span>');
		                $('#mod-table').hide();
		                $('#mymodal').modal('show');
	                }                
	            });
	            return false;
	        });
  });