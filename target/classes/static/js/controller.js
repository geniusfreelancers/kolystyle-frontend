/**
 * Created by Samima on 1/22/2017.
 */

var cartApp = angular.module("cartApp", []);

cartApp.controller("cartCtrl", function ($scope, $http) {

    $scope.refreshCart = function () {
        $http.get('/realtorsNepal/rest/cart/'+$scope.cartId).success(function (data) {
            $scope.cart = data;
        });
    };

    $scope.clearCart = function () {
        $http.delete('/realtorsNepal/rest/cart/'+$scope.cartId).success($scope.refreshCart());

    };

    $scope.initCartId = function (cartId) {
        $scope.cartId = cartId;
        $scope.refreshCart(cartId);
    };

    $scope.addToCart = function (productId) {
        $http.put('/realtorsNepal/rest/cart/add/'+productId).success(function () {
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
        $http.put('/realtorsNepal/rest/cart/remove/'+productId).success(function (data) {
            $scope.refreshCart();
        });
    };

    $scope.applyPromoCode = function () {
        var promocode = angular.element('#promocode').val();
        if(promocode !=""){
        $http.put('/realtorsNepal/rest/cart/applypromo/33/'+promocode).success(function (data) {
            $scope.refreshCart();
        });
        }
    };

    $scope.calGrandTotal = function () {
        var grandTotal = 0;

        for (var i=0; i<$scope.cart.cartItems.length; i++){
            grandTotal += $scope.cart.cartItems[i].totalPrice;
        }

        return grandTotal;

    };

});
