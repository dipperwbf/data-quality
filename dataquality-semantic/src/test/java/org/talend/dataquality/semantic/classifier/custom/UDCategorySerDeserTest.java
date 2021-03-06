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
package org.talend.dataquality.semantic.classifier.custom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.talend.dataquality.semantic.recognizer.MainCategory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * DOC qiongli class global comment. Detailled comment
 */
public class UDCategorySerDeserTest {

    private static final String tmpFile = "categ.tmp.json"; //$NON-NLS-1$

    private static final String[][] CATEGORIES = {
            // id, name, regex, description, main category
            { "POSTAL_CODE_BEL", "POSTAL CODE", "^(F-[0-9]{4,5}|B-[0-9]{4})$", "this a description", "AlphaNumeric" },
            { "POSTAL_CODE_FRA", "POSTAL CODE", "^(0[1-9]|[1-9][0-9])[0-9]{3}$", null, "Numeric" },
            { "POSTAL_CODE_DEU", "POSTAL CODE", "^(?!01000|99999)(0[1-9]\\d{3}|[1-9]\\d{4})$", null, "Numeric" },
            { "POSTAL_CODE_CHE", "POSTAL CODE", "^[1-9][0-9][0-9][0-9]$", null, "Numeric" },
            { "GENDER", "GENDER", "^(m|M|male|Male|f|F|female|Female)$", null, "Alpha" } };

    /**
     * Test method for {@link org.talend.dataquality.semantic.classifier.custom.UDCategorySerDeser#readJsonFile()}.
     * 
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @Test
    public void testReadJsonFile() throws IOException {
        UserDefinedClassifier userDefinedClassifier = UDCategorySerDeser.readJsonFile();
        assertNotNull(userDefinedClassifier);
        int nbCat = userDefinedClassifier.getClassifiers().size();
        assertTrue("Expected to read at least 10 category but only get " + nbCat, nbCat > 9); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.talend.dataquality.semantic.classifier.custom.UserDefinedHelper#writeToJsonFile(org.talend.dataquality.semantic.classifier.custom.UserDefinedClassifier)}
     * .
     * 
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @Test
    public void testWriteToJsonFile() throws JsonParseException, JsonMappingException, IOException {

        UDCategorySerDeser helper = new UDCategorySerDeser();

        UserDefinedClassifier fc = new UserDefinedClassifier();

        for (String[] cat : CATEGORIES) {
            UserDefinedCategory c = new UserDefinedCategory(cat[0]);
            c.setName(cat[1]);
            UserDefinedRegexValidator v = new UserDefinedRegexValidator();
            v.setPatternString(cat[2]);
            c.setValidator(v);

            c.setDescription(cat[3]);
            c.setMainCategory(MainCategory.valueOf(cat[4]));

            fc.getClassifiers().add(c);

        }

        File file = new File(tmpFile);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream fos = new FileOutputStream(file);
        helper.writeToJsonFile(fc, fos);
        // System.out.println("Categories written in " + file.getAbsolutePath()); //$NON-NLS-1$
        assertTrue(file.exists());

        // then read this file again
        UserDefinedClassifier userDefinedClassifier = UDCategorySerDeser.readJsonFile(new FileInputStream(file));
        assertNotNull(userDefinedClassifier);
        int nbCat = userDefinedClassifier.getClassifiers().size();
        assertEquals("Expected " + CATEGORIES.length + "categories but get " + nbCat, CATEGORIES.length, nbCat); //$NON-NLS-1$ //$NON-NLS-2$

        assertTrue(file.delete());
    }

}
