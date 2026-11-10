package com.solodefstudio.stockmaster;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import java.util.*;

public class StockDb extends SQLiteOpenHelper {
    public StockDb(Context c){super(c,"stockmaster.db",null,1);}
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE products(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,barcode TEXT,sku TEXT,price REAL DEFAULT 0,qty REAL DEFAULT 0,min_qty REAL DEFAULT 0,unit TEXT DEFAULT 'шт')");
        db.execSQL("CREATE TABLE movements(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER,type TEXT,delta REAL,note TEXT,created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE INDEX idx_barcode ON products(barcode)");
        db.execSQL("INSERT INTO products(name,barcode,sku,price,qty,min_qty) VALUES('Кофе молотый 250 г','4601234567890','CF-250',349,18,5),('Чай чёрный 100 г','4601234567891','TEA-100',189,7,4),('Сахар 1 кг','4601234567892','SUG-1',89,32,8)");
    }
    public void onUpgrade(SQLiteDatabase db,int oldV,int newV){}
    public Cursor products(String q){String s="%"+q+"%";return getReadableDatabase().rawQuery("SELECT id,name,barcode,sku,price,qty,min_qty,unit FROM products WHERE name LIKE ? OR barcode LIKE ? OR sku LIKE ? ORDER BY name",new String[]{s,s,s});}
    public long add(String name,String barcode,String sku,double price,double qty,double min){
        ContentValues v=new ContentValues();v.put("name",name);v.put("barcode",barcode);v.put("sku",sku);v.put("price",price);v.put("qty",qty);v.put("min_qty",min);
        long id=getWritableDatabase().insert("products",null,v);if(id>0&&qty>0)move(id,"Начальный остаток",qty,"Стартовый остаток при создании");return id;
    }
    public void move(long id,String type,double delta,String note){
        SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{
            ContentValues m=new ContentValues();m.put("product_id",id);m.put("type",type);m.put("delta",delta);m.put("note",note);db.insert("movements",null,m);
            db.execSQL("UPDATE products SET qty=qty+? WHERE id=?",new Object[]{delta,id});db.setTransactionSuccessful();
        }finally{db.endTransaction();}
    }
    public Cursor movements(){return getReadableDatabase().rawQuery("SELECT m.type,m.delta,m.note,m.created_at,p.name FROM movements m JOIN products p ON p.id=m.product_id ORDER BY m.id DESC LIMIT 100",null);}
    public int count(){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM products",null);c.moveToFirst();int n=c.getInt(0);c.close();return n;}
    public double total(){Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(qty*price),0) FROM products",null);c.moveToFirst();double n=c.getDouble(0);c.close();return n;}
    public int low(){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM products WHERE qty<=min_qty",null);c.moveToFirst();int n=c.getInt(0);c.close();return n;}
}