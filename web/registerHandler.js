var shopModel = require("./models/shop");
var geolocalizer = require("./geolocalizer");
var registerHandler = {};

registerHandler.register = function(shopFromRequest, callback) {
    geolocalizer.getCoords(shopFromRequest.shopCity, shopFromRequest.shopStreet, function(coords) {
        if (coords == null) {
            callback(0)
        } else {
            var shop = new shopModel({
                name: shopFromRequest.shopName,
                city: shopFromRequest.shopCity,
                street: shopFromRequest.shopStreet,
                latitude: coords.latitude,
                longitude: coords.longitude
            });
            shop.save(function (err) {
                if (err) {
                    callback(0)
                } else {
                    console.log("Successfully registered shop " + shopFromRequest.shopName);
                    callback(1);
                }
            });
        }
    });
};

module.exports = registerHandler;