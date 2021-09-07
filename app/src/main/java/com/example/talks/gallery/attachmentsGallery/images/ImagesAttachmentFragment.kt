package com.example.talks.gallery.attachmentsGallery.images

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.talks.others.Helper
import com.example.talks.R
import com.example.talks.databinding.FragmentImagesAttachmentBinding
import com.example.talks.gallery.attachmentsGallery.AttachmentStateListener
import kotlinx.android.synthetic.main.fragment_images_attachment.*

@Suppress("DEPRECATION")
class ImagesAttachmentFragment : Fragment(), AttachmentStateListener {

    private lateinit var binding : FragmentImagesAttachmentBinding
    private lateinit var viewModel: ImagesViewModel

    private var toolbarSubtitle = 0
    private var toolbarNormalState = true

    private lateinit var imagesAdapter: ImagesAdapter

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImagesAttachmentBinding.inflate(inflater, container,false)
        viewModel = ViewModelProvider(this).get(ImagesViewModel::class.java)

        context?.let { viewModel.listOfImages(it) }

        viewModel.imagesList.observe(viewLifecycleOwner,{
            val images = it
            imagesAdapter = ImagesAdapter(images, this.requireContext())
            imagesAdapter.setAttachmentStateListener(this)
            binding.attachmentImagesView.apply {
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(context, 4)
                adapter = imagesAdapter
            }
            
            imagesToolbar.apply {
                title = "Select Images"
                subtitle = "${images.size} images"
                toolbarSubtitle = images.size

                setNavigationOnClickListener {
                    if (toolbarNormalState) {
                        requireActivity().finish()
                    } else {
                        imagesAdapter.clearSelection()
                    }
                }
            }
        })

        return binding.root
    }

    override fun state(state: Boolean, listSize: Int) {
        toolbarNormalState = if (state){
            enableSelectedStateOnToolbar(listSize)
            inflateMenu()
            false
        }else{
            menuInflatedOnce = false
            imagesToolbar.menu.clear()
            disableSelectedStateOnToolbar()
            true
        }
    }

    private fun enableSelectedStateOnToolbar(listSize: Int){
        imagesToolbar.apply {
            setNavigationIcon(R.drawable.cross_icon)
            title = if (listSize == 1) {
                "$listSize image selected"
            }else{
                "$listSize images selected"
            }
            subtitle = null
        }
    }

    private fun disableSelectedStateOnToolbar(){
        imagesToolbar.apply {
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            title = "Select Images"
            subtitle = "$toolbarSubtitle images"
        }
    }

    private var menuInflatedOnce = false
    private fun inflateMenu(){
        if (!menuInflatedOnce){
            imagesToolbar.inflateMenu(R.menu.edit_profile_menu)
            menuInflatedOnce = true

            imagesToolbar.setOnMenuItemClickListener {item ->
                when(item.itemId){
                    R.id.confirm_button ->{
                        val imagesList = imagesAdapter.getSelectedImagesList()
                        Helper.setImages(imagesList)
                        activity?.finish()
                    }
                }
                true
            }
        }
    }
}