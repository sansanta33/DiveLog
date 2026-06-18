package com.example.manba;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CampusDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "dive_notes.db";
    private static final int DB_VERSION = 4;

    public CampusDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "created_at TEXT NOT NULL)");
        db.execSQL("CREATE TABLE dives (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "dive_type TEXT NOT NULL," +
                "location TEXT NOT NULL," +
                "dive_date TEXT NOT NULL," +
                "depth REAL NOT NULL," +
                "duration INTEGER NOT NULL," +
                "visibility TEXT NOT NULL," +
                "water_temp TEXT NOT NULL," +
                "buddy TEXT NOT NULL," +
                "fish_seen TEXT NOT NULL," +
                "note TEXT NOT NULL," +
                "ai_tip TEXT NOT NULL," +
                "created_at TEXT NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES users(id))");
        db.execSQL("CREATE TABLE notes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "dive_id INTEGER DEFAULT 0," +
                "title TEXT NOT NULL," +
                "content TEXT NOT NULL," +
                "mood TEXT NOT NULL," +
                "created_at TEXT NOT NULL," +
                "updated_at TEXT NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES users(id))");
        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS notes");
        db.execSQL("DROP TABLE IF EXISTS dives");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public long register(String username, String password) {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("created_at", now());
        return getWritableDatabase().insert("users", null, values);
    }

    public long login(String username, String password) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id FROM users WHERE username=? AND password=?",
                new String[]{username, password});
        try {
            return cursor.moveToFirst() ? cursor.getLong(0) : -1;
        } finally {
            cursor.close();
        }
    }

    public String getUsername(long userId) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT username FROM users WHERE id=?", new String[]{String.valueOf(userId)});
        try {
            return cursor.moveToFirst() ? cursor.getString(0) : "潜水员";
        } finally {
            cursor.close();
        }
    }

    public List<ActivityItem> getActivities(long userId, String filter) {
        List<ActivityItem> list = new ArrayList<>();
        String sql;
        String[] args;
        if ("全部".equals(filter)) {
            sql = "SELECT * FROM dives WHERE user_id=? ORDER BY dive_date DESC, id DESC";
            args = new String[]{String.valueOf(userId)};
        } else if ("深潜".equals(filter)) {
            sql = "SELECT * FROM dives WHERE user_id=? AND depth>=18 ORDER BY dive_date DESC, id DESC";
            args = new String[]{String.valueOf(userId)};
        } else {
            sql = "SELECT * FROM dives WHERE user_id=? AND dive_type=? ORDER BY dive_date DESC, id DESC";
            args = new String[]{String.valueOf(userId), filter};
        }
        Cursor cursor = getReadableDatabase().rawQuery(sql, args);
        try {
            while (cursor.moveToNext()) {
                list.add(readDive(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public ActivityItem getActivity(long id) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM dives WHERE id=?", new String[]{String.valueOf(id)});
        try {
            return cursor.moveToFirst() ? readDive(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    public long addActivity(long userId, String title, String diveType, String location,
                            String date, double depth, int duration, String visibility,
                            String waterTemp, String buddy, String fishSeen, String note) {
        String aiTip = buildAiTip(depth, duration, visibility, waterTemp, fishSeen, note);
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("title", title);
        values.put("dive_type", diveType);
        values.put("location", location);
        values.put("dive_date", date);
        values.put("depth", depth);
        values.put("duration", duration);
        values.put("visibility", visibility);
        values.put("water_temp", waterTemp);
        values.put("buddy", buddy);
        values.put("fish_seen", fishSeen);
        values.put("note", note);
        values.put("ai_tip", aiTip);
        values.put("created_at", now());
        return getWritableDatabase().insert("dives", null, values);
    }

    public int countDives(long userId) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM dives WHERE user_id=?", new String[]{String.valueOf(userId)});
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    public int countNotes(long userId) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM notes WHERE user_id=?", new String[]{String.valueOf(userId)});
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    public String buildHomeRecommendation(long userId) {
        List<ActivityItem> items = getActivities(userId, "全部");
        if (items.isEmpty()) {
            return "先记录一次潜水经历，系统会根据深度、时长、地点和笔记整理复盘建议。";
        }
        ActivityItem latest = items.get(0);
        return "最近一次「" + latest.title + "」整理：\n" + latest.aiTip;
    }

    public List<SpotItem> searchRecommendedSpots(long userId, String keyword) {
        List<ActivityItem> history = getActivities(userId, "全部");
        List<SpotItem> spots = buildSpotSamples();
        String lowerKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.CHINA);
        double avgDepth = 0;
        String favoriteType = "";
        String observedFish = "";
        for (ActivityItem item : history) {
            avgDepth += item.depth;
            favoriteType = item.category;
            observedFish += item.fishSeen + " ";
        }
        if (!history.isEmpty()) {
            avgDepth = avgDepth / history.size();
        }
        List<SpotItem> result = new ArrayList<>();
        for (SpotItem spot : spots) {
            if (!lowerKeyword.isEmpty() && !matchesKeyword(spot, lowerKeyword)) {
                continue;
            }
            int score = 48;
            List<String> reasons = new ArrayList<>();
            if (!favoriteType.isEmpty() && spot.diveType.equals(favoriteType)) {
                score += 18;
                reasons.add("类型接近你常记录的「" + favoriteType + "」");
            }
            if (avgDepth > 0 && Math.abs(spot.depth - avgDepth) <= 8) {
                score += 16;
                reasons.add("深度区间接近你的记录");
            }
            if (containsSharedFish(observedFish, spot.fishSeen)) {
                score += 12;
                reasons.add("有相似的生物观察");
            }
            if (spot.visibility.contains("清") || spot.visibility.contains("好")) {
                score += 6;
                reasons.add("能见度反馈较好");
            }
            if (isOverseasSpot(spot.location)) {
                score += 4;
                reasons.add("适合加入进阶旅行潜点收藏");
            }
            spot.score = Math.min(score, 98);
            spot.reason = reasons.isEmpty() ? "适合作为下一次潜水灵感" : TextUtils.join("；", reasons);
            result.add(spot);
        }
        result.sort((a, b) -> b.score - a.score);
        return result;
    }

    public List<NoteItem> getNotes(long userId) {
        List<NoteItem> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id, user_id, dive_id, title, content, mood, created_at, updated_at " +
                        "FROM notes WHERE user_id=? ORDER BY updated_at DESC, id DESC",
                new String[]{String.valueOf(userId)});
        try {
            while (cursor.moveToNext()) {
                NoteItem item = new NoteItem();
                item.id = cursor.getLong(0);
                item.userId = cursor.getLong(1);
                item.diveId = cursor.getLong(2);
                item.title = cursor.getString(3);
                item.content = cursor.getString(4);
                item.mood = cursor.getString(5);
                item.createdAt = cursor.getString(6);
                item.updatedAt = cursor.getString(7);
                list.add(item);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public NoteItem getNote(long id) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id, user_id, dive_id, title, content, mood, created_at, updated_at FROM notes WHERE id=?",
                new String[]{String.valueOf(id)});
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            NoteItem item = new NoteItem();
            item.id = cursor.getLong(0);
            item.userId = cursor.getLong(1);
            item.diveId = cursor.getLong(2);
            item.title = cursor.getString(3);
            item.content = cursor.getString(4);
            item.mood = cursor.getString(5);
            item.createdAt = cursor.getString(6);
            item.updatedAt = cursor.getString(7);
            return item;
        } finally {
            cursor.close();
        }
    }

    public long addNote(long userId, long diveId, String title, String content, String mood) {
        String timestamp = now();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("dive_id", diveId);
        values.put("title", title);
        values.put("content", content);
        values.put("mood", mood);
        values.put("created_at", timestamp);
        values.put("updated_at", timestamp);
        return getWritableDatabase().insert("notes", null, values);
    }

    public boolean updateNote(long noteId, String title, String content, String mood) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("mood", mood);
        values.put("updated_at", now());
        return getWritableDatabase().update("notes", values, "id=?",
                new String[]{String.valueOf(noteId)}) > 0;
    }

    public boolean deleteNote(long noteId) {
        return getWritableDatabase().delete("notes", "id=?",
                new String[]{String.valueOf(noteId)}) > 0;
    }

    private ActivityItem readDive(Cursor cursor) {
        ActivityItem item = new ActivityItem();
        item.id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        item.title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        item.category = cursor.getString(cursor.getColumnIndexOrThrow("dive_type"));
        item.location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
        item.date = cursor.getString(cursor.getColumnIndexOrThrow("dive_date"));
        item.depth = cursor.getDouble(cursor.getColumnIndexOrThrow("depth"));
        item.duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration"));
        item.visibility = cursor.getString(cursor.getColumnIndexOrThrow("visibility"));
        item.waterTemp = cursor.getString(cursor.getColumnIndexOrThrow("water_temp"));
        item.buddy = cursor.getString(cursor.getColumnIndexOrThrow("buddy"));
        item.fishSeen = cursor.getString(cursor.getColumnIndexOrThrow("fish_seen"));
        item.description = cursor.getString(cursor.getColumnIndexOrThrow("note"));
        item.aiTip = cursor.getString(cursor.getColumnIndexOrThrow("ai_tip"));
        return item;
    }

    private String buildAiTip(double depth, int duration, String visibility,
                              String waterTemp, String fishSeen, String note) {
        List<String> tips = new ArrayList<>();
        if (depth >= 30) {
            tips.add("深度超过 30 米，下次建议重点记录安全停留、气瓶余压和上升速度。");
        } else if (depth >= 18) {
            tips.add("这次属于进阶深度，适合复盘中性浮力、耳压平衡和下潜节奏。");
        } else {
            tips.add("浅水记录适合积累路线、拍照点位和生物观察。");
        }
        if (duration >= 50) {
            tips.add("潜水时长较长，建议补充水面休息时间和体感疲劳。");
        }
        if (visibility.contains("低") || visibility.contains("差")) {
            tips.add("能见度偏低，下次可以提前标记导航路线和集合点。");
        }
        if (waterTemp.contains("冷") || waterTemp.contains("低")) {
            tips.add("水温偏低，推荐检查防寒装备并记录保暖体验。");
        }
        if (!TextUtils.isEmpty(fishSeen) && !"无".equals(fishSeen)) {
            tips.add("你记录了「" + fishSeen + "」，建议在笔记本里单独整理观察位置和拍摄角度。");
        }
        if (note.contains("紧张") || note.contains("耳压")) {
            tips.add("备注里出现不适关键词，下次建议降低难度并做专项练习。");
        }
        return TextUtils.join("\n", tips);
    }

    private void seedData(SQLiteDatabase db) {
        ContentValues user = new ContentValues();
        user.put("username", "demo");
        user.put("password", "123456");
        user.put("created_at", now());
        long userId = db.insert("users", null, user);
        long firstDiveId = insertSeed(db, userId, "三亚珊瑚礁观察", "休闲潜水", "三亚蜈支洲岛",
                "2026-06-06", 12.5, 42, "清晰", "温暖", "小周",
                "小丑鱼、蝶鱼", "记录珊瑚区路线，适合做鱼类和拍照点位整理。");
        insertSeed(db, userId, "千岛湖沉船点", "深潜", "杭州千岛湖",
                "2026-06-01", 26.0, 36, "偏低", "偏冷", "教练阿明",
                "无", "下潜后能见度下降，耳压平衡需要更慢。");
        insertNote(db, userId, firstDiveId, "珊瑚礁复盘",
                "下水前检查相机防水壳，沿右侧礁盘慢慢推进。小丑鱼出现在浅色海葵附近，适合下次重点拍摄。",
                "轻松");
    }

    private long insertSeed(SQLiteDatabase db, long userId, String title, String diveType,
                            String location, String date, double depth, int duration,
                            String visibility, String waterTemp, String buddy,
                            String fishSeen, String note) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("title", title);
        values.put("dive_type", diveType);
        values.put("location", location);
        values.put("dive_date", date);
        values.put("depth", depth);
        values.put("duration", duration);
        values.put("visibility", visibility);
        values.put("water_temp", waterTemp);
        values.put("buddy", buddy);
        values.put("fish_seen", fishSeen);
        values.put("note", note);
        values.put("ai_tip", buildAiTip(depth, duration, visibility, waterTemp, fishSeen, note));
        values.put("created_at", now());
        return db.insert("dives", null, values);
    }

    private void insertNote(SQLiteDatabase db, long userId, long diveId, String title,
                            String content, String mood) {
        String timestamp = now();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("dive_id", diveId);
        values.put("title", title);
        values.put("content", content);
        values.put("mood", mood);
        values.put("created_at", timestamp);
        values.put("updated_at", timestamp);
        db.insert("notes", null, values);
    }

    private List<SpotItem> buildSpotSamples() {
        List<SpotItem> list = new ArrayList<>();
        list.add(spot("陵水分界洲岛蓝洞", "海南陵水", "休闲潜水", 14, "清晰", "小丑鱼、蝶鱼、海葵"));
        list.add(spot("蜈支洲岛珊瑚花园", "海南三亚", "休闲潜水", 12, "清晰", "小丑鱼、雀鲷、蝶鱼"));
        list.add(spot("千岛湖沉船二号点", "杭州千岛湖", "深潜", 28, "偏低", "无"));
        list.add(spot("大鹏半岛训练湾", "深圳大鹏", "训练潜水", 9, "一般", "雀鲷、海胆"));
        list.add(spot("涠洲岛石螺口", "广西北海", "自由潜", 8, "清晰", "热带鱼、珊瑚"));
        list.add(spot("万宁加井岛外礁", "海南万宁", "休闲潜水", 16, "好", "蝶鱼、海龟、珊瑚"));
        list.add(spot("大堡礁阿金考特礁", "澳大利亚昆士兰", "休闲潜水", 15, "清晰", "小丑鱼、蝶鱼、珊瑚、海龟"));
        list.add(spot("马尔代夫香蕉礁", "马尔代夫北马累环礁", "休闲潜水", 18, "清晰", "礁鲨、海龟、蝠鲼、热带鱼"));
        list.add(spot("帕劳蓝角", "帕劳科罗尔", "深潜", 24, "好", "礁鲨、拿破仑鱼、海龟"));
        list.add(spot("塞班蓝洞", "北马里亚纳塞班岛", "深潜", 22, "清晰", "蝴蝶鱼、海龟、珊瑚"));
        list.add(spot("四王岛米索尔礁", "印度尼西亚四王岛", "休闲潜水", 16, "清晰", "蝠鲼、海扇、蝶鱼、珊瑚"));
        list.add(spot("巴厘岛图兰奔沉船", "印度尼西亚巴厘岛", "深潜", 25, "好", "隆头鱼、杰克鱼群、珊瑚"));
        list.add(spot("达哈卜蓝洞", "埃及西奈半岛", "自由潜", 20, "清晰", "珊瑚、礁鱼、海鳗"));
        list.add(spot("科苏梅尔帕兰卡礁", "墨西哥科苏梅尔", "休闲潜水", 17, "清晰", "鹰鳐、海龟、珊瑚、热带鱼"));
        list.add(spot("博内尔盐码头", "加勒比荷属博内尔", "训练潜水", 12, "好", "海马、海龟、珊瑚"));
        list.add(spot("夏威夷莫洛基尼火山口", "美国夏威夷", "休闲潜水", 14, "清晰", "蝶鱼、海龟、礁鱼"));
        return list;
    }

    private SpotItem spot(String title, String location, String diveType, double depth,
                          String visibility, String fishSeen) {
        SpotItem item = new SpotItem();
        item.title = title;
        item.location = location;
        item.diveType = diveType;
        item.depth = depth;
        item.visibility = visibility;
        item.fishSeen = fishSeen;
        return item;
    }

    private boolean matchesKeyword(SpotItem spot, String keyword) {
        return spot.title.toLowerCase(Locale.CHINA).contains(keyword)
                || spot.location.toLowerCase(Locale.CHINA).contains(keyword)
                || spot.diveType.toLowerCase(Locale.CHINA).contains(keyword)
                || spot.fishSeen.toLowerCase(Locale.CHINA).contains(keyword);
    }

    private boolean containsSharedFish(String historyFish, String spotFish) {
        if (TextUtils.isEmpty(historyFish) || TextUtils.isEmpty(spotFish)) {
            return false;
        }
        String[] parts = spotFish.split("、|,|，| ");
        for (String part : parts) {
            String token = part.trim();
            if (token.length() > 1 && historyFish.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOverseasSpot(String location) {
        return !(location.contains("海南") || location.contains("杭州") || location.contains("深圳")
                || location.contains("广西") || location.contains("三亚") || location.contains("陵水")
                || location.contains("万宁") || location.contains("北海"));
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(new Date());
    }
}
