package com.happymax.gmshelper

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {
    private val activityList = ArrayList<GMSPageClass>()
    private lateinit var recyclerView:RecyclerView
    private lateinit var mSearchView:SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar:Toolbar = findViewById(R.id.toolBar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        getGMSActivities()
    }

    override fun onBackPressed() {
        PressBackBtn()
    }

    private fun PressBackBtn(){
        if(!mSearchView.isIconified){
            /*val method = mSearchView.javaClass.getDeclaredMethod("onCloseClicked")
            method.isAccessible = true
            method.invoke(mSearchView)*/
            mSearchView.setQuery("", false)
            mSearchView.clearFocus()
            mSearchView.onActionViewCollapsed()
            supportActionBar?.setDisplayHomeAsUpEnabled(false)//添加默认的返回图标
            supportActionBar?.setHomeButtonEnabled(false)//设置返回键可用
        }else{
            moveTaskToBack(true)
        }
    }

    private fun ShowDefaultList(){
        recyclerView.adapter = GMSPageListAdapter(activityList)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        val item = menu?.findItem(R.id.search)
        if(item != null){
            mSearchView = MenuItemCompat.getActionView(item) as SearchView
            mSearchView.isIconified = true
            mSearchView.isSubmitButtonEnabled = false
            mSearchView.setOnSearchClickListener(object : View.OnClickListener{
                override fun onClick(view: View?) {
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)//添加默认的返回图标
                    supportActionBar?.setHomeButtonEnabled(true)//设置返回键可用
                }
            })

            mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    //提交文本时调用
                    if(!query.isNullOrEmpty())
                    {
                        val querySequence = query
                        val filteredList = activityList.filter { it.activityName.contains(querySequence, true) || it.name.contains(querySequence, true) }
                        recyclerView.adapter = GMSPageListAdapter(filteredList)
                        return true
                    }
                    else
                        return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    //文本搜索框发生变化时调用
                    if(!newText.isNullOrEmpty()){
                        val querySequence = newText
                        val filteredList = activityList.filter { it.activityName.contains(querySequence, true) || it.name.contains(querySequence, true) }
                        recyclerView.adapter = GMSPageListAdapter(filteredList)
                        return true
                    }
                    else{
                        ShowDefaultList()
                        return true
                    }
                    return false
                }
            })
            mSearchView.setOnCloseListener(object : SearchView.OnCloseListener{
                override fun onClose(): Boolean {
                    ShowDefaultList()
                    mSearchView.clearFocus()
                    mSearchView.onActionViewCollapsed()
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)//添加默认的返回图标
                    supportActionBar?.setHomeButtonEnabled(false)//设置返回键可用
                    return true
                }
            })

        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> {
                PressBackBtn()
            }
            R.id.clear_shortcut -> {
                val shortCuts = ShortcutManagerCompat.getDynamicShortcuts(this)
                val shortCutsID = ArrayList<String>()
                for (shortcut in shortCuts){
                    shortCutsID.add(shortcut.id)
                }
                ShortcutManagerCompat.removeDynamicShortcuts(this, shortCutsID)
            }
            R.id.help -> Toast.makeText(this, "You clicked ${getText(R.string.toolbar_help)}", Toast.LENGTH_SHORT).show()
        }
        return true
    }

    private fun getGMSActivities(){
        val pm = this.getPackageManager()
        val pi: PackageInfo = pm.getPackageInfo("com.google.android.gms", PackageManager.GET_ACTIVITIES)

        for (activity in pi.activities){
            var isPermissionProtected = false
            /*val permission = activity.permission;
            if(permission != null){
                val permissionInfo = pm.getPermissionInfo(permission, 0)
                val level = permissionInfo.protectionLevel
                if(level == PermissionInfo.PROTECTION_SIGNATURE || level == PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM
                    || level == PermissionInfo.PROTECTION_DANGEROUS){
                    isPermissionProtected = true
                }
            }*/

            if(activity.exported == true && isPermissionProtected == false){
                val name = activity.name
                var labelSB = StringBuilder()
                //获取activity的label在R文件中的值
                val labelRes: Int = activity.labelRes
                //如果labelRes不为0，则使用PackageManager对象加载label名
                if (labelRes != 0) {
                    val label: String = pm.getText("com.google.android.gms", labelRes, null).toString()
                    labelSB.append(label)
                } else {
                    //否则，使用类名作为label名
                    labelSB.append(name.split(".").last())
                }
                val labelStr = labelSB.toString()

                if(name != null && !labelStr.isNullOrEmpty()){
                    activityList.add(GMSPageClass(labelStr, activity.name, activity.icon))
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = GMSPageListAdapter(activityList)
    }
}