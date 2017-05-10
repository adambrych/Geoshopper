var mongoose = require('mongoose');

mongoose.Promise = global.Promise;
mongoose.connect('mongodb://localhost:27017/geoshopper', function () {
    console.log('Connected to mongodb.');
});

module.exports = mongoose;