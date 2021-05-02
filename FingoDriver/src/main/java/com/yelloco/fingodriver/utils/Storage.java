package com.yelloco.fingodriver.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class Storage
{

    private static final Storage prefUtils = new Storage();
    private static SharedPreferences sharedPreferences;

    public static Storage getInstance() {
        return prefUtils;
    }

    public void initialize(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE);
        }
    }

    public void storeString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void storeInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void storeLong(String key, long value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();

    }

    public void storeBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    public long getLong(String key, long defaultValue)
    {
        return sharedPreferences.getLong(key, defaultValue);
    }

    public String getString(String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    public void removeValue(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public <T> void storeList(String key, List<T> list){
        Gson gson = new Gson();
        String s = gson.toJson(list);
        storeString(key, s);
    }

    public <T> List<T> getList(String key, TypeToken<List<T>> tt){
        Gson gson = new Gson();
        String string = getString(key);
        return gson.fromJson(string, tt.getType());
    }

    public void registerChangeListener(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener)
    {
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public void unregisterChangeListener(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public void destroy(){
        sharedPreferences = null;
    }

}

