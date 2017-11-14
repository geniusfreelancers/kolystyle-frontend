/**
 * Created by Samima on 1/22/2017.
 */

var checkoutApp = angular.module("checkoutApp", []);

checkoutApp.controller("checkoutCtrl", function ($scope, $http) {

    $scope.refreshCheck = function () {
        $http.get('/rest/shipping/'+$scope.cartId).success(function (data) {
            $scope.shoppingCart = data;
        });
    };

    $scope.clearCart = function () {
        $http.delete('/rest/shipping/'+$scope.cartId).success($scope.refreshCart());

    };

    $scope.initCartId = function (cartId) {
        $scope.cartId = cartId;
        $scope.refreshCheck(cartId);
    };

    $scope.addToCart = function (productId) {
        $http.put('/rest/shipping/add/'+productId).success(function () {
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
        $http.put('/rest/shipping/remove/'+productId).success(function (data) {
            $scope.refreshCheck();
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
