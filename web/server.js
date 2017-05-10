var express = require("express");
var bodyParser = require("body-parser");
var path = require("path");

var routes = require("./routes");
var config = require("./config");
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
    console.log(shopFromRequest.shopName);
    if (validator.validateShop(shopFromRequest)) {
        var shop = new shopModel({
            name: shopFromRequest.shopName,
            city: shopFromRequest.shopCity,
            street: shopFromRequest.shopStreet,
            latitude: 123,
            longitude: 456
        });
        shop.save(function (err) {
           if (err) {
               res.status(200).json({status: "0"});
           } else {
               res.status(200).json({status: "1"});
           }
        });
    } else {
        res.status(200).json({status: "0"});
    }
});

server.listen(config.PORT, function() {
    console.log("Geoshopper server listening on port " + config.PORT);
});