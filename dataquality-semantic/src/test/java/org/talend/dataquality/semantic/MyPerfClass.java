
// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.semantic;

import org.apache.log4j.Logger;
import org.apache.lucene.search.BitsFilteredDocIdSet;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.FieldCacheRangeFilter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.semantic.index.LuceneIndex;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizer;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;
import org.talend.dataquality.standardization.index.SynonymIndexSearcher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * created by talend on 2015-07-28 Detailled comment.
 *
 */
public class MyPerfClass {

    private static Logger log = Logger.getLogger(CategoryRecognizerTest.class);

    private static Map<String, String> EXPECTED_CAT_ID = new LinkedHashMap<String, String>() {

        private static final long serialVersionUID = -5067273062214728849L;

        {
            put("heiss", SemanticCategoryEnum.LAST_NAME.getId());
            put("dupont", SemanticCategoryEnum.LAST_NAME.getId());
            put("jarmon", SemanticCategoryEnum.LAST_NAME.getId());
            put("schad", SemanticCategoryEnum.LAST_NAME.getId());
            put("OBANDO", SemanticCategoryEnum.LAST_NAME.getId());
            put("CZECH", SemanticCategoryEnum.LAST_NAME.getId());
            put("CANON", SemanticCategoryEnum.LAST_NAME.getId());
            put("BHATTI", SemanticCategoryEnum.LAST_NAME.getId());
            put("LUCKY", SemanticCategoryEnum.LAST_NAME.getId());
            put("smith", SemanticCategoryEnum.LAST_NAME.getId());
            put("walker", SemanticCategoryEnum.LAST_NAME.getId());
            put("BEVILACQUA", SemanticCategoryEnum.LAST_NAME.getId());
            put("ogorman", SemanticCategoryEnum.LAST_NAME.getId());

        }
    };

    private static CategoryRecognizer catRecognizer;

    @BeforeClass
    public static void prepare() throws URISyntaxException, IOException {
        CategoryRecognizerBuilder b = CategoryRecognizerBuilder.newBuilder();
        // catRecognizer = b.es().host("localhost").port(9300).cluster("elasticsearch").build();
        final URI ddPath = CategoryRecognizerTest.class.getResource("/luceneIdx/dictionary").toURI();
        final URI kwPath = CategoryRecognizerTest.class.getResource("/luceneIdx/keyword").toURI();
        catRecognizer = b.lucene().ddPath(ddPath).kwPath(kwPath).build();
    }

    @Before
    public void init() {
        catRecognizer.reset();
    }

    @Test
    public void testSin5gl632S1Column() throws URISyntaxException {

        catRecognizer.prepare();
        long start_time;
        for (int j = 1000; j < 100001; j *= 10) {
            start_time = System.currentTimeMillis();
            for (int i = 0; i < j; i++)
                for (String data : EXPECTED_CAT_ID.keySet()) {
                    List<String> catNames = Arrays.asList(catRecognizer.process(data));
                    // System.out.println(data + " data: " + Arrays.asList(catNames));
                    // System.out.println(data + " expected category id: " + Arrays.asList(EXPECTED_CAT_ID.get(data)));
                    // for (String catName : catNames) {
                    // System.out.println(Arrays.asList(EXPECTED_DISPLAYNAME.get(data)) + catName.toString());
                    // assertTrue("Category ID <" + catNames + "> is not recognized for data <" + data + ">" + " et "
                    // + EXPECTED_CAT_ID.get(data), catNames.contains(EXPECTED_CAT_ID.get(data)));
                    // }
                }
            System.out.println("time for " + j + " iterations = " + (System.currentTimeMillis() - start_time));
        }

    }

}
