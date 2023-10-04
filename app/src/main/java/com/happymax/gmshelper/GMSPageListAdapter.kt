package com.happymax.gmshelper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView

class GMSPageListAdapter(val activityList: List<GMSPageClass>) :
    RecyclerView.Adapter<GMSPageListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val name:TextView = view.findViewById(R.id.Name)
        val activityName:TextView = view.findViewById(R.id.ActivityName)
        val menuImageView: ImageView = view.findViewById(R.id.menuImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gmspage_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            try {
                val position = viewHolder.adapterPosition
                val activityItem = activityList[position]
                val activityName = activityItem.activityName
                if(!activityName.isNullOrEmpty()){
                    val intent = Intent()
                    val comp = ComponentName("com.google.android.gms", activityName)
                    intent.setComponent(comp)
                    view.context.startActivity(intent)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        viewHolder.itemView.setOnLongClickListener(object : View.OnLongClickListener{
            override fun onLongClick(view: View?): Boolean {
                if(view != null){
                    var pos = viewHolder.layoutPosition
                    val menu = PopupMenu(view.context, viewHolder.menuImageView)
                    menu.inflate(R.menu.item_popupmenu)
                    menu.setOnMenuItemClickListener(object :PopupMenu.OnMenuItemClickListener{
                        override fun onMenuItemClick(item: MenuItem?): Boolean {
                                when(item?.itemId){
                                    R.id.create_shortcut -> {
                                        val activityName = activityList[pos].activityName
                                        val name = activityList[pos].name
                                        val icon = activityList[pos].icon

                                        val shortCuts = ShortcutManagerCompat.getDynamicShortcuts(view.context)
                                        if(shortCuts.any{ activityName == it.id }){
                                            ShortcutManagerCompat.removeDynamicShortcuts(view.context, listOf(activityName))
                                            Toast.makeText(view.context, "Shortcut removed", Toast.LENGTH_SHORT).show()
                                        }else{
                                            var shortcutName = name
                                            if(name=="com.google.android.gms")
                                                shortcutName = activityName.split(".").last()
                                            val intent = Intent(Intent.ACTION_VIEW)
                                            val comp = ComponentName("com.google.android.gms", activityName)
                                            intent.setComponent(comp)

                                            var iconCompat:IconCompat = IconCompat.createWithResource(view.context, R.mipmap.ic_launcher)
                                            if(icon > 0){
                                                //获取gms的上下文
                                                val appResContext: Context =
                                                    view.context.createPackageContext(
                                                        "com.google.android.gms",
                                                        Context.CONTEXT_IGNORE_SECURITY
                                                    )
                                                if (appResContext != null) {
                                                    val resources = appResContext.resources
                                                    try {
                                                        val drawable = resources.getDrawable(icon)
                                                        if(drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0){
                                                            val bitmap = drawable.toBitmap()
                                                            if(bitmap.width > 0 && bitmap.height > 0)
                                                                iconCompat = IconCompat.createWithBitmap(bitmap)
                                                        }

                                                    }catch (e:Exception){
                                                        e.printStackTrace()
                                                        throw e
                                                    }
                                                }
                                            }

                                            val shortcut = ShortcutInfoCompat.Builder(view.context, activityName)
                                                .setShortLabel(shortcutName)
                                                .setLongLabel(shortcutName)
                                                .setIcon(iconCompat)
                                                .setIntent(intent)
                                                .build()

                                            ShortcutManagerCompat.pushDynamicShortcut(view.context, shortcut)
                                            Toast.makeText(view.context, "Shortcut added", Toast.LENGTH_SHORT).show()
                                        }

                                        true
                                    }
                                    else -> false
                                }
                            return false
                        }
                    })
                    menu.show()

                    return true
                }
                return false
            }
        })
        return  viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activityList[position]
        holder.name.text = activity.name
        holder.activityName.text = activity.activityName
    }

    override fun getItemCount(): Int {
        return activityList.size
    }
}