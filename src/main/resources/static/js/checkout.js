/**
 * Created by Samima on 1/22/2017.
 */

var checkoutApp = angular.module("checkoutApp", []);

checkoutApp.controller("checkoutCtrl", function ($scope, $http) {

    $scope.refreshCheck = function () {
        $http.get('/customize/minicart').success(function (data) {
        	data.errors = $scope.calSaving(data);
        	data.shippingCost = $scope.calShippingCost(data);
            $scope.checkOut = data;
        });
    };

    $scope.initCartId = function (cartId) {
        $scope.cartId = cartId;
        $scope.refreshCheck(cartId);
    };


    $scope.changeShippingMethod = function (shippingMethod) {
    	$('.spinnerspin').ploading({
    	    action: 'show', 
    	    spinner: 'wave'
    	  })
        $http.put('/customize/add/'+shippingMethod+'/'+$scope.cartId).success(function (data) {
        	data.errors = $scope.calSaving(data);
        	data.shippingCost = $scope.calShippingCost(data);
            $scope.checkOut = data;
            $('.spinnerspin').ploading({
        	    action: 'hide'
            })
        });
    };


    $scope.calShippingCost = function (data) {
    	data.errors = $scope.calSaving(data);
        $scope.checkOut = data;
        var shippingCost = $scope.checkOut.shippingCost;
        
        if (shippingCost == 0){
        	shippingCost = 'FREE';
        }else{
        	shippingCost = "$"+shippingCost;
        }
        return shippingCost;
    };
    
    $scope.calSaving = function (data) {
    	$scope.checkOut = data;
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
        $http.get('/customize/minicart').success(function (data) {
            $scope.miniCart = data;
        });
    };

    $scope.initCartId = function () {
        $scope.refreshCheck();
    };
    
    $scope.showOrNoshow = function () {
    	var itemList= false;
    	if ( $scope.miniCart==null){
    		console.log("No data");
    	}else{
    	var itemLists = $scope.miniCart.cartItemList.length;
        
        if (itemLists > 0){
        	itemList = true;
        }
    	}
        return itemList;
    };
});


/*checkoutApp.controller("miniCartCtrl", function ($scope, $http) {

    $scope.refreshCheck = function () {
        $http.get('/customize/'+$scope.cartId).success(function (data) {
            $scope.miniCart = data;
        });
    };

    $scope.initCartId = function (cartId) {
        $scope.cartId = cartId;
        $scope.refreshCheck(cartId);
    };
    
    $scope.showOrNoshow = function () {
    	var itemList= false;
    	var itemLists = $scope.miniCart.cartItemList.length;
        
        if (itemLists > 0){
        	itemList = true;
        }
        return itemList;
    };
});*/

