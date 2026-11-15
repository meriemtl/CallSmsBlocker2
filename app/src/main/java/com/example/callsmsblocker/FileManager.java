package com.example.callsmsblocker;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FileManager {

    private static final String TAG = "FileManager";
    private static final String BLACKLIST_FILE = "blacklist.txt";
    private static final String WHITELIST_FILE = "whitelist.txt";
    private Context context;

    public FileManager(Context context) {
        this.context = context;
    }

    // Sauvegarder la blacklist dans un fichier texte
    public void saveBlacklist(Set<String> blacklist) {
        saveToFile(BLACKLIST_FILE, new ArrayList<>(blacklist));
    }

    // Sauvegarder la whitelist dans un fichier texte
    public void saveWhitelist(Set<String> whitelist) {
        saveToFile(WHITELIST_FILE, new ArrayList<>(whitelist));
    }

    // Charger la blacklist depuis le fichier texte
    public Set<String> loadBlacklist() {
        return new HashSet<>(loadFromFile(BLACKLIST_FILE));
    }

    // Charger la whitelist depuis le fichier texte
    public Set<String> loadWhitelist() {
        return new HashSet<>(loadFromFile(WHITELIST_FILE));
    }

    // Sauvegarder une liste dans un fichier
    private void saveToFile(String filename, ArrayList<String> list) {
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            for (String item : list) {
                osw.write(item + "\n");
            }

            osw.close();
            fos.close();
            Log.d(TAG, "Fichier sauvegardé: " + filename + ", " + list.size() + " éléments");

        } catch (Exception e) {
            Log.e(TAG, "Erreur sauvegarde " + filename, e);
        }
    }

    // Charger une liste depuis un fichier
    private ArrayList<String> loadFromFile(String filename) {
        ArrayList<String> list = new ArrayList<>();

        try {
            File file = new File(context.getFilesDir(), filename);
            if (!file.exists()) {
                Log.d(TAG, "Fichier n'existe pas: " + filename);
                return list;
            }

            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    list.add(line);
                }
            }

            br.close();
            isr.close();
            fis.close();

            Log.d(TAG, "Fichier chargé: " + filename + ", " + list.size() + " éléments");

        } catch (Exception e) {
            Log.e(TAG, "Erreur chargement " + filename, e);
        }

        return list;
    }

    // Supprimer un fichier
    public void deleteFile(String filename) {
        File file = new File(context.getFilesDir(), filename);
        if (file.exists()) {
            file.delete();
            Log.d(TAG, "Fichier supprimé: " + filename);
        }
    }

    // Vérifier si les fichiers existent
    public boolean filesExist() {
        File blacklistFile = new File(context.getFilesDir(), BLACKLIST_FILE);
        File whitelistFile = new File(context.getFilesDir(), WHITELIST_FILE);
        return blacklistFile.exists() || whitelistFile.exists();
    }
}