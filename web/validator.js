var validator = {};

validator.validateShopName = function(name) {
    return (name && name.length > 2);
};

validator.validateShopCity = function(city) {
    if (!city || city.length < 3) {
        return false;
    }
    if (/\d/.test(city)) {
        return false;
    }
    return true;
};

validator.validateShopStreet = function(street) {
    if (!street || street.length < 3) {
        return false;
    }

    if (!/[a-zA-Z]/.test(street)) {
        return false;
    }
    return true;
};

validator.validateShop = function(shop) {
    if (!validator.validateShopName(shop.shopName)) {
        return false;
    }
    if (!validator.validateShopCity(shop.shopCity)) {
        return false;
    }
    if (!validator.validateShopStreet(shop.shopStreet)) {
        return false;
    }
    return true;
};

module.exports = validator;