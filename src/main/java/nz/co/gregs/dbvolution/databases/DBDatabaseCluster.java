/*
 * Copyright 2017 gregorygraham.
 *
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0/ 
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 * 
 * You are free to:
 *     Share - copy and redistribute the material in any medium or format
 *     Adapt - remix, transform, and build upon the material
 * 
 *     The licensor cannot revoke these freedoms as long as you follow the license terms.               
 *     Under the following terms:
 *                 
 *         Attribution - 
 *             You must give appropriate credit, provide a link to the license, and indicate if changes were made. 
 *             You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 *         NonCommercial - 
 *             You may not use the material for commercial purposes.
 *         ShareAlike - 
 *             If you remix, transform, or build upon the material, 
 *             you must distribute your contributions under the same license as the original.
 *         No additional restrictions - 
 *             You may not apply legal terms or technological measures that legally restrict others from doing anything the 
 *             license permits.
 * 
 * Check the Creative Commons website for any details, legalese, and updates.
 */
package nz.co.gregs.dbvolution.databases;

import nz.co.gregs.dbvolution.exceptions.UnableToRemoveLastDatabaseFromClusterException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import nz.co.gregs.dbvolution.DBQueryRow;
import nz.co.gregs.dbvolution.DBReport;
import nz.co.gregs.dbvolution.DBRow;
import nz.co.gregs.dbvolution.DBScript;
import nz.co.gregs.dbvolution.DBTable;
import nz.co.gregs.dbvolution.actions.DBAction;
import nz.co.gregs.dbvolution.actions.DBActionList;
import nz.co.gregs.dbvolution.actions.DBQueryable;
import nz.co.gregs.dbvolution.databases.definitions.ClusterDatabaseDefinition;
import nz.co.gregs.dbvolution.databases.definitions.DBDefinition;
import nz.co.gregs.dbvolution.exceptions.AccidentalDroppingOfTableException;
import nz.co.gregs.dbvolution.exceptions.AutoCommitActionDuringTransactionException;
import nz.co.gregs.dbvolution.exceptions.DBRuntimeException;
import nz.co.gregs.dbvolution.exceptions.UnableToCreateDatabaseConnectionException;
import nz.co.gregs.dbvolution.exceptions.UnableToFindJDBCDriver;
import nz.co.gregs.dbvolution.exceptions.UnexpectedNumberOfRowsException;
import nz.co.gregs.dbvolution.reflection.DataModel;
import nz.co.gregs.dbvolution.transactions.DBTransaction;

/**
 * Creates a database cluster programmatically.
 *
 * <p>
 * Clustering provides several benefits: automatic replication, reduced server
 * load on individual servers, improved server failure tolerance, and, with a
 * little programming, dynamic server replacement.</p>
 *
 * <p>
 * Please note that this class is not required to use database clusters provided
 * by database vendors. Use the normal DBDatabase subclass for those
 * vendors.</p>
 *
 * <p>
 * DBDatabaseCluster collects together several databases and ensures that all
 * actions are performed on all databases. This ensures that all databases stay
 * in synch and allows queries to be distributed to any database and produce the
 * same results. Different databases can be any supported database, for instance
 * the DBvolutionDemo application uses H2 and SQLite.</p>
 *
 * <p>
 * Upon creation, known tables and data are synchronized, the first database in
 * the cluster being used as the template. Added databases are synchronized
 * before being used</p>
 *
 * <p>
 * Automatically generated keys are still supported with a slight change: the
 * key will be generated in the first database and used as a literal value in
 * all other databases.
 *
 * @author gregorygraham
 */
public class DBDatabaseCluster extends DBDatabase {

	private static final long serialVersionUID = 1l;

	private final List<DBDatabase> allDatabases = Collections.synchronizedList(new ArrayList<DBDatabase>(0));
	private final List<DBDatabase> addedDatabases = Collections.synchronizedList(new ArrayList<DBDatabase>(0));
	private final List<DBDatabase> readyDatabases = Collections.synchronizedList(new ArrayList<DBDatabase>(0));
	private final Set<DBRow> requiredTables;
	private transient final DBStatementCluster clusterStatement;
	private final Map<DBDatabase, Queue<DBAction>> queuedActions;
	private transient final ExecutorService threadPool;

	public DBDatabaseCluster() throws SQLException {
		super();
		clusterStatement = new DBStatementCluster(this);
		requiredTables = Collections.synchronizedSet(DataModel.getRequiredTables());
		queuedActions = Collections.synchronizedMap(new HashMap<DBDatabase, Queue<DBAction>>(0));
		threadPool = Executors.newCachedThreadPool();
	}

	public DBDatabaseCluster(DBDatabase... databases) throws SQLException {
		this();
		setDefinition(new ClusterDatabaseDefinition());
		addedDatabases.addAll(Arrays.asList(databases));
		allDatabases.addAll(Arrays.asList(databases));
		synchronizeSecondaryDatabases();
	}

	/**
	 * Appends the specified element to the end of this list (optional operation).
	 *
	 * <p>
	 * Lists that support this operation may place limitations on what elements
	 * may be added to this list. In particular, some lists will refuse to add
	 * null elements, and others will impose restrictions on the type of elements
	 * that may be added. List classes should clearly specify in their
	 * documentation any restrictions on what elements may be added.
	 *
	 * @param database element to be appended to this list
	 * @return <tt>true</tt> if the database has been added to the cluster.
	 * @throws java.sql.SQLException
	 * @throws UnsupportedOperationException if the <tt>add</tt> operation is not
	 * supported by this list
	 * @throws ClassCastException if the class of the specified element prevents
	 * it from being added to this list
	 * @throws NullPointerException if the specified element is null and this list
	 * does not permit null elements
	 * @throws IllegalArgumentException if some property of this element prevents
	 * it from being added to this list
	 */
	public synchronized boolean addDatabase(DBDatabase database) throws SQLException {
		addedDatabases.add(database);
		boolean add = allDatabases.add(database);
		synchronizeAddedDatabases();
		return add;
	}

	public synchronized DBDatabase[] getDatabases() {
		return allDatabases.toArray(new DBDatabase[]{});
	}

	public synchronized DatabaseStatus getDatabaseStatus(DBDatabase db) {
		if (readyDatabases.contains(db)) {
			return DatabaseStatus.READY;
		} else if (addedDatabases.contains(db)) {
			return DatabaseStatus.ADDED;
		} else if (allDatabases.contains(db)) {
			return DatabaseStatus.SYNCHRONISED;
		} else {
			return DatabaseStatus.UNKNOWN;
		}
	}

	/**
	 * Removes the first occurrence of the specified element from this list, if it
	 * is present (optional operation). If this list does not contain the element,
	 * it is unchanged. More formally, removes the element with the lowest index
	 * <tt>i</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
	 * (if such an element exists). Returns <tt>true</tt> if this list contained
	 * the specified element (or equivalently, if this list changed as a result of
	 * the call).
	 *
	 * @param databases DBDatabases to be removed from this list, if present
	 * @return <tt>true</tt> if this list contained the specified element
	 * @throws ClassCastException if the type of the specified element is
	 * incompatible with this list
	 * (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified element is null and this list
	 * does not permit null elements
	 * (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws UnsupportedOperationException if the <tt>remove</tt> operation is
	 * not supported by this list
	 */
	public synchronized boolean removeDatabases(List<DBDatabase> databases) throws UnableToRemoveLastDatabaseFromClusterException{
		return removeDatabases(databases.toArray(new DBDatabase[]{}));
	}

	/**
	 * Removes the first occurrence of the specified element from this list, if it
	 * is present (optional operation). If this list does not contain the element,
	 * it is unchanged. More formally, removes the element with the lowest index
	 * <tt>i</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
	 * (if such an element exists). Returns <tt>true</tt> if this list contained
	 * the specified element (or equivalently, if this list changed as a result of
	 * the call).
	 *
	 * @param databases DBDatabases to be removed from this list, if present
	 * @return <tt>true</tt> if this list contained the specified element
	 * @throws ClassCastException if the type of the specified element is
	 * incompatible with this list
	 * (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified element is null and this list
	 * does not permit null elements
	 * (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws UnsupportedOperationException if the <tt>remove</tt> operation is
	 * not supported by this list
	 */
	public synchronized boolean removeDatabases(DBDatabase... databases) throws UnableToRemoveLastDatabaseFromClusterException{
		for (DBDatabase database : databases) {
			removeDatabase(database);
		}
		return true;
	}

	/**
	 * Removes the first occurrence of the specified element from this list, if it
	 * is present (optional operation). If this list does not contain the element,
	 * it is unchanged. More formally, removes the element with the lowest index
	 * <tt>i</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
	 * (if such an element exists). Returns <tt>true</tt> if this list contained
	 * the specified element (or equivalently, if this list changed as a result of
	 * the call).
	 *
	 * @param database DBDatabase to be removed from this list, if present
	 * @return <tt>true</tt> if this list contained the specified element
	 * @throws ClassCastException if the type of the specified element is
	 * incompatible with this list
	 * (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified element is null and this list
	 * does not permit null elements
	 * (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws UnsupportedOperationException if the <tt>remove</tt> operation is
	 * not supported by this list
	 */
	public synchronized boolean removeDatabase(DBDatabase database) throws UnableToRemoveLastDatabaseFromClusterException {
		if (readyDatabases.size() == 1 && readyDatabases.get(0).equals(database)) {
			// Unable to remove the only remaining database
			throw new UnableToRemoveLastDatabaseFromClusterException();
		} else {
			queuedActions.remove(database);
			allDatabases.remove(database);
			readyDatabases.remove(database);
		}
		return true;
	}

	/**
	 * Returns a single random database that is ready for queries
	 *
	 * @return a ready database
	 */
	public synchronized DBDatabase getReadyDatabase() {
		Random rand = new Random();
		DBDatabase[] dbs = getReadyDatabases();
		DBDatabase randomElement = dbs[rand.nextInt(dbs.length)];
		return randomElement;
	}

	@Override
	public void addFeatureToFixException(Exception exp) throws Exception {
		throw new UnsupportedOperationException("DBDatabase.addFeatureToFixException(Exception) should not be called");
	}

	@Override
	protected void addDatabaseSpecificFeatures(Statement statement) throws SQLException {
		throw new UnsupportedOperationException("DBDatabase.addDatabaseSpecificFeatures(Statement) should not be called");
	}

	@Override
	public synchronized void discardConnection(Connection connection) {
		throw new UnsupportedOperationException("DBDatabase.discardConnection() should not be called");
	}

	@Override
	public synchronized void unusedConnection(Connection connection) throws SQLException {
		throw new UnsupportedOperationException("DBDatabase.unusedConnection() should not be called");
	}

	@Override
	public Connection getConnectionFromDriverManager() throws SQLException {
		throw new UnsupportedOperationException("DBDatabase.getConnectionFromDriverManager() should not be called");
	}

	@Override
	public synchronized void preventDroppingOfDatabases(boolean justLeaveThisAtTrue) {
		DBDatabase[] dbs = getReadyDatabases();
		for (DBDatabase next : dbs) {
			next.preventDroppingOfDatabases(justLeaveThisAtTrue);
		}
	}

	@Override
	public synchronized void preventDroppingOfTables(boolean droppingTablesIsAMistake) {
		DBDatabase[] dbs = getReadyDatabases();
		for (DBDatabase next : dbs) {
			next.preventDroppingOfTables(droppingTablesIsAMistake);
		}
	}

	@Override
	public synchronized void setBatchSQLStatementsWhenPossible(boolean batchSQLStatementsWhenPossible) {
		DBDatabase[] dbs = getReadyDatabases();
		for (DBDatabase next : dbs) {
			next.setBatchSQLStatementsWhenPossible(batchSQLStatementsWhenPossible);
		}
	}

	@Override
	public synchronized boolean batchSQLStatementsWhenPossible() {
		boolean result = true;
		DBDatabase[] dbs = getReadyDatabases();
		for (DBDatabase next : dbs) {
			result &= next.batchSQLStatementsWhenPossible();
		}
		return result;
	}

	@Override
	public synchronized void dropDatabase(String databaseName, boolean doIt) throws Exception, UnsupportedOperationException, AutoCommitActionDuringTransactionException {
		boolean finished = false;
		do {
			DBDatabase[] dbs = getReadyDatabases();
			for (DBDatabase next : dbs) {
				try {
					next.dropDatabase(databaseName, doIt);
					finished = true;
				} catch (Exception e) {
//					try {
						handleExceptionDuringQuery(e, next);
//					} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//						throw e;
//					}
				}
			}
		} while (!finished);
	}

	@Override
	public synchronized void dropDatabase(boolean doIt) throws Exception, UnsupportedOperationException, AutoCommitActionDuringTransactionException {
		boolean finished = false;
		do {
			DBDatabase[] dbs = getReadyDatabases();
			for (DBDatabase next : dbs) {
				try {
					next.dropDatabase(doIt);
					finished = true;
				} catch (Exception e) {
//					try {
						handleExceptionDuringQuery(e, next);
//					} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//						throw e;
//					}
				}
			}
		} while (!finished);
	}

	@Override
	public boolean willCreateBlankQuery(DBRow row) {
		return getReadyDatabase().willCreateBlankQuery(row);
	}

	@Override
	public synchronized <TR extends DBRow> void dropTableNoExceptions(TR tableRow) throws AccidentalDroppingOfTableException, AutoCommitActionDuringTransactionException {
		try{boolean finished = false;
		do {
			DBDatabase[] dbs = getReadyDatabases();
			for (DBDatabase next : dbs) {
				try {
					next.dropTableNoExceptions(tableRow);
					finished = true;
				} catch (Exception e) {
//					try {
						handleExceptionDuringQuery(e, next);
//					} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//						throw e;
//					}
				}
			}
		} while (!finished);
	}catch(SQLException e){}}

	@Override
	public synchronized void dropTable(DBRow tableRow) throws SQLException, AutoCommitActionDuringTransactionException, AccidentalDroppingOfTableException {
		boolean finished = false;
		do {
			DBDatabase[] dbs = getReadyDatabases();
			for (DBDatabase next : dbs) {
				try {
					next.dropTable(tableRow);
					finished = true;
				} catch (Exception e) {
//					try {
						handleExceptionDuringQuery(e, next);
//					} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//						throw e;
//					}
				}
			}
		} while (!finished);
	}

	@Override
	public synchronized void createIndexesOnAllFields(DBRow newTableRow) throws SQLException {
		boolean finished = false;
		do {
			DBDatabase[] dbs = getReadyDatabases();
			for (DBDatabase next : dbs) {
				try {
					next.createIndexesOnAllFields(newTableRow);
					finished = true;
				} catch (Exception e) {
//					try {
						handleExceptionDuringQuery(e, next);
//					} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//						throw e;
//					}
				}
			}
		} while (!finished);
	}

	@Override
	public synchronized void removeForeignKeyConstraints(DBRow newTableRow) throws SQLException {
		boolean finished = false;
		do {
			DBDatabase[] dbs = getReadyDatabases();
			for (DBDatabase next : dbs) {
				try {
					next.removeForeignKeyConstraints(newTableRow);
					finished = true;
				} catch (Exception e) {
//					try {
						handleExceptionDuringQuery(e, next);
//					} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//						throw e;
//					}
				}
			}
		} while (!finished);
	}

	@Override
	public synchronized void createForeignKeyConstraints(DBRow newTableRow) throws SQLException {
		boolean finished = false;
		do {
			DBDatabase[] dbs = getReadyDatabases();
			for (DBDatabase next : dbs) {
				try {
					next.createForeignKeyConstraints(newTableRow);
					finished = true;
				} catch (Exception e) {
//					try {
						handleExceptionDuringQuery(e, next);
//					} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//						throw e;
//					}
				}
			}
		} while (!finished);
	}

	@Override
	public synchronized void createTableWithForeignKeys(DBRow newTableRow) throws SQLException, AutoCommitActionDuringTransactionException {
		boolean finished = false;
		do {
			DBDatabase[] dbs = getReadyDatabases();
			for (DBDatabase next : dbs) {
				try {
					next.createTableWithForeignKeys(newTableRow);
					finished = true;
				} catch (Exception e) {
//					try {
						handleExceptionDuringQuery(e, next);
//					} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//						throw e;
//					}
				}
			}
		} while (!finished);
	}

	@Override
	public synchronized void createTable(DBRow newTableRow) throws SQLException, AutoCommitActionDuringTransactionException {
		boolean finished = false;
		do {
			DBDatabase[] dbs = getReadyDatabases();
			for (DBDatabase next : dbs) {
				try {
					next.createTable(newTableRow);
					finished = true;
				} catch (Exception e) {
//					try {
						handleExceptionDuringQuery(e, next);
//					} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//						throw e;
//					}
				}
			}
		} while (!finished);
	}

	@Override
	public synchronized void createTablesWithForeignKeysNoExceptions(DBRow... newTables) {try{
		boolean finished = false;
		do {
			DBDatabase[] dbs = getReadyDatabases();
			for (DBDatabase next : dbs) {
				try {
					next.createTablesWithForeignKeysNoExceptions(newTables);
					finished = true;
				} catch (Exception e) {
//					try {
						handleExceptionDuringQuery(e, next);
//					} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//						throw e;
//					}
				}
			}
		} while (!finished);}catch(SQLException e){}
	}

	@Override
	public synchronized void createTablesNoExceptions(DBRow... newTables)  {
		try{
		boolean finished = false;
		do {
			DBDatabase[] dbs = getReadyDatabases();
			for (DBDatabase next : dbs) {
				try {
					next.createTablesNoExceptions(newTables);
					finished = true;
				} catch (Exception e) {
//					try {
						handleExceptionDuringQuery(e, next);
//					} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//						throw e;
//					}
				}
			}
		} while (!finished);
	}catch(SQLException e){}}

	@Override
	public synchronized void createTablesNoExceptions(boolean includeForeignKeyClauses, DBRow... newTables) {
		try {
			boolean finished = false;
			do {
				DBDatabase[] dbs = getReadyDatabases();
				for (DBDatabase next : dbs) {
					try {
						next.createTablesNoExceptions(includeForeignKeyClauses, newTables);
						finished = true;
					} catch (Exception e) {
//					try {
						handleExceptionDuringQuery(e, next);
//					} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//						throw e;
//					}
					}
				}
			} while (!finished);
		} catch (SQLException e) {
		}
	}

	@Override
	public synchronized void createTableNoExceptions(DBRow newTable) throws AutoCommitActionDuringTransactionException {
		try {
			boolean finished = false;
			do {
				DBDatabase[] dbs = getReadyDatabases();
				for (DBDatabase next : dbs) {
					try {
						next.createTableNoExceptions(newTable);
						finished = true;
					} catch (Exception e) {
//					try {
						handleExceptionDuringQuery(e, next);
//					} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//						throw e;
//					}
					}
				}
			} while (!finished);
		} catch (SQLException e) {
		}
	}

	@Override
	public synchronized void createTableNoExceptions(boolean includeForeignKeyClauses, DBRow newTable) throws AutoCommitActionDuringTransactionException {
		try {
			boolean finished = false;
			do {
				DBDatabase[] dbs = getReadyDatabases();
				for (DBDatabase next : dbs) {
					try {
						next.createTableNoExceptions(includeForeignKeyClauses, newTable);
						finished = true;
					} catch (Exception e) {
//					try {
						handleExceptionDuringQuery(e, next);
//					} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//						throw e;
//					}
					}
				}
			} while (!finished);
		} catch (SQLException e) {
		}
	}

	@Override
	public DBActionList test(DBScript script) throws Exception {
		return getReadyDatabase().test(script);
	}

	@Override
	public <V> V doReadOnlyTransaction(DBTransaction<V> dbTransaction) throws SQLException, Exception {
		return getReadyDatabase().doReadOnlyTransaction(dbTransaction);
	}

	@Override
	public synchronized <V> V doTransaction(DBTransaction<V> dbTransaction, Boolean commit) throws SQLException, Exception {
		V result = null;
		boolean rollbackAll = false;
		List<DBDatabase> transactionDatabases = new ArrayList<>();
		try {
			for (DBDatabase database : readyDatabases) {
				DBDatabase db;
				db = database.clone();
				transactionDatabases.add(db);
				V returnValues = null;
				db.transactionStatement = db.getDBTransactionStatement();
				try {
					db.isInATransaction = true;
					db.transactionConnection = db.transactionStatement.getConnection();
					db.transactionConnection.setAutoCommit(false);
					try {
						returnValues = dbTransaction.doTransaction(db);
						if (!commit) {
							try {
								db.transactionConnection.rollback();
							} catch (SQLException rollbackFailed) {
								discardConnection(db.transactionConnection);
							}
						}
					} catch (Exception ex) {
						try {
							if (!explicitCommitActionRequired) {
								db.transactionConnection.rollback();
							}
						} catch (SQLException excp) {
							LOG.warn("Exception Occurred During Rollback: " + ex.getMessage());
						}
						throw ex;
					}
				} finally {
				}
				result = returnValues;
			}
		} catch (Exception exc) {
			rollbackAll = true;
		} finally {
			for (DBDatabase db : transactionDatabases) {
				if (commit) {
					if (rollbackAll) {
						db.transactionConnection.rollback();
					} else {
						db.transactionConnection.commit();
					}
				}
				db.isInATransaction = false;
				db.transactionStatement.transactionFinished();
				db.discardConnection(db.transactionConnection);
				db.transactionConnection = null;
				db.transactionStatement = null;
			}
		}
		return result;
	}

	@Override
	public <A extends DBReport> List<A> getRows(A report, DBRow... examples) throws SQLException {
		DBDatabase readyDatabase;
		boolean finished = false;
		do {
			readyDatabase = getReadyDatabase();
			try {
				return readyDatabase.getRows(report, examples);
			} catch (Exception e) {
//				try {
				handleExceptionDuringQuery(e, readyDatabase);
//				} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//					throw e;
//				}
			}
		} while (!finished);
		return new ArrayList<>(0);
	}

	@Override
	public <A extends DBReport> List<A> getAllRows(A report, DBRow... examples) throws SQLException {
		DBDatabase readyDatabase;
		boolean finished = false;
		do {
			readyDatabase = getReadyDatabase();
			try {
				return readyDatabase.getAllRows(report, examples);
			} catch (Exception e) {
//				try {
				handleExceptionDuringQuery(e, readyDatabase);
//				} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//					throw e;
//				}
			}
		} while (!finished);
		return new ArrayList<>(0);
	}

	@Override
	public <A extends DBReport> List<A> get(A report, DBRow... examples) throws SQLException {
		DBDatabase readyDatabase;
		boolean finished = false;
		do {
			readyDatabase = getReadyDatabase();
			try {
				return readyDatabase.get(report, examples);
			} catch (Exception e) {
//				try {
				handleExceptionDuringQuery(e, readyDatabase);
//				} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//					throw e;
//				}
			}
		} while (!finished);
		return new ArrayList<>(0);
	}

	@Override
	public List<DBQueryRow> get(Long expectedNumberOfRows, DBRow... rows) throws SQLException, UnexpectedNumberOfRowsException {
		DBDatabase readyDatabase;
		boolean finished = false;
		do {
			readyDatabase = getReadyDatabase();
			try {
				return readyDatabase.get(expectedNumberOfRows, rows);
			} catch (Exception e) {
//				try {
				handleExceptionDuringQuery(e, readyDatabase);
//				} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//					throw e;
//				}
			}
		} while (!finished);
		return new ArrayList<>(0);
	}

	@Override
	public List<DBQueryRow> getByExamples(DBRow... rows) throws SQLException {
		DBDatabase readyDatabase;
		boolean finished = false;
		do {
			readyDatabase = getReadyDatabase();
			try {
				return readyDatabase.getByExamples(rows);
			} catch (Exception e) {
//				try {
				handleExceptionDuringQuery(e, readyDatabase);
//				} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//					throw e;
//				}
			}
		} while (!finished);
		return new ArrayList<>(0);
	}

	@Override
	public List<DBQueryRow> get(DBRow... rows) throws SQLException {
		DBDatabase readyDatabase;
		boolean finished = false;
		do {
			readyDatabase = getReadyDatabase();
			try {
				return readyDatabase.get(rows);
			} catch (Exception e) {
//				try {
				handleExceptionDuringQuery(e, readyDatabase);
//				} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//					throw e;
//				}
			}
		} while (!finished);
		return new ArrayList<>(0);
	}

	@Override
	public <R extends DBRow> List<R> getByExample(Long expectedNumberOfRows, R exampleRow) throws SQLException, UnexpectedNumberOfRowsException {
		DBDatabase readyDatabase;
		boolean finished = false;
		do {
			readyDatabase = getReadyDatabase();
			try {
				return readyDatabase.getByExample(expectedNumberOfRows, exampleRow);
			} catch (Exception e) {
//				try {
				handleExceptionDuringQuery(e, readyDatabase);
//				} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//					throw e;
//				}
			}
		} while (!finished);
		return new ArrayList<R>(0);
	}

	@Override
	public <R extends DBRow> List<R> get(Long expectedNumberOfRows, R exampleRow) throws SQLException, UnexpectedNumberOfRowsException {
		DBDatabase readyDatabase;
		boolean finished = false;
		do {
			readyDatabase = getReadyDatabase();
			try {
				return readyDatabase.get(expectedNumberOfRows, exampleRow);
			} catch (Exception e) {
//				try {
				handleExceptionDuringQuery(e, readyDatabase);
//				} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//					throw e;
//				}
			}
		} while (!finished);
		return new ArrayList<R>(0);
	}

	@Override
	public <R extends DBRow> List<R> getByExample(R exampleRow) throws SQLException {
		DBDatabase readyDatabase;
		boolean finished = false;
		do {
			readyDatabase = getReadyDatabase();
			try {
				return readyDatabase.getByExample(exampleRow);
			} catch (Exception e) {
//				try {
				handleExceptionDuringQuery(e, readyDatabase);
//				} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//					throw e;
//				}
			}
		} while (!finished);
		return new ArrayList<R>(0);
	}

	@Override
	public <R extends DBRow> List<R> get(R exampleRow) throws SQLException {
		DBDatabase readyDatabase;
		boolean finished = false;
		do {
			readyDatabase = getReadyDatabase();
			try {
				return readyDatabase.get(exampleRow);
			} catch (Exception e) {
//				try {
				handleExceptionDuringQuery(e, readyDatabase);
//				} catch (UnableToRemoveLastDatabaseFromClusterException un) {
//					throw e;
//				}
			}
		} while (!finished);
		return new ArrayList<R>(0);
	}

	@Override
	public Connection getConnection() throws UnableToCreateDatabaseConnectionException, UnableToFindJDBCDriver, SQLException {
		throw new UnsupportedOperationException("DBDatabase.getConnection should not be used.");
	}

	@Override
	protected DBStatement getLowLevelStatement() throws UnableToCreateDatabaseConnectionException, UnableToFindJDBCDriver, SQLException {
		return clusterStatement;
	}

	@Override
	public DBDatabase clone() throws CloneNotSupportedException {
		return super.clone();
	}

	private void synchronizeSecondaryDatabases() throws SQLException {
		DBDatabase[] addedDBs;
		synchronized (addedDatabases) {
			addedDBs = addedDatabases.toArray(new DBDatabase[]{});
		}
		for (DBDatabase db : addedDBs) {
			addedDatabases.remove(db);

			//Do The Synchronising...
			synchronizeSecondaryDatabase(db);
		}
	}

	@Override
	public DBActionList executeDBAction(DBAction action) throws SQLException {
		addActionToQueue(action);
		List<ActionTask> tasks = new ArrayList<ActionTask>();
		DBActionList actionsPerformed = new DBActionList();
		try {
			DBDatabase readyDatabase = getReadyDatabase();
			boolean finished = false;
			do {
				try {
					if (action.requiresRunOnIndividualDatabaseBeforeCluster()) {
						// Because of autoincrement PKs we need to execute on one database first
						actionsPerformed = new ActionTask(this, readyDatabase, action).call();
						removeActionFromQueue(readyDatabase, action);
						finished = true;
					} else {
						finished = true;
					}
				} catch (Exception e) {
//					try {
					handleExceptionDuringAction(e, readyDatabase);
//					} catch (UnableToRemoveLastDatabaseFromClusterException lastDB) {
//						throw e;
//					}
				}
			} while (!finished && size() > 1);
			// Now execute on all the other databases
			for (DBDatabase next : readyDatabases) {
				if (action.runOnDatabaseDuringCluster(readyDatabase, next)) {
					final ActionTask task = new ActionTask(this, next, action);
					tasks.add(task);
					removeActionFromQueue(next, action);
				}
			}
			threadPool.invokeAll(tasks);
		} catch (InterruptedException ex) {
			Logger.getLogger(DBDatabaseCluster.class.getName()).log(Level.SEVERE, null, ex);
			throw new DBRuntimeException("Unable To Run Actions", ex);
		}
		if (actionsPerformed.isEmpty()) {
			actionsPerformed = tasks.get(0).getActionList();
		}
		return actionsPerformed;
	}

	@Override
	public DBQueryable executeDBQuery(DBQueryable query) throws SQLException, UnableToRemoveLastDatabaseFromClusterException {
		DBQueryable actionsPerformed = query;
		boolean finished = false;
		while (!finished) {
			final DBDatabase readyDatabase = getReadyDatabase();
			try {
				actionsPerformed = readyDatabase.executeDBQuery(query);
				finished = true;
			} catch (Exception e) {
				try {
					handleExceptionDuringQuery(e, readyDatabase);
				} catch (UnableToRemoveLastDatabaseFromClusterException lastDB) {
					throw new SQLException(e);
				}
			}
		}
		return actionsPerformed;
	}

	private void handleExceptionDuringQuery(Exception e, final DBDatabase readyDatabase) throws SQLException {
		if (size() == 1) {
			throw new SQLException(e);
		} else {
			removeDatabases(readyDatabase);
		}
	}

	private void handleExceptionDuringAction(Exception e, final DBDatabase readyDatabase) throws SQLException {
		if (size() == 1) {
			throw new SQLException(e);
		} else {
			removeDatabases(readyDatabase);
		}
	}

	@Override
	public String getSQLForDBQuery(DBQueryable query) {
		return this.getReadyDatabase().getSQLForDBQuery(query);
	}

	synchronized ArrayList<DBStatement> getDBStatements() throws SQLException {
		ArrayList<DBStatement> arrayList = new ArrayList<>();
		for (DBDatabase db : readyDatabases) {
			arrayList.add(db.getDBStatement());
		}
		return arrayList;
	}

	@Override
	public DBDefinition getDefinition() {
		return getReadyDatabase().getDefinition();
	}

	public synchronized DBDatabase getPrimaryDatabase() {
		if (readyDatabases.size() > 0) {
			return readyDatabases.get(0);
		} else {
			return allDatabases.get(0);
		}
	}

	@Override
	public synchronized void setPrintSQLBeforeExecuting(boolean b) {
		for (DBDatabase db : allDatabases) {
			db.setPrintSQLBeforeExecuting(b);
		}
	}

	private synchronized void addActionToQueue(DBAction action) {
		for (DBDatabase db : allDatabases) {
			queuedActions.get(db).add(action);
		}
	}

	private synchronized void removeActionFromQueue(DBDatabase database, DBAction action) {
		final Queue<DBAction> db = queuedActions.get(database);
		if (db != null) {
			db.remove(action);
		}
	}

	private synchronized void synchronizeSecondaryDatabase(DBDatabase secondary) throws SQLException {

		// Get some sort of lock
		DBDatabase primary = getPrimaryDatabase();
		synchronized (this) {
			// Create a action queue for the new database
			queuedActions.put(secondary, new LinkedBlockingQueue<DBAction>());
			// Check that we're not synchronising the reference database
			if (!primary.equals(secondary)) {
				for (DBRow table : requiredTables) {
					if (true) {
						if (primary.tableExists(table)) {
							// Make sure it exists in the new database
							if (secondary.tableExists(table) == false) {
								secondary.createTable(table);
							}
							// Check that the table has data
							final DBTable<DBRow> primaryTable = primary.getDBTable(table);
							final DBTable<DBRow> secondaryTable = secondary.getDBTable(table);
							final Long primaryTableCount = primaryTable.count();
							final Long secondaryTableCount = secondaryTable.count();
							if (primaryTableCount > 0) {
								final DBTable<DBRow> primaryData = primaryTable.setBlankQueryAllowed(true);
								// Check that the new database has data
								if (secondaryTableCount == 0) {
									List<DBRow> allRows = primaryData.getAllRows();
									secondaryTable.insert(allRows);
								} else if (!secondaryTableCount.equals(primaryTableCount)) {
									// Something is different in the data so correct it
									secondary.delete(secondaryTable.setBlankQueryAllowed(true).getAllRows());
									List<DBRow> allRows = primaryData.getAllRows();
									secondary.insert(allRows);
								}
							}
						}
					}
				}
			}
		}
		synchronizeActions(secondary);
		secondary.setExplicitCommitAction(true);
		readyDatabases.add(secondary);
	}

	private synchronized void synchronizeActions(DBDatabase db) throws SQLException {
		Queue<DBAction> queue = queuedActions.get(db);
		while (!queue.isEmpty()) {
			DBAction action = queue.remove();
			db.executeDBAction(action);
		}
	}

	private synchronized void synchronizeAddedDatabases() throws SQLException {
		for (DBDatabase addedDatabase : addedDatabases) {
			synchronizeSecondaryDatabase(addedDatabase);
		}
	}

	@Override
	public synchronized boolean tableExists(DBRow table) throws SQLException {
		boolean tableExists = true;
		for (DBDatabase readyDatabase : readyDatabases) {
			final boolean tableExists1 = readyDatabase.tableExists(table);
			tableExists &= tableExists1;
		}
		return tableExists;
	}

	/**
	 * Returns the number of ready databases.
	 *
	 * <p>
	 * The size of the cluster is dynamic as databases are added, removed, and
	 * synchronized but this method returns the size of the cluster in terms of
	 * active databases at this point in time.</p>
	 *
	 * <ul>
	 * <li>DBDatabaseClusters within this cluster count as 1 database each.</li>
	 * <li>Unsynchronized databases are not counted by this method.</li>
	 * </ul>.
	 *
	 * @return the number of ready database.
	 */
	public synchronized int size() {
		return getReadyDatabases().length;
	}

	private synchronized DBDatabase[] getReadyDatabases() {
		return readyDatabases.toArray(new DBDatabase[]{});
	}

	private static class ActionTask implements Callable<DBActionList> {

		private final DBDatabase database;
		private final DBAction action;
		private final DBDatabaseCluster cluster;
		private DBActionList actionList = new DBActionList();

		public ActionTask(DBDatabaseCluster cluster, DBDatabase db, DBAction action) {
			this.cluster = cluster;
			this.database = db;
			this.action = action;
		}

		@Override
		public DBActionList call() {
			try {
				DBActionList actions = database.executeDBAction(action);
				setActionList(actions);
				return getActionList();
			} catch (Exception e) {
				cluster.removeDatabases(database);
				System.out.println("REMOVING DATABASE:" + database.toString());
				System.out.println("" + e.getLocalizedMessage());
			}
			return getActionList();
		}

		public synchronized DBActionList getActionList() {
			final DBActionList newList = new DBActionList();
			newList.addAll(actionList);
			return newList;
		}

		private synchronized void setActionList(DBActionList actions) {
			this.actionList = actions;
		}
	}

	public static enum DatabaseStatus {

		READY,
		ADDED,
		SYNCHRONISED,
		STOPPED,
		DEAD,
		REJECTED,
		UNKNOWN
	}
}
