var config = require("./config");
var request = require("request");
    
var geolocalizer = {};

geolocalizer.getCoords = function(city, street, callback) {
    var address = street + " " + city;
    address = encodeURIComponent(address);
    request("https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=" + config.GOOGLE_API_KEY, function(err, res, body) {
        if (err) {
            callback(null);
        } else {
            body = JSON.parse(body);
            if (body.status == "OK" && body.results.length > 0) {
                var geocodes = body.results[0].geometry;
                var coords = {};
                coords.latitude = geocodes.location.lat;
                coords.longitude = geocodes.location.lng;
                callback(coords);
            } else {
                callback(null);
            }
        }
    });
};

geolocalizer.getDistanceFromLatLonInKm = function(lat1,lon1,lat2,lon2) {
    var R = 6371; // Radius of the earth in km
    var dLat = geolocalizer.deg2rad(lat2-lat1);  // deg2rad below
    var dLon = geolocalizer.deg2rad(lon2-lon1);
    var a =
            Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(geolocalizer.deg2rad(lat1)) * Math.cos(geolocalizer.deg2rad(lat2)) *
            Math.sin(dLon/2) * Math.sin(dLon/2)
        ;
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    var d = R * c; // Distance in km
    return d;
};

geolocalizer.deg2rad = function(deg) {
    return deg * (Math.PI/180)
};

geolocalizer.getNearest = function(coords, shops) {
    var min = 1000000;
    var minCoords = {};
    for (var i = 0; i < shops.length; i++) {
        var shop = shops[i];
        var distance = geolocalizer.getDistanceFromLatLonInKm(coords.latitude, coords.longitude, shop.latitude, shop.longitude);
        if (distance < min) {
            min = distance;
            minCoords.latitude = shop.latitude;
            minCoords.longitude = shop.longitude;
        }
    }
    return minCoords;
};

geolocalizer.getShopsNearby = function(coords, shops) {
    var shopsNearby = [];
    for (var i = 0; i < shops.length; i++) {
        var shop = shops[i];
        var distance = geolocalizer.getDistanceFromLatLonInKm(coords.latitude, coords.longitude, shop.latitude, shop.longitude);
        console.log(distance);
        if (distance <= config.SHOP_NEARBY) {
            shopsNearby.push(shop);
        }
    }
    return shopsNearby;
};

module.exports = geolocalizer;