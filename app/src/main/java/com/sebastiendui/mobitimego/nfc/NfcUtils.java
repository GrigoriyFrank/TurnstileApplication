package com.sebastiendui.mobitimego.nfc;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;

/**
 * Utility class for NFC tag reading functions
 */
public class NfcUtils {

    static final String TAG = "NfcUtils";

    /**
     * read {@Link Tag}
     *
     * @param tag to read
     * @return string representation of tag data
     */
    public static String dumpTagData(Tag tag) {
        StringBuilder sb = new StringBuilder();
        byte[] id = tag.getId();
        sb.append(toHex(id));
        // Log.i(TAG, " from NFC(NfcUtils): " +   sb.append(toHex(id)));

        for (String tech : tag.getTechList()) {

            if (tech.equals(IsoDep.class.getName())) {
                //sb.append('\n');
                sb.delete(0, sb.length());


                byte[] result = new byte[200];
                try {

                    IsoDep mifare = IsoDep.get(tag);
                    mifare.connect();

                    byte[] SELECT = {
                            (byte) 0x90, // CLA Class
                            (byte) 0x6a, // INS Instruction
                            (byte) 0x00, // P1  Parameter 1
                            (byte) 0x00, // P2  Parameter 2
                            (byte) 0x00 // Length
                    };

                    byte[] appid = mifare.transceive(SELECT);

                    byte[] SELECT1 = {
                            (byte) 0x90, // CLA Class
                            (byte) 0x5a, // INS Instruction
                            (byte) 0x00, // P1  Parameter 1
                            (byte) 0x00, // P2  Parameter 2
                            (byte) 0x03, // Length
                            appid[0],
                            appid[1],
                            appid[2],
                            (byte) 0x00
                    };

                    byte[] selectApp = mifare.transceive(SELECT1);

                    byte[] SELECT2 = {
                            (byte) 0x90, // CLA Class
                            (byte) 0xbd, // INS Instruction
                            (byte) 0x00, // P1  Parameter 1
                            (byte) 0x00, // P2  Parameter 2
                            (byte) 0x07, // Length
                            (byte) 0x01, // file no
                            (byte) 0x00,
                            (byte) 0x00,
                            (byte) 0x00,
                            (byte) 0x00,
                            (byte) 0x00,
                            (byte) 0x00,
                            (byte) 0x00
                    };

                    byte[] packetbuffer = mifare.transceive(SELECT2);
                    byte EOF = (byte) 0x00;

                    byte[] filecontents = new byte[200];
                    int i = 0;


                    do {
                        int nextByte = 0;
                        int max_count = 59;

                        do {
                            filecontents[i] = packetbuffer[nextByte];
                            nextByte += 1;
                            i++;
                        } while (nextByte < (max_count) && packetbuffer[nextByte] != EOF);

                        if (packetbuffer[nextByte] == 0x00) break; //EOF

                        byte[] SELECT3 = {
                                (byte) 0x90, // CLA Class
                                (byte) 0xaf, // INS Instruction
                                (byte) 0x00, // P1  Parameter 1
                                (byte) 0x00, // P2  Parameter 2
                                (byte) 0x00 // Length

                        };
                        packetbuffer = mifare.transceive(SELECT3);


                    } while (true);

                    result = filecontents;

                    String str = new String(result);
                    sb.append(str);
                    mifare.close();
                } catch (Exception e) {
                    Log.e(TAG, "Something wrong --> ", e);
                    sb.append("\n");
                    sb.append(" Catch Exception while scanning: " + e.toString());
                }


            }

        }

        return sb.toString();

    }

    /**
     * Useful methods for tag data reading/converting
     */
    static Tag cleanupTag(Tag oTag) {
        if (oTag == null)
            return null;

        String[] sTechList = oTag.getTechList();

        Parcel oParcel = Parcel.obtain();
        oTag.writeToParcel(oParcel, 0);
        oParcel.setDataPosition(0);

        int len = oParcel.readInt();
        byte[] id = null;
        if (len >= 0) {
            id = new byte[len];
            oParcel.readByteArray(id);
        }
        int[] oTechList = new int[oParcel.readInt()];
        oParcel.readIntArray(oTechList);
        Bundle[] oTechExtras = oParcel.createTypedArray(Bundle.CREATOR);
        int serviceHandle = oParcel.readInt();
        int isMock = oParcel.readInt();
        IBinder tagService;
        if (isMock == 0) {
            tagService = oParcel.readStrongBinder();
        } else {
            tagService = null;
        }
        oParcel.recycle();

        int nfca_idx = -1;
        int mc_idx = -1;
        short oSak = 0;
        short nSak = 0;

        for (int idx = 0; idx < sTechList.length; idx++) {
            if (sTechList[idx].equals(NfcA.class.getName())) {
                if (nfca_idx == -1) {
                    nfca_idx = idx;
                    if (oTechExtras[idx] != null && oTechExtras[idx].containsKey("sak")) {
                        oSak = oTechExtras[idx].getShort("sak");
                        nSak = oSak;
                    }
                } else {
                    if (oTechExtras[idx] != null && oTechExtras[idx].containsKey("sak")) {
                        nSak = (short) (nSak | oTechExtras[idx].getShort("sak"));
                    }
                }
            } else if (sTechList[idx].equals(MifareClassic.class.getName())) {
                mc_idx = idx;
            }
        }

        boolean modified = false;

        if (oSak != nSak) {
            oTechExtras[nfca_idx].putShort("sak", nSak);
            modified = true;
        }

        if (nfca_idx != -1 && mc_idx != -1 && oTechExtras[mc_idx] == null) {
            oTechExtras[mc_idx] = oTechExtras[nfca_idx];
            modified = true;
        }

        if (!modified) {
            return oTag;
        }

        Parcel nParcel = Parcel.obtain();
        nParcel.writeInt(id.length);
        nParcel.writeByteArray(id);
        nParcel.writeInt(oTechList.length);
        nParcel.writeIntArray(oTechList);
        nParcel.writeTypedArray(oTechExtras, 0);
        nParcel.writeInt(serviceHandle);
        nParcel.writeInt(isMock);
        if (isMock == 0) {
            nParcel.writeStrongBinder(tagService);
        }
        nParcel.setDataPosition(0);

        Tag nTag = Tag.CREATOR.createFromParcel(nParcel);

        nParcel.recycle();

        return nTag;
    }

    static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                //sb.append(" ");
                sb.append("");
            }
        }
        return sb.toString();
    }

    static String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    static long toDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    static long toReversedDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

}
