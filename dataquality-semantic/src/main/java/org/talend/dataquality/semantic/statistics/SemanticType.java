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
package org.talend.dataquality.semantic.statistics;

import java.util.HashMap;
import java.util.Map;

import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.semantic.recognizer.CategoryFrequency;

/**
 * Semantic type bean which hold semantic type to its count information in a map.
 *
 */
public class SemanticType {

    private Map<CategoryFrequency, Long> categoryToCount = new HashMap<CategoryFrequency, Long>();

    /**
     * Get categoryToCount.
     */
    public Map<CategoryFrequency, Long> getCategoryToCount() {
        return categoryToCount;
    }

    /**
     * Get suggested suggsted category.
     */
    public String getSuggestedCategory() {
        long max = 0;
        String electedCategory = "UNKNOWN"; // Unknown by default
        int categoryOrdinal = Integer.MAX_VALUE;
        for (Map.Entry<CategoryFrequency, Long> entry : categoryToCount.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                electedCategory = entry.getKey().getCategoryId();
                categoryOrdinal = SemanticCategoryEnum.getCategoryById(electedCategory).ordinal();
            } else if (entry.getValue() == max) {
                final String currentCat = entry.getKey().getCategoryId();
                final int currentOrdinal = SemanticCategoryEnum.getCategoryById(currentCat).ordinal();
                if (currentOrdinal < categoryOrdinal) {
                    electedCategory = currentCat;
                    categoryOrdinal = currentOrdinal;
                }
            }
        }
        return electedCategory;
    }

    /**
     * Increment the category with count of one category.
     * 
     * @param category
     * @param count
     */
    public void increment(CategoryFrequency category, long count) {
        if (!categoryToCount.containsKey(category)) {
            categoryToCount.put(category, count);
        } else {
            categoryToCount.put(category, categoryToCount.get(category) + count);
        }
    }

}
