package com.example.lsound.sound;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class MainActivity extends Activity {

    class UpdateHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg)
        {
            Audio.proc();
            next();
        }

        public void next()
        {
            removeMessages(0);
            sendMessageDelayed(obtainMessage(0), 1);
        }
    }
    UpdateHandler updateHandler_ = new UpdateHandler();
    AssetManager assetManager_;

    static final String Tag = "LIME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AudioManager audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        //android.util.Log.d(Tag, "output sample rate " + audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
        //android.util.Log.d(Tag, "output frames per buffer " + audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER));

        //String storage = getStoragePath();
        Audio.create();
        setContentView(R.layout.activity_main);
        assetManager_ = getAssets();
        Audio.loadResourcePackFromAsset(assetManager_, 0, "bgm.pak", true);
        Audio.loadResourcePackFromAsset(assetManager_, 1, "se.pak", false);

        Audio.createUserPlayer(0, 0);
        updateHandler_.next();
    }

    private String getStoragePath() {
        List<String> mountList = new ArrayList<String>();

        Scanner scanner = null;
        try {
            // �}�E���g�����擾����
            scanner = new Scanner(new FileInputStream(new File("/system/etc/vold.fstab")));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("dev_mount") || line.startsWith("fuse_mount")) {
                    // dev_mount �܂��� fuse_mount �̃p�X��o�^����i�����p�X�͓o�^���Ȃ��j
                    String path = line.replaceAll("\t", " ").split(" ")[2];
                    if (!mountList.contains(path)) {
                        mountList.add(path);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            android.util.Log.d(Tag, e.toString());
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        // Environment.isExternalStorageRemovable()��GINGERBREAD�ȍ~�����g���Ȃ�
        if(Build.VERSION_CODES.GINGERBREAD <= Build.VERSION.SDK_INT){
            // getExternalStorageDirectory()��㩂ł���΁A����path�����X�g���珜�O
            // StorageRemovable ���O���\�ȊO���X�g���[�W�i�܂�SD�J�[�h�j�Ȃ��true
            if (!Environment.isExternalStorageRemovable()) {   //
                mountList.remove(Environment.getExternalStorageDirectory().getPath());
            }
        }
        // ���O���ꂸ�Ɏc�������̂�SD�J�[�h�̃}�E���g��
        if(mountList.size() <= 0 ) {
            return getExternalStoragePath();
        }
        return mountList.get(0);
    }

    public static String getExternalStoragePath() {
        String path;
        // MOTOROLA �Ή�
        path = System.getenv("EXTERNAL_ALT_STORAGE");
        if (path != null){
            android.util.Log.d(Tag, "EXTERNAL_ALT_STORAGE");
            return path;
        }
        // Samsung �Ή�
        path = System.getenv("EXTERNAL_STORAGE2");
        if (path != null){
            android.util.Log.d(Tag, "EXTERNAL_STORAGE2");
            return path;
        }
        // �� Samsung + �W�� �Ή�
        path = System.getenv("EXTERNAL_STORAGE");
        if (path == null){
            android.util.Log.d(Tag, "EXTERNAL_STORAGE");
            path = Environment.getExternalStorageDirectory().getPath();
        }
        // HTC �Ή�
        File file = new File(path + "/ext_sd");
        if (file.exists()){
            android.util.Log.d(Tag, "ext_sd");
            path = file.getPath();
        }
        // ���̑��@��
        return path;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        Audio.terminate();
        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        Audio.pause(true);
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        Audio.pause(false);
        super.onResume();
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event)
    {
        switch(event.getAction())
        {
            case android.view.MotionEvent.ACTION_UP:
                Audio.play(1, 0, 1.0f);
                break;
        }
        return super.onTouchEvent(event);
    }
}
