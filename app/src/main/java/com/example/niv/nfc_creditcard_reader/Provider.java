package com.example.niv.nfc_creditcard_reader;


import android.nfc.tech.IsoDep;
import android.util.Log;

import com.github.devnied.emvnfccard.enums.SwEnum;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import java.io.IOException;

import fr.devnied.bitlib.BytesUtils;

/**
 * Provider used to communicate with EMV card
 *
 * @author Millau Julien
 *
 */
public class Provider implements IProvider {

    /**
     * TAG for logger
     */
    private static final String TAG = Provider.class.getName();

    /**
     * Logger
     */
    private StringBuffer log = new StringBuffer();

    /**
     * Tag comm
     */
    private IsoDep mTagCom;

    @Override
    public byte[] transceive(final byte[] pCommand) throws CommunicationException {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "send: " + BytesUtils.bytesToString(pCommand));
        }

        byte[] response = null;
        try {
            // send command to emv card
            response = mTagCom.transceive(pCommand);
        } catch (IOException e) {
            throw new CommunicationException(e.getMessage());
        }

        try {
            Log.d(TAG, "resp: " + TlvUtil.prettyPrintAPDUResponse(response));
            SwEnum val = SwEnum.getSW(response);
            if (val != null) {
                Log.d(TAG, "resp: " + val.getDetail());
            }
            log.append("<pre>").append(TlvUtil.prettyPrintAPDUResponse(response).replace("\n", "<br/>").replace(" ", "&nbsp;"))
                    .append("</pre><br/>");
        } catch (Exception e) {
        }

        return response;
    }

    /**
     * Setter for the field mTagCom
     *
     * @param mTagCom
     *            the mTagCom to set
     */
    public void setmTagCom(final IsoDep mTagCom) {
        this.mTagCom = mTagCom;
        try {
            mTagCom.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used to get the field log
     *
     * @return the log
     */
    public StringBuffer getLog() {
        return log;
    }

}