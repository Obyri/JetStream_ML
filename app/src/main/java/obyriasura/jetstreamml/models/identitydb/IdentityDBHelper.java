package obyriasura.jetstreamml.models.identitydb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by obyri on 4/11/15.
 */
public class IdentityDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "homestream";
    private static int DATABASE_VERSION = 2;

    public IdentityDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IDENTITY");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
