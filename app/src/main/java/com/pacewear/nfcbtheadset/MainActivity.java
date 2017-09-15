
package com.pacewear.nfcbtheadset;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {
    NfcAdapter nfcAdapter;
    TextView promt;
    private PendingIntent mPendingIntent;
    IntentFilter[] filters;
    String[][] techList = new String[3][1];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        promt = (TextView) findViewById(R.id.promt);
        findViewById(R.id.btn_connect).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
//                new BtTest().invoke(MainActivity.this);
            }
        });
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (nfcAdapter == null) {
            promt.setText("�豸��֧��NFC��");
            finish();
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            promt.setText("����ϵͳ������������NFC���ܣ�");
            finish();
            return;
        }
        filters = new IntentFilter[2];
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        filters[1] = new IntentFilter();
        filters[1].addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filters[1].addCategory(Intent.CATEGORY_DEFAULT);
        techList = new String[][] {
                {
                        IsoDep.class.getName()
                }, {
                        NfcV.class.getName()
                }, {
                        NfcF.class.getName()
                }
        };
        // techList[0][0] = IsoDep.class.getName();
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("yqq", "onNewIntent action:" + intent.getAction());
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Log.d("yqq", "tech discovered");
            processIntent(intent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        nfcAdapter.enableForegroundDispatch(this, mPendingIntent, filters, techList);
        String action = getIntent().getAction();
        Log.d("yqq", "onresume action:" + action);
        // if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
        // Log.d("yqq", "tech discovered");
        // processIntent(getIntent());
        // }
    }

    @Override
    protected void onPause() {
        nfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }

    // �ַ�����ת��Ϊ16�����ַ�
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    private void processIntent(Intent intent) {
        Log.d("yqq", "tech processIntent!!!!!!!");
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tagFromIntent == null) {
            Log.d("yqq", "tech processIntent,tagFromIntent is null!!");
            return;
        }
        for (String tech : tagFromIntent.getTechList()) {
            System.out.println(tech);
        }
        boolean auth = false;
        MifareClassic mfc = MifareClassic.get(tagFromIntent);
        try {
            String metaInfo = "";
            // Enable I/O operations to the tag from this TagTechnology object.
            mfc.connect();
            int type = mfc.getType();// ��ȡTAG������
            int sectorCount = mfc.getSectorCount();// ��ȡTAG�а��������
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "��Ƭ���ͣ�" + typeS + "\n��" + sectorCount + "������\n��"
                    + mfc.getBlockCount() + "����\n�洢�ռ�: " + mfc.getSize() + "B\n";
            for (int j = 0; j < sectorCount; j++) {
                // Authenticate a sector with key A.
                auth = mfc.authenticateSectorWithKeyA(j,
                        MifareClassic.KEY_DEFAULT);
                int bCount;
                int bIndex;
                if (auth) {
                    metaInfo += "Sector " + j + ":��֤�ɹ�\n";
                    // ��ȡ�����еĿ�
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo += "Block " + bIndex + " : "
                                + bytesToHexString(data) + "\n";
                        bIndex++;
                    }
                } else {
                    metaInfo += "Sector " + j + ":��֤ʧ��\n";
                }
            }
            promt.setText(metaInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
