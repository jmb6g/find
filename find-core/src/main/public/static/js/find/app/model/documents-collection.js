/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection',
    'find/app/model/document-model',
    'underscore'
], function(FindBaseCollection, DocumentModel, _) {

    return FindBaseCollection.extend({
        model: DocumentModel,
        url: '../api/public/search/query-text-index/results',

        autoCorrection: null,
        totalResults: null,
        warnings: null,

        fetch: function(options) {
            this.autoCorrection = null;
            this.totalResults = null;
            this.warnings = null;

            return FindBaseCollection.prototype.fetch.call(this, options);
        },

        parse: function(response) {
            this.autoCorrection = response.autoCorrection;
            this.totalResults = response.totalResults;
            this.warnings = response.warnings;

            return response.documents;
        },

        getAutoCorrection: function() {
            return this.autoCorrection;
        }
    });

});
