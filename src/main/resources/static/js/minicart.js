var miniCartApp = angular.module("miniCartApp", []);

miniCartApp.controller("miniCartCtrl", function ($scope, $http) {

    $scope.refreshCheck = function () {
        $http.get('/rest/cart/mycart').success(function (data) {
        	data.errors = $scope.calSaving(data);
        	data.shippingCost = $scope.calShippingCost(data);
            $scope.miniCart = data;
        });
    };

    $scope.initCartId = function () {
    	//if(cartId != null){
    		//$scope.cartId = cartId;
    		$scope.refreshCheck();
    	//}  
    };

    $scope.calShippingCost = function (data) {
    	data.errors = $scope.calSaving(data);
        $scope.miniCart = data;
        var shippingCost = $scope.miniCart.shippingCost;
        
        if (shippingCost == 0){
        	shippingCost = 'FREE';
        }else{
        	shippingCost = "$"+shippingCost;
        }
        return shippingCost;
    };
    
    $scope.calSaving = function (data) {
    	$scope.miniCart = data;
    	var saving = 0;
    	var listPrice = 0;
    	var ourPrice =0;
    	var discountAmount = $scope.miniCart.discountedAmount
        for (var i=0; i<$scope.miniCart.cartItemList.length; i++){
         		listPrice += $scope.miniCart.cartItemList[i].product.listPrice;
         		ourPrice += $scope.miniCart.cartItemList[i].product.ourPrice;
        }
        saving = (listPrice-ourPrice)+discountAmount;
        return saving;
    };

});