define([
    'backbone'
], function(Backbone) {

    return Backbone.Collection.extend({
        url: '../api/parametric',

        comparator: 'name',

        sync: function(method, model, options) {
            options = options || {};
            options.traditional = true; // Force "traditional" serialization of query parameters, e.g. index=foo&index=bar, for IOD multi-index support.

            return Backbone.Collection.prototype.sync.call(this, method, model, options);
        },

        model: Backbone.Model.extend({
            idAttribute: 'name',

            parse: function(response, options) {
                return {
                    name: response.name,
                    values: _.sortBy(response.values, function (value) {
                        return -1 * value.count;
                    })
                }
            },

            defaults: {
                values: []
            }
        })
    });

});