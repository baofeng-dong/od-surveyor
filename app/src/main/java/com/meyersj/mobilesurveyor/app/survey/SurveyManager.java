package com.meyersj.mobilesurveyor.app.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.meyersj.mobilesurveyor.app.survey.Confirm.ValidateResult;
import com.meyersj.mobilesurveyor.app.util.Cons;

import static com.meyersj.mobilesurveyor.app.survey.SurveyActivity.compareType;


public class SurveyManager {

    protected static final String TAG = "SurveyManager";

    protected Context context;
    protected Activity activity;
    protected String line;
    protected String dir;
    protected Location orig;
    protected Location dest;
    protected Marker onStop;
    protected Marker offStop;
    protected String[] transfers;
    protected String validated = "2"; //validated, yes == 1, no == 2, ignore location

    public class Location {
        public Marker loc;
        public String region; //outside of region, yes == 1, no == 2, ignore location
        public String purpose;
        public String purposeOther;
        public String mode;
        public String modeOther;
        public String blocks;
        public String parking;

        public Location() {
            loc = null;
            region = "2";  //default not outside region
            purpose = "";
            purposeOther = "";
            mode = "";
            modeOther = "";
            blocks = "";
            parking = "";
        }

        public Boolean validate() {
            if(loc == null && region.equals("2"))
                return false;
            if(purpose.isEmpty() || mode.isEmpty())
                return false;
            return true;
        }
    }

    public SurveyManager(Context context, Activity activity, String line) {
        this.context = context;
        this.activity = activity;
        this.orig = new Location();
        this.dest = new Location();
        this.line = line;
    }

    public String key(String prefix, String cons) {
        return prefix + "_" + cons;
    }

    public ValidateResult validate() {
        String[] tabTypes = SurveyActivity.fetchTabTypes(context);

        if (tabTypes == null || tabTypes.length == 0 || tabTypes[0].isEmpty()) {
            return new ValidateResult();
        }

        Integer tabIndex = 0;
        for (; tabIndex < tabTypes.length; tabIndex++) {
            if (compareType(tabTypes[tabIndex], SurveyActivity.TabTypes.ORIGIN)) {
                if (!this.orig.validate()) {
                    return new ValidateResult(false, tabIndex, "missing information about origin location");
                }
            } else if (compareType(tabTypes[tabIndex], SurveyActivity.TabTypes.DESTINATION)) {
                if (!this.dest.validate()) {
                    return new ValidateResult(false, tabIndex, "missing information about destination location");
                }
            } else if (compareType(tabTypes[tabIndex], SurveyActivity.TabTypes.ON_OFF)) {
                if(onStop == null || offStop == null) {
                    return new ValidateResult(false, tabIndex, "on and off locations are incomplete");
                }
            } else if (compareType(tabTypes[tabIndex], SurveyActivity.TabTypes.TRANSFERS)) {
                Boolean validTransfer = false;
                for(String route: transfers) {
                    if(route != null && !route.isEmpty() && route.equals(line)) {
                        validTransfer = true;
                        break;
                    }
                }
                if (!validTransfer) {
                    return new ValidateResult(false, tabIndex, "you must include the current route");
                }
            } else {
                // this is an error
                throw new RuntimeException("unexpected tab type " + tabTypes[tabIndex]);
            }
        }
        return new ValidateResult();
//
//        Boolean[] flags = new Boolean[4];
//        flags[0] = this.orig.validate();
//        flags[1] = this.dest.validate();
//        flags[2] = false;
//        if(onStop != null && offStop != null)
//            flags[2] = true;
//        flags[3] = false;
//        printTransfers();
//        Log.d(TAG, line);
//        for(String route: transfers) {
//            if(route != null && !route.isEmpty() && route.equals(line)) {
//                flags[3] = true;
//                break;
//            }
//        }
//        return flags;
    }

    public void setLocation(Marker marker, String mode) {
        if(mode.equals("origin")) {
            this.orig.loc = marker;
        }
        else if(mode.equals("destination")) {
            this.dest.loc = marker;
        }
    }

    public Marker getOrig(){
        return this.orig.loc;
    }

    public Marker getDest(){
        return this.dest.loc;
    }

    public void setValidated(String validated) {
        this.validated = validated;
    }

    public void setStop(Marker marker, String passage) {
        if(passage.equals(Cons.BOARD)) {
            this.onStop = marker;
        }
        else if(passage.equals(Cons.ALIGHT)) {
            this.offStop = marker;
        }
    }

    public void setRegion(String region, String passage) {
        if(passage.equals("origin")) {
            this.orig.region = region;
        }
        else if(passage.equals("destination")) {
            this.dest.region = region;
        }
    }

    public void updatePurpose(String passage, String purposeValue) {
        if(passage.equals("origin")) {
            this.orig.purpose = purposeValue;
            this.orig.purposeOther = null;
        }
        else if(passage.equals("destination")) {
            this.dest.purpose = purposeValue;
            this.dest.purposeOther = null;
        }
    }
    public void updatePurposeOther(String passage, String otherValue) {
        if(passage.equals("origin")) {
            this.orig.purposeOther = otherValue;
        }
        else if(passage.equals("destination")) {
            this.dest.purposeOther = otherValue;
        }
    }

    public void updateMode(String passage, String modeValue) {
        if(passage.equals("origin")) {
            this.orig.mode = modeValue;
            this.orig.blocks = null;
            this.orig.parking = null;
        }
        else if(passage.equals("destination")) {
            this.dest.mode = modeValue;
            this.dest.blocks = null;
            this.dest.parking = null;
        }
    }

    public void updateModeOther(String passage, String otherValue) {
        if(passage.equals("origin")) {
            this.orig.modeOther = otherValue;
        }
        else if(passage.equals("destination")) {
            this.dest.modeOther = otherValue;
        }
    }

    public void updateBlocks(String passage, String blocksValue) {
        if(passage.equals("origin")) {
            this.orig.blocks = blocksValue;
        }
        else if(passage.equals("destination")) {
            this.dest.blocks = blocksValue;
        }
    }

    public void updateParking(String passage, String parkingValue) {
        if(passage.equals("origin")) {
            this.orig.parking = parkingValue;
        }
        else if(passage.equals("destination")) {
            this.dest.parking = parkingValue;
        }
    }

    public Marker getOnStop(){
        return this.onStop;
    }

    public Marker getOffStop(){
        return this.offStop;
    }

    public Intent addExtras(Intent intent) {
        intent.putExtra(key("orig", Cons.PURPOSE_ODK), orig.purpose);
        intent.putExtra(key("orig", Cons.PURPOSE_OTHER_ODK), orig.purposeOther);
        intent.putExtra(key("orig", Cons.ACCESS_ODK), orig.mode);
        intent.putExtra(key("orig", Cons.ACCESS_OTHER_ODK), orig.modeOther);
        intent.putExtra(key("orig", Cons.BLOCKS_ODK), orig.blocks);
        intent.putExtra(key("orig", Cons.PARKING_ODK), orig.parking);
        intent.putExtra(key("orig", "region"), orig.region);

        if (orig.region.equals("1") || orig.loc == null) {
            intent.putExtra(key("orig", Cons.LAT_ODK), "");
            intent.putExtra(key("orig", Cons.LNG_ODK), "");
        } else {
            LatLng latLng = orig.loc.getPoint();
            intent.putExtra(key("orig", Cons.LAT_ODK), latLng.getLatitude());
            intent.putExtra(key("orig", Cons.LNG_ODK), latLng.getLongitude());
        }

        intent.putExtra(key("dest", Cons.PURPOSE_ODK), dest.purpose);
        intent.putExtra(key("dest", Cons.PURPOSE_OTHER_ODK), dest.purposeOther);
        intent.putExtra(key("dest", Cons.EGRESS_ODK), dest.mode);
        intent.putExtra(key("dest", Cons.EGRESS_OTHER_ODK), dest.modeOther);
        intent.putExtra(key("dest", Cons.BLOCKS_ODK), dest.blocks);
        intent.putExtra(key("dest", Cons.PARKING_ODK), dest.parking);
        intent.putExtra(key("dest", "region"), dest.region);

        if (dest.region.equals("1") || dest.loc == null) {
            intent.putExtra(key("dest", Cons.LAT_ODK), "");
            intent.putExtra(key("dest", Cons.LNG_ODK), "");
        } else  {
            LatLng latLng = dest.loc.getPoint();
            intent.putExtra(key("dest", Cons.LAT_ODK), latLng.getLatitude());
            intent.putExtra(key("dest", Cons.LNG_ODK), latLng.getLongitude());
        }

        if(onStop != null) {
            String onStopID = onStop.getDescription();
            intent.putExtra(Cons.BOARD_ID_ODK, onStopID);
        }
        else {
            intent.putExtra(Cons.BOARD_ID_ODK, "");
        }
        if(offStop != null) {
            String offStopID = offStop.getDescription();
            intent.putExtra(Cons.ALIGHT_ID_ODK, offStopID);
        }
        else {
            intent.putExtra(Cons.ALIGHT_ID_ODK, "");
        }
        Integer count = 0;

        if (transfers != null && transfers.length > 0) {
            for (int i = 0; i < Cons.MAX_TRANSFERS; i++) {
                if (transfers[i] != null && !transfers[i].isEmpty()) {
                    intent.putExtra("route" + String.valueOf(++count), transfers[i]);
                } else {
                    intent.putExtra("route" + String.valueOf(++count), "");
                }
            }
        }
        intent.putExtra("validated", validated);
        return intent;
    }

    public void inputPurposeOther(final Activity activity, final String mode) {
        String title = "Location Type";
        String prompt =  "Specify other";
        final EditText input = new EditText(context);
        AlertDialog.Builder alert = buildAlert(activity, title, prompt, input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Editable value = input.getText();
                updatePurposeOther(mode, value.toString());
            }
        });
        alert.show();
    }

    public void inputModeOther(final Activity activity, final String mode) {
        String title = "Mode of Arrival/Departure";
        String prompt = "Specify other";
        final EditText input = new EditText(context);
        AlertDialog.Builder alert = buildAlert(activity, title, prompt, input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Editable value = input.getText();
                updateModeOther(mode, value.toString());
            }
        });
        alert.show();
    }

    public void inputBlocks(final Activity activity, final String mode) {
        String title = "Number of Blocks";
        String prompt = "How many blocks did/will you walk?";
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder alert = buildAlert(activity, title, prompt, input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Editable value = input.getText();
                updateBlocks(mode, value.toString());
            }
        });
        alert.show();
    }

    public void inputParking(final Activity activity, final String mode) {
        String title = "Parking Location";
        String prompt =  "Where were/are you parked?";
        final EditText input = new EditText(context);
        AlertDialog.Builder alert = buildAlert(activity, title, prompt, input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Editable value = input.getText();
                updateParking(mode, value.toString());
            }
        });
        alert.show();
    }

    protected AlertDialog.Builder buildAlert(final Activity activity, String title, String prompt,
                                             EditText input) {
        return new AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(prompt)
            .setView(input)
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
    }

    public void unfinishedExit(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent = addExtras(intent);
                        activity.setResult(activity.RESULT_OK, intent);
                        activity.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                });

        AlertDialog select = builder.create();
        select.show();
    }

    public void setTransfers(String[] transfers) {
        this.transfers = transfers;
    }

    protected void printTransfers() {
        String sel = "";
        for (String s : transfers) {
            if (s == null || s.isEmpty()) {
                sel += "null, ";
            } else {
                sel += s + ", ";
            }
        }
        Log.d(TAG, "Transfers: " + sel);
    }
}
