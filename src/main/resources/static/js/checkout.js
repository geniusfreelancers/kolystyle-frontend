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
    
    $scope.calSaving = function () {
    	var saving = 0;
    	var listPrice = 0;
    	var ourPrice =0;
    	var discountAmount = $scope.checkOut.discountedAmount
        for (var i=0; i<$scope.checkOut.cartItemList.length; i++){
         		listPrice += $scope.checkOut.cartItemList[i].product.listPrice;
         		ourPrice += $scope.checkOut.cartItemList[i].product.ourPrice;
        }
        saving = (listPrice-ourPrice)+discountAmount;
        return saving;
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

