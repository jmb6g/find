/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.queryHelper.IdolQueryTestHelper;
import com.autonomy.abc.selenium.error.Errors.Search;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.results.ListView;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.QueryResultsPage;
import com.autonomy.abc.selenium.query.QueryService;
import com.autonomy.abc.shared.QueryTestHelper;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.categories.CoreFeature;
import com.hp.autonomy.frontend.selenium.framework.environment.Deployment;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.apache.commons.collections4.ListUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.WebDriverException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assumeThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.openqa.selenium.lift.Matchers.displayed;

public class QueryTermsITCase extends FindTestBase {
    private FindPage findPage;
    private FindService findService;

    public QueryTermsITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
    }

    @Test
    @Category(CoreFeature.class)
    @Role(UserRole.FIND)
    public void testSearchOnSimpleTerms() throws InterruptedException {
        findPage.goToListView();

        final String searchTerm = "Fred is a chimpanzee";
        final ListView results = findService.search(searchTerm);
        assertThat(getElementFactory().getSearchBox().getValue(), is(searchTerm));
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
        assertThat(getElementFactory().getConceptsPanel().selectedConceptHeaders(), empty());
    }

    @Test
    @Category(CoreFeature.class)
    @Role(UserRole.FIND)
    @ActiveBug(value = "FIND-784 -> it's possible there's another bug to do with Selenium submit", type = ApplicationType.ON_PREM)
    public void testSearchForAll() {
        findPage.goToListView();

        final String searchTerm = "*";
        final ListView results = findService.search(searchTerm);
        assertThat(getElementFactory().getSearchBox().getValue(), is(searchTerm));
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
        assertThat(getElementFactory().getConceptsPanel().selectedConceptHeaders(), empty());
    }

    @Test
    @Category(CoreFeature.class)
    @Role(UserRole.BIFHI)
    public void testSearchOnSimpleConcepts() throws InterruptedException {
        assumeThat("Currently should only run on prem - requires role infrastructure", !isHosted());

        findPage.goToListView();

        final String searchTerm = "chimpanzee";
        final ListView results = findService.search(searchTerm);
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
        assertThat(getElementFactory().getConceptsPanel().selectedConceptHeaders(), contains(searchTerm));
    }

    @Test
    @Category(CoreFeature.class)
    @Role(UserRole.BIFHI)
    public void testImplicitSearchForAll() throws InterruptedException {
        assumeThat("Currently should only run on prem - requires role infrastructure", !isHosted());

        findPage.goToListView();

        assertThat(getElementFactory().getListView().getResults(), not(empty()));
        findService.search("*");
        assertThat(getElementFactory().getConceptsPanel().selectedConceptHeaders(), empty());
    }

    @Test
    public void testBooleanOperators() {
        findPage.goToListView();

        final List<String> potentialTerms = new ArrayList<>(Arrays.asList("brevity", "tessellate", "hydrangea", "\"dearly departed\"", "abstruse", "lobotomy"));
        final String termOne = findService.termWithBetween1And30Results(potentialTerms);
        final String termTwo = findService.termWithBetween1And30Results(potentialTerms);
        assertThat("Test only works if query terms both have <=30 results ", "", not(anyOf(is(termOne), is(termTwo))));

        final List<String> resultsTermOne = getResultsList(termOne);
        final int resultsNumberTermOne = resultsTermOne.size();

        final List<String> resultsTermTwo = getResultsList(termTwo);
        final int resultsNumberTermTwo = resultsTermTwo.size();

        final List<String> andResults = getResultsList(termOne + " AND " + termTwo);
        final int numberOfAndResults = andResults.size();
        assertThat(numberOfAndResults, allOf(lessThanOrEqualTo(resultsNumberTermOne), lessThanOrEqualTo(resultsNumberTermTwo)));
        assertThat(termOne + " results contain every results in the 'AND' results", resultsTermOne.containsAll(andResults));
        assertThat(termTwo + " results contain every results in the 'AND' results", resultsTermTwo.containsAll(andResults));

        final List<String> orResults = getResultsList(termOne + " OR " + termTwo);
        final Set<String> concatenatedResults = new HashSet<>(ListUtils.union(resultsTermOne, resultsTermTwo));
        assertThat(orResults, hasSize(concatenatedResults.size()));
        assertThat("'OR' results contains all the results that are present for each term alone", orResults.containsAll(concatenatedResults));

        final List<String> xorResults = getResultsList(termOne + " XOR " + termTwo);
        concatenatedResults.removeAll(andResults);
        assertThat(xorResults.size(), is(concatenatedResults.size()));
        assertThat(xorResults, containsInAnyOrder(concatenatedResults.toArray()));

        checkANotB(termOne + " NOT " + termTwo, new HashSet<>(concatenatedResults), resultsTermTwo);
        checkANotB(termTwo + " NOT " + termOne, new HashSet<>(concatenatedResults), resultsTermOne);
    }

    private List<String> getResultsList(final String term) {
        final ListView results = findService.search(term);
        results.waitForResultsToLoad();
        final List<String> searchResults = results.getResultTitles();
        removeAllConcepts();
        return searchResults;
    }

    private void checkANotB(final String term, final Set<String> uniqueResults, final List<String> notIncludeResults) {
        final List<String> notTermOne = getResultsList(term);
        uniqueResults.removeAll(notIncludeResults);
        assertThat(notTermOne.size(), is(uniqueResults.size()));
        assertThat(notTermOne, containsInAnyOrder(uniqueResults.toArray()));
    }

    private void ensureOnCorrectView() {
        findService.searchAnyView("get rid of");
        removeAllConcepts();
        final ListView results = findPage.goToListView();
        results.waitForResultsToLoad();
    }

    @Test
    @ActiveBug({"CORE-2925","FIND-853"})
    public void testCorrectErrorMessageDisplayed() {
        ensureOnCorrectView();
        new QueryTestHelper<>(findService)
                .booleanOperatorQueryText(Search.OPERATORS, Search.OPENING_BOOL, Search.GENERIC_HOSTED_ERROR);
        new QueryTestHelper<>(findService)
                .emptyQueryText(Search.STOPWORDS, Search.NO_TEXT, Search.GENERIC_HOSTED_ERROR, Search.HOSTED_INVALID);
    }

    @Test
    @ResolvedBug("FIND-151")
    public void testAllowSearchOfStringsThatContainBooleansWithinThem() {
        ensureOnCorrectView();
        new IdolQueryTestHelper<ListView>(findService).hiddenQueryOperatorText(getElementFactory());
    }

    @Test
    public void testSearchParentheses() {
        ensureOnCorrectView();
        //noinspection AnonymousInnerClassWithTooManyMethods
        new QueryTestHelper<>(new QueryService<QueryResultsPage>() {
            @Override
            public QueryResultsPage search(final String s) {
                return search(new Query(s));
            }

            @Override
            public QueryResultsPage search(final Query query) {
                removeAllConcepts();
                return findService.search(query);
            }
        }).mismatchedBracketQueryText();
    }

    @Test
    @ResolvedBug("HOD-2170")
    @ActiveBug("CCUK-3634")
    public void testSearchQuotationMarks() {
        new QueryTestHelper<>(findService).mismatchedQuoteQueryText(Search.QUOTES);
    }

    @Test
    @ActiveBug(value = "CCUK-3700", type = ApplicationType.ON_PREM)
    public void testWhitespaceSearch() {
        assumeThat("Currently should only run on prem - requires role infrastructure", !isHosted());

        findPage.goToListView();

        try {
            findService.search("       ");
        } catch(final WebDriverException ignored) { /* Expected behaviour */ }

        assertThat(findPage.footerLogo(), displayed());

        final ListView results = findService.search("Kevin Costner");

        final List<String> resultTitles = results.getResultTitles();

        findService.search(" ");
        assertThat(results.getResultTitles(), is(resultTitles));
        assertThat(findPage.parametricContainer().getText(), not(isEmptyOrNullString()));
        assertThat(findPage.parametricContainer().getText(), not(containsString("No parametric fields found")));
    }

    @Test
    @ResolvedBug("CCUK-3624")
    @Role(UserRole.FIND)
    public void testRefreshEmptyQuery() throws InterruptedException {
        findPage.goToListView();

        findService.search("something");
        findService.search("");
        Thread.sleep(5000);

        getWindow().refresh();
        findPage = getElementFactory().getFindPage();

        verifyThat(getElementFactory().getSearchBox().getValue(), is(""));
        verifyThat("taken back to landing page after refresh", findPage.footerLogo(), displayed());
    }

    private void removeAllConcepts() {
        getElementFactory().getConceptsPanel().removeAllConcepts();
    }
}
