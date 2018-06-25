var miniCartApp = angular.module("miniCartApp", []);

miniCartApp.controller("miniCartCtrl", function ($scope, $http) {

    $scope.refreshCheck = function () {
        $http.get('/rest/cart/mycart').success(function (data) {
            $scope.miniCart = data;
            if(data == "" || data.cartItemQty < 1){
            	$('.count').html("0 item");
            	$('.amount').html("$0.00");
            	$('.mini-cart-total').hide();
            	$('.mini-cart-info').html("You have no items in your shopping bag.");
            }
        });
    };

    $scope.initCartId = function () {
    		$scope.refreshCheck();
    };
});