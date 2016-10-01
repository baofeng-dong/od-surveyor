package com.meyersj.mobilesurveyor.app.survey.Confirm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.survey.MapFragment;
import com.meyersj.mobilesurveyor.app.survey.SurveyManager;
import com.meyersj.mobilesurveyor.app.util.Utils;


public class ConfirmFragment extends MapFragment {

    protected Context context;
    protected SurveyManager manager;
    protected Button submit;
    protected ViewPager pager;

    public void setParams(Context context, SurveyManager manager, ViewPager pager) {
        this.context = context;
        this.manager = manager;
        this.pager = pager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_confirm, container, false);

        activity = getActivity();
        context = activity.getApplicationContext();

        submit = (Button) view.findViewById(R.id.submit_btn);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Boolean[] validate = manager.validate();
                ValidateResult result = manager.validate();

                if (!result.getValid()) {
                    Utils.shortToastCenter(context, result.getError());
                    pager.setCurrentItem(result.getTabIndex());
                    return;
                }
                exitWithSurveyBundle(true);

//                for(int i = 0; i < validate.length; i++) {
//                    Log.d(TAG, validate[i].toString());
//                    if(!validate[i]) {
//                        String msg = "";
//                        switch(i) {
//                            case 0:
//                                msg = "missing information about origin location";
//                                break;
//                            case 1:
//                                msg = "missing information about destination location";
//                                break;
//                            case 2:
//                                msg = "on and off locations are incomplete";
//                                break;
//                            case 3:
//                                msg = "you must include the current route";
//                                break;
//                        }
//                        Utils.shortToastCenter(context, msg);
//                        pager.setCurrentItem(i);
//                        //TODO change previous and next buttons being enabled/disabled
//                        return;
//                    }
//                }
//                exitWithSurveyBundle(true);
            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //this.activity = (SurveyActivity) activity;
    }

    //@Override
    //public void onAttach(Activity myActivity) {
    //    super.onAttach(myActivity);
    //    this.activity= (Activity_Main) myActivity;
    //}

    @Override
    public void onDetach() {
        super.onDetach();
    }

    protected void exitWithSurveyBundle(Boolean valid) {
        int result  = activity.RESULT_CANCELED;
        Intent intent = new Intent();
        if (valid) {
            manager.setValidated("1");
            intent = manager.addExtras(intent);
            result = activity.RESULT_OK;
        }
        activity.setResult(result, intent);
        activity.finish();
    }

    @Override
    public void updateView(SurveyManager manager) {}

}
