package example.activity;

import android.widget.TextView;

import com.style.base.BaseTitleBarActivity;
import com.style.framework.R;
import com.style.utils.AssetsUtil;
import kotlinx.android.synthetic.main.activity_user_agree.*
import java.io.IOException

public class ReadAssetsActivity : BaseTitleBarActivity() {

    override fun getLayoutResId(): Int {
        return R.layout.activity_user_agree;
    }

    override fun initData() {
        setToolbarTitle("用户协议");
        readData();
    }

    fun readData() {
        try {

            tv_useragree.setText(AssetsUtil.getAssetsText(this, "useragree.txt"));
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
