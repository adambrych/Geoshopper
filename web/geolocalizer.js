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

module.exports = geolocalizer;