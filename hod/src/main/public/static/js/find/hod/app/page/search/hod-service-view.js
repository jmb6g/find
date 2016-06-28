/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/page/search/service-view',
    'find/hod/app/page/search/results/hod-results-view-augmentation',
    'find/hod/app/page/search/results/hod-results-view',
    'find/app/util/model-any-changed-attribute-listener'
], function (_, ServiceView, ResultsViewAugmentation, ResultsView, addChangeListener) {

    'use strict';

    //noinspection JSUnusedGlobalSymbols
    return ServiceView.extend({
        ResultsView: ResultsView,
        ResultsViewAugmentation: ResultsViewAugmentation,

        // TODO: Enable sunburst in HOD when IOD-9173 is complete
        // TODO: When sunburst is enabled make this.fetchParametricFields also fetch the restrictedParametricCollection
        displayDependentParametricViews: false,
        mapViewResultsStep: 2500,
        mapViewAllowIncrement: false,

        initialize: function(options) {
            ServiceView.prototype.initialize.call(this, options);
            addChangeListener(this, this.queryModel, ['queryText', 'fieldText', 'minDate', 'maxDate', 'minScore', 'stateMatchIds'], this.fetchEntities);
            addChangeListener(this, this.queryModel, ['indexes'], _.bind(function () {
                this.fetchEntities();
                this.parametricFieldsCollection.reset();
                this.numericParametricFieldsCollection.reset();
                this.dateParametricFieldsCollection.reset();
                if (this.queryModel.get('indexes').length !== 0) {
                    this.fetchParametricFields(this.parametricFieldsCollection, this.parametricCollection);
                    this.fetchParametricFields(this.numericParametricFieldsCollection);
                    this.fetchParametricFields(this.dateParametricFieldsCollection);
                }
            }, this));
        },
        
        fetchParametricFields: function (fieldsCollection, valuesCollection) {
            if (this.queryModel.get('indexes').length > 0) {
                fieldsCollection.fetch({
                    data: {
                        databases: this.queryModel.get('indexes')
                    },
                    success: _.bind(function () {
                        if (valuesCollection) {
                            this.fetchParametricValues(fieldsCollection, valuesCollection);
                            // disabled until we support sunburst on HOD 
                            //this.fetchRestrictedParametricCollection();
                        }
                    }, this)
                });
            }
        },

        fetchParametricValues: function (fieldsCollection, valuesCollection) {
            valuesCollection.reset();

            if (this.queryModel.get('indexes').length !== 0) {
                var fieldNames = fieldsCollection.pluck('id');
                if (fieldNames.length > 0) {
                    valuesCollection.fetch({data: {
                        databases: this.queryModel.get('indexes'),
                        fieldNames: fieldNames
                    }});
                }
            }
        }
    });
});
