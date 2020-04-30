package com.totsp.bookworm.data.dao;
import com.totsp.bookworm.Constants;
import com.totsp.bookworm.data.DataConstants;
import com.totsp.bookworm.model.Author;
import com.totsp.bookworm.model.Book;
import com.totsp.bookworm.model.BookUserData;
import com.totsp.bookworm.model.Folder;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class FolderDAO implements DAO<Folder>  {

    private final SQLiteStatement FolderInsert;

    private static final String Folder_Insert =
            "insert into " + DataConstants.Folder_Table + "(" + DataConstants.NAME + ") values (?)";

    private static final String Query =
            "select fid, name from FOLDER";



    private SQLiteDatabase db;

    public FolderDAO(SQLiteDatabase db) {
        this.db = db;
        // statements
        FolderInsert = db.compileStatement(FolderDAO.Folder_Insert);
    }




    public static void onCreate(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();

        // author table
        sb.setLength(0);
        sb.append("CREATE TABLE " + DataConstants.Folder_Table + " (");
        sb.append(DataConstants.FID + " INTEGER PRIMARY KEY, ");
        sb.append(DataConstants.NAME + " TEXT");
        sb.append(");");
        db.execSQL(sb.toString());
    }

    public Cursor getCursor(final String orderBy, final String whereClauseLimit) {
        StringBuilder sb = new StringBuilder();
        sb.append(FolderDAO.Query);
        if ((whereClauseLimit != null) && (whereClauseLimit.length() > 0)) {
            sb.append(" " + whereClauseLimit);
        }
        sb.append(" group by fid");
        if ((orderBy != null) && (orderBy.length() > 0)) {
            sb.append(" order by " + orderBy);
        }

        return db.rawQuery(sb.toString(), null);
    }

    public Folder select(final long id) {
        Folder b = null;
        Cursor c =
                db.query(DataConstants.Folder_Table,
                        new String[] { DataConstants.FID, DataConstants.NAME}, DataConstants.FID + " = ?", new String[] { String
                                .valueOf(id) }, null, null, null, "1");
        if (c.moveToFirst()) {
            b = buildFolderFromFullQueryCursor(c);
        }
        if (!c.isClosed()) {
            c.close();
        }
        return b;
    }

    public ArrayList<Folder> selectByName(String fname){
        ArrayList<Folder> set = new ArrayList<Folder>();
        Cursor c =
                db.query(DataConstants.Folder_Table, new String[] { DataConstants.FID }, DataConstants.NAME + " = ?",
                        new String[] { fname }, null, null, DataConstants.NAME + " asc", null);
        if (c.moveToFirst()) {
            do {
                // makes an addtl query for every title, not the best approach here
                Folder b = select(c.getLong(0));
                set.add(b);
            } while (c.moveToNext());
        }
        if (!c.isClosed()) {
            c.close();
        }
        return set;
    }




    public  List<Folder> selectAll(){
        List<Folder>  x = new ArrayList<Folder> ();

        return x;
    }

    public ArrayList<Folder> selectAllFolderNames () {
        ArrayList<Folder> set = new ArrayList<Folder>();
        Cursor c =
                db.query(DataConstants.Folder_Table,
                        new String[] { DataConstants.FID, DataConstants.NAME}, null, null, null, null, DataConstants.NAME + " asc", null);
        if (c.moveToFirst()) {
            do {
                 Folder b = buildFolderFromFullQueryCursor(c);
                 set.add(b);
            } while (c.moveToNext());
        }
        if (!c.isClosed()) {
            c.close();
        }
        return set;
    }

    public Folder buildFolderFromFullQueryCursor(Cursor c) {
        Folder b = null;
        if (!c.isClosed()) {
            b = new Folder();
            b.fid = c.getLong(0);

            b.name = (c.getString(1));
        }
        return b;
    }

    public long insert(final Folder entity) {
        long folderID = 0L;
        if ((entity != null) && (entity.name != null)) {
            // use transaction
            db.beginTransaction();
            try {
                FolderInsert.clearBindings();
                FolderInsert.bindString(1, entity.name);
                folderID = FolderInsert.executeInsert();
                Log.d("folder inserted ", "folderID -- " + folderID);
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                folderID = 0;
                Log.e(Constants.LOG_TAG, "Error inserting book.", e);
            } finally {
                db.endTransaction();
            }
        }else {
            throw new IllegalArgumentException("Error, book cannot be null, and must have a title.");
        }
        return folderID;
    }


    public void deleteAll(){

    }

    public void update(final Folder entity){

    }

    public void delete(final long id){

    }



}
