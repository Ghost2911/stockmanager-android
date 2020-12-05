package com.ghost2911.stockmanager;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.NumberUtils;

public class AddProductActivity extends AppCompatActivity {

    TextView etName,etDesc,etPrice,etCount,etAnalog, etStorage;
    static TextView etBarcode;
    SQLiteHelper dbHelper;
    int idCategory;
    ImageButton btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        setTitle("Добавить новый товар");
        //boolean proceed = Boolean.parseBoolean(getIntent().getExtras().getString("isFolder", "false"));
        idCategory = getIntent().getExtras().getInt("idCategory", 0);
        etName = ((TextView) findViewById(R.id.etName));
        etDesc = ((TextView) findViewById(R.id.etDesc));
        etPrice = ((TextView) findViewById(R.id.etPrice));
        etCount = ((TextView) findViewById(R.id.etCount));
        etAnalog = ((TextView) findViewById(R.id.etAnalog));
        etStorage = ((TextView) findViewById(R.id.etStorage));
        etBarcode = ((TextView) findViewById(R.id.etBarcode));
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
            }
        });

        btnScan = findViewById(R.id.btnScanner);
        View.OnClickListener onclickScan = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ScannerActivity.class);
                i.putExtra("senderActivity","create");
                startActivity(i);
            }
        };
        btnScan.setOnClickListener(onclickScan);
    }

    public void addProduct()
    {
        if(TextUtils.isEmpty(etName.getText())) {
            etName.setError("Заполните поле НАЗВАНИЕ");
            etName.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(etDesc.getText())) {
            etDesc.setError("Заполните поле ОПИСАНИЕ");
            etDesc.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(etPrice.getText())) {
            etPrice.setError("Заполните поле ЦЕНА");
            etPrice.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(etCount.getText())) {
            etCount.setError("Заполните поле КОЛИЧЕСТВО");
            etCount.requestFocus();
            return;
        }
        try {
            ContentValues cv = new ContentValues();
            dbHelper = new SQLiteHelper(getApplicationContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            cv.put("idRoot", idCategory);
            cv.put("name", etName.getText().toString());
            cv.put("desc", etDesc.getText().toString());
            cv.put("price", Integer.parseInt(etPrice.getText().toString()));
            cv.put("count", Integer.parseInt(etCount.getText().toString()));
            cv.put("analog", etAnalog.getText().toString());
            cv.put("barcode", etBarcode.getText().toString());
            cv.put("storage", etStorage.getText().toString());

            db.insert("products", null, cv);
            dbHelper.close();
            finish();
        }
        catch (Exception ex){
            Log.d("D1",ex.toString());
        }
    }

}
