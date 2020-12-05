package com.ghost2911.stockmanager;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private StorageReference mStorageRef;
    ArrayList<Product> products = new ArrayList<Product>();
    BoxAdapter boxAdapter;
    EditText searchString;
    ImageButton btnScan;
    Boolean isLoaded = false;

    public SQLiteHelper dbHelper;
    public static SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            mStorageRef = FirebaseStorage.getInstance().getReference();

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(), AddProductActivity.class);
                    i.putExtra("idCategory",boxAdapter.navigation.get(boxAdapter.navigation.size()-1));
                    i.putExtra("isFolder",false);
                    startActivity(i);
                }
            });
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            boxAdapter = new BoxAdapter(this, products);

            searchString = findViewById(R.id.searchString);
            searchString.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                    boxAdapter.getFilter().filter(cs.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            btnScan = findViewById(R.id.btnScanner);
            View.OnClickListener onclickScan = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(), ScannerActivity.class);
                    i.putExtra("senderActivity","main");
                    startActivity(i);
                }
            };
            btnScan.setOnClickListener(onclickScan);

            ListView lvMain = (ListView) findViewById(R.id.listView);
            lvMain.setAdapter(boxAdapter);
            updateAdapter();

            searchString.setText("");

    }

    public ArrayList<String> stringsFromCsvFile(String filePath)
    {
        ArrayList<String> dataFromFile=new ArrayList<String>();
        try{
            Scanner scanner=new Scanner(new InputStreamReader(new FileInputStream(filePath), Charset.forName("IBM866")));
            scanner.useDelimiter("\\n");

            while(scanner.hasNext())
                dataFromFile.add(scanner.nextLine());

            scanner.close();
            dataFromFile.remove(0);
            dataFromFile.remove(0);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return dataFromFile;
    }

    @Override
    public void onBackPressed() {
        navigationBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {

            Toast.makeText(getApplicationContext(), "Выберите файл для импорта", Toast.LENGTH_SHORT).show();
            products.clear();
            OpenFileDialog fileDialog = new OpenFileDialog(this);
            fileDialog.setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
                @Override
                public void OnSelectedFile(String fileName) {

                    SQLiteHelper dbHelper = new SQLiteHelper(getApplicationContext());
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    int[] bufGroup = {0,0,0,0,0};
                    int lastCat = 0;

                    //чтение исходника с SD карты
                    for (String str : stringsFromCsvFile(fileName)) {
                        try {
                            str = new String(str.getBytes("cp866"),"windows-1251"); // енкодер
                            ContentValues cv = new ContentValues();
                            String[] mas = str.split(";");

                            //общие поля CV
                            cv.put("name", mas[2]);

                            if (isNumeric(mas[0])) {
                                //разграничение по уровням категорий
                                int nowGr = Integer.parseInt(mas[0]);
                                lastCat =  Integer.parseInt(mas[1]);
                                bufGroup[nowGr]=lastCat;

                                //CV-группы формирования записи в БД
                                cv.put("idProduct", lastCat);
                                cv.put("idRoot", bufGroup[nowGr-1]);
                                cv.put("price", 0);
                                cv.put("count", 0);
                                cv.put("desc", "Категория");
                                cv.put("analog","");

                                //заполнение групп
                                Log.d("debug","idRoot="+bufGroup[nowGr-1]+", idProduct="+lastCat+", Name="+mas[2]);
                            } else {
                                //CV-товара
                                cv.put("idProduct", Integer.parseInt(mas[1]));
                                cv.put("idRoot", lastCat);
                                cv.put("price", Integer.parseInt(mas[4]));
                                cv.put("count", Integer.parseInt(mas[3]));
                                cv.put("desc", mas[9]+" "+mas[10]);
                                cv.put("analog",mas[11]);

                                //заполнение товаров
                                Log.d("debug","idRoot="+lastCat+", idProduct="+mas[1]+", Name="+mas[2]);
                            }
                            // вставляем запись и получаем ее ID
                            long rowID = db.insert("products", null, cv);
                            Log.d("D1", "ВСТАВЛЕННАЯ СТРОКА, ID = " + rowID);
                        } catch (Exception ex) {
                            Log.d("D1",ex.getMessage());
                        }
                    }
                    Toast.makeText(getApplicationContext(), "Импорт завершен", Toast.LENGTH_SHORT).show();
                    updateAdapter();
                    dbHelper.close();
                }
            });
            fileDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    void navigationBack(boolean isBackPressed) {
        if (isBackPressed) {
            int adapterSize = boxAdapter.navigation.size();
            if (adapterSize != 1) {
                boxAdapter.navigation.remove(adapterSize - 1);
                boxAdapter.navigation.get(adapterSize - 2);
            }
        }

        dbHelper = new SQLiteHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase();
        Cursor c;


        c = db.query("products", null, "idRoot = ?", new String[]{boxAdapter.navigation.get(boxAdapter.navigation.size() - 1) + ""}, null, null, null);
        boxAdapter.mDisplayedValues.clear();

        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int idProd = c.getColumnIndex("idProduct");
            int idRoot = c.getColumnIndex("idRoot");
            int count = c.getColumnIndex("count");
            int price = c.getColumnIndex("price");
            int name = c.getColumnIndex("name");
            int desc = c.getColumnIndex("desc");
            int analog = c.getColumnIndex("analog");
            int barcode = c.getColumnIndex("barcode");
            int storage = c.getColumnIndex("storage");

            do {
                boxAdapter.mDisplayedValues.add(new Product(c.getInt(idProd), c.getInt(idRoot), c.getString(name), c.getString(desc), c.getInt(price), c.getInt(count), c.getString(analog), c.getString(barcode), c.getString(storage)));
            } while (c.moveToNext());
        }
        boxAdapter.notifyDataSetChanged();
        c.close();
        dbHelper.close();
    }

    void updateAdapter()
    {
        try {
            SQLiteHelper dbHelper = new SQLiteHelper(getApplicationContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor c;

            c = db.query("products", null, null , null, null, null, null);
            products.clear();

            if (c.moveToFirst()) {

                int idProd = c.getColumnIndex("idProduct");
                int idRoot = c.getColumnIndex("idRoot");
                int count = c.getColumnIndex("count");
                int price = c.getColumnIndex("price");
                int name = c.getColumnIndex("name");
                int desc = c.getColumnIndex("desc");
                int analog = c.getColumnIndex("analog");
                int barcode = c.getColumnIndex("barcode");
                int storage = c.getColumnIndex("storage");

                do {
                    products.add(new Product(c.getInt(idProd),c.getInt(idRoot),c.getString(name),c.getString(desc),c.getInt(price),c.getInt(count),c.getString(analog), c.getString(barcode),c.getString(storage)));
                } while (c.moveToNext());
            }
            boxAdapter.notifyDataSetChanged();
            c.close();
            dbHelper.close();
        }
        catch (Exception ex)
        {
            Log.d("D1",ex.getMessage());
        }
    }

    void clearDatabase() {

        Toast.makeText(getApplicationContext(), "ОЧИСТКА", Toast.LENGTH_SHORT).show();
        SQLiteHelper dbHelper = new SQLiteHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Log.d("D1", "ОЧИСТКА ТАБЛИЦЫ");
        int clearCount = db.delete("products", null, null);
        Log.d("D1", "УДАЛЕНО СТРОК = " + clearCount);

        dbHelper.close();
        navigationBack(false);
        updateAdapter();
    }

    void unloadDatabase() {
        //ВЫРЕЗАНО ДЛЯ GIT
        Toast.makeText(getApplicationContext(),"Данный раздел вырезан для Git",Toast.LENGTH_SHORT).show();
    }

    void loadDatabase() {
            String DB_PATH = "//data/data/"
                    + getApplicationContext().getPackageName()
                    + "/databases/";

            final File localFile = new File(DB_PATH, "SMDB");
            StorageReference riversRef = mStorageRef.child("SMDB.db");
            riversRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getApplicationContext(),"База обновлена из облака",Toast.LENGTH_SHORT).show();
                    navigationBack(false);
                    updateAdapter();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(),"Не удалось обновить базу. Проверьте интернет-соединение",Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLoaded) {
            navigationBack(false);
            updateAdapter();
        }
        else
            isLoaded = true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_upload) {
            unloadDatabase();
        } else if (id == R.id.nav_update) {
            updateAdapter();
        } else if (id == R.id.nav_clear) {
            clearDatabase();
        } else if (id == R.id.nav_load) {
            loadDatabase();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
