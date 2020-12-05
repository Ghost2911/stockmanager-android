package com.ghost2911.stockmanager;

import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BoxAdapter extends BaseAdapter implements Filterable {

    private ArrayList<Product> mOriginalValues; // Original Values
    public ArrayList<Product> mDisplayedValues; // Values to be displayed
    public ArrayList<Integer> navigation = new ArrayList<Integer>();
    LayoutInflater inflater;

    public BoxAdapter(Context context, ArrayList<Product> mProductArrayList) {
        navigation.add(0);
        this.mOriginalValues = mProductArrayList;
        this.mDisplayedValues = mProductArrayList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mDisplayedValues.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        LinearLayout llContainer;
        TextView tvName,tvPrice, tvDesc, tvCount;
        ImageView img;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {

            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.row_layout, null);
            holder.llContainer = (LinearLayout) convertView.findViewById(R.id.llContainer);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            holder.tvPrice = (TextView) convertView.findViewById(R.id.tvPrice);
            holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDescription);
            holder.tvCount = (TextView) convertView.findViewById(R.id.tvCount);
            holder.img = (ImageView) convertView.findViewById(R.id.img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (mDisplayedValues.get(position).price == 0) {

            holder.tvName.setText(mDisplayedValues.get(position).name);
            holder.tvPrice.setText("");
            holder.tvDesc.setText("");
            holder.tvCount.setText("");
            holder.img.setImageResource(R.drawable.ic_folder_row);


            //НАВИГАЦИЯ ПО ПАПКАМ
            holder.llContainer.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    SQLiteHelper dbHelper = new SQLiteHelper(v.getContext());
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Cursor c;

                    navigation.add(mDisplayedValues.get(position).idProduct);
                    c = db.query("products", null, "idRoot = ?", new String[]{navigation.get(navigation.size() - 1) + ""}, null, null, null);
                    mDisplayedValues.clear();

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
                            mDisplayedValues.add(new Product(c.getInt(idProd), c.getInt(idRoot), c.getString(name), c.getString(desc), c.getInt(price), c.getInt(count), c.getString(analog), c.getString(barcode),c.getString(storage)));
                        } while (c.moveToNext());
                    } else
                        Log.d("D1", "ПУСТАЯ БАЗА");
                    notifyDataSetChanged();
                    c.close();
                    dbHelper.close();
                }
            });
        } else {
            holder.tvName.setText(mDisplayedValues.get(position).name);
            holder.tvPrice.setText(mDisplayedValues.get(position).price + " р.");
            holder.tvDesc.setText(mDisplayedValues.get(position).desc);
            holder.tvCount.setText(mDisplayedValues.get(position).count + " шт.");
            holder.img.setImageResource(R.drawable.ic_item_row);
            holder.llContainer.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent i = new Intent(context, EditProductActivity.class);
                    i.putExtra("name", mDisplayedValues.get(position).name);
                    i.putExtra("desc", mDisplayedValues.get(position).desc);
                    i.putExtra("price", mDisplayedValues.get(position).price);
                    i.putExtra("count", mDisplayedValues.get(position).count);
                    i.putExtra("analog", mDisplayedValues.get(position).analog);
                    i.putExtra("barcode", mDisplayedValues.get(position).barcode);
                    i.putExtra("storage", mDisplayedValues.get(position).storage);
                    i.putExtra("idProduct", mDisplayedValues.get(position).idProduct);
                    v.getContext().startActivity(i);
                }
            });
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                mDisplayedValues = (ArrayList<Product>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();


                String filterString = constraint.toString().toLowerCase().replaceAll("[^a-zA-Zа-яА-Я0-9]", "");
                ArrayList<Product> list = mOriginalValues;
                int count = list.size();
                ArrayList<Product> nlist = new ArrayList<Product>(count);

                String filterableString1;
                String filterableString2;

                for (int i = 0; i < count; i++) {
                    filterableString1 = (list.get(i).name).replaceAll("[^a-zA-Zа-яА-Я0-9]", "");
                    filterableString2 = (list.get(i).analog).replaceAll("[^a-zA-Zа-яА-Я0-9]", "");
                    if (filterableString1.toLowerCase().contains(filterString) || filterableString2.toLowerCase().contains(filterString)) {
                        nlist.add(new Product(list.get(i).idProduct,list.get(i).idRoot,list.get(i).name,
                                list.get(i).desc,list.get(i).price,list.get(i).count,list.get(i).analog,list.get(i).barcode,list.get(i).storage));
                    }
                }
                results.values = nlist;
                results.count = nlist.size();

                return results;
            }

        };
        return filter;
    }

}