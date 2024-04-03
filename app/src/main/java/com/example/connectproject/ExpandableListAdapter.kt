package com.example.connectproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.storage.StorageReference

class ExpandableListAdapter(
    private val context: Context,
    private val titles: List<String>,
    private val listData: Map<String, List<String>>,
    private val userUids: List<String>,
    private val storageReference: StorageReference
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return titles.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return listData[titles[groupPosition]]?.size ?: 0
    }

    override fun getGroup(groupPosition: Int): Any {
        return titles[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return listData[titles[groupPosition]]?.getOrNull(childPosition) ?: ""
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.group_item, null)
        }
        val title = getGroup(groupPosition) as String
        val itemTitle = convertView!!.findViewById<TextView>(R.id.group_name)
        itemTitle.text = title

        val groupImageView = convertView.findViewById<ImageView>(R.id.group_image)
        val userUid = userUids[groupPosition]
        val imageRef = storageReference.child(userUid) // Use UID as reference
        imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.profile_placeholder)
                .into(groupImageView)
        }.addOnFailureListener {
            groupImageView.setImageResource(R.drawable.profile_placeholder)
        }
        return convertView
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.child_item, null)
        }
        val childText = getChild(groupPosition, childPosition) as String
        val itemTitle = convertView!!.findViewById<TextView>(R.id.child_title)
        itemTitle.text = childText
        return convertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}