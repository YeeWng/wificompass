/*
 * Created on Dec 21, 2011
 * Author: Paul Woelfel
 * Email: frig@frig.at
 */
package at.fhstp.wificompass.model.helper;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import at.fhstp.wificompass.Logger;
import at.fhstp.wificompass.R;
import at.fhstp.wificompass.model.AccessPoint;
import at.fhstp.wificompass.model.BssidResult;
import at.fhstp.wificompass.model.Location;
import at.fhstp.wificompass.model.Project;
import at.fhstp.wificompass.model.ProjectSite;
import at.fhstp.wificompass.model.SensorData;
import at.fhstp.wificompass.model.WifiScanResult;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	protected static final Logger log = new Logger(DatabaseHelper.class);

	// name of the database file for your application -- change to something appropriate for your app
	public static final String DATABASE_NAME = "wificompass.db";

	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 19;

	protected Context context;

	protected static final Class<?>[] ormClasses = { Project.class, ProjectSite.class, WifiScanResult.class, BssidResult.class, SensorData.class, AccessPoint.class,Location.class };

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
		// to speed up, we could use a config file
		// must be generated by ormlite.sh or
		// java -classpath lib/ormlite-core-4.31.jar:lib/ormlite-android-4.31.jar:bin/res:bin/classes com.j256.ormlite.android.apptools.OrmLiteConfigUtil ormlite_config.txt
		// super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}
	
	/**
	 * @param context
	 * @param name
	 */
	public DatabaseHelper(Context context,String name){
		super(context, name, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
		try {
			Logger.d("Database Helper onCreate");

			for (int i = 0; i < ormClasses.length; i++) {
				log.debug("creating table :"+ormClasses[i]);
				TableUtils.createTable(this.getConnectionSource(), ormClasses[i]);
			}

		} catch (SQLException e) {
			log.error("could not create tables!", e);
			Toast.makeText(context, R.string.database_create_failed, Toast.LENGTH_LONG);
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		Logger.d("Database Helper onUpgrade");
		if (oldVersion < newVersion) {
			Logger.i("Database outdated, updateing from "+oldVersion+" to "+newVersion+". Datbase should be "+DATABASE_VERSION);
			try {
				switch(oldVersion){
				
				case 16:
					
					Logger.i("Upgrading database to version 17");
					// AccessPoint has been changed, the boolean field calculated was added
					// since all data in the AccessPoint table are calcualted AccessPoints and there is much development ongoing,
					// it's ok to delete the table and recreate it.
					TableUtils.dropTable(getConnectionSource(), AccessPoint.class, false);
					TableUtils.createTable(getConnectionSource(), AccessPoint.class);
					
				case 17:
					
					Logger.i("Upgrading database to version 19");
					// 18 and 19 are the same
					Dao<ProjectSite,Integer> psDao=DaoManager.createDao(getConnectionSource(),ProjectSite.class);
					
					psDao.executeRaw("ALTER TABLE `"+ProjectSite.TABLE_NAME+"` ADD COLUMN `gridSpacingX` FLOAT DEFAULT 30;");
					psDao.executeRaw("ALTER TABLE `"+ProjectSite.TABLE_NAME+"` ADD COLUMN `gridSpacingY` FLOAT DEFAULT 30;");
					psDao.executeRaw("ALTER TABLE `"+ProjectSite.TABLE_NAME+"` ADD COLUMN `north` FLOAT DEFAULT 0;");
					
					
					// do not break on versions before, only last version should use break;
					break;
				
				default:
					log.warn("There are not instrunctions to handle a upgrade from this version, recreateing database!");
					recreateDatabase();
					break;
				}
				
								
			} catch (SQLException e) {
				log.error("could not update table structure!", e);
				Toast.makeText(context, R.string.database_create_failed, Toast.LENGTH_LONG);
			}
		}
	}
	
	
	public void recreateDatabase() throws SQLException{
		dropTables();
		createTables();
	}

	
	protected void dropTables() throws SQLException{
		for (int i = 0; i < ormClasses.length; i++) {
			log.debug("droping table :"+ormClasses[i]);
			TableUtils.dropTable(this.getConnectionSource(), ormClasses[i], true);
		}
	}
	
	protected void createTables() throws SQLException{
		for (int i = 0; i < ormClasses.length; i++) {
			log.debug("creating table :"+ormClasses[i]);
			TableUtils.createTable(this.getConnectionSource(), ormClasses[i]);
		}
	}
}
