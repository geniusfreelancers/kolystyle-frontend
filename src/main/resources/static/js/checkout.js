/**
 * Created by Samima on 1/22/2017.
 */

var checkoutApp = angular.module("checkoutApp", []);

checkoutApp.controller("checkoutCtrl", function ($scope, $http) {

    $scope.refreshCheck = function () {
        $http.get('/customize/'+$scope.cartId).success(function (data) {
            $scope.checkOut = data;
        });
    };

    $scope.initCartId = function (cartId) {
        $scope.cartId = cartId;
        $scope.refreshCheck(cartId);
    };


    $scope.changeShippingMethod = function (shippingMethod) {
        $http.put('/customize/add/'+shippingMethod+'/'+$scope.cartId).success(function (data) {
            $scope.refreshCheck();
        });
    };


    $scope.calShippingCost = function () {
        var shippingCost = $scope.checkOut.shippingCost;
        
        if (shippingCost == 0){
        	shippingCost = 'FREE';
        }
        return shippingCost;
    };
    
/*    $scope.calOrderTotal = function () {
        var orderTotal = $scope.checkOut.orderTotal;
        return orderTotal;

    };*/

});


//Testing Some shitty ideas



checkoutApp.controller("miniCartCtrl", function ($scope, $http) {

    $scope.refreshCheck = function () {
        $http.get('/customize/'+$scope.cartId).success(function (data) {
            $scope.miniCart = data;
        });
    };

    $scope.initCartId = function (cartId) {
        $scope.cartId = cartId;
        $scope.refreshCheck(cartId);
    };
});

