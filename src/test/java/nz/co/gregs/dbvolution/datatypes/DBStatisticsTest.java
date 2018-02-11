/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.dbvolution.datatypes;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import javassist.ClassPool;
import nz.co.gregs.dbvolution.DBQuery;
import nz.co.gregs.dbvolution.DBQueryRow;
import nz.co.gregs.dbvolution.DBRow;
import nz.co.gregs.dbvolution.annotations.DBColumn;
import nz.co.gregs.dbvolution.columns.IntegerColumn;
import nz.co.gregs.dbvolution.example.CarCompany;
import nz.co.gregs.dbvolution.example.Marque;
import nz.co.gregs.dbvolution.expressions.IntegerExpression;
import nz.co.gregs.dbvolution.expressions.StringExpression;
import nz.co.gregs.dbvolution.generic.AbstractTest;
import nz.co.gregs.dbvolution.results.IntegerResult;
import nz.co.gregs.dbvolution.results.StringResult;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import org.junit.Assert;
import static org.junit.Assert.assertThat;

/**
 *
 * <p style="color: #F90;">Support DBvolution at
 * <a href="http://patreon.com/dbvolution" target=new>Patreon</a></p>
 *
 * @author gregorygraham
 */
public class DBStatisticsTest extends AbstractTest {

	public DBStatisticsTest(Object testIterationName, Object db) {
		super(testIterationName, db);
	}

	@Test
	public void testBasic() throws SQLException {

		DBQuery dbQuery = database.getDBQuery(new StatsIntegerTest()).setBlankQueryAllowed(true);
		List<DBQueryRow> allRows = dbQuery.getAllRows();

		assertThat(allRows.size(), is(1));
		final StatsIntegerTest row = allRows.get(0).get(new StatsIntegerTest());
		assertThat(row.carCoStats.count().intValue(), is(22));
	}

	@Test
	public void testModeSimpleQuery() throws SQLException {
		final Marque marque = new Marque();
		final IntegerColumn updateCountColumn = marque.column(marque.updateCount);

		DBInteger count = updateCountColumn.count().asExpressionColumn();
		count.setSortOrderDescending();
		DBQuery query = database
				.getDBQuery(marque, new CarCompany());
		query.setBlankQueryAllowed(true)
				.setReturnFieldsToNone()
				.addExpressionColumn("mode", updateCountColumn.asExpressionColumn())
				.addExpressionColumn("mode count", count)
				.setSortOrder(query.column(count))
				.setRowLimit(1);

		List<DBQueryRow> allRows = query.getAllRows();

		// Check there is only 1 row
		Assert.assertThat(allRows.size(), is(1));

		final DBQueryRow onlyRow = allRows.get(0);

		// Check that the mode is 2
		QueryableDatatype<?> mode = onlyRow.getExpressionColumnValue("mode");
		Assert.assertThat(mode.stringValue(), is("2"));

		// Check that the mode was found 9 times
		QueryableDatatype<?> counted = onlyRow.getExpressionColumnValue("mode count");
		Assert.assertThat(counted.stringValue(), is("9"));
	}

	@Test
	public void testModeSimpleQueryWithCondition() throws SQLException {
		final Marque marque = new Marque();
		marque.updateCount.excludedValues(2);
		final IntegerColumn updateCountColumn = marque.column(marque.updateCount);

		DBInteger count = updateCountColumn.count().asExpressionColumn();
		count.setSortOrderDescending();
		Set<DBRow> tablesInvolved = updateCountColumn.getTablesInvolved();
		DBQuery query = database
				.getDBQuery(tablesInvolved);
		query.setBlankQueryAllowed(true)
				.setReturnFieldsToNone()
				.addExpressionColumn("mode", updateCountColumn.asExpressionColumn())
				.addExpressionColumn("mode count", count)
				.setSortOrder(query.column(count))
				.setRowLimit(1);

		List<DBQueryRow> allRows = query.getAllRows();

		// Check there is only 1 row
		Assert.assertThat(allRows.size(), is(1));

		final DBQueryRow onlyRow = allRows.get(0);

		// Check that the mode is 2
		QueryableDatatype<?> mode = onlyRow.getExpressionColumnValue("mode");
		Assert.assertThat(mode.stringValue(), is("0"));

		// Check that the mode was found 9 times
		QueryableDatatype<?> counted = onlyRow.getExpressionColumnValue("mode count");
		Assert.assertThat(counted.stringValue(), is("4"));
	}

	@Test
	public void testModeSimpleExpression() throws SQLException {
		final Marque marque = new Marque();

		final IntegerColumn updateCountColumn = marque.column(marque.updateCount);

		DBInteger count = updateCountColumn.count().asExpressionColumn();
		count.setSortOrderDescending();
		DBQuery query = database
				.getDBQuery(marque)
				.setBlankQueryAllowed(true)
				.setReturnFieldsToNone()
				.addExpressionColumn("mode", updateCountColumn.modeSimple().asExpressionColumn());

		List<DBQueryRow> allRows = query.getAllRows();

		// Check there is only 1 row
		Assert.assertThat(allRows.size(), is(22));

		final DBQueryRow onlyRow = allRows.get(0);

		// Check that the mode is 2
		QueryableDatatype<?> mode = onlyRow.getExpressionColumnValue("mode");
		Assert.assertThat(mode.stringValue(), is("2"));
	}

	@Test
	public void testModeSimpleExpressionInTable() throws SQLException {
		final StatsOfUpdateCountTest stat = new StatsOfUpdateCountTest();

		DBQuery query = database
				.getDBQuery(stat).setBlankQueryAllowed(true);

		List<DBQueryRow> allRows = query.getAllRows();

		Assert.assertThat(allRows.size(), is(22));

		final StatsOfUpdateCountTest onlyRow = allRows.get(0).get(stat);

		// Check that the mode is 2
		Integer mode = onlyRow.mode.intValue();
		Assert.assertThat(mode, is(2));
	}

	@Test
	public void testModeSimpleExpressionInDBStatistics() throws SQLException {
		final StatsIntegerTest stat = new StatsIntegerTest();

		DBQuery query = database
				.getDBQuery(stat).setBlankQueryAllowed(true);

		List<DBQueryRow> allRows = query.getAllRows();

		// Check there is only 1 row
		Assert.assertThat(allRows.size(), is(1));

		final StatsIntegerTest onlyRow = allRows.get(0).get(stat);

		// Check that the mode is 2
		Integer mode = onlyRow.carCoStats.modeSimple().intValue();
		Assert.assertThat(mode, is(4));
	}

	@Test
	public void testModeSimpleStringExpressionInDBStatistics() throws SQLException {
		final StatsStringTest stat = new StatsStringTest();

		DBQuery query = database
				.getDBQuery(stat).setBlankQueryAllowed(true);

		List<DBQueryRow> allRows = query.getAllRows();

		// Check there is only 1 row
		Assert.assertThat(allRows.size(), is(1));

		final StatsStringTest onlyRow = allRows.get(0).get(stat);

		// Check that the mode is 2
		String mode = onlyRow.carNameStats.modeSimple();
		Assert.assertThat(mode, is("O"));
	}

	@Test
	public void testModeStrictExpressionInDBStatistics() throws SQLException {
		final StatsIntegerTest stat = new StatsIntegerTest();

		DBQuery query = database
				.getDBQuery(stat).setBlankQueryAllowed(true);

		List<DBQueryRow> allRows = query.getAllRows();

		// Check there is only 1 row
		Assert.assertThat(allRows.size(), is(1));

		final StatsIntegerTest onlyRow = allRows.get(0).get(stat);

		// Check that the mode is 4 for car company
		Long mode = onlyRow.carCoStats.modeStrict();
		Assert.assertThat(mode, is(4l));
	}

	@Test
	public void testModeStrictExpressionInDBStatisticsWithStringResult() throws SQLException {
		StatsStringTest stat = new StatsStringTest();
		DBQuery query = database
				.getDBQuery(stat).setBlankQueryAllowed(true);

		List<DBQueryRow> allRows = query.getAllRows();

		// Check there is only 1 row
		Assert.assertThat(allRows.size(), is(1));

		final StatsStringTest onlyRow = allRows.get(0).get(stat);

		// Check that the mode is 4 for car company
		String mode = onlyRow.carNameStats.modeStrict();
		Assert.assertThat(mode, is("O"));
	}

	@Test
	public void testModeStrictQuery() throws SQLException {

		final Marque marque = new Marque();
		final IntegerColumn updateCountColumn = marque.column(marque.updateCount);

		DBInteger count = updateCountColumn.count().asExpressionColumn();
		count.setSortOrderDescending();

		DBQuery query1 = database
				.getDBQuery(marque, new CarCompany());
		query1.setBlankQueryAllowed(true)
				.setReturnFieldsToNone()
				.addExpressionColumn("mode", updateCountColumn.asExpressionColumn())
				.addExpressionColumn("mode count", count)
				.setSortOrder(query1.column(count))
				.setRowLimit(1);

		final Marque marque2 = new Marque();
		final IntegerColumn updateCountColumn2 = marque2.column(marque2.updateCount);
		DBInteger count2 = updateCountColumn2.count().asExpressionColumn();
		count2.setSortOrderDescending();

		DBQuery query2 = database
				.getDBQuery(marque2, new CarCompany());
		query2.setBlankQueryAllowed(true)
				.setReturnFieldsToNone()
				.addExpressionColumn("mode", updateCountColumn2.asExpressionColumn())
				.addExpressionColumn("mode count", count2)
				.setSortOrder(query2.column(count2))
				.setRowLimit(1)
				.setPageRequired(1);

		List<DBQueryRow> allRows1 = query1.getAllRows();
		List<DBQueryRow> allRows2 = query2.getAllRows();

		// Check there is only 1 row
		Assert.assertThat(allRows1.size(), is(1));
		Assert.assertThat(allRows2.size(), is(1));

		DBQueryRow onlyRow = allRows1.get(0);

		// Check that the mode is 2
		QueryableDatatype<?> mode = onlyRow.getExpressionColumnValue("mode");
		Assert.assertThat(mode.stringValue(), is("2"));

		// Check that the mode was found 9 times
		QueryableDatatype<?> counted = onlyRow.getExpressionColumnValue("mode count");
		Assert.assertThat(counted.stringValue(), is("9"));

		onlyRow = allRows2.get(0);

		// Check that the second mode is 0
		mode = onlyRow.getExpressionColumnValue("mode");
		Assert.assertThat(mode.stringValue(), is("0"));

		// Check that the mode was found 9 times
		counted = onlyRow.getExpressionColumnValue("mode count");
		Assert.assertThat(counted.stringValue(), is("4"));
	}

	@Test
	public void testModeStrictQueryWithCondition() throws SQLException {

		final Marque marque = new Marque();
		marque.updateCount.permittedValues(1, 3);
		final IntegerColumn updateCountColumn = marque.column(marque.updateCount);

		DBInteger count = updateCountColumn.count().asExpressionColumn();
		count.setSortOrderDescending();

		DBQuery query1 = database
				.getDBQuery(marque, new CarCompany());
		query1.setBlankQueryAllowed(true)
				.setReturnFieldsToNone()
				.addExpressionColumn("mode", updateCountColumn.asExpressionColumn())
				.addExpressionColumn("mode count", count)
				.setSortOrder(query1.column(count))
				.setRowLimit(1);

		final Marque marque2 = new Marque();
		marque2.updateCount.permittedValues(1, 3);
		final IntegerColumn updateCountColumn2 = marque2.column(marque2.updateCount);
		DBInteger count2 = updateCountColumn2.count().asExpressionColumn();
		count2.setSortOrderDescending();

		DBQuery query2 = database
				.getDBQuery(marque2, new CarCompany());
		query2.setBlankQueryAllowed(true)
				.setReturnFieldsToNone()
				.addExpressionColumn("mode", updateCountColumn2.asExpressionColumn())
				.addExpressionColumn("mode count", count2)
				.setSortOrder(query2.column(count2))
				.setRowLimit(1)
				.setPageRequired(1);

		List<DBQueryRow> allRows1 = query1.getAllRows();
		List<DBQueryRow> allRows2 = query2.getAllRows();

		// Check there is only 1 row
		Assert.assertThat(allRows1.size(), is(1));
		Assert.assertThat(allRows2.size(), is(1));

		DBQueryRow onlyRow = allRows1.get(0);

		// Check that the mode is 2
		QueryableDatatype<?> mode = onlyRow.getExpressionColumnValue("mode");
		Assert.assertThat(mode.stringValue(), isOneOf("1", "3"));

		// Check that the mode was found 9 times
		QueryableDatatype<?> counted = onlyRow.getExpressionColumnValue("mode count");
		Assert.assertThat(counted.stringValue(), is("3"));

		onlyRow = allRows2.get(0);

		// Check that the second mode is 0
		mode = onlyRow.getExpressionColumnValue("mode");
		Assert.assertThat(mode.stringValue(), isOneOf("1", "3"));

		// Check that the mode was found 9 times
		counted = onlyRow.getExpressionColumnValue("mode count");
		Assert.assertThat(counted.stringValue(), is("3"));
	}

	@Test
	public void testModeStrictExpression() throws SQLException {
		final Marque marque = new Marque();

		final IntegerColumn updateCountColumn = marque.column(marque.updateCount);

		DBInteger count = updateCountColumn.count().asExpressionColumn();
		count.setSortOrderDescending();
		DBQuery query = database
				.getDBQuery(marque)
				.setBlankQueryAllowed(true)
				.setReturnFieldsToNone()
				.addExpressionColumn("mode", updateCountColumn.modeStrict().asExpressionColumn());

		List<DBQueryRow> allRows = query.getAllRows();

		// Check there is only 1 row
		Assert.assertThat(allRows.size(), is(22));

		final DBQueryRow onlyRow = allRows.get(0);

		// Check that the mode is 2
		QueryableDatatype<?> mode = onlyRow.getExpressionColumnValue("mode");
		Assert.assertThat(mode.stringValue(), is("2"));
	}

	@Test
	public void testMedianQuery() throws SQLException {

		final Marque table1 = new Marque();
		final IntegerColumn updateCountColumn = table1.column(table1.updateCount);
		Marque table2 = new Marque(){};
		
		DBQuery dbQuery = database.getDBQuery(table1).add(table2).setBlankQueryAllowed(true);
		dbQuery.printSQLForQuery();
	}

	public static class StatsIntegerTest extends Marque {

		private static final long serialVersionUID = 1L;

		public StatsIntegerTest() {
		}

		@DBColumn
		public DBStatistics<Long, IntegerResult, DBInteger, IntegerExpression> carCoStats
				= new DBStatistics<Long, IntegerResult, DBInteger, IntegerExpression>(this.column(this.carCompany));

		{
			this.setReturnFields(carCoStats);
		}
	}

	public static class StatsStringTest extends Marque {

		private static final long serialVersionUID = 1L;

		public StatsStringTest() {
		}

		@DBColumn
		public DBStatistics<String, StringResult, DBString, StringExpression> carNameStats
				= new DBStatistics<String, StringResult, DBString, StringExpression>(
						this.column(this.name).substring(1, 2));

		{
			this.setReturnFields(carNameStats);
		}
	}

	public static class StatsOfUpdateCountTest extends Marque {

		private static final long serialVersionUID = 1L;

		public StatsOfUpdateCountTest() {
		}

		@DBColumn
		public DBInteger mode = this.column(this.updateCount).modeSimple().asExpressionColumn();

		@DBColumn
		public DBInteger modeStrict = this.column(this.updateCount).modeStrict().asExpressionColumn();

		{
			this.setReturnFields(mode);
		}
	}

}
