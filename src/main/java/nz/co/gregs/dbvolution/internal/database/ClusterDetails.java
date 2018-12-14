/*
 * Copyright 2018 gregorygraham.
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
package nz.co.gregs.dbvolution.internal.database;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import nz.co.gregs.dbvolution.exceptions.NoAvailableDatabaseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import nz.co.gregs.dbvolution.DBRow;
import nz.co.gregs.dbvolution.actions.DBAction;
import nz.co.gregs.dbvolution.databases.DBDatabase;
import nz.co.gregs.dbvolution.databases.DBDatabaseCluster;
import nz.co.gregs.dbvolution.databases.DatabaseConnectionSettings;
import nz.co.gregs.dbvolution.exceptions.UnableToRemoveLastDatabaseFromClusterException;
import nz.co.gregs.dbvolution.reflection.DataModel;

/**
 *
 * @author gregorygraham
 */
public class ClusterDetails implements Serializable {

	private final static long serialVersionUID = 1l;

	private final List<DBDatabase> allDatabases = Collections.synchronizedList(new ArrayList<DBDatabase>(0));
	private final List<DBDatabase> unsynchronizedDatabases = Collections.synchronizedList(new ArrayList<DBDatabase>(0));
	private final List<DBDatabase> readyDatabases = Collections.synchronizedList(new ArrayList<DBDatabase>(0));
	private final List<DBDatabase> pausedDatabases = Collections.synchronizedList(new ArrayList<DBDatabase>(0));
	private final List<DBDatabase> quarantinedDatabases = Collections.synchronizedList(new ArrayList<DBDatabase>(0));

	private final Set<DBRow> requiredTables = Collections.synchronizedSet(DataModel.getRequiredTables());
	private final transient Map<DBDatabase, Queue<DBAction>> queuedActions = Collections.synchronizedMap(new HashMap<DBDatabase, Queue<DBAction>>(0));

	private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
	private String clusterName;
	private boolean useAutoRebuild;

	public ClusterDetails(String clusterName, boolean autoRebuild) {
		this.clusterName = clusterName;
		setAutoRebuild(autoRebuild);
	}

	public final synchronized boolean add(DBDatabase database) {
		if (clusterContainsDatabase(database)) {
			readyDatabases.remove(database);
			pausedDatabases.remove(database);
			quarantinedDatabases.remove(database);
			return unsynchronizedDatabases.add(database);
		} else {
			unsynchronizedDatabases.add(database);
			return allDatabases.add(database);
		}
	}

	public DBDatabase[] getAllDatabases() {
		synchronized (allDatabases) {
			return allDatabases.toArray(new DBDatabase[]{});
		}
	}

	public synchronized DBDatabaseCluster.Status getStatusOf(DBDatabase db) {
		final boolean ready = readyDatabases.contains(db);
		final boolean paused = pausedDatabases.contains(db);
		final boolean quarantined = quarantinedDatabases.contains(db);
		final boolean unsynched = unsynchronizedDatabases.contains(db);
		if (ready) {
			return DBDatabaseCluster.Status.READY;
		}
		if (paused) {
			return DBDatabaseCluster.Status.PAUSED;
		}
		if (quarantined) {
			return DBDatabaseCluster.Status.QUARANTINED;
		}
		if (unsynched) {
			return DBDatabaseCluster.Status.UNSYNCHRONISED;
		}
		return DBDatabaseCluster.Status.UNKNOWN;
	}

	public synchronized void quarantineDatabase(DBDatabase database, Exception except) throws UnableToRemoveLastDatabaseFromClusterException {
		if (readyDatabases.size() < 2 && readyDatabases.contains(database)) {
			// Unable to quarantine the only remaining database
			throw new UnableToRemoveLastDatabaseFromClusterException();
		} else {
			except.printStackTrace();
			database.setLastException(except);

			readyDatabases.remove(database);
			pausedDatabases.remove(database);
			unsynchronizedDatabases.remove(database);

			queuedActions.remove(database);

			quarantinedDatabases.add(database);
		}
	}

	public synchronized boolean removeDatabase(DBDatabase database) {
		if (readyDatabases.size() < 2 && readyDatabases.contains(database)) {
			// Unable to quarantine the only remaining database
			throw new UnableToRemoveLastDatabaseFromClusterException();
		} else {
			final boolean result = removeDatabaseFromAllLists(database);
			if (result) {
				setAuthoritativeDatabase();
			}
			return result;
		}
	}

	private synchronized boolean removeDatabaseFromAllLists(DBDatabase database) {
		boolean result = queuedActions.containsKey(database) ? queuedActions.remove(database) != null : true;
		result = result && quarantinedDatabases.contains(database) ? quarantinedDatabases.remove(database) : true;
		result = result && unsynchronizedDatabases.contains(database) ? unsynchronizedDatabases.remove(database) : true;
		result = result && pausedDatabases.contains(database) ? pausedDatabases.remove(database) : true;
		result = result && readyDatabases.contains(database) ? readyDatabases.remove(database) : true;
		result = result && allDatabases.contains(database) ? allDatabases.remove(database) : true;
		return result;
	}

	public synchronized DBDatabase[] getUnsynchronizedDatabases() {
		return unsynchronizedDatabases.toArray(new DBDatabase[]{});
	}

	public synchronized void synchronizingDatabase(DBDatabase db) {
		unsynchronizedDatabases.remove(db);
	}

	public Queue<DBAction> getActionQueue(DBDatabase db) {
		synchronized (queuedActions) {
			Queue<DBAction> queue = queuedActions.get(db);
			if (queue == null) {
				queue = new LinkedBlockingQueue<DBAction>();
				queuedActions.put(db, queue);
			}
			return queue;
		}
	}

	public DBRow[] getRequiredTables() {
		synchronized (requiredTables) {
			return requiredTables.toArray(new DBRow[]{});
		}
	}

	public synchronized void readyDatabase(DBDatabase secondary) {
		unsynchronizedDatabases.remove(secondary);
		pausedDatabases.remove(secondary);
		DBDatabase readyDatabase = getReadyDatabase();
		if (readyDatabase != null) {
			secondary.setPrintSQLBeforeExecuting(readyDatabase.getPrintSQLBeforeExecuting());
			secondary.setBatchSQLStatementsWhenPossible(readyDatabase.getBatchSQLStatementsWhenPossible());
		}
		readyDatabases.add(secondary);
		setAuthoritativeDatabase();
	}

	public synchronized DBDatabase[] getReadyDatabases() {
		if (readyDatabases == null || readyDatabases.isEmpty()) {
			return new DBDatabase[]{};
		} else {
			return readyDatabases.toArray(new DBDatabase[]{});
		}
	}

	public synchronized void pauseDatabase(DBDatabase template) {
		if (template != null) {
			readyDatabases.remove(template);
			pausedDatabases.add(template);
		}
	}

	public synchronized DBDatabase getPausedDatabase() {
		DBDatabase template = getReadyDatabase();
		pauseDatabase(template);
		return template;
	}

	public DBDatabase getReadyDatabase() {
		DBDatabase[] dbs = getReadyDatabases();
		int tries = 0;
		while (dbs.length < 1 && pausedDatabases.size() > 0 && tries <= 1000) {
			tries++;
			try {
				Thread.sleep(1);
			} catch (InterruptedException ex) {
				Logger.getLogger(ClusterDetails.class.getName()).log(Level.SEVERE, null, ex);
			}
			dbs = getReadyDatabases();
		}
		Random rand = new Random();
		if (dbs.length > 0) {
			final int randNumber = rand.nextInt(dbs.length);
			DBDatabase randomElement = dbs[randNumber];
			return randomElement;
		}
		return null;
	}

	public synchronized void addAll(DBDatabase[] databases) {
		for (DBDatabase database : databases) {
			add(database);
		}
	}

	public synchronized DBDatabase getTemplateDatabase() throws NoAvailableDatabaseException {
		final DatabaseConnectionSettings authoritativeDatabase = getAuthoritativeDatabase();
		if (allDatabases.isEmpty() && authoritativeDatabase != null) {
			try {
				return authoritativeDatabase.createDBDatabase();
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				Logger.getLogger(ClusterDetails.class.getName()).log(Level.SEVERE, null, ex);
				throw new NoAvailableDatabaseException();
			}
		} else {
			if (readyDatabases.isEmpty() && pausedDatabases.isEmpty()) {
				throw new NoAvailableDatabaseException();
			}
			return getPausedDatabase();
		}
	}

	private synchronized void setAuthoritativeDatabase() {
		if (useAutoRebuild) {
			for (DBDatabase db : allDatabases) {
				final String name = getClusterName();
				if (!db.isMemoryDatabase() && name != null && !name.isEmpty()) {
					final String encode = db.getSettings().encode();
					prefs.put(name, encode);
					return;
				}
			}
		}
	}

	private synchronized void removeAuthoritativeDatabase() {
		prefs.remove(getClusterName());
	}

	public DatabaseConnectionSettings getAuthoritativeDatabase() {
		if (useAutoRebuild) {
			String encodedSettings = prefs.get(getClusterName(), null);
			if (encodedSettings != null) {
				DatabaseConnectionSettings settings = DatabaseConnectionSettings.decode(encodedSettings);
				return settings;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public boolean clusterContainsDatabase(DBDatabase database) {
		if (database != null) {
			final DatabaseConnectionSettings newEncode = database.getSettings();
			for (DBDatabase db : allDatabases) {
				if (db.getSettings().equals(newEncode)) {
					return true;
				}
			}
		}
		return false;
	}

	public final void setAutoRebuild(boolean b) {
		useAutoRebuild = b;
		if (useAutoRebuild) {
			setAuthoritativeDatabase();
		} else {
			removeAuthoritativeDatabase();
		}
	}

	/**
	 * @return the clusterName
	 */
	public String getClusterName() {
		return clusterName;
	}

	/**
	 * @param clusterName the clusterName to set
	 */
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
		setAuthoritativeDatabase();
	}

	public List<DBDatabase> getQuarantinedDatabases() {
		return quarantinedDatabases;
	}

	public synchronized void removeAllDatabases() {
		DBDatabase[] dbs = allDatabases.toArray(new DBDatabase[]{});
		for (DBDatabase db : dbs) {
			removeDatabaseFromAllLists(db);
		}
	}

	public synchronized void dismantle() {
		removeAuthoritativeDatabase();
		removeAllDatabases();
	}

}
