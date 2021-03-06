package org.talend.dataquality.record.linkage.constant;

import java.util.ArrayList;
import java.util.List;

public enum TokenizedResolutionMethod {
    NO("No"), //$NON-NLS-1$
    ANYORDER("Any order"), //$NON-NLS-1$
    SAMEPLACE("Same place"), //$NON-NLS-1$
    SAMEORDER("Same order"); //$NON-NLS-1$

    private final String componentValue;

    TokenizedResolutionMethod(String componentValue) {
        this.componentValue = componentValue;
    }

    /**
     * Getter for componentValue.
     * 
     * @return the componentValue
     */
    public String getComponentValue() {
        return this.componentValue;
    }

    /**
     * @return get all label of Tokenized Resolution Method .
     */
    public static String[] getAllTypes() {
        List<String> types = new ArrayList<>();
        for (int i = 0; i < TokenizedResolutionMethod.values().length; i++) {
            types.add(TokenizedResolutionMethod.values()[i].componentValue);
        }
        return types.toArray(new String[types.size() - 1]);
    }

    /**
     * 
     * get type of the value which in this Enum
     * 
     * @param value
     * @return null can not find this index
     */
    public static TokenizedResolutionMethod getTypeByValue(String value) {
        for (TokenizedResolutionMethod element : TokenizedResolutionMethod.values()) {
            if (element.getComponentValue().equalsIgnoreCase(value)) {
                return element;
            }
        }

        return null;
    }
}
