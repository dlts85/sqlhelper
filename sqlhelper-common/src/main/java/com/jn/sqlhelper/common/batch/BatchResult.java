/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the LGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jn.sqlhelper.common.batch;

import java.util.List;

public class BatchResult<E> {
    private BatchStatement statement;
    private List<E> parameters;
    private int rowsAffected;

    public BatchStatement getStatement() {
        return statement;
    }

    public void setStatement(BatchStatement statement) {
        this.statement = statement;
    }

    public List<E> getParameters() {
        return parameters;
    }

    public void setParameters(List<E> parameters) {
        this.parameters = parameters;
    }

    public int getRowsAffected() {
        return rowsAffected;
    }

    public void setRowsAffected(int rowsAffected) {
        this.rowsAffected = rowsAffected;
    }

    public String getSql(){
        return statement.getSql();
    }
}