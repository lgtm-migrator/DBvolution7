/*
 * Copyright 2013 Gregory Graham.
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

import java.sql.SQLException;
import java.util.List;
import java.util.SortedSet;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.*;
import nz.co.gregs.dbvolution.example.CarCompany;
import nz.co.gregs.dbvolution.example.CompanyLogo;
import nz.co.gregs.dbvolution.example.LinkCarCompanyAndLogo;
import nz.co.gregs.dbvolution.example.LinkCarCompanyAndLogoWithPreviousLink;
import nz.co.gregs.dbvolution.example.Marque;
import nz.co.gregs.dbvolution.example.MarqueSelectQuery;
import nz.co.gregs.dbvolution.exceptions.IncorrectRowProviderInstanceSuppliedException;
import nz.co.gregs.dbvolution.generic.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Gregory Graham
 */
public class DBQueryTest extends AbstractTest {

	public DBQueryTest(Object testIterationName, Object db) {
		super(testIterationName, db);
	}

	@Test
	public void testQueryGeneration() throws SQLException {
		DBQuery dbQuery = database.getDBQuery();
		CarCompany carCompany = new CarCompany();
		carCompany.name.permittedValues("TOYOTA");
		dbQuery.add(new Marque());
		dbQuery.add(carCompany);
//		final String generateSQLString = dbQuery.getSQLForQuery();//.replaceAll(" +", " ");
//
//		String expectedResultUsingONClause = "select __1997432637.numeric_code, __1997432637.uid_marque, __1997432637.isusedfortafros, __1997432637.fk_toystatusclass, __1997432637.intindallocallowed, __1997432637.upd_count, __1997432637.auto_created, __1997432637.name, __1997432637.pricingcodeprefix, __1997432637.reservationsalwd, __1997432637.creation_date, __1997432637.enabled, __1997432637.fk_carcompany, __78874071.name, __78874071.uid_carcompany from marque as __1997432637 inner join car_company as __78874071 on( ((__78874071.name = 'toyota')) and (__1997432637.fk_carcompany = __78874071.uid_carcompany) ) ;";
//		String expectedResultUsingWHEREClause   = "select __1997432637.numeric_code, __1997432637.uid_marque, __1997432637.isusedfortafros, __1997432637.fk_toystatusclass, __1997432637.intindallocallowed, __1997432637.upd_count, __1997432637.auto_created, __1997432637.name, __1997432637.pricingcodeprefix, __1997432637.reservationsalwd, __1997432637.creation_date, __1997432637.enabled, __1997432637.fk_carcompany, __78874071.name, __78874071.uid_carcompany from marque as __1997432637 inner join car_company as __78874071 on( __1997432637.fk_carcompany = __78874071.uid_carcompany ) where 1=1 and (__78874071.name = 'toyota') ;";
//		String expectedResultUsingWHEREClause2 = "select __78874071.name, __78874071.uid_carcompany, __1997432637.numeric_code, __1997432637.uid_marque, __1997432637.isusedfortafros, __1997432637.fk_toystatusclass, __1997432637.intindallocallowed, __1997432637.upd_count, __1997432637.auto_created, __1997432637.name, __1997432637.pricingcodeprefix, __1997432637.reservationsalwd, __1997432637.creation_date, __1997432637.enabled, __1997432637.fk_carcompany from car_company as __78874071 inner join marque as __1997432637 on( __1997432637.fk_carcompany = __78874071.uid_carcompany ) where 1=1 and (__78874071.name = 'toyota') ;";
//		String expectedResultUsingWHEREClause3 = "select __78874071.name, __78874071.uid_carcompany, __1997432637.numeric_code, __1997432637.uid_marque, __1997432637.isusedfortafros, __1997432637.fk_toystatusclass, __1997432637.intindallocallowed, __1997432637.upd_count, __1997432637.auto_created, __1997432637.name, __1997432637.pricingcodeprefix, __1997432637.reservationsalwd, __1997432637.creation_date, __1997432637.enabled, __1997432637.fk_carcompany from car_company __78874071 inner join marque __1997432637 on( __1997432637.fk_carcompany = __78874071.uid_carcompany ) where 1=1 and (__78874071.name = 'toyota')";
////		System.out.println(expectedResultUsingONClause);
////		System.out.println(generateSQLString);
//		Assert.assertThat(testableSQLWithoutColumnAliases(generateSQLString),
//				anyOf(is(testableSQLWithoutColumnAliases(expectedResultUsingONClause)),
//						is(testableSQLWithoutColumnAliases(expectedResultUsingWHEREClause)),
//						is(testableSQLWithoutColumnAliases(expectedResultUsingWHEREClause2)),
//						is(testableSQLWithoutColumnAliases(expectedResultUsingWHEREClause3))
//				));
		// make sure it works
		List<DBQueryRow> allRows = dbQuery.getAllRows();
		Assert.assertThat(allRows.size(), is(2));
	}

	@Test
	public void testQueryExecution() throws SQLException {
		DBQuery dbQuery = database.getDBQuery();
		CarCompany carCompany = new CarCompany();
		carCompany.name.permittedValues("TOYOTA");
		dbQuery.add(carCompany);
		dbQuery.add(new Marque());

		List<DBQueryRow> results = dbQuery.getAllRows();
		dbQuery.print();
		assertEquals(2, results.size());

		for (DBQueryRow queryRow : results) {

			CarCompany carCo = queryRow.get(carCompany);
			String carCoName = carCo.name.toString();

			Marque marque = queryRow.get(new Marque());
			Long marqueUID = marque.getUidMarque().getValue();

			System.out.println(carCoName + ": " + marqueUID);
			assertTrue(carCoName.equals("TOYOTA"));
			assertTrue(marqueUID == 1 || marqueUID == 4896300);
		}
	}

	@Test
	public void quickQueryCreation() throws SQLException {

		CarCompany carCompany = new CarCompany();
		carCompany.name.permittedValues("TOYOTA");
		DBQuery dbQuery = database.getDBQuery(carCompany, new Marque());

		List<DBQueryRow> results = dbQuery.getAllRows();
		System.out.println(dbQuery.getSQLForQuery());
		dbQuery.print();
		assertEquals(2, results.size());

		for (DBQueryRow queryRow : results) {

			CarCompany carCo = queryRow.get(carCompany);
			String carCoName = carCo.name.toString();

			Marque marque = queryRow.get(new Marque());
			Long marqueUID = marque.getUidMarque().getValue();

			System.out.println(carCoName + ": " + marqueUID);
			assertTrue(carCoName.equals("TOYOTA"));
			assertTrue(marqueUID == 1 || marqueUID == 4896300);
		}
	}

	@Test
	public void testDBTableRowReuse() throws SQLException {
		CarCompany carCompany = new CarCompany();
		carCompany.name.permittedValues("TOYOTA");
		DBQuery dbQuery = database.getDBQuery(carCompany, new Marque());

		List<DBQueryRow> results = dbQuery.getAllRows();
		System.out.println(dbQuery.getSQLForQuery());
		dbQuery.print();
		assertEquals(2, results.size());

		DBQueryRow[] rows = new DBQueryRow[2];
		rows = results.toArray(rows);

		CarCompany firstCarCo = rows[0].get(carCompany);
		CarCompany secondCarCo = rows[1].get(carCompany);
		assertTrue(firstCarCo == secondCarCo);
	}

	@Test
	public void testGettingRelatedInstances() throws SQLException {
		CarCompany carCompany = new CarCompany();
		DBQuery dbQuery = database.getDBQuery(carCompany, new Marque());
		dbQuery.setBlankQueryAllowed(true);

		CarCompany toyota = null;
		for (CarCompany carco : dbQuery.getAllInstancesOf(carCompany)) {
			if (carco.name.stringValue().equalsIgnoreCase("toyota")) {
				toyota = carco;
			}
		}

		if (toyota != null) {
			List<Marque> relatedInstances = toyota.getRelatedInstancesFromQuery(dbQuery, new Marque());

			database.print(relatedInstances);
			Assert.assertThat(relatedInstances.size(), is(2));
		} else {
			throw new RuntimeException("Unable To Find Toyota From The Query Results");
		}
	}

	@Test
	public void thrownExceptionIfTheForeignKeyFieldToBeIgnoredIsNotInTheInstance() throws Exception {
		Marque wrongMarque = new Marque();
		Marque marqueQuery = new Marque();
		marqueQuery.uidMarque.permittedValues(wrongMarque.uidMarque.getValue());

		DBQuery query = database.getDBQuery(marqueQuery, new CarCompany());
		try {
			marqueQuery.ignoreForeignKey(wrongMarque.carCompany);
			throw new RuntimeException("IncorrectDBRowInstanceSuppliedException should have been thrown");
		} catch (IncorrectRowProviderInstanceSuppliedException wrongDBRowEx) {
		}
		marqueQuery.ignoreForeignKey(marqueQuery.carCompany);
		query.setCartesianJoinsAllowed(true);
		List<DBQueryRow> rows = query.getAllRows();
//		System.out.println(query.getSQLForQuery());
//
//		System.out.println();
//		System.out.println(rows);
	}

	@Test
	public void thrownExceptionIfTheForeignKeyFieldsToBeIgnoredIsNotInTheInstance() throws Exception {
		Marque wrongMarque = new Marque();
		Marque marqueQuery = new Marque();
		marqueQuery.uidMarque.permittedValues(wrongMarque.uidMarque.getValue());

		DBQuery query = database.getDBQuery(marqueQuery, new CarCompany());
		try {
			marqueQuery.ignoreForeignKeys(wrongMarque.carCompany, wrongMarque.carCompany);
			throw new RuntimeException("IncorrectDBRowInstanceSuppliedException should have been thrown");
		} catch (IncorrectRowProviderInstanceSuppliedException wrongDBRowEx) {
		}
		marqueQuery.ignoreForeignKey(marqueQuery.carCompany);
		query.setCartesianJoinsAllowed(true);
		List<DBQueryRow> rows = query.getAllRows();
//		System.out.println(query.getSQLForQuery());
//
//		System.out.println();
//		System.out.println(rows);
	}

	@Test
	public void thrownExceptionIfTheForeignKeyColumnsToBeIgnoredIsNotInTheInstance() throws Exception {
		Marque wrongMarque = new Marque();
		Marque marqueQuery = new Marque();
		marqueQuery.uidMarque.permittedValues(wrongMarque.uidMarque.getValue());

		DBQuery query = database.getDBQuery(marqueQuery, new CarCompany());
		try {
			marqueQuery.ignoreForeignKeys(marqueQuery.column(wrongMarque.carCompany), marqueQuery.column(wrongMarque.carCompany));
			throw new RuntimeException("IncorrectDBRowInstanceSuppliedException should have been thrown");
		} catch (IncorrectRowProviderInstanceSuppliedException wrongDBRowEx) {
		}
		marqueQuery.ignoreForeignKeys(marqueQuery.column(marqueQuery.carCompany), marqueQuery.column(marqueQuery.carCompany));
		query.setCartesianJoinsAllowed(true);
		List<DBQueryRow> rows = query.getAllRows();
//		System.out.println(query.getSQLForQuery());
//
//		System.out.println();
//		System.out.println(rows);
	}

	@Test
	public void thrownExceptionIfAllTheForeignKeyFieldsToBeIgnoredIsNotInTheInstance() throws Exception {
		Marque wrongMarque = new Marque();
		Marque marqueQuery = new Marque();
		marqueQuery.uidMarque.permittedValues(wrongMarque.uidMarque.getValue());

		DBQuery query = database.getDBQuery(marqueQuery, new CarCompany());
		try {
			marqueQuery.ignoreAllForeignKeysExcept(wrongMarque.carCompany);
			throw new RuntimeException("IncorrectDBRowInstanceSuppliedException should have been thrown");
		} catch (IncorrectRowProviderInstanceSuppliedException wrongDBRowEx) {
		}
		marqueQuery.ignoreAllForeignKeysExcept(marqueQuery.carCompany);
		query.setCartesianJoinsAllowed(true);
		List<DBQueryRow> rows = query.getAllRows();
//		System.out.println(query.getSQLForQuery());
//
//		System.out.println();
//		System.out.println(rows);
	}

	@Test
	public void getRelatedTablesTest() {
		CarCompany carco = new CarCompany();
		SortedSet<DBRow> relatedTables = database.getDBQuery(carco).getRelatedTables();

		Assert.assertThat(relatedTables.size(), is(5));
		final DBRow[] rowArray = relatedTables.toArray(new DBRow[]{});
		Assert.assertEquals(CompanyLogo.class, rowArray[0].getClass());
		Assert.assertEquals(LinkCarCompanyAndLogo.class, rowArray[1].getClass());
		Assert.assertEquals(LinkCarCompanyAndLogoWithPreviousLink.class, rowArray[2].getClass());
		Assert.assertEquals(Marque.class, rowArray[3].getClass());
		Assert.assertEquals(MarqueSelectQuery.class, rowArray[4].getClass());
	}

	@Test
	public void getReferencedTablesTest() {
		CarCompany carco = new CarCompany();
		SortedSet<DBRow> relatedTables = database.getDBQuery(carco).getReferencedTables();

		Assert.assertThat(relatedTables.size(), is(0));
		
		relatedTables = database.getDBQuery(new Marque(), new LinkCarCompanyAndLogo()).getReferencedTables();
		
		Assert.assertThat(relatedTables.size(), is(2));
		
		final DBRow[] rowArray = relatedTables.toArray(new DBRow[]{});
		Assert.assertEquals(CarCompany.class, rowArray[0].getClass());
		Assert.assertEquals(CompanyLogo.class, rowArray[1].getClass());
	}
}
