package example.dialog;

import android.content.Context;

import com.style.dialog.BaseSingleWheelDialog;

import example.helper.DataHelper;

/**
 * Created by xiajun on 2018/6/9.
 */

public class OccupationDialog extends BaseSingleWheelDialog {
    public OccupationDialog(Context context) {
        super(context, DataHelper.getOccupationSelf(context));

    }

    @Override
    protected void init() {
        super.init();
        //setMyTitle("选择职业");
    }
}
