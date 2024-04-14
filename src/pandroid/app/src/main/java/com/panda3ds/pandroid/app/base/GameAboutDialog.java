package com.panda3ds.pandroid.app.base;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.documentfile.provider.DocumentFile;

import com.panda3ds.pandroid.R;
import com.panda3ds.pandroid.app.PandroidApplication;
import com.panda3ds.pandroid.app.game.GameLauncher;
import com.panda3ds.pandroid.data.game.GameMetadata;
import com.panda3ds.pandroid.utils.CompatUtils;
import com.panda3ds.pandroid.utils.FileUtils;
import com.panda3ds.pandroid.utils.ZipBuilder;
import com.panda3ds.pandroid.utils.GameUtils;
import com.panda3ds.pandroid.view.gamesgrid.GameIconView;
import com.panda3ds.pandroid.lang.Task;

public class GameAboutDialog extends BaseSheetDialog {
    private final GameMetadata game;
    private static final String MIME_TYPE_ZIP = "application/zip";
    public GameAboutDialog(@NonNull Context context, GameMetadata game) {
        super(context);
        this.game = game;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_game_about);

        ((GameIconView) findViewById(R.id.game_icon)).setImageBitmap(game.getIcon());
        ((TextView) findViewById(R.id.game_title)).setText(game.getTitle());
        ((TextView) findViewById(R.id.game_publisher)).setText(game.getPublisher());
        ((TextView) findViewById(R.id.region)).setText(game.getRegions()[0].localizedName());
        ((TextView) findViewById(R.id.directory)).setText(FileUtils.obtainUri(game.getRealPath()).getPath());
        findViewById(R.id.play).setOnClickListener(v -> {
            dismiss();
            GameUtils.launch(getContext(), game);
        });
        findViewById(R.id.shortcut).setOnClickListener(v -> {
            dismiss();
            makeShortcut();
        });
        findViewById(R.id.export_save).setOnClickListener(v -> {
            String inputPath = FileUtils.getPrivatePath() + "/" + FileUtils.getName(game.getRealPath()).replaceAll("\\..*", "") + "/SaveData/";
            String outputPath = "/storage/emulated/0/Android/media/com.panda3ds.pandroid/";
            String outputName = game.getTitle() + ".zip";

            // Create an instance of ZipBuilder
            ZipBuilder zipBuilder = new ZipBuilder(outputPath, outputName);

            
         new Task(()->{
            try {
              // Begin the zip file creation process
              zipBuilder.begin();

              // Append files or folders to the zip file
              zipBuilder.append(inputPath);

              // End the zip file creation process
              zipBuilder.end();

              System.out.println("Zip file created successfully.");
              createDocument(MIME_TYPE_ZIP, outputName, outputPath);
            } catch (Exception e) {
              System.err.println("Error creating zip file: " + e.getMessage());
           }
         }).start();
        });

        if (game.getRomPath().startsWith("folder:")) {
            findViewById(R.id.remove).setVisibility(View.GONE);
        } else {
            findViewById(R.id.remove).setOnClickListener(v -> {
                dismiss();
                if (game.getRomPath().startsWith("elf:")) {
                    FileUtils.delete(game.getRealPath());
                }
                GameUtils.removeGame(game);
            });
        }
    }

    private void createDocument(@NonNull String mimeType, String fileName, String outputDirPath) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.fromFile(new File(outputDirPath)));
        startActivityForResult(intent, 1);
    }

    // Make a shortcut for a specific game
    private void makeShortcut() {
        Context context = CompatUtils.findActivity(getContext());
        ShortcutInfoCompat.Builder shortcut = new ShortcutInfoCompat.Builder(context, game.getId());
        if (game.getIcon() != null) {
            shortcut.setIcon(IconCompat.createWithAdaptiveBitmap(game.getIcon()));
        } else {
            shortcut.setIcon(IconCompat.createWithResource(getContext(), R.mipmap.ic_launcher));
        }

        shortcut.setActivity(new ComponentName(context, GameLauncher.class));
        shortcut.setLongLabel(game.getTitle());
        shortcut.setShortLabel(game.getTitle());
        Intent intent = new Intent(PandroidApplication.getAppContext(), GameLauncher.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(new Uri.Builder().scheme("pandroid-game").authority(game.getId()).build());
        shortcut.setIntent(intent);
        ShortcutManagerCompat.requestPinShortcut(context, shortcut.build(), null);
    }
}
