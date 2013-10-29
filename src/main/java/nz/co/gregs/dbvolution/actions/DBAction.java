/*
 * Copyright 2013 gregorygraham.
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
package nz.co.gregs.dbvolution.actions;

import java.sql.SQLException;
import java.sql.Statement;
import nz.co.gregs.dbvolution.DBDatabase;
import nz.co.gregs.dbvolution.DBRow;
import nz.co.gregs.dbvolution.databases.DBStatement;

/**
 *
 * @author gregorygraham
 */
public  abstract class DBAction {

    private DBRow row;
    protected String sql = "";
//    protected final DBDatabase database;
    
    public <R extends DBRow> DBAction(R row) {
        super();
        this.row = row;
    }
    
    public <R extends DBRow> DBAction(R row, String sql) {
        this(row);
        this.sql = sql;
    }
    
    public String getSQLStatement(DBDatabase db){
        return sql;
    }

    public abstract boolean canBeBatched();

    public abstract void execute(DBDatabase db, DBStatement statement) throws SQLException ;

    /**
     * @return the row
     */
    public DBRow getRow() {
        return row;
    }
    
}
