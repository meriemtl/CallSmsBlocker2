package com.example.callsmsblocker;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // Composants UI
    private EditText etPhoneNumber;
    private Button btnAddToBlacklist, btnAddToWhitelist;
    private Button tabBlacklist, tabWhitelist;
    private ListView lvBlacklist, lvWhitelist;
    private TextView tvEmptyList, tvModeInfo;
    private SwitchCompat switchMode;

    // Données
    private ArrayList<String> blacklistArray, whitelistArray;
    private ArrayAdapter<String> blacklistAdapter, whitelistAdapter;

    // Gestionnaires
    private FileManager fileManager;
    private SharedPreferences sharedPreferences;

    // Constantes
    private static final String PREFS_NAME = "CallBlockerPrefs";
    private static final String KEY_WHITELIST_MODE = "whitelist_mode";
    private boolean isWhitelistMode = false;

    // Permissions
    private static final int PERMISSION_REQUEST_CODE = 100;
    private String[] permissions = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.VIBRATE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Animation d'entrée pour l'activité
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // Initialisation
        fileManager = new FileManager(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Charger le mode (whitelist ou blacklist)
        isWhitelistMode = sharedPreferences.getBoolean(KEY_WHITELIST_MODE, false);

        blacklistArray = new ArrayList<>();
        whitelistArray = new ArrayList<>();

        initViews();
        loadListsFromFiles();
        updateListDisplay();
        setupListeners();
        setupSwitchMode();
        checkPermissions();
        updateModeInfo();

        // Animation d'apparition des vues
        animateViewsEntrance();

        // Débogage
        debugShowLists();
    }

    // ==================== MÉTHODES D'ANIMATION ====================

    /**
     * Animation de clic avec effet de zoom
     */
    private void animateClick(View view) {
        view.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(80)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .start();
                })
                .start();
    }

    /**
     * Animation de clic avec retour visuel
     */
    private void animateButtonPress(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    /**
     * Animation de transition entre les onglets
     */
    private void animateTabSwitch(View tab, boolean isActive, String tabType) {
        if (isActive) {
            tab.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        tab.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
        } else {
            tab.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        tab.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
        }

        // Animation de fondu pour la liste
        if (tabType.equals("blacklist")) {
            lvBlacklist.setAlpha(0f);
            lvBlacklist.animate().alpha(1f).setDuration(300).start();
        } else if (tabType.equals("whitelist")) {
            lvWhitelist.setAlpha(0f);
            lvWhitelist.animate().alpha(1f).setDuration(300).start();
        }
    }

    /**
     * Animation d'apparition des éléments au démarrage
     */
    private void animateViewsEntrance() {
        // Animation pour le titre
        View title = findViewById(android.R.id.content).getRootView();

        // Animation pour chaque vue
        if (etPhoneNumber != null) {
            etPhoneNumber.setTranslationY(50f);
            etPhoneNumber.setAlpha(0f);
            etPhoneNumber.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(400)
                    .setStartDelay(100)
                    .start();
        }

        if (btnAddToBlacklist != null) {
            btnAddToBlacklist.setTranslationX(-50f);
            btnAddToBlacklist.setAlpha(0f);
            btnAddToBlacklist.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(400)
                    .setStartDelay(200)
                    .start();
        }

        if (btnAddToWhitelist != null) {
            btnAddToWhitelist.setTranslationX(50f);
            btnAddToWhitelist.setAlpha(0f);
            btnAddToWhitelist.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(400)
                    .setStartDelay(250)
                    .start();
        }

        if (switchMode != null) {
            switchMode.setAlpha(0f);
            switchMode.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .setStartDelay(300)
                    .start();
        }

        if (tabBlacklist != null && tabWhitelist != null) {
            tabBlacklist.setAlpha(0f);
            tabWhitelist.setAlpha(0f);
            tabBlacklist.animate().alpha(1f).setDuration(300).setStartDelay(400).start();
            tabWhitelist.animate().alpha(1f).setDuration(300).setStartDelay(450).start();
        }

        if (lvBlacklist != null) {
            lvBlacklist.setAlpha(0f);
            lvBlacklist.animate().alpha(1f).setDuration(400).setStartDelay(500).start();
        }

        if (tvModeInfo != null) {
            tvModeInfo.setAlpha(0f);
            tvModeInfo.animate().alpha(1f).setDuration(400).setStartDelay(550).start();
        }
    }

    /**
     * Animation de suppression d'un élément de liste
     */
    private void animateItemRemoval(View view, Runnable onComplete) {
        view.animate()
                .translationX(200f)
                .alpha(0f)
                .setDuration(200)
                .withEndAction(onComplete)
                .start();
    }

    /**
     * Animation d'ajout d'un élément à la liste
     */
    private void animateItemAddition(ListView listView, int position) {
        View item = listView.getChildAt(position - listView.getFirstVisiblePosition());
        if (item != null) {
            item.setScaleX(0.8f);
            item.setScaleY(0.8f);
            item.setAlpha(0f);
            item.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(200)
                    .start();
        }
    }

    /**
     * Animation de changement de mode avec vibration légère
     */
    private void animateModeChange() {
        tvModeInfo.animate()
                .scaleX(1.02f)
                .scaleY(1.02f)
                .setDuration(100)
                .withEndAction(() -> {
                    tvModeInfo.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    /**
     * Animation pour le message Toast personnalisé
     */
    private void showAnimatedToast(String message, boolean isSuccess) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();

        // Animation pour le Toast (effet visuel)
        View toastView = toast.getView();
        if (toastView != null) {
            toastView.setAlpha(0f);
            toastView.animate().alpha(1f).setDuration(300).start();
            new Handler().postDelayed(() -> {
                if (toastView != null) {
                    toastView.animate().alpha(0f).setDuration(300).start();
                }
            }, 2000);
        }
    }

    // ==================== FIN DES MÉTHODES D'ANIMATION ====================

    private void initViews() {
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnAddToBlacklist = findViewById(R.id.btnAddToBlacklist);
        btnAddToWhitelist = findViewById(R.id.btnAddToWhitelist);
        tabBlacklist = findViewById(R.id.tabBlacklist);
        tabWhitelist = findViewById(R.id.tabWhitelist);
        lvBlacklist = findViewById(R.id.lvBlacklist);
        lvWhitelist = findViewById(R.id.lvWhitelist);
        tvEmptyList = findViewById(R.id.tvEmptyList);
        tvModeInfo = findViewById(R.id.tvModeInfo);
        switchMode = findViewById(R.id.switchMode);
    }

    private void setupSwitchMode() {
        switchMode.setChecked(isWhitelistMode);

        switchMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isWhitelistMode = isChecked;
            sharedPreferences.edit().putBoolean(KEY_WHITELIST_MODE, isChecked).apply();
            updateModeInfo();
            animateModeChange();
            debugShowLists();

            String mode = isChecked ? "WHITELIST (seuls les numéros autorisés passent)" : "BLACKLIST (seuls les numéros bloqués sont rejetés)";
            showAnimatedToast("Mode " + mode, true);
        });
    }

    private void setupListeners() {
        // Ajouter à la blacklist avec animation
        btnAddToBlacklist.setOnClickListener(v -> {
            animateButtonPress(v);

            String number = etPhoneNumber.getText().toString().trim();
            if (number.isEmpty()) {
                showAnimatedToast("Veuillez entrer un numéro", false);
                return;
            }

            String normalized = normalizeNumber(number);
            whitelistArray.remove(normalized);

            if (!blacklistArray.contains(normalized)) {
                blacklistArray.add(normalized);
                saveListsToFiles();
                updateBlacklistDisplay();
                showAnimatedToast("Ajouté à la blacklist: " + normalized, true);
                etPhoneNumber.setText("");

                // Animation pour le nouvel élément
                if (lvBlacklist.getVisibility() == View.VISIBLE) {
                    new Handler().postDelayed(() -> {
                        animateItemAddition(lvBlacklist, blacklistArray.size() - 1);
                    }, 100);
                }
                debugShowLists();
            } else {
                showAnimatedToast("Déjà dans la blacklist", false);
            }
        });

        // Ajouter à la whitelist avec animation
        btnAddToWhitelist.setOnClickListener(v -> {
            animateButtonPress(v);

            String number = etPhoneNumber.getText().toString().trim();
            if (number.isEmpty()) {
                showAnimatedToast("Veuillez entrer un numéro", false);
                return;
            }

            String normalized = normalizeNumber(number);
            blacklistArray.remove(normalized);

            if (!whitelistArray.contains(normalized)) {
                whitelistArray.add(normalized);
                saveListsToFiles();
                updateWhitelistDisplay();
                showAnimatedToast("Ajouté à la whitelist: " + normalized, true);
                etPhoneNumber.setText("");

                // Animation pour le nouvel élément
                if (lvWhitelist.getVisibility() == View.VISIBLE) {
                    new Handler().postDelayed(() -> {
                        animateItemAddition(lvWhitelist, whitelistArray.size() - 1);
                    }, 100);
                }
                debugShowLists();
            } else {
                showAnimatedToast("Déjà dans la whitelist", false);
            }
        });

        // Onglet Blacklist avec animation
        tabBlacklist.setOnClickListener(v -> {
            animateClick(v);
            animateTabSwitch(tabBlacklist, true, "blacklist");
            animateTabSwitch(tabWhitelist, false, "whitelist");

            tabBlacklist.setBackgroundTintList(getColorStateList(android.R.color.holo_red_dark));
            tabWhitelist.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
            lvBlacklist.setVisibility(View.VISIBLE);
            lvWhitelist.setVisibility(View.GONE);
            updateEmptyMessage(blacklistArray.isEmpty());
        });

        // Onglet Whitelist avec animation
        tabWhitelist.setOnClickListener(v -> {
            animateClick(v);
            animateTabSwitch(tabWhitelist, true, "whitelist");
            animateTabSwitch(tabBlacklist, false, "blacklist");

            tabWhitelist.setBackgroundTintList(getColorStateList(android.R.color.holo_green_dark));
            tabBlacklist.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
            lvWhitelist.setVisibility(View.VISIBLE);
            lvBlacklist.setVisibility(View.GONE);
            updateEmptyMessage(whitelistArray.isEmpty());
        });

        // Suppression par appui long sur blacklist avec animation
        lvBlacklist.setOnItemLongClickListener((parent, view, position, id) -> {
            String number = blacklistArray.get(position);

            animateItemRemoval(view, () -> {
                blacklistArray.remove(position);
                saveListsToFiles();
                updateBlacklistDisplay();
                showAnimatedToast("Supprimé de la blacklist: " + number, true);
                debugShowLists();
            });

            return true;
        });

        // Suppression par appui long sur whitelist avec animation
        lvWhitelist.setOnItemLongClickListener((parent, view, position, id) -> {
            String number = whitelistArray.get(position);

            animateItemRemoval(view, () -> {
                whitelistArray.remove(position);
                saveListsToFiles();
                updateWhitelistDisplay();
                showAnimatedToast("Supprimé de la whitelist: " + number, true);
                debugShowLists();
            });

            return true;
        });

        // Animation au focus sur le champ de saisie
        etPhoneNumber.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                etPhoneNumber.animate()
                        .scaleX(1.02f)
                        .scaleY(1.02f)
                        .setDuration(150)
                        .start();
            } else {
                etPhoneNumber.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start();
            }
        });
    }

    private void debugShowLists() {
        Set<String> blacklist = fileManager.loadBlacklist();
        Set<String> whitelist = fileManager.loadWhitelist();

        Log.d("DEBUG_MAIN", "=========================================");
        Log.d("DEBUG_MAIN", "=== CONTENU DE LA BLACKLIST (" + blacklist.size() + " éléments) ===");
        int i = 1;
        for (String b : blacklist) {
            Log.d("DEBUG_MAIN", "  " + i + ". " + b);
            i++;
        }

        Log.d("DEBUG_MAIN", "=== CONTENU DE LA WHITELIST (" + whitelist.size() + " éléments) ===");
        i = 1;
        for (String w : whitelist) {
            Log.d("DEBUG_MAIN", "  " + i + ". " + w);
            i++;
        }

        Log.d("DEBUG_MAIN", "=== MODE ACTUEL ===");
        Log.d("DEBUG_MAIN", "Mode Whitelist: " + isWhitelistMode);
        Log.d("DEBUG_MAIN", "=========================================");
    }

    private void loadListsFromFiles() {
        Set<String> blacklistSet = fileManager.loadBlacklist();
        Set<String> whitelistSet = fileManager.loadWhitelist();

        blacklistArray.clear();
        whitelistArray.clear();

        blacklistArray.addAll(blacklistSet);
        whitelistArray.addAll(whitelistSet);

        for (int i = 0; i < blacklistArray.size(); i++) {
            blacklistArray.set(i, normalizeNumber(blacklistArray.get(i)));
        }
        for (int i = 0; i < whitelistArray.size(); i++) {
            whitelistArray.set(i, normalizeNumber(whitelistArray.get(i)));
        }
    }

    private void saveListsToFiles() {
        fileManager.saveBlacklist(new HashSet<>(blacklistArray));
        fileManager.saveWhitelist(new HashSet<>(whitelistArray));
    }

    private void updateBlacklistDisplay() {
        blacklistAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, blacklistArray);
        lvBlacklist.setAdapter(blacklistAdapter);
        if (lvBlacklist.getVisibility() == View.VISIBLE) {
            updateEmptyMessage(blacklistArray.isEmpty());
        }
    }

    private void updateWhitelistDisplay() {
        whitelistAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, whitelistArray);
        lvWhitelist.setAdapter(whitelistAdapter);
        if (lvWhitelist.getVisibility() == View.VISIBLE) {
            updateEmptyMessage(whitelistArray.isEmpty());
        }
    }

    private void updateListDisplay() {
        updateBlacklistDisplay();
        updateWhitelistDisplay();

        if (isWhitelistMode) {
            tabWhitelist.performClick();
        } else {
            tabBlacklist.performClick();
        }
    }

    private void updateEmptyMessage(boolean isEmpty) {
        if (isEmpty) {
            tvEmptyList.setVisibility(View.VISIBLE);
            tvEmptyList.setAlpha(0f);
            tvEmptyList.animate().alpha(1f).setDuration(300).start();

            if (lvBlacklist.getVisibility() == View.VISIBLE) {
                tvEmptyList.setText("📭 Aucun numéro dans la blacklist\nAppuyez sur '+' pour en ajouter");
            } else {
                tvEmptyList.setText("📭 Aucun numéro dans la whitelist\nAppuyez sur '+' pour en ajouter");
            }
        } else {
            tvEmptyList.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                tvEmptyList.setVisibility(View.GONE);
            }).start();
        }
    }

    private void updateModeInfo() {
        if (isWhitelistMode) {
            tvModeInfo.setText("✅ MODE WHITELIST ACTIF - Seuls les numéros de la whitelist sont autorisés");
            tvModeInfo.setBackgroundColor(getColor(android.R.color.holo_green_dark));
        } else {
            tvModeInfo.setText("⚠️ MODE BLACKLIST ACTIF - Les numéros de la blacklist sont bloqués");
            tvModeInfo.setBackgroundColor(getColor(android.R.color.holo_orange_dark));
        }

        // Animation de changement
        tvModeInfo.animate()
                .scaleX(1.02f)
                .scaleY(1.02f)
                .setDuration(100)
                .withEndAction(() -> {
                    tvModeInfo.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private String normalizeNumber(String number) {
        if (number == null) return "";

        String cleaned = number.replaceAll("[\\s\\-()]", "");

        if (cleaned.startsWith("+213")) {
            cleaned = "0" + cleaned.substring(4);
        }

        if (cleaned.startsWith("00213")) {
            cleaned = "0" + cleaned.substring(5);
        }

        if (cleaned.length() == 9 && !cleaned.startsWith("0")) {
            cleaned = "0" + cleaned;
        }

        return cleaned;
    }

    // Méthodes publiques pour les Receivers
    public static String normalizeNumberStatic(String number) {
        if (number == null) return "";

        String cleaned = number.replaceAll("[\\s\\-()]", "");

        if (cleaned.startsWith("+213")) {
            cleaned = "0" + cleaned.substring(4);
        }

        if (cleaned.startsWith("00213")) {
            cleaned = "0" + cleaned.substring(5);
        }

        if (cleaned.length() == 9 && !cleaned.startsWith("0")) {
            cleaned = "0" + cleaned;
        }

        return cleaned;
    }

    public static boolean isAllowed(String number, SharedPreferences prefs, FileManager fm) {
        if (prefs == null || number == null) return true;

        String normalized = normalizeNumberStatic(number);
        boolean isWhitelistMode = prefs.getBoolean(KEY_WHITELIST_MODE, false);

        Log.d("isAllowed", "Numéro: " + number);
        Log.d("isAllowed", "Normalisé: " + normalized);
        Log.d("isAllowed", "Mode Whitelist: " + isWhitelistMode);

        if (isWhitelistMode) {
            Set<String> whitelist = fm != null ? fm.loadWhitelist() : new HashSet<>();
            Log.d("isAllowed", "Whitelist: " + whitelist.toString());

            for (String w : whitelist) {
                String normalizedW = normalizeNumberStatic(w);
                Log.d("isAllowed", "Comparaison: " + normalizedW + " == " + normalized);
                if (normalizedW.equals(normalized)) {
                    Log.d("isAllowed", "✅ NUMÉRO TROUVÉ DANS WHITELIST - AUTORISÉ");
                    return true;
                }
            }
            Log.d("isAllowed", "❌ NUMÉRO NON TROUVÉ DANS WHITELIST - BLOQUÉ");
            return false;

        } else {
            Set<String> blacklist = fm != null ? fm.loadBlacklist() : new HashSet<>();
            Log.d("isAllowed", "Blacklist: " + blacklist.toString());

            for (String b : blacklist) {
                String normalizedB = normalizeNumberStatic(b);
                Log.d("isAllowed", "Comparaison: " + normalizedB + " == " + normalized);
                if (normalizedB.equals(normalized)) {
                    Log.d("isAllowed", "❌ NUMÉRO TROUVÉ DANS BLACKLIST - BLOQUÉ");
                    return false;
                }
            }
            Log.d("isAllowed", "✅ NUMÉRO NON TROUVÉ DANS BLACKLIST - AUTORISÉ");
            return true;
        }
    }

    // Permissions
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissionsNeeded = new ArrayList<>();

            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(permission);
                }
            }

            if (!permissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission refusée: " + permissions[i], Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        // Animation de fermeture
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}