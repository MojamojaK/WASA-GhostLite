package wasa.ghostlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.PrintStream;

public class MapDBQuery {

    private SQLiteDatabase db;
    private File dbpath;

    MapDBQuery(File dbpath) {
        this.dbpath = dbpath;
    }

    public void open(){
        db = SQLiteDatabase.openDatabase(dbpath.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
    }

    public void close(){
        db.close();
    }

    public void printByteToPrintStream(PrintStream ps, String x, String y, String z) {
        String queryString = "select tile_data from tiles where tile_column=? and tile_row=? and zoom_level=?";
        Cursor c = db.rawQuery(queryString, new String[]{x, y, z});
        try {
            if (!c.moveToFirst()) {
                c.close();
                return;
            }
            byte[] tmpByteArray = c.getBlob(c.getColumnIndex("tile_data"));
            int bodyLength = tmpByteArray.length;
            // System.out.println("bodyLength: " + Integer.toString(bodyLength));
            ps.println("Content-Length: " + Integer.toString(bodyLength));
            ps.println("");
            ps.write(tmpByteArray, 0, bodyLength);
            ps.flush();
        } finally {
            if (!c.isClosed()) c.close();
        }
    }

}
