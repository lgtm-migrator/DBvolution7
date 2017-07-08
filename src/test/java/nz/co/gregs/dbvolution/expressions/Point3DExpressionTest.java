/*
 * Copyright 2015 gregorygraham.
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
package nz.co.gregs.dbvolution.expressions;

import com.vividsolutions.jts.geom.Coordinate;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import nz.co.gregs.dbvolution.*;
import nz.co.gregs.dbvolution.annotations.*;
import nz.co.gregs.dbvolution.datatypes.*;
import nz.co.gregs.dbvolution.datatypes.spatial3D.*;
import nz.co.gregs.dbvolution.generic.AbstractTest;
import static org.hamcrest.Matchers.*;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gregorygraham
 */
public class Point3DExpressionTest extends AbstractTest {

	final GeometryFactory3D geometryFactory = new GeometryFactory3D();

	public Point3DExpressionTest(Object testIterationName, DBDatabase db) throws SQLException {
		super(testIterationName, db);
		PointTestTable pointTestTable = new PointTestTable();

		db.preventDroppingOfTables(false);
		db.dropTableNoExceptions(pointTestTable);
		db.createTable(pointTestTable);

		pointTestTable.point.setValue(geometryFactory.createPointZ(new Coordinate(2, 3, 4)));
		db.insert(pointTestTable);

		pointTestTable = new PointTestTable();
		pointTestTable.point.setValue(geometryFactory.createPointZ(new Coordinate(4, 6, 4)));
		db.insert(pointTestTable);

		pointTestTable = new PointTestTable();
		pointTestTable.point.setValue(geometryFactory.createPointZ(new Coordinate(0, 0, 4)));
		db.insert(pointTestTable);
	}

	public static class PointTestTable extends DBRow {

		private static final long serialVersionUID = 1L;

		@DBColumn
		@DBPrimaryKey
		@DBAutoIncrement
		public DBInteger point_id = new DBInteger();

		@DBColumn("point_col")
		public DBPoint3D point = new DBPoint3D();
	}

	@Test
	public void testToWKTValue() throws SQLException {
		PointZ point = geometryFactory.createPointZ(new Coordinate(2.0, 3.0, 4.0));
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(Point3DExpression.value(point).toWKTFormat().is(pointTestTable.column(pointTestTable.point).toWKTFormat()));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(1));
		Assert.assertThat(allRows.get(0).point.pointZValue(), is(point));
	}

	@Test
	public void testValue() throws SQLException {
		PointZ point = geometryFactory.createPointZ(new Coordinate(2.0, 3.0, 4.0));
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(Point3DExpression.value(point).is(pointTestTable.column(pointTestTable.point)));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(1));
		Assert.assertThat(allRows.get(0).point.pointZValue(), is(point));
	}

	@Test
	public void testValueUsingLongs() throws SQLException {
		PointZ point = geometryFactory.createPointZ(new Coordinate(2.0, 3.0, 4.0));
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(Point3DExpression.value(2L,3L,4L).is(pointTestTable.column(pointTestTable.point)));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(1));
		Assert.assertThat(allRows.get(0).point.pointZValue(), is(point));
	}

	@Test
	public void testValueUsingPoint3DResult() throws SQLException {
		PointZ point = geometryFactory.createPointZ(new Coordinate(2.0, 3.0,4.0));
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(Point3DExpression.value(Point3DExpression.value(point)).is(pointTestTable.column(pointTestTable.point)));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(1));
		Assert.assertThat(allRows.get(0).point.pointZValue(), is(point));
	}

	@Test
	public void testGetQueryableDatatypeForExpressionValue() {
		Point3DExpression instance = new Point3DExpression();
		DBPoint3D result = instance.getQueryableDatatypeForExpressionValue();
		assertEquals(DBPoint3D.class, result.getClass());
	}

	@Test
	public void testCopy() {
		Point3DExpression instance = new Point3DExpression();
		Point3DExpression result = instance.copy();
		assertEquals(instance, result);
	}

	@Test
	public void testIsAggregator() {
		Point3DExpression instance = new Point3DExpression();
		boolean expResult = false;
		boolean result = instance.isAggregator();
		assertEquals(expResult, result);
	}

	@Test
	public void testGetTablesInvolved() {
		final PointTestTable pointTestTable = new PointTestTable();
		Point3DExpression instance = new Point3DExpression(pointTestTable.column(pointTestTable.point));
		Set<DBRow> result = instance.getTablesInvolved();
		Assert.assertThat(result.size(), is(1));
		DBRow[] resultArray = result.toArray(new DBRow[]{});
		DBRow aRow = resultArray[0];
		if (!(aRow instanceof PointTestTable)) {
			fail("Set should include PointTestTable");
		}
	}

	@Test
	public void testIsPurelyFunctional() {
		Point3DExpression instance = new Point3DExpression();
		boolean result = instance.isPurelyFunctional();
		Assert.assertThat(result, is(true));

		final PointTestTable pointTestTable = new PointTestTable();
		instance = new Point3DExpression(pointTestTable.column(pointTestTable.point));
		Assert.assertThat(instance.isPurelyFunctional(), is(false));
	}

	@Test
	public void testGetIncludesNull() {
		Point3DExpression instance = new Point3DExpression((PointZ) null);
		Assert.assertThat(instance.getIncludesNull(), is(true));

		final PointTestTable pointTestTable = new PointTestTable();
		instance = new Point3DExpression(pointTestTable.column(pointTestTable.point));
		Assert.assertThat(instance.getIncludesNull(), is(false));
	}

	@Test
	public void testStringResult() throws SQLException {
		PointZ point = geometryFactory.createPointZ(new Coordinate(2.0, 3.0, 4.0));
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(Point3DExpression.value(point).stringResult().is(pointTestTable.column(pointTestTable.point).stringResult()));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(1));
	}

	@Test
	public void testIs_Point() throws SQLException {
		PointZ point = geometryFactory.createPointZ(new Coordinate(2.0, 3.0, 4.0));
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).is(point));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(1));
	}

	@Test
	public void testIs_Point3DResult() throws SQLException {
		PointZ point = geometryFactory.createPointZ(new Coordinate(2.0, 3.0, 4.0));
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(Point3DExpression.value(point).is(pointTestTable.column(pointTestTable.point)));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(1));
	}

	@Test
	public void testIsNot_Point3D() throws SQLException {
		PointZ point = geometryFactory.createPointZ(new Coordinate(2.0, 3.0, 4.0));
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).isNot(point));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(2));
		Assert.assertThat(allRows.get(0).point_id.intValue(), isOneOf(2,3));
	}

	@Test
	public void testGetX() throws SQLException {
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).getX().is(2));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(1));

		dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).getX().is(4));
		allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(2));
	}

	@Test
	public void testGetY() throws SQLException {
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).getY().is(3));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(1));

		dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).getY().is(6));
		allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(2));
	}

	@Test
	public void testGetZ() throws SQLException {
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).getZ().is(4));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(3));

		dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).getZ().is(6));
		allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(0));
	}

	@Test
	public void testMaxX() throws SQLException {
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).maxX().is(2));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(1));

		dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).maxX().is(4));
		allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(2));
	}

	@Test
	public void testMaxY() throws SQLException {
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).maxY().is(3));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(1));

		dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).maxY().is(6));
		allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(2));
	}

	@Test
	public void testMinX() throws SQLException {
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).minX().is(2));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(1));

		dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).minX().is(4));
		allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(2));
	}

	@Test
	public void testMinY() throws SQLException {
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).minY().is(3));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(1));

		dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).minY().is(6));
		allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(2));
	}

	@Test
	public void testDimension() throws SQLException {
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).measurableDimensions().is(0));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(3));
	}

	@Test
	public void testHasMagnitude() throws SQLException {
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).hasMagnitude().isNot(Boolean.TRUE));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(3));
		
		dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).hasMagnitude().is(Boolean.FALSE));
		allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(3));
	}

	@Test
	public void testMagnitude() throws SQLException {
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).magnitude().isNull());
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(3));
	}

	@Test
	public void testSpatialDimension() throws SQLException {
		final PointTestTable pointTestTable = new PointTestTable();
		DBQuery dbQuery = database.getDBQuery(pointTestTable);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).spatialDimensions().is(3));
		List<PointTestTable> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		Assert.assertThat(allRows.size(), is(3));
	}

	public static class BoundingBoxTest extends PointTestTable {

		private static final long serialVersionUID = 1L;

		@DBColumn
		public DBString stringPoint = new DBString(this.column(this.point).stringResult().substringBetween("(", " "));
		@DBColumn
		public DBString stringAfter = new DBString(this.column(this.point).stringResult().substringAfter("("));
		@DBColumn
		public DBString stringBefore = new DBString(this.column(this.point).stringResult().substringBefore(" "));
		@DBColumn
		public DBNumber getX = new DBNumber(this.column(this.point).getX());
		@DBColumn
		public DBNumber getY = new DBNumber(this.column(this.point).getY());
		@DBColumn
		public DBPolygon3D boundingBox = new DBPolygon3D(this.column(this.point).boundingBox());
		@DBColumn
		public DBBoolean getXis2 = new DBBoolean(this.column(this.point).getX().is(2));

	}

	@Test
	public void testBoundingBox() throws SQLException {
		final BoundingBoxTest pointTestTable = new BoundingBoxTest();
		DBQuery dbQuery = database.getDBQuery(pointTestTable).setBlankQueryAllowed(true);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.point).getY().is(3));
		List<BoundingBoxTest> allRows = dbQuery.getAllInstancesOf(pointTestTable);

		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(1));
		final String boundingText = allRows.get(0).boundingBox.jtsPolygonValue().toText();
		Assert.assertThat(boundingText, is("POLYGON ((2 3 4, 2 3 4, 2 3 4, 2 3 4, 2 3 4))"));
	}

	public static class DistanceTest extends PointTestTable {

		private static final long serialVersionUID = 1L;

		@DBColumn
		public DBNumber getX = new DBNumber(this.column(this.point).getX());
		@DBColumn
		public DBNumber getY = new DBNumber(this.column(this.point).getY());
		@DBColumn
		public DBPoint3D point2 = new DBPoint3D(Point3DExpression.value(2, 3, 4));
		@DBColumn
		public DBNumber getX2 = new DBNumber(Point3DExpression.value(2, 3, 4).getX());
		@DBColumn
		public DBNumber getY2 = new DBNumber(Point3DExpression.value(2, 3, 4).getY());
		@DBColumn
		public DBNumber distance = new DBNumber(this.column(this.point).distanceTo(Point3DExpression.value(2, 3, 4)));
	}

	@Test
	public void testDistance() throws SQLException {
		final DistanceTest pointTestTable = new DistanceTest();
		DBQuery dbQuery = database.getDBQuery(pointTestTable).setBlankQueryAllowed(true);
		
		dbQuery.addCondition(pointTestTable.column(pointTestTable.distance).is(0));
		List<DistanceTest> allRows = dbQuery.getAllInstancesOf(pointTestTable);
		
		Assert.assertThat(allRows.size(), is(1));
		Assert.assertThat(allRows.get(0).point_id.intValue(), is(1));

		dbQuery = database.getDBQuery(pointTestTable).setBlankQueryAllowed(true);
		dbQuery.addCondition(pointTestTable.column(pointTestTable.distance).round(2).is(3.61));
		allRows = dbQuery.getAllInstancesOf(pointTestTable);
		
		Assert.assertThat(allRows.size(), is(2));
	}
}
