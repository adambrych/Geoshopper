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
var shopModel = require("./models/shop");
var geolocalizer = require("./geolocalizer");

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
    var query = req.query;
    if (query && query.latitude && query.longitude > 0) {
        shopModel.find({}, function(err, shops) {
            if (err) {
                res.status(200).json([]);
            } else {
                var coords = {};
                coords.latitude = query.latitude;
                coords.longitude = query.longitude;
                var shopsNearby = geolocalizer.getShopsNearby(coords, shops);
                res.status(200).json(shopsNearby);
            }
        })
    } else {
        res.status(200).json([]);
    }
});

server.post(routes.API_SHOPS, function(req, res) {
    var productsList = req.body.products;
    var type = req.body.type;
    var coords = req.body.coords;
    var resultList = [];
    if (!productsList || productsList.length == 0 || !coords) {
        res.status(200).json([]);
        return;
    }
    if (type == "CHEAPEST") {
        var shops = [];
        async.forEach(productsList, function(product, callback) {
            productModel.findOne().where({name: product.name, size: product.size}).sort("price").exec(function(err, product) {
                if (!shops[product.shop]) {
                    shops[product.shop] = [];
                }
                var obj = {
                    name: product.name,
                    size: product.size,
                    price: product.price
                };
                shops[product.shop].push(obj);
                callback();
            });
        }, function(err) {
            if (!err) {
                async.forEach(Object.keys(shops), function(shop, callback) {
                    shopModel.find({name: shop}, function (err, shopsFromDb) {
                        var result = geolocalizer.getNearest(coords, shopsFromDb);
                        resultList.push({
                            name: shop,
                            city: result.shop.city,
                            street: result.shop.street,
                            coords: result.coords,
                            products: shops[shop]
                        });
                        callback();
                    });
                }, function(err) {
                    if (err) {
                        res.status(200).json([]);
                        return;
                    }
                    console.log(resultList);
                    res.status(200).json(resultList);
                });
            } else {
                res.status(200).json([]);
            }
        });
    } else if (type == "SHORTEST") {
        var products = [];
        async.forEach(productsList, function(product, callback) {
            productModel.find().where({name: product.name, size: product.size}).exec(function(err, productsFromDb) {
                for (var i = 0; i < productsFromDb.length; i++) {
                    var product = productsFromDb[i];
                    var key = product.name + ";" + product.size;
                    if (!products[key]) {
                        products[key] = [];
                    }
                    if (products[key].indexOf(product.shop) === -1) {
                        products[key].push(product.shop);
                    }
                }
                callback();
            });
        }, function(err) {
            if (!err) {
                async.forEach(Object.keys(products), function(productKey, callback) {
                    var temp = productKey.split(';');
                    var productName = temp[0];
                    var productSize = temp[1];
                    shopModel.find({ name: { "$in" : products[productKey]} }, function(err, shops) {
                        var result = geolocalizer.getNearest(coords, shops);
                        var missing = true;
                        for (var s in resultList) {
                            if (resultList[s].name == result.shop.name) {
                                missing = false;
                                break;
                            }
                        }
                        if (missing) {
                            resultList.push({
                                name: result.shop.name,
                                city: result.shop.city,
                                street: result.shop.street,
                                coords: result.coords,
                                products: []
                            });
                        }
                        for (var s in resultList) {
                            if (resultList[s].name == result.shop.name) {
                                productModel.findOne({shop: result.shop.name, name: productName, size: productSize}, function(err, product) {
                                    resultList[s].products.push({name: productName, size: productSize, price: product.price});
                                    callback();
                                });
                            }
                        }
                    });
                }, function(err) {
                    if (err) {
                        res.status(200).json([]);
                        return;
                    }
                    res.status(200).json(resultList);
                });
            } else {
                res.status(200).json([]);
            }
        });
    }
});

server.get(routes.API_PRODUCTS, function(req, res) {
    var query = req.query;
    if (query && query.product && query.product.length > 0) {
        var product = query.product;
        productModel.aggregate([
            {
                $match: {
                    "name": {$regex: new RegExp("^" + product, "i")},
                    "from": {$lte: new Date()},
                    "to": {$gte: new Date()}
                }
            },
            {
                $group: {
                    "_id": {"name": "$name", "size": "$size"},
                    name: { $first: "$name" },
                    size: { $first: "$size" }
                }
            },
            { $project : {
                _id : 0,
                name : 1,
                size: 1
            }}
        ]).exec(function(err, products) {
            if (!err) {
                res.status(200).json(products);
            } else {
                console.log(err);
            }
        });
    } else {
        res.status(200).json([]);
    }
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