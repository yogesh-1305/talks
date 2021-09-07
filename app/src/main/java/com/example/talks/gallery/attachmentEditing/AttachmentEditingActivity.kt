package com.example.talks.gallery.attachmentEditing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.example.talks.others.Helper
import com.example.talks.R
import kotlinx.android.synthetic.main.activity_attachment_editing.*

class AttachmentEditingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attachment_editing)

        val images = Helper.getImages()
        val pager = attachmentEditPager as ViewPager
        val adapter = images?.let { AttachmentViewPagerAdapter(this, it) }
        pager.adapter = adapter

        toolbarAttachmentEditing.inflateMenu(R.menu.attachment_edit_menu)
        toolbarAttachmentEditing.setNavigationOnClickListener {
            finish()
        }
    }
}