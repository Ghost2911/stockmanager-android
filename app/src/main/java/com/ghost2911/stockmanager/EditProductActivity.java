package com.ghost2911.stockmanager;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

public class EditProductActivity extends AppCompatActivity {

    int idProduct;
    TextView etName, etDesc, etPrice, etCount, etAnalog, etStorage;
    public static TextView etBarcode;
    SQLiteHelper dbHelper;
    ImageButton btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);
        dbHelper = new SQLiteHelper(this);

        setTitle("Редактирование");
        Bundle arg = getIntent().getExtras();

        etName = ((TextView) findViewById(R.id.etName));
        etName.setText(arg.getString("name"));
        etDesc = ((TextView) findViewById(R.id.etDesc));
        etDesc.setText(arg.getString("desc"));
        etPrice = ((TextView) findViewById(R.id.etPrice));
        etPrice.setText(arg.getInt("price") + "");
        etCount = ((TextView) findViewById(R.id.etCount));
        etCount.setText(arg.getInt("count") + "");
        etBarcode = ((TextView) findViewById(R.id.etBarcode));
        etBarcode.setText(arg.getString("barcode"));
        etStorage = ((TextView) findViewById(R.id.etStorage));
        etStorage.setText(arg.getString("storage"));
        etAnalog = ((TextView) findViewById(R.id.etAnalog));
        etAnalog.setText(arg.getString("analog"));
        idProduct = arg.getInt("idProduct");
        FloatingActionButton fab = findViewById(R.id.fab);
        FloatingActionButton fabDel = findViewById(R.id.fabDel);
        new DownloadImageTask((ImageView) findViewById(R.id.ivProduct))
                .execute("http://xn--80aitdhdaqq.xn--p1ai/admin/pictures/"+idProduct+"b.jpg");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues cv = new ContentValues();
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                cv.put("name", etName.getText().toString());
                cv.put("desc", etDesc.getText().toString());
                cv.put("price", Integer.parseInt(etPrice.getText().toString()));
                cv.put("count", Integer.parseInt(etCount.getText().toString()));
                cv.put("barcode", etBarcode.getText().toString());
                cv.put("storage", etStorage.getText().toString());

                db.update("products", cv, "idProduct = ?",
                        new String[]{idProduct + ""});
                dbHelper.close();

                finish();
            }
        });

        fabDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                db.delete("products", "idProduct = ?",
                        new String[]{idProduct + ""});
                Toast.makeText(getApplicationContext(),"Товар успешно удален",Toast.LENGTH_SHORT).show();
                dbHelper.close();

                finish();
            }
        });

        btnScan = findViewById(R.id.btnScanner);
        View.OnClickListener onclickScan = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ScannerActivity.class);
                i.putExtra("senderActivity","edit");
                startActivity(i);
            }
        };
        btnScan.setOnClickListener(onclickScan);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {}
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
             bmImage.setImageBitmap(result);
        }
    }
}
