/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 24.01.15.
 */

import java.util.*;

import android.content.*;
import android.net.Uri;
import android.util.Log;


import org.geometerplus.android.fbreader.api.PluginApi;

public class PluginInfo extends PluginApi.PluginInfo {

    @Override
    protected List<PluginApi.ActionInfo> implementedActions(Context context) {

/*
        return Collections.<PluginApi.ActionInfo>singletonList(new PluginApi.MenuActionInfo(
                Uri.parse("http://data.viorsan.com/plugin/test/plugin"),
                context.getText(R.string.test_plugin_menu_item).toString(),
                Integer.MAX_VALUE


        )); */
        return null;
    }
}

