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
	console.log("fetching notifications since " + lastupdate);
	$.get( "/notifications.js?since=" + lastupdate, function( data, textStatus, jqxhr ) {
		  $("#notificationscript").replaceWith(data);
		  
	});
	if ( nrNotifications > 0 ) {
		if(window.Notification && Notification.permission !== "denied") {
			Notification.requestPermission(function(status) {  // status is "granted", if accepted by user
				for (let i = 0; i<nrNotifications; i++) {
					var t = notification.title[i];
					var m = notification.message[i];
					var icon = notification.icon[i];
					new Notification(t, { 
						body: m,
						icon: icon
					}); 
				}
			});
		}
	}
}

if(window.Notification && Notification.permission !== "denied") {
	Notification.requestPermission(function(status) {
		console.log("notifications allowed.");
		});
}

var winbuy;
var winsell;

function initSliders() {

	var slider1 = document.getElementById("winbuy");
	var output1 = document.getElementById("buypercentage");
	var slider2 = document.getElementById("winsell");
	var output2 = document.getElementById("sellpercentage");
	
	if (slider1) {
		winbuy = + parseFloat(slider1.value).toFixed(2);
		winsell = + parseFloat(slider2.value).toFixed(2);
		profit();
		
		output1.innerHTML = winbuy; // Display the default slider value
		output2.innerHTML = winsell; // Display the default slider value
		// Update the current slider value (each time you drag the slider handle)
		slider1.oninput = function() {
			winbuy = + parseFloat(this.value);
		    output1.innerHTML = winbuy.toFixed(2);
		    profit();
		}
		slider2.oninput = function() {
			winsell = + parseFloat(this.value);
		    output2.innerHTML = winsell.toFixed(2);
		    profit();
		}
	}
}

function setHourInterval() {
	interval = 'HOUR';
	$("#chartselect ul li.active").removeClass("active");
	$("#hint").addClass("active");
	getChart()
	setTimeout(drawChart, 1000);
	return false;
}
function set8HourInterval() {
	interval = 'EIGHTHOUR';
	$("#chartselect ul li.active").removeClass("active");
	$("#h8int").addClass("active");
	getChart()
	setTimeout(drawChart, 1000);
	return false;
}
function setDayInterval() {
	interval = 'DAY';
	$("#chartselect ul li.active").removeClass("active");
	$("#dint").addClass("active");
	getChart()
	setTimeout(drawChart, 1000);
	return false;
}
function setWeekInterval() {
	interval = 'WEEK';
	$("#chartselect ul li.active").removeClass("active");
	$("#wint").addClass("active");
	getChart()
	setTimeout(drawChart, 1000);
	return false;
}
function setMonthInterval() {
	interval = 'MONTH';
	$("#chartselect ul li.active").removeClass("active");
	$("#mint").addClass("active");
	getChart()
	setTimeout(drawChart, 1000);
	return false;
}

function set6MonthInterval() {
	interval = 'SIXMONTH';
	$("#chartselect ul li.active").removeClass("active");
	$("#m6int").addClass("active");
	getChart()
	setTimeout(drawChart, 1000);
	return false;
}

function drawChart() {
    console.log("drawing");
    var data = google.visualization.arrayToDataTable(	dataArr, true);
    var formatter = new google.visualization.NumberFormat({ 
  	  pattern: '#.#####', 
  	  fractionDigits: 7
  	});
    formatter.format(data, 1); // Apply formatter to first column.
    formatter.format(data, 2); 
    formatter.format(data, 3); 
    formatter.format(data, 4); 
    formatter.format(data, 5);
    if (chartWithLimit) {
    		formatter.format(data, 6);
	}
    var options = {
    		legend:'none',
    		bar: { groupWidth: '80%' }, // Remove space between bars.
    		crosshair: { trigger: 'focus' },
    		chartArea:{left:60,top:20,width:'90%',height:'90%'},
    		hAxis: {
    			minorGridlines: {count: 1},
    			showTextEvery: 12,
    		}, 
    		vAxis: {
    			minorGridlines: {count: 10},
    			showTextEvery: 10,
    			// format: '#.#####'
    		}, 
    		interpolateNulls: true,
    		series: chartWithLimitAndHistory ? seriesWithLimitAndHistory : (chartWithLimit ? seriesWithLimit : (chartWithHistory ? seriesWithHistory : seriesWithoutLimit)),
    		seriesType: "candlesticks",
    		tooltip: { 
    			trigger:'both',
    			format:'#.#####'
    		},
    		candlestick: {
    			fallingColor: { strokeWidth: 0, fill: '#a52714' }, // red
    			risingColor: { strokeWidth: 0, fill: '#0f9d58' }   // green
    		}
    };
    chart.draw(data, options);
}

var seriesWithHistory = {
		0: {visibleInLegend: false},
		1: { type: "line"},
		2: { type: "line", color: 'orange'}
	};

var seriesWithLimit = {
		0: {visibleInLegend: false},
		1: { type: "line"},
		2: { type: "line", color: 'blue'}
	};

var seriesWithLimitAndHistory = {
		0: {visibleInLegend: false},
		1: { type: "line"},
		2: { type: "line", color: 'blue'},
		3: { type: "line", color: 'orange'}
	};
var seriesWithoutLimit = {
		0: {visibleInLegend: false},
		1: { type: "line"}
	};

function profit() {
	var profitbuy = winbuy - (2 * fees);
	$("#profitbuy").html(profitbuy.toFixed(2));
	var profitsell = winsell - (2 * fees);
	$("#profitsell").html(profitsell.toFixed(2));
}

function adjustPrice() {
	if (buy) {
		var newPrice;
		var newQty = $("#input_quantity").val();
		newPrice = qty2 / newQty;
		$("#input_price").val(newPrice.toFixed(priceScale));
	}
}

function adjustQty() {
	tradePrice = $("#input_price").val();
	var newQty;
	if (buy) {
		newQty = qty2 / tradePrice; 
		$("#input_quantity").val(quantityScale(newQty, qtyScale));
	} else {
		newQty = quantity * tradePrice; 
		$("#quantity2").text(quantityScale(newQty, 5));
	}
	
	dataArr[0][6] = parseFloat(tradePrice);
	dataArr[99][6] = parseFloat(tradePrice);
	drawChart();
}

function quantityScale(qty, scale) {
	return Math.floor(qty * Math.pow(10, scale))/Math.pow(10,scale);
}

function sleep(ms) {
	  return new Promise(resolve => setTimeout(resolve, ms));
}

