package fr.free.nrw.commons.customselector.ui.adapter

import android.content.Context
import android.view.ViewGroup
import fr.free.nrw.commons.R
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fr.free.nrw.commons.customselector.listeners.ImageSelectListener
import fr.free.nrw.commons.customselector.model.Image

class ImageAdapter(
    /**
     * Application Context.
     */
    context: Context,

    /**
     * Image select listener for click events on image.
     */
    private var imageSelectListener: ImageSelectListener ):

    RecyclerViewAdapter<ImageAdapter.ImageViewHolder>(context) {

    /**
     * Currently selected images.
     */
    private var selectedImages = arrayListOf<Image>()

    /**
     * List of all images in adapter.
     */
    private var images: ArrayList<Image> = ArrayList()

    /**
     * create View holder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = inflater.inflate(R.layout.item_custom_selector_image,parent, false)
        return ImageViewHolder(itemView)
    }

    /**
     * Bind View holder, load image, selected view, click listeners.
     */
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image=images[position]
        // todo load image thumbnail, set selected view.
        holder.itemView.setOnClickListener {
            selectOrRemoveImage(image, position)
        }
    }

    /**
     * Handle click event on an image, update counter on images.
     */
    private fun selectOrRemoveImage(image:Image, position:Int){
        // todo select the image if not selected and remove it if already selected
    }

    /**
     * Initialize the data set.
     */
    fun init(newImages:List<Image>) {
        val oldImageList:ArrayList<Image> = images
        val newImageList:ArrayList<Image> = ArrayList(newImages)
        val diffResult = DiffUtil.calculateDiff(
            ImagesDiffCallback(oldImageList, newImageList)
        )
        images = newImageList
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return images.size
    }

    /**
     * Image view holder.
     */
    class ImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image_thumbnail)
        val selectedNumber: TextView = itemView.findViewById(R.id.selected_count)
        val uploadedGroup: Group = itemView.findViewById(R.id.uploaded_group)
        val selectedGroup: Group = itemView.findViewById(R.id.selected_group)
    }

    /**
     * DiffUtilCallback.
     */
    class ImagesDiffCallback(
        var oldImageList: ArrayList<Image>,
        var newImageList: ArrayList<Image>
    ) : DiffUtil.Callback(){

        /**
         * Returns the size of the old list.
         */
        override fun getOldListSize(): Int {
            return oldImageList.size
        }

        /**
         * Returns the size of the new list.
         */
        override fun getNewListSize(): Int {
            return newImageList.size
        }

        /**
         * Called by the DiffUtil to decide whether two object represent the same Item.
         */
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return newImageList[newItemPosition].id == oldImageList[oldItemPosition].id
        }

        /**
         * Called by the DiffUtil when it wants to check whether two items have the same data.
         * DiffUtil uses this information to detect if the contents of an item has changed.
         */
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldImageList[oldItemPosition].equals(newImageList[newItemPosition])
        }

    }

}