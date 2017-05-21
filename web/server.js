var express = require("express");
var bodyParser = require("body-parser");
var path = require("path");
var async = require("async");

var routes = require("./routes");
var config = require("./config");
var registerHandler = require("./registerHandler");
var validator = require("./validator");
var brandModel = require("./models/brand");
var productModel = require("./models/product");

var server = express();
server.use(bodyParser.json());
server.use(express.static(path.join(__dirname, routes.STATIC_RESOURCES)));

server.get(routes.HOME, function(req, res) {
   res.sendFile(path.join(__dirname + routes.VIEW_PRODUCTS));
});

server.get(routes.REGISTER, function(req, res) {
    res.sendFile(path.join(__dirname + routes.VIEW_REGISTER));
});

server.get(routes.PRODUCTS, function(req, res) {
    res.sendFile(path.join(__dirname + routes.VIEW_PRODUCTS));
});

server.post(routes.API_REGISTER, function(req, res) {
    var shopFromRequest = req.body;
    var pin;
    if (validator.validateShop(shopFromRequest)) {
        brandModel.findOne({"name" : shopFromRequest.shopName}, function(err, brand) {
            if (err) {
                res.status(200).json({status: "0"});
            } else {
                if (brand) {
                    pin = brand.pin;
                    registerHandler.register(shopFromRequest, function(status) {
                        if (status == 0) {
                            res.status(200).json({status: "0"});
                        } else {
                            res.status(200).json({status: "1", pin: pin});
                        }
                    })
                } else {
                    pin = Math.floor(Math.random() * (9999 - 1000 + 1)) + 1000;
                    var newBrand = new brandModel({
                        name: shopFromRequest.shopName,
                        pin: pin
                    });
                    newBrand.save(function(err) {
                        if (err) {
                            res.status(200).json({status: "0"});
                        } else {
                            registerHandler.register(shopFromRequest, function(status) {
                                if (status == 0) {
                                    res.status(200).json({status: "0"});
                                } else {
                                    res.status(200).json({status: "1", pin: pin});
                                }
                            })
                        }
                    })
                }
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

server.post(routes.API_PRODUCTS, function(req, res) {
    var products = req.body.shopProducts;
    var shopName = req.body.shopName;
    var shopPin = req.body.shopPin;
    var promotionDateFrom = new Date(req.body.shopPromotionFrom);
    var promotionDateTo = new Date(req.body.shopPromotionTo);
    if (promotionDateFrom > promotionDateTo || products == null || promotionDateTo < new Date()) {
        res.status(200).json({status: "0"});
        return;
    }
    brandModel.findOne({"name" : shopName}, function(err, brand) {
        if (err) {
            res.status(200).json({status: "0"});
            return;
        }
        if (brand.pin != shopPin) {
            res.status(200).json({status: "0"});
            return;
        }
        products = JSON.parse(products);
        products = products.products;
        var productsAmount = products.length;
        async.forEach(products, function(product, callback) {
            console.log(product);
            if (product.name && product.size && product.price) {
                var newProduct = new productModel({
                    shop: shopName,
                    name: product.name,
                    size: product.size,
                    price: product.price,
                    from: promotionDateFrom,
                    to: promotionDateTo
                });
                newProduct.save(function(err) {
                    if (!err) {
                        productsAmount--;
                        callback();
                    }
                });
            }
        }, function(err) {
            if (err) {
                res.status(200).json({status: "0"});
                return;
            }
            if (productsAmount == 0) {
                res.status(200).json({status: "1"});
            } else {
                res.status(200).json({status: "0"});
            }
        });
    });
});

server.listen(config.PORT, function() {
    console.log("Geoshopper server listening on port " + config.PORT);
});