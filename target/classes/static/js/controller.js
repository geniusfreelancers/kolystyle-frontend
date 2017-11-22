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

    $scope.removeFromCart = function (productId) {
        $http.put('/rest/cart/remove/'+productId).success(function (data) {
            $scope.refreshCart();
        });
    };


    $scope.calGrandTotal = function () {
        var grandTotal = 0;

        for (var i=0; i<$scope.shoppingCart.cartItemList.length; i++){
            grandTotal += $scope.shoppingCart.cartItemList[i].subtotal;
        }

        return grandTotal;

    };
    
    $scope.calPromoDiscount = function () {
        /*var promoDiscount = 0;
        if($scope.shoppingCart.discountedAmount == null){
        	promoDiscount = $scope.shoppingCart.grandTotal - $scope.shoppingCart.discountedAmount;
        }else{
        	promoDiscount = 'FREE';
        }
        return promoDiscount;*/
    	return $scope.shoppingCart.discountedAmount;

    };
    
    $scope.calOrderTotal = function () {
        var orderTotal = 0;
       /* if($scope.shoppingCart.discountedAmount =! null){
        	orderTotal = $scope.shoppingCart.discountedAmount + $scope.shoppingCart.shippingCost;
        }else{
        	orderTotal = $scope.calGrandTotal() + $scope.shoppingCart.shippingCost;
        }*/
        
        orderTotal = $scope.calGrandTotal() + $scope.shoppingCart.shippingCost;
        return orderTotal;

    };

});


$(function() {
	$('#addthisproduct').click(
	        function() {
	            $.post("/shoppingCart/addItem", {
	            	id : $('#id').val(),
	                size : $('#size').val(),
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