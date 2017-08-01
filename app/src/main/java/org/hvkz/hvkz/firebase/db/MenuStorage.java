package org.hvkz.hvkz.firebase.db;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.reflect.TypeToken;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.utils.serialize.JSONFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MenuStorage
{
    private static final String MENU_KEY = "menu";
    private static final String PRODUCTS_KEY = "products";
    private static final String MENU_FILE = "menu.json";
    private static final String PRODUCTS_FILE = "products.json";
    private static final int DAYS_IN_WEEK = 7;

    private HVKZApp app;
    private DatabaseReference menuDatabase;
    private DatabaseReference productsDatabase;
    private File menuFile;
    private File productsFile;

    public MenuStorage(HVKZApp app) {
        this.app = app;
        this.menuDatabase = FirebaseDatabase.getInstance().getReference(MENU_KEY);
        this.productsDatabase = FirebaseDatabase.getInstance().getReference(PRODUCTS_KEY);
        this.menuFile = new File(app.getCacheDir(), MENU_FILE);
        this.productsFile = new File(app.getCacheDir(), PRODUCTS_FILE);
        this.menuDatabase.keepSynced(true);
        this.productsDatabase.keepSynced(true);
    }

    public <T> List<T> getAllFromCache(File file, Type type) {
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return JSONFactory.fromJson(new String(bytes), type);
    }

    public void getWeeklyMenu(Callback<List<MenuItem>> callback) {
        if (menuFile.exists()) {
            List<MenuItem> data = getAllFromCache(menuFile, new TypeToken<List<MenuItem>>(){}.getType());
            List<MenuItem> weekly = new ArrayList<>();

            User user = app.getCurrentUser();
            user.getRegTimestamp();

            long difference = Tools.timestamp() - user.getRegTimestamp();
            int regday  = (int) (difference / (24 * 60 * 60));

            int today = regday % data.size();
            if (today == 0) today = data.size();
            int dayCount = DAYS_IN_WEEK;

            for (int i = today; i < today + dayCount; i++) {
                if (i >= data.size()) {
                    dayCount = (today + dayCount) - data.size();
                    today = 0;
                    i = 0;
                }

                weekly.add(data.get(i));
            }

            callback.call(weekly);
        } else {
            getAllFromRemote(value -> getWeeklyMenu(callback));
        }
    }

    public void getProductCatalog(Callback<List<Product>> callback) {
        if (productsFile.exists()) {
            callback.call(getAllFromCache(productsFile, new TypeToken<List<Product>>(){}.getType()));
        } else {
            getAllFromRemote(value -> getProductCatalog(callback));
        }
    }

    public void getAllFromRemote(Callback<List<MenuItem>> callback) {
        menuDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<MenuItem> data = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    data.add(snapshot.getValue(MenuItem.class));
                saveCacheFile(data, menuFile);
                callback.call(data);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        productsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Product> data = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        data.add(snapshot.getValue(Product.class));
                    saveCacheFile(data, productsFile);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
    }

    private <T> void saveCacheFile(List<T> data, File file) {
        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(JSONFactory.toJson(data).getBytes());
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class MenuItem {
        public List<Integer> bzu;
        public List<String> dayArray;
    }

    public static class Product implements Parcelable {
        public String calories;
        public String product;

        public Product(){
        }

        protected Product(Parcel in) {
            calories = in.readString();
            product = in.readString();
        }

        public static final Creator<Product> CREATOR = new Creator<Product>()
        {
            @Override
            public Product createFromParcel(Parcel in) {
                return new Product(in);
            }

            @Override
            public Product[] newArray(int size) {
                return new Product[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(calories);
            dest.writeString(product);
        }
    }
}
