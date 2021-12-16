package com.example.printing;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.printer.UsbThermalPrinter;
import com.telpo.tps550.api.util.ShellUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //handler = new MyHandler();
        print();
    }

    public void print(){
        //handler.sendMessage(handler.obtainMessage(PRINTPAPERWALK, 1, 0, null));
        //new paperWalkPrintThread().start();
        preference = getSharedPreferences("TPS390PRINTER", MODE_PRIVATE);
        editor = preference.edit();
        handler=new MyHandler();
        new contentPrintThread().start();
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PRINTPAPERWALK:
                    new paperWalkPrintThread().start();
                    break;
                default:
                    Toast.makeText(MainActivity.this, "Print Error!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    String[] portNum = new String[20];
    String[] productNum = new String[20];
    String[] readerNum = new String[4];
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;
    UsbThermalPrinter mUsbThermalPrinter = new UsbThermalPrinter(MainActivity.this);
    boolean isHighSpeed = false;
    private String printVersion;
    private final int PRINTVERSION = 5;
    MyHandler handler;
    public static int paperWalk;
    private String Result;
    private Boolean nopaper = false;
    private final int PRINTERR = 11;
    private final int OVERHEAT = 12;
    private final int CANCELPROMPT = 10;
    private final int NOPAPER = 3;
    private final int PRINTPAPERWALK = 8;
    private int leftDistance=0;
    private int lineDistance=0;
    private boolean isBold=false;
    private int printGray=5;
    private int wordFont=24;
    private String printContent="Text \n Text  " +
            "\n Text";

    private String getUsbPrinterDev(){
        String msgSuccess = ShellUtils.execCommand("cat /proc/bus/usb/devices", false).successMsg;
        searchAllIndex(msgSuccess,"Dev#=",1);
        searchAllIndex(msgSuccess,"Product=",2);
        return checkPort(portNum,productNum);
    }

    private String checkPort(String[] port,String[] product) {
        int k = -1;
        for(int i=0;i<20;i++) {
            if(productNum[i] != null &&productNum[i].equals("USB Thermal Printer")) {
                k++;
                readerNum[k] = portNum[i];
                Log.d("tagg", "readnum[]:"+readerNum[k]);
                editor.putString("usbPrinterDev", readerNum[k]);
                editor.commit();
                return readerNum[k];
            }
        }
        editor.putString("usbPrinterDev", "-2");
        editor.commit();
        return "-2";
    }

    private void searchAllIndex(String str,String key,int type) {
        if(str != null && !str.equals("")){
            int a = str.indexOf(key);
            int i=-1;
            while (a != -1) {
                i++;
                if(type ==1) {
                    portNum[i] = str.substring(a+5, a+8);
                    Log.d("tagg", "portNum["+i+"]:"+portNum[i]);
                }
                else if(type ==2) {
                    productNum[i] = str.substring(a+8, a+27);
                    Log.d("tagg", "portNum["+i+"]:"+portNum[i]);
                }
                a = str.indexOf(key, a + 1);//*浠庤繖涓储寮曞線鍚庡紑濮嬬涓�涓嚭鐜扮殑浣嶇疆
            }
        }
    }

    private void getVersion(){
        try {
            Log.d("tagg", "getVersion");
            if(isHighSpeed){
                mUsbThermalPrinter.start(1);
            }else{
                mUsbThermalPrinter.start(0);
            }
            mUsbThermalPrinter.reset();
            printVersion = mUsbThermalPrinter.getVersion();
            Log.d("tagg", "printVersion:"+printVersion);
        } catch (TelpoException e) {
            e.printStackTrace();
        } finally {
            if (printVersion != null) {
                Message message = new Message();
                message.what = PRINTVERSION;
                message.obj = "1";
                handler.sendMessage(message);
            } else {
                Message message = new Message();
                message.what = PRINTVERSION;
                message.obj = "0";
                handler.sendMessage(message);
            }
        }
    }

    private class paperWalkPrintThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                String temp = getUsbPrinterDev();
                Log.d("tagg", "getUsbPrinterDev():"+temp);
                Log.d("tagg", "preference:"+preference.getString("usbPrinterDev", "-1"));
                if(temp.equals("-2") || !temp.equals(preference.getString("usbPrinterDev", "-1"))){
                    mUsbThermalPrinter.stop();
                    Thread.sleep(250);
                    if(isHighSpeed){
                        mUsbThermalPrinter.start(1);
                    }else{
                        mUsbThermalPrinter.start(0);
                    }
                    getVersion();
                }
                mUsbThermalPrinter.reset();
                mUsbThermalPrinter.walkPaper(paperWalk);
            } catch (Exception e) {
                e.printStackTrace();
                Result = e.toString();
                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                    nopaper = true;
                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                } else {
                    handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
                }
            } finally {
                handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
                if (nopaper) {
                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                    nopaper = false;
                    return;
                }
            }
        }
    }

    private class contentPrintThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                String temp = getUsbPrinterDev();
                Log.d("tagg", "getUsbPrinterDev():"+temp);
                Log.d("tagg", "preference:"+preference.getString("usbPrinterDev", "-1"));
                if(temp.equals("-2") || !temp.equals(preference.getString("usbPrinterDev", "-1"))){
                    mUsbThermalPrinter.stop();
                    Thread.sleep(250);
                    if(isHighSpeed){
                        mUsbThermalPrinter.start(1);
                    }else{
                        mUsbThermalPrinter.start(0);
                    }
                    getVersion();
                }
                mUsbThermalPrinter.reset();
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);
                mUsbThermalPrinter.setLeftIndent(leftDistance);
                mUsbThermalPrinter.setLineSpace(lineDistance);
                mUsbThermalPrinter.setBold(isBold);
                mUsbThermalPrinter.setTextSize(wordFont);
                //mUsbThermalPrinter.setHighlight(true);
				/*if (isSupportAutoBreak()) {
					mUsbThermalPrinter.autoBreakSet(button_auto_linefeed.isChecked());
				}*/
                mUsbThermalPrinter.setGray(printGray);
                mUsbThermalPrinter.addString(printContent);
                mUsbThermalPrinter.printString();
                mUsbThermalPrinter.walkPaper(20);
            } catch (Exception e) {
                e.printStackTrace();
                Result = e.toString();
                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                    nopaper = true;
                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                } else {
                    handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
                }
            } finally {
                handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
                if (nopaper) {
                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                    nopaper = false;
                    return;
                }
            }
        }
    }
}