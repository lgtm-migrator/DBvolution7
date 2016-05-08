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
package nz.co.gregs.dbvolution.actions;

import java.util.ArrayList;
import java.util.List;
import nz.co.gregs.dbvolution.DBDatabase;
import nz.co.gregs.dbvolution.DBRow;
import nz.co.gregs.dbvolution.databases.definitions.DBDefinition;
import nz.co.gregs.dbvolution.datatypes.QueryableDatatype;
import nz.co.gregs.dbvolution.internal.properties.PropertyWrapper;

/**
 * Used by {@link DBInsert} to insert BLOB columns.
 *
 * @author Gregory Graham
 */
public class DBInsertLargeObjects extends DBUpdateLargeObjects {

	/**
	 * Creates a DBInsertLargeObjects action for the row.
	 *
	 * @param row the row to be inserted
	 */
	public DBInsertLargeObjects(DBRow row) {
		super(row);
	}

	/**
	 * Finds all the DBLargeObject fields that this action will need to update.
	 *
	 * @param row the row to be inserted
	 * @return a list of the interesting DBLargeObjects.
	 */
	@Override
	protected List<PropertyWrapper> getInterestingLargeObjects(DBRow row) {
		ArrayList<PropertyWrapper> returnList = new ArrayList<>();
		for (QueryableDatatype<?> qdt : row.getLargeObjects()) {
			returnList.add(row.getPropertyWrapperOf(qdt));
		}
		return returnList;
	}

	@Override
	protected DBActionList getActions() {
		return new DBActionList(new DBInsertLargeObjects(getRow()));
	}

	@Override
	protected String getPrimaryKeySQL(DBDefinition defn, DBRow row, DBDatabase db) {
		String sqlString = "";
		List<QueryableDatatype<?>> primaryKeys = row.getPrimaryKeys();
		String separator = "";
		for (QueryableDatatype<?> pk : primaryKeys) {
			PropertyWrapper wrapper = row.getPropertyWrapperOf(pk);
			String pkValue = pk.toSQLString(db);
			//String pkValue = (pk.hasChanged() ? pk.getPreviousSQLValue(db) : pk.toSQLString(db));
			sqlString += separator + defn.formatColumnName(wrapper.columnName()) + defn.getEqualsComparator() + pkValue;
			//				+ defn.formatColumnName(row.getPrimaryKeyColumnNames())
			//				+ defn.getEqualsComparator()
			//				+ row.getPrimaryKeys().toSQLString(db)
			separator = defn.beginAndLine();
		}
		return sqlString;
	}

}
