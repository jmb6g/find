/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.find.core.search.RelatedConceptsController;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.search.Entity;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.RelatedConceptsRequest;
import com.hp.autonomy.searchcomponents.core.search.RelatedConceptsService;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodRelatedConceptsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(RelatedConceptsController.RELATED_CONCEPTS_PATH)
public class HodRelatedConceptsController extends RelatedConceptsController<Entity, HodQueryRestrictions, ResourceIdentifier, HodErrorException> {
    @Autowired
    public HodRelatedConceptsController(final RelatedConceptsService<Entity, ResourceIdentifier, HodErrorException> relatedConceptsService, final QueryRestrictions.Builder<HodQueryRestrictions, ResourceIdentifier> queryRestrictionsBuilder) {
        super(relatedConceptsService, queryRestrictionsBuilder);
    }

    @Override
    protected RelatedConceptsRequest<ResourceIdentifier> buildRelatedConceptsRequest(final QueryRestrictions<ResourceIdentifier> queryRestrictions) {
        return new HodRelatedConceptsRequest.Builder()
                .setQueryRestrictions(queryRestrictions)
                .build();
    }
}
