package com.example.gamebyna;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class koneksi extends SQLiteOpenHelper {
    private static final String DATABASE_NAME    = "db_game";
    private static final int    DATABASE_VERSION = 4;

    // Tabel User
    private static final String TABLE_USER          = "user";
    private static final String COLUMN_ID           = "id";
    private static final String COLUMN_USERNAME     = "username";
    private static final String COLUMN_PASSWORD     = "password";
    private static final String COLUMN_CONFIRM_PASS = "confirm_password";
    private static final String COLUMN_TOTAL_POINTS = "total_points";

    // Tabel Progress
    private static final String TABLE_PROGRESS      = "progress";
    private static final String COLUMN_PROGRESS_ID  = "progress_id";
    private static final String COLUMN_LEVEL        = "level";
    private static final String COLUMN_STARS        = "stars";
    private static final String COLUMN_TIME_USED    = "time_used";
    private static final String COLUMN_USER_FK      = "user_id";

    public koneksi(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT UNIQUE, "
                + COLUMN_PASSWORD + " TEXT, "
                + COLUMN_CONFIRM_PASS + " TEXT, "
                + COLUMN_TOTAL_POINTS + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_USER_TABLE);

        String CREATE_PROGRESS_TABLE = "CREATE TABLE " + TABLE_PROGRESS + " ("
                + COLUMN_PROGRESS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_LEVEL + " INTEGER, "
                + COLUMN_STARS + " INTEGER, "
                + COLUMN_TIME_USED + " INTEGER, "
                + COLUMN_USER_FK + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_USER_FK + ") REFERENCES "
                + TABLE_USER + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(CREATE_PROGRESS_TABLE);

        // Pre-register admin
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, "admin");
        cv.put(COLUMN_PASSWORD, "admin");
        cv.put(COLUMN_CONFIRM_PASS, "admin");
        cv.put(COLUMN_TOTAL_POINTS, 0);
        db.insert(TABLE_USER, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    /** Tambah user baru (kecuali admin) */
    public boolean addUser(String username, String password, String confirmPassword) {
        if ("admin".equalsIgnoreCase(username)) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, username);
        cv.put(COLUMN_PASSWORD, password);
        cv.put(COLUMN_CONFIRM_PASS, confirmPassword);
        long row = db.insert(TABLE_USER, null, cv);
        db.close();
        return row != -1;
    }

    /** Cek login */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(
                TABLE_USER,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null
        );
        boolean ok = c.getCount() > 0;
        c.close();
        db.close();
        return ok;
    }

    /** Cek username sudah terdaftar */
    public boolean checkUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(
                TABLE_USER,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null
        );
        boolean exists = c.getCount() > 0;
        c.close();
        db.close();
        return exists;
    }

    /** Cek apakah admin */
    public boolean isAdmin(String username) {
        return "admin".equalsIgnoreCase(username);
    }

    /** Ambil total_points untuk satu user */
    public int getTotalPointsForUser(String username) {
        if (username == null || username.isEmpty()) return 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(
                TABLE_USER,
                new String[]{COLUMN_TOTAL_POINTS},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null
        );
        int pts = 0;
        if (c.moveToFirst()) {
            pts = c.getInt(0);
        }
        c.close();
        db.close();
        return pts;
    }

    /** Ambil leaderboard (username + total_points) terurut desc, tanpa admin */
    public Cursor getLeaderboard() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT "
                + COLUMN_USERNAME + ", "
                + COLUMN_TOTAL_POINTS
                + " FROM " + TABLE_USER
                + " WHERE " + COLUMN_USERNAME + " <> 'admin'"
                + " ORDER BY " + COLUMN_TOTAL_POINTS + " DESC";
        return db.rawQuery(sql, null);
    }

    /** Simpan progress level & update total_points */
    public void updateProgress(int levelId, int stars, int timeUsed, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Insert ke tabel progress
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LEVEL,     levelId);
        cv.put(COLUMN_STARS,     stars);
        cv.put(COLUMN_TIME_USED, timeUsed);
        cv.put(COLUMN_USER_FK,   userId);
        db.insert(TABLE_PROGRESS, null, cv);

        // Update total_points di user
        int poin = (stars == 3 ? 10 : stars * 3);
        db.execSQL(
                "UPDATE " + TABLE_USER +
                        " SET " + COLUMN_TOTAL_POINTS + " = " + COLUMN_TOTAL_POINTS + " + ?" +
                        " WHERE " + COLUMN_ID + " = ?",
                new Object[]{ poin, userId }
        );
        db.close();
    }

    /** Ambil user ID dari username */
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userId = -1;
        Cursor c = db.query(
                TABLE_USER,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null
        );
        if (c.moveToFirst()) {
            userId = c.getInt(0);
        }
        c.close();
        db.close();
        return userId;
    }
}
