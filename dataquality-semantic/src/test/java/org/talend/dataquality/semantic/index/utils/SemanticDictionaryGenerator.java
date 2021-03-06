package org.talend.dataquality.semantic.index.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.talend.dataquality.semantic.index.DictionarySearcher;
import org.talend.dataquality.semantic.index.utils.optimizer.CategoryOptimizer;

public class SemanticDictionaryGenerator {

    private static final String DD_PATH = "src/main/resources/luceneIdx/dictionary/";

    private static final String KW_PATH = "src/main/resources/luceneIdx/keyword/";

    private static Pattern SPLITTER = Pattern.compile("\\|");

    private Analyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);

    private static Set<String> STOP_WORDS = new HashSet<String>(
            Arrays.asList("yes", "no", "y", "o", "n", "oui", "non", "true", "false", "vrai", "faux", "null"));

    private void generateDictionaryForSpec(DictionaryGenerationSpec spec, IndexWriter writer) throws IOException {

        System.out.println("-------------------" + spec.name() + "---------------------");
        // load CSV file
        Reader reader = new FileReader(SemanticDictionaryGenerator.class.getResource(spec.getSourceFile()).getPath());
        CSVFormat csvFormat = CSVFormat.DEFAULT.withDelimiter(spec.getCsvConfig().getDelimiter());
        if (spec.getCsvConfig().isWithHeader()) {
            csvFormat = csvFormat.withFirstRecordAsHeader();
        }

        // collect synonyms
        Iterable<CSVRecord> records = csvFormat.parse(reader);
        List<Set<String>> synonymSetList = getDictinaryForCategory(records, spec);

        int countCategory = 0;
        for (Set<String> synonymSet : synonymSetList) {
            writer.addDocument(generateDocument(spec.getCategoryName(), synonymSet));
            countCategory++;
        }
        System.out.println("Total document count: " + countCategory + "\nExamples:");
        Iterator<Set<String>> it = synonymSetList.iterator();
        int count = 0;
        while (it.hasNext() && count < 10) {
            System.out.println("- " + it.next());
            count++;
        }

        reader.close();
    }

    private List<Set<String>> getDictinaryForCategory(Iterable<CSVRecord> records, DictionaryGenerationSpec spec) {
        List<Set<String>> results = new ArrayList<Set<String>>();
        final int[] columnsToIndex = spec.getColumnsToIndex();
        final CategoryOptimizer optimizer = spec.getOptimizer();
        Set<String> existingValuesOfCategory = new HashSet<String>();
        int ignoredCount = 0;

        for (CSVRecord record : records) {

            List<String> allInputColumns = new ArrayList<String>();
            if (DictionaryGenerationSpec.CITY.equals(spec)) { // For CITY index, take all columns
                for (int col = 0; col < record.size(); col++) {
                    final String colValue = record.get(col);
                    final String[] splits = SPLITTER.split(colValue);
                    for (String syn : splits) {
                        if (syn != null && syn.trim().length() > 0) {
                            allInputColumns.add(syn.trim());
                        }
                    }
                }
            } else {
                for (int col : columnsToIndex) {
                    if (col < record.size()) { // sometimes, the value of last column can be missing.
                        final String colValue = record.get(col);
                        final String[] splits = SPLITTER.split(colValue);
                        for (String syn : splits) {
                            if (syn != null && syn.trim().length() > 0) {
                                allInputColumns.add(syn.trim());
                            }
                        }
                    }
                }
            }

            if (optimizer != null) {
                allInputColumns = new ArrayList<String>(optimizer.optimize(allInputColumns.toArray(new String[0])));
            }

            Set<String> synonymsInRecord = new HashSet<String>();
            for (String syn : allInputColumns) {
                if (STOP_WORDS.contains(syn.toLowerCase()) //
                        && (DictionaryGenerationSpec.COMPANY.equals(spec) //
                                || DictionaryGenerationSpec.FIRST_NAME.equals(spec) //
                                || DictionaryGenerationSpec.LAST_NAME.equals(spec) //
                        )) {
                    System.out.println("[" + syn + "] is exclued from the category [" + spec.getCategoryName() + "]");
                    continue;
                }
                if (!existingValuesOfCategory.contains(syn.toLowerCase())) {
                    synonymsInRecord.add(syn);
                    existingValuesOfCategory.add(syn.toLowerCase());
                } else {
                    ignoredCount++;
                }
            }
            if (synonymsInRecord.size() > 0) { // at least one synonym
                results.add(synonymsInRecord);
            }
        }
        System.out.println("Ignored value count: " + ignoredCount);
        return results;

    }

    /**
     * generate a document.
     *
     * @param word
     * @param synonyms
     * @return
     */
    private Document generateDocument(String word, Set<String> synonyms) {
        String tempWord = word.trim();
        Document doc = new Document();
        FieldType ftSyn = new FieldType();
        ftSyn.setStored(false);
        ftSyn.setIndexed(true);
        ftSyn.setOmitNorms(true);
        ftSyn.freeze();

        Field wordTermField = new StringField(DictionarySearcher.F_WORD, tempWord, Field.Store.YES);
        doc.add(wordTermField);
        for (String syn : synonyms) {
            if (syn != null) {
                syn = syn.trim();
                if ("CITY".equals(tempWord)) { // ignore city abbreviations
                    if (syn.length() == 3 && syn.charAt(0) >= 'A' && syn.charAt(0) <= 'Z'//
                            && syn.charAt(1) >= 'A' && syn.charAt(1) <= 'Z'//
                            && syn.charAt(2) >= 'A' && syn.charAt(2) <= 'Z') {
                        continue;
                    }
                }

                if (syn.length() > 0 && !syn.equals(tempWord)) {
                    try {
                        List<String> aux = DictionarySearcher.getTokensFromAnalyzer(syn);
                        doc.add(new StringField(DictionarySearcher.F_SYNTERM, StringUtils.join(aux, ' '), Field.Store.NO));
                        if (aux.size() > 1)
                            doc.add(new Field(DictionarySearcher.F_SYN, syn, ftSyn));

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return doc;
    }

    private void generate(GenerationType type, String path) {
        try {
            FileUtils.deleteDirectory(new File(path));
            FSDirectory outputDir = FSDirectory.open(new File(path));
            IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LATEST, analyzer);
            IndexWriter writer = new IndexWriter(outputDir, writerConfig);
            for (DictionaryGenerationSpec spec : DictionaryGenerationSpec.values()) {
                if (spec.getGenerationType().equals(type)) {
                    try {
                        generateDictionaryForSpec(spec, writer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            writer.commit();
            writer.close();
            outputDir.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        final String resourcePath = SemanticDictionaryGenerator.class.getResource(".").getFile();
        final String projectRoot = new File(resourcePath).getParentFile().getParentFile().getParentFile().getParentFile()
                .getParentFile().getParentFile().getParentFile().getParentFile().getPath() + File.separator;
        SemanticDictionaryGenerator generator = new SemanticDictionaryGenerator();
        generator.generate(GenerationType.DICTIONARY, projectRoot + DD_PATH);
        generator.generate(GenerationType.KEYWORD, projectRoot + KW_PATH);
    }

}
