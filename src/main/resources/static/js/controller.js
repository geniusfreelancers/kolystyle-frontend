/**
 * Created by Samima on 1/22/2017.
 */

var cartApp = angular.module("cartApp", []);

cartApp.controller("cartCtrl", function ($scope, $http) {

    $scope.refreshCart = function () {
        $http.get('/rest/cart/'+$scope.cartId).success(function (data) {
            $scope.shoppingCart = data;
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
        $http.put('/shoppingCart/updateCartItem/'+cartItemId+'/'+qty).success(function () {
        	 
        	 $scope.calGrandTotal();
             $scope.calPromoDiscount();
             $scope.calShipping();
             $scope.calOrderTotal();
             $('.updateCartitem').show();
             $('.spinnerspin').ploading({
            	    action: 'hide'
            })
        });
    };
////
    $scope.removeFromCart = function (cartItemId) {
    	
        $http.post('/rest/cart/remove/'+cartItemId).success(function (data) {
            $scope.refreshCart();
            $scope.calGrandTotal();
            $scope.calPromoDiscount();
            $scope.calShipping();
            $scope.calOrderTotal();
            
        });
    };


    $scope.calGrandTotal = function () {
        var grandTotal = $scope.shoppingCart.grandTotal;
       /* for (var i=0; i<$scope.shoppingCart.cartItemList.length; i++){
            grandTotal += $scope.shoppingCart.cartItemList[i].subtotal;
        }*/
        return grandTotal;

    };
    
    $scope.calPromoDiscount = function () {
        var promoDiscount = $scope.shoppingCart.discountedAmount;
    	return promoDiscount;

    };
    
    $scope.calShipping = function () {
        var shippingTotal = $scope.shoppingCart.shippingCost;
        if (shippingTotal == 0){
        	shippingTotal = 'FREE';
        }
        return shippingTotal;

    };
    
    $scope.calOrderTotal = function () {
        var orderTotal = $scope.shoppingCart.orderTotal;
        return orderTotal;

    };
    
    
    //Promo code logic
    $scope.applyPromo = function (promoCode) {
    	$('.spinnerspin').ploading({
    	    action: 'show', 
    	    spinner: 'wave'
    	  })
        $http.put('/shoppingCart/applyPromoCode/'+promoCode).success(function () {
        	$('.spinnerspin').ploading({
        	    action: 'hide'
        })
            $(function()
            {
                $('#applyPromoError').show();
                setTimeout(function() {
                    $('#applyPromoError').fadeOut('fast');
                }, 3000);
            });
            
        });
    };

    $scope.removePromo = function () {
        $http.put('/shoppingCart/removePromoCode/'+$scope.cartId).success(function (data) {
            $scope.refreshCart();
        });
    };
    
    
    

});

/*if($('input[name=stitching]:checked').val() != "readytowear"){
	  size : "unstiched";
}else{
	  size : $('#size').val();
},*/

$(function() {
	var sizes = "";
	$('#addthisproduct').click(
	        function() {
	        	if($('input[name=stitching]:checked').val() != "readytowear"){
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
	                $('#serverRespone').show();
	                $('#serverRespone').html('<i class="fa fa-check" aria-hidden="true" style="color: forestgreen"></i> <span style="color: forestgreen;">Added to cart.</span>');	
	                }else{	
	                	$('#serverRespone').show();
		                $('#serverRespone').html('<i class="fa fa-exclamation-circle text-danger" aria-hidden="true"></i> <span class="text-danger">Not Enough Quantity.</span>');	
		             }                
	            });
	            return false;
	        });
  });