/*
 * Copyright 2013 gregory.graham.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.gregs.dbvolution.datatypes;

import java.beans.PropertyEditorSupport;

/**
 *
 * @author gregory.graham
 */
public class DBNumberEditor extends PropertyEditorSupport {

    private String format;

    /**
     *
     * @param format
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     *
     * @param text
     */
    @Override
    public void setAsText(String text) {
        DBNumber type;
        if (text == null || text.isEmpty()) {
            type = new DBNumber();
        } else {
            type = new DBNumber();
            type.setValue(Double.parseDouble(text));
        }
        setValue(type);
    }
}