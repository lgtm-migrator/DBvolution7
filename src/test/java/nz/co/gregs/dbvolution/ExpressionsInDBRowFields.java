/*
 * Copyright 2014 gregory.graham.
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
package nz.co.gregs.dbvolution;

import java.util.List;
import nz.co.gregs.dbvolution.annotations.*;
import nz.co.gregs.dbvolution.datatypes.*;
import nz.co.gregs.dbvolution.expressions.DateExpression;
import nz.co.gregs.dbvolution.expressions.NumberExpression;
import nz.co.gregs.dbvolution.expressions.StringExpression;
import nz.co.gregs.dbvolution.generic.AbstractTest;
import static org.hamcrest.Matchers.*;
import org.junit.*;

/**
 *
 * @author gregory.graham
 */
public class ExpressionsInDBRowFields extends AbstractTest {

    public ExpressionsInDBRowFields(Object testIterationName, Object db) {
        super(testIterationName, db);
    }

//    @Ignore
    @Test
    public void selectDBRowExpressionWithDBQuery() throws Exception {
        final ExpressionRow exprExample = new ExpressionRow();

        exprExample.name.permittedValuesIgnoreCase("TOYOTA");
        DBQuery query = database.getDBQuery(exprExample);

        final String sqlForQuery = query.getSQLForQuery();
        Assert.assertThat(sqlForQuery, containsString(database.getDefinition().getCurrentDateFunctionName()));
        Assert.assertThat(sqlForQuery, containsString(ExpressionRow.STRING_VALUE));
        Assert.assertThat(sqlForQuery, containsString(NumberExpression.value(5).times(3).toSQLString(database)));
        final List<DBQueryRow> allRows = query.getAllRows();

        for (DBQueryRow row : allRows) {
            ExpressionRow expressionRow = row.get(exprExample);
            System.out.println("Expression Row SysDate SQL: " + expressionRow.sysDateColumnOnClass.toSQLString(database));
            System.out.println("Expression Row SysDate SQL: " + expressionRow.stringColumnOnClass.toSQLString(database));
            System.out.println("Expression Row SysDate SQL: " + expressionRow.numberColumnOnClass.toSQLString(database));
            DBDate currentDate = expressionRow.sysDateColumnOnClass;
            System.out.println("Expression Row.sysDateColumnOnClass = " + currentDate.dateValue());
            Assert.assertThat(expressionRow.stringColumnOnClass.stringValue(), is(ExpressionRow.STRING_VALUE.toUpperCase()));
            Assert.assertThat(expressionRow.numberColumnOnClass.intValue(), is(15));
        }
    }

    @Test
    public void selectDBRowExpressionWithDBQueryAndExpressionCriteria() throws Exception {
        final ExpressionRow exprExample = new ExpressionRow();
        exprExample.numberColumnOnClass.permittedValues(15);

        exprExample.name.permittedValuesIgnoreCase("TOYOTA");
        DBQuery query = database.getDBQuery(exprExample);

        String sqlForQuery = query.getSQLForQuery();
        Assert.assertThat(sqlForQuery, containsString(database.getDefinition().getCurrentDateFunctionName()));

        for (DBQueryRow row : query.getAllRows()) {
            ExpressionRow expressionRow = row.get(exprExample);
            System.out.println("Expression Row SysDate SQL: " + expressionRow.sysDateColumnOnClass.toSQLString(database));
            System.out.println("Expression Row stringColumnOnClass SQL: " + expressionRow.stringColumnOnClass.toSQLString(database));
            System.out.println("Expression Row numberColumnOnClass SQL: " + expressionRow.numberColumnOnClass.toSQLString(database));
            DBDate currentDate = expressionRow.sysDateColumnOnClass;
            System.out.println("Expression Row.sysDateColumnOnClass = " + currentDate.dateValue());
        }

        final ExpressionRow exprExample2 = new ExpressionRow();
        exprExample2.numberColumnOnClass.excludedValues(15);
        exprExample2.name.permittedValuesIgnoreCase("TOYOTA");

        query = database.getDBQuery(exprExample2);

        sqlForQuery = query.getSQLForQuery();
        Assert.assertThat(sqlForQuery, containsString(database.getDefinition().getCurrentDateFunctionName()));
        final List<DBQueryRow> allRows = query.getAllRows();
        database.print(allRows);
        Assert.assertThat(allRows.size(), is(0));
    }

    @Test
    public void selectDBRowExpressionWithDBTable() throws Exception {
        final ExpressionRow exprExample = new ExpressionRow();
        exprExample.name.permittedValuesIgnoreCase("TOYOTA");
        DBTable<ExpressionRow> table = database.getDBTable(exprExample);

        final String sqlForQuery = table.getSQLForQuery();
        System.out.println(sqlForQuery);
        Assert.assertThat(sqlForQuery, containsString(database.getDefinition().getCurrentDateFunctionName()));
        final List<ExpressionRow> rowsByExample = table.getAllRows();
        database.print(rowsByExample);

        for (ExpressionRow expressionRow : table.getAllRows()) {
            System.out.println("Expression Row SysDate SQL: " + expressionRow.sysDateColumnOnClass.toSQLString(database));
            System.out.println("Expression Row stringColumnOnClass SQL: " + expressionRow.stringColumnOnClass.toSQLString(database));
            System.out.println("Expression Row numberColumnOnClass SQL: " + expressionRow.numberColumnOnClass.toSQLString(database));
            DBDate currentDate = expressionRow.sysDateColumnOnClass;
            System.out.println("Expression Row.sysDateColumnOnClass = " + currentDate.dateValue());
        }
    }

    @DBTableName("marque")
    public static class ExpressionRow extends DBRow {

        public static final long serialVersionUID = 1L;
        public static final String STRING_VALUE = "THis ValuE";
        
        @DBColumn
        DBDate sysDateColumnOnClass = new DBDate(DateExpression.currentDate());
        
        @DBColumn
        DBString stringColumnOnClass = new DBString(StringExpression.value(STRING_VALUE).uppercase());
        
        @DBColumn
        DBNumber numberColumnOnClass = new DBNumber(NumberExpression.value(5).times(3));
        
        @DBColumn("uid_marque")
        @DBPrimaryKey
        public DBInteger uidMarque = new DBInteger();
        
        @DBColumn
        public DBString name = new DBString();
    }
}
