package com.ghost2911.stockmanager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    ZXingScannerView view;
    String senderActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new ZXingScannerView(this);
        setContentView(view);
        setTitle("Сканер штрих-кодов");
        senderActivity = getIntent().getExtras().getString("senderActivity");
    }

    @Override
    public void handleResult(Result result) {

        switch (senderActivity)
        {
            case "main":
                ScanMainActivity(result);
                break;
            case "edit":
                EditProductActivity.etBarcode.setText(result.toString());
                break;
            case "create":
                AddProductActivity.etBarcode.setText(result.toString());
                break;
        }
        onBackPressed();
    }

    private void ScanMainActivity(Result result) {
        SQLiteHelper dbHelper = new SQLiteHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c;

        try {
            c = db.query("products", null, "barcode = ?", new String[]{result.toString() + ""}, null, null, null);

            if (c.moveToFirst()) {
                // определяем номера столбцов по имени в выборке
                int idProd = c.getColumnIndex("idProduct");
                int count = c.getColumnIndex("count");
                int price = c.getColumnIndex("price");
                int name = c.getColumnIndex("name");
                int desc = c.getColumnIndex("desc");
                int analog = c.getColumnIndex("analog");
                int barcode = c.getColumnIndex("barcode");

                Intent i = new Intent(getApplicationContext(), EditProductActivity.class);
                i.putExtra("name", c.getString(name));
                i.putExtra("desc", c.getString(desc));
                i.putExtra("price", c.getInt(price));
                i.putExtra("count", c.getInt(count));
                i.putExtra("analog", c.getString(analog));
                i.putExtra("barcode", c.getString(barcode));
                i.putExtra("idProduct", c.getInt(idProd));

                c.close();
                dbHelper.close();

                startActivity(i);
            } else {
                c.close();
                dbHelper.close();

                Toast.makeText(getApplicationContext(), "Данного товара нет в базе", Toast.LENGTH_LONG).show();
            }

        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        view.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        view.setResultHandler(this);
        view.startCamera();
    }
}
