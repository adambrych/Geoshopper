var express = require("express");
var bodyParser = require("body-parser");
var path = require("path");

var routes = require("./routes");
var config = require("./config");
var geolocalizer = require("./geolocalizer");
var validator = require("./validator");
var shopModel = require("./models/shop");

var server = express();
server.use(bodyParser.json());
server.use(express.static(path.join(__dirname, routes.STATIC_RESOURCES)));

server.get(routes.HOME, function(req, res) {
   res.sendFile(path.join(__dirname + routes.VIEW_REGISTER));
});

server.post(routes.API_REGISTER, function(req, res) {
    var shopFromRequest = req.body;
    if (validator.validateShop(shopFromRequest)) {
        geolocalizer.getCoords(shopFromRequest.shopCity, shopFromRequest.shopStreet, function(coords) {
           if (coords == null) {
               res.status(200).json({status: "0"});
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
                       res.status(200).json({status: "0"});
                   } else {
                       console.log("Successfully registered shop " + shopFromRequest.shopName);
                       res.status(200).json({status: "1"});
                   }
               });
           }
        });
    } else {
        res.status(200).json({status: "0"});
    }
});

server.get(routes.API_SHOPS, function(req, res) {
    shopModel.find(function(err, shops) {
        if (err) { return next(err) }
        res.json(shops);
    })
});

server.listen(config.PORT, function() {
    console.log("Geoshopper server listening on port " + config.PORT);
});