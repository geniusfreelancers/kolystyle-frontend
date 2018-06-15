//Countdown

 var expiryDate = document.getElementById("hotDealExpiry").value;
 var deadline = new Date(expiryDate).getTime();
var x = setInterval(function() {
 
var now = new Date().getTime();
var t = deadline - now;
var days = Math.floor(t / (1000 * 60 * 60 * 24));
var hours = Math.floor((t%(1000 * 60 * 60 * 24))/(1000 * 60 * 60));
var minutes = Math.floor((t % (1000 * 60 * 60)) / (1000 * 60));
var seconds = Math.floor((t % (1000 * 60)) / 1000);
document.getElementById("cdown_day").innerHTML =days ;
document.getElementById("cdown_hour").innerHTML =hours;
document.getElementById("cdown_minutes").innerHTML = minutes; 
document.getElementById("cdown_second").innerHTML =seconds; 
if (t < 0) {
        clearInterval(x);
        document.getElementById("hotDealProduct").innerHTML = "EXPIRED";
        }
}, 1000);
