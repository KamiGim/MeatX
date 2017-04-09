package th.ac.ku.madlab.beefx;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.widget.Toast;

/**
 * Created by kami on 4/7/2017.
 */

public class DBManager {
    private SQLiteDatabase sqlDB;
    static final String DBName="meatx";
    static final String TableName="info";
    static final  int DBVersion=1;
    // create table Logins(ID integer primary key autoincrment, UserName text, Password text)
    static final  String CreateTable=" CREATE TABLE IF NOT EXISTS " +TableName+
            "([ID] INTEGER PRIMARY KEY AUTOINCREMENT,[longtitude] FLOAT, [latitude] FLOAT  NULL,\n" +
            "[device_id] VARCHAR(128)  NULL,\n" +
            "[sdX] FLOAT  NULL,\n" +
            "[sdY] FLOAT  NULL,\n" +
            "[fatPercent] FLOAT  ,\n" +
            "[countSmall] INTEGER  ,\n" +
            "[countMedium] INTEGER  ,\n" +
            "[countLarge] INTEGER  ,\n" +
            "[imgPath] VARCHAR(256)  ,\n" +
            "[img] BLOB  ,\n" +
            "[status] BOOLEAN ,\n"+
            "[created_at] DATETIME " +");";

    private static class  DatabaseHelperUser extends SQLiteOpenHelper {
        Context context;
        DatabaseHelperUser(Context context){
            super(context,DBName,null,DBVersion);
            this.context=context;

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CreateTable);
            Toast.makeText(context,"Table is created",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("Drop table IF  EXISTS "+ TableName);
            onCreate(db);
        }
    }



    public DBManager(Context context){

        DatabaseHelperUser db=new DatabaseHelperUser(context) ;
        sqlDB=db.getWritableDatabase();

    }

    public  long Insert(ContentValues values){
        long ID=   sqlDB.insert(TableName,"",values);
        //could insert id is user id, or fail id is or equal 0
        return ID;
    }
    //select username,Password from Logins where ID=1
    public Cursor query(String[] Projection, String Selection, String[] SelectionArgs, String SortOrder){

        SQLiteQueryBuilder qb= new SQLiteQueryBuilder();
        qb.setTables(TableName);

        Cursor cursor=qb.query(sqlDB,Projection,Selection,SelectionArgs,null,null,SortOrder);
        return cursor;
    }

    public int Delete(String Selection,String[] SelectionArgs){
        int count=sqlDB.delete(TableName,Selection,SelectionArgs);
        return count;
    }

    public  int Update(ContentValues values,String Selection,String[] SelectionArgs)
    {
        int count=sqlDB.update(TableName,values,Selection,SelectionArgs);
        return count;
    }

}