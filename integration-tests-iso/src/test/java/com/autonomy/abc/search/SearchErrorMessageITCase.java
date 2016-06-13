package com.autonomy.abc.search;

import com.autonomy.abc.base.HybridIsoTestBase;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.shared.QueryTestHelper;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.net.MalformedURLException;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

public class SearchErrorMessageITCase extends HybridIsoTestBase {
    private SearchService searchService;

    public SearchErrorMessageITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() throws MalformedURLException {
        searchService = getApplication().searchService();
    }

    @Test
    @ResolvedBug("CCUK-3741")
    public void testSearchParentheses() {
        new QueryTestHelper<>(searchService).mismatchedBracketQueryText();
    }

    @Test
    @ResolvedBug("CCUK-3741")
    @ActiveBug(value = "HOD-2170", type = ApplicationType.HOSTED)
    public void testSearchQuotationMarks() {
        @RelatedTo("CCUK-3747")
        Serializable error = isHosted() ?
                Errors.Search.INVALID : Errors.Search.QUOTES;
        new QueryTestHelper<>(searchService).mismatchedQuoteQueryText(error);
    }

    @Test
    @ResolvedBug("CCUK-3741")
    public void testQueriesWithNoTerms() {
        @RelatedTo("CCUK-3747")
        Serializable booleanError = isHosted() ?
                Errors.Search.INVALID : Errors.Search.OPENING_BOOL;
        Serializable emptyError = isHosted() ?
                Errors.Search.INVALID : Errors.Search.NO_TEXT;

        new QueryTestHelper<>(searchService).booleanOperatorQueryText(booleanError);
        new QueryTestHelper<>(searchService).emptyQueryText(emptyError);
    }

    @Test
    public void testQueryAnalysisForBadQueries() {
        for (final String term : QueryTestHelper.NO_TERMS) {
            Query query = new Query(term).withFilter(new LanguageFilter(Language.ENGLISH));
            String error = searchService.search(query).getKeywordError();
            assertThat(error, not(isEmptyOrNullString()));
            assertThat(error, containsString(Errors.Keywords.NO_TERMS));
        }
    }

    @Test
    public void testAllowSearchOfStringsThatContainBooleansWithinThem() {
        new QueryTestHelper<>(searchService).hiddenQueryOperatorText();
    }
}
