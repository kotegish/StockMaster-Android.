package com.solodefstudio.stockmaster;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import android.database.Cursor;
import java.util.Locale;

public class MainActivity extends Activity {
    final int navy=Color.rgb(12,25,48), panel=Color.rgb(24,42,69), gold=Color.rgb(220,177,91), white=Color.WHITE, muted=Color.rgb(174,188,207);
    StockDb db; LinearLayout base,content; String page="home";
    public void onCreate(Bundle b){super.onCreate(b);db=new StockDb(this);render();}
    TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(0,6,0,6);return t;}
    GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
    void render(){
        base=new LinearLayout(this);base.setOrientation(1);base.setPadding(20,24,20,16);base.setBackgroundColor(navy);setContentView(base);
        LinearLayout head=new LinearLayout(this);head.setOrientation(1);
        head.addView(text("SoloDefStudio",13,gold,true));head.addView(text("STOCKMASTER",25,white,true));head.addView(text("Управление запасами • офлайн",13,muted,false));base.addView(head);
        LinearLayout nav=new LinearLayout(this);nav.setPadding(0,18,0,14);
        String[] labels={"Обзор","Каталог","История"};
        for(String l:labels){Button b=button(l);b.setOnClickListener(v->{page=l.equals("Обзор")?"home":l.equals("Каталог")?"products":"history";render();});nav.addView(b,new LinearLayout.LayoutParams(0,48,1));}base.addView(nav);
        ScrollView scroll=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(1);scroll.addView(content);base.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        if(page.equals("home"))home();else if(page.equals("products"))products();else history();
    }
    Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(white);b.setAllCaps(false);b.setTextSize(12);b.setBackground(bg(panel,18));return b;}
    void card(String title,String value){LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(16,12,16,12);c.setBackground(bg(panel,20));TextView a=text(title,13,muted,false),v=text(value,23,gold,true);c.addView(a);c.addView(v);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=10;content.addView(c,p);}
    void home(){
        content.addView(text("Рабочая панель",20,white,true));
        card("Товаров в каталоге",String.valueOf(db.count()));
        card("Стоимость остатков",String.format(Locale.getDefault(),"%.2f ₽",db.total()));
        card("Позиции с низким остатком",String.valueOf(db.low()));
        Button add=button("+ Добавить товар");add.setOnClickListener(v->addDialog());content.addView(add);
        content.addView(text("Операции со складом",17,white,true));
        Button receipt=button("Приход товара");receipt.setOnClickListener(v->movementDialog("Приход",1));content.addView(receipt);
        Button sale=button("Продажа / списание");sale.setOnClickListener(v->movementDialog("Расход",-1));content.addView(sale);
        content.addView(text("Прототип 0.1 • данные хранятся на устройстве",12,muted,false));
    }
    void products(){
        content.addView(text("Каталог товаров",20,white,true));
        EditText search=new EditText(this);search.setSingleLine();search.setHint("Поиск по названию, штрихкоду, SKU");search.setTextColor(white);search.setHintTextColor(muted);content.addView(search);
        LinearLayout list=new LinearLayout(this);list.setOrientation(1);content.addView(list);
        Runnable load=()->{list.removeAllViews();Cursor c=db.products(search.getText().toString());while(c.moveToNext()){
            long id=c.getLong(0);String name=c.getString(1);double price=c.getDouble(4),qty=c.getDouble(5);
            Button row=button(name+"\n"+String.format(Locale.getDefault(),"Цена: %.2f ₽ • Остаток: %.2f",price,qty));row.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);row.setOnClickListener(v->movementForProduct(id,name));list.addView(row,new LinearLayout.LayoutParams(-1,72));
        }c.close();};
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){load.run();}public void afterTextChanged(android.text.Editable e){}});
        load.run();Button add=button("+ Новый товар");add.setOnClickListener(v->addDialog());content.addView(add);
    }
    void movementForProduct(long id,String name){new AlertDialog.Builder(this).setTitle(name).setItems(new String[]{"Приход","+ Продажа","Списание"},(d,w)->quantityDialog(id,w==0?"Приход":w==1?"Продажа":"Списание",w==0?1:-1)).show();}
    void quantityDialog(long id,String type,int sign){EditText q=new EditText(this);q.setInputType(8194);q.setHint("Количество");new AlertDialog.Builder(this).setTitle(type).setView(q).setNegativeButton("Отмена",null).setPositiveButton("Сохранить",(d,w)->{try{double n=Double.parseDouble(q.getText().toString());if(n>0)db.move(id,type,sign*n,"Операция из каталога");render();}catch(Exception e){toast("Введите корректное количество");}}).show();}
    void movementDialog(String type,int sign){Cursor c=db.products("");java.util.ArrayList<Long> ids=new java.util.ArrayList<>();java.util.ArrayList<String> names=new java.util.ArrayList<>();while(c.moveToNext()){ids.add(c.getLong(0));names.add(c.getString(1));}c.close();if(ids.isEmpty()){toast("Сначала добавьте товар");return;}new AlertDialog.Builder(this).setTitle(type).setItems(names.toArray(new String[0]),(d,w)->quantityDialog(ids.get(w),type,sign)).show();}
    void addDialog(){
        LinearLayout form=new LinearLayout(this);form.setPadding(20,4,20,4);form.setOrientation(1);
        EditText name=field("Название товара"),barcode=field("Штрихкод"),sku=field("Артикул"),price=number("Цена продажи"),qty=number("Начальный остаток"),min=number("Минимальный остаток");
        for(EditText e:new EditText[]{name,barcode,sku,price,qty,min})form.addView(e);
        new AlertDialog.Builder(this).setTitle("Новый товар").setView(form).setNegativeButton("Отмена",null).setPositiveButton("Добавить",(d,w)->{try{if(name.getText().toString().trim().isEmpty())throw new Exception();db.add(name.getText().toString(),barcode.getText().toString(),sku.getText().toString(),val(price),val(qty),val(min));render();}catch(Exception e){toast("Проверьте название и числовые поля");}}).show();
    }
    EditText field(String h){EditText e=new EditText(this);e.setHint(h);e.setSingleLine();e.setTextColor(white);e.setHintTextColor(muted);return e;}
    EditText number(String h){EditText e=field(h);e.setInputType(8194);return e;}
    double val(EditText e){return e.getText().length()==0?0:Double.parseDouble(e.getText().toString().replace(',','.'));}
    void history(){content.addView(text("История движений",20,white,true));Cursor c=db.movements();while(c.moveToNext()){String line=c.getString(4)+" • "+c.getString(0)+" • "+c.getDouble(1)+"\n"+c.getString(3);content.addView(text(line,14,muted,false));View sep=new View(this);sep.setBackgroundColor(panel);content.addView(sep,new LinearLayout.LayoutParams(-1,1));}c.close();}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}