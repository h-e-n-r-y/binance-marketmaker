function tradetick() {
	$.get( "/ticker.js?symbols=" + symbols, function( data, textStatus, jqxhr ) {
		  $("#tickerscript").replaceWith(data);
		  
	});
	console.log(symbols[0] + ": " + prices[0]);
	var oldPrice = $("#tickerprice").text();
	if (oldPrice > prices[0]) {
		$("#ticker").addClass("red")
		$("#ticker").removeClass("green")
	}
	if (oldPrice < prices[0]) {
		$("#ticker").addClass("green")
		$("#ticker").removeClass("red")
	}
	$("#tickerprice").text(prices[0]);
}

function indextick() {
	$.get( "/ticker.js?symbols=" + symbols, function( data, textStatus, jqxhr ) {
		  $("#tickerscript").replaceWith(data);
	});
	console.log(symbols + ": " + prices);
	for (let i in symbols) {
		var oldPrice = $("#price" + symbols[i]).text();
		if (oldPrice > prices[i]) {
			$("#price" + symbols[i]).addClass("red")
			$("#price" + symbols[i]).removeClass("green")
		}
		if (oldPrice < prices[i]) {
			$("#price" + symbols[i]).addClass("green")
			$("#price" + symbols[i]).removeClass("red")
		}
		$("#price" + symbols[i]).text(prices[i]);
	}	
}


var nrNotifications = 0;
function getNotifications() {
	$.get( "/notifications.js?since=" + lastupdate, function( data, textStatus, jqxhr ) {
		  $("#notificationscript").replaceWith(data);
		  
	});
	if ( nrNotifications > 0 ) {
		if(window.Notification && Notification.permission !== "denied") {
			for (i = 0; i<nrNotifications; i++) {
				var t = notification.title[i];
				var m = notification.message[i];
				Notification.requestPermission(function(status) {  // status is "granted", if accepted by user
					var n = new Notification(t, { 
						body: m,
						icon: '/path/to/icon.png' // optional
					}); 
				});
			}
		}
	}
}

if(window.Notification && Notification.permission !== "denied") {
	Notification.requestPermission(function(status) {
		console.log("notifications allowed.");
		});
}

function initSliders() {

	var slider1 = document.getElementById("winbuy");
	var output1 = document.getElementById("buypercentage");
	var slider2 = document.getElementById("winsell");
	var output2 = document.getElementById("sellpercentage");
	
	var winbuy;
	var winsell;
	
	if (slider1) {
		winbuy = slider1.value;
		winsell = slider2.value;
		
		output1.innerHTML = winbuy; // Display the default slider value
		output2.innerHTML = winsell; // Display the default slider value
		// Update the current slider value (each time you drag the slider handle)
		slider1.oninput = function() {
			winbuy = this.value;
		    output1.innerHTML = winbuy;
		}
		slider2.oninput = function() {
			winsell = this.value;
		    output2.innerHTML = winsell;
		}
	}
}

function sleep(ms) {
	  return new Promise(resolve => setTimeout(resolve, ms));
}