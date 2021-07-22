package com.example.talks.gallery.attachmentEditing

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.talks.R
import kotlinx.android.synthetic.main.gallery_item.view.*
import kotlinx.android.synthetic.main.gallery_item.view.gallery_image
import kotlinx.android.synthetic.main.pager_image.view.*

class AttachmentViewPagerAdapter(val context: Context, val images: List<Uri>): PagerAdapter() {

    override fun getCount(): Int {
        return images.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    private lateinit var viewPager: ViewPager
    private lateinit var layoutInflater: LayoutInflater
    @SuppressLint("InflateParams")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.pager_image, null)
        val imageView = view.pager_image as ImageView
        imageView.setImageURI(images[position])

        viewPager = container as ViewPager
        viewPager.addView(view)

        imageView.setOnClickListener {
            Toast.makeText(context, "image clicked", Toast.LENGTH_SHORT).show()
        }
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val viewPager = container as ViewPager
        val view = `object` as View
        viewPager.removeView(view)
    }
}