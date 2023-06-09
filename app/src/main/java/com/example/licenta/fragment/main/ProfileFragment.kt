package com.example.licenta.fragment.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.example.licenta.R
import com.example.licenta.activity.auth.LoginActivity
import com.example.licenta.data.LoggedUserData
import com.example.licenta.data.LoggedUserGoals
import com.example.licenta.data.LoggedUserProfilePhoto
import com.example.licenta.firebase.Auth
import com.example.licenta.fragment.main.profile.GoalsFragment
import com.example.licenta.fragment.main.profile.RecordsFragment
import com.example.licenta.util.PermissionsChecker
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.tabs.TabLayout
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment(), TabLayout.OnTabSelectedListener, View.OnClickListener {
    private lateinit var fragmentFrameLayout: FrameLayout
    private lateinit var profilePhoto: ImageView
    private lateinit var nameTV: TextView
    private lateinit var logOut: ImageView
    private lateinit var infoTab: TabLayout
    private lateinit var editPhotoDialog: AlertDialog
    private lateinit var savingPhotoPb: CircularProgressIndicator
    private lateinit var currentImageUri: Uri
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val profilePhotoReference: StorageReference =
        storage.reference.child("profile-pics/JPEG_${LoggedUserData.getLoggedUser().uuid}.jpg")

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            if (imageUri != null) savePhotoToStorage(imageUri)
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isTaken ->
            if (isTaken) savePhotoToStorage(currentImageUri)
        }

    private fun initComponents(view: View) {
        fragmentFrameLayout = view.findViewById(R.id.fragment_profile_items_fragment)
        infoTab = view.findViewById(R.id.fragment_profile_tab_layout)
        infoTab.addOnTabSelectedListener(this)
        setProfilePhoto(view)
        logOut = view.findViewById(R.id.fragment_profile_button_log_out_btn)
        nameTV = view.findViewById(R.id.fragment_profile_name_tv)
        nameTV.text =
            "${LoggedUserData.getLoggedUser().firstName} ${LoggedUserData.getLoggedUser().lastName}"
        logOut.setOnClickListener(this)
        switchFragment(GoalsFragment())
    }


    private fun switchFragment(fragment: Fragment) {
        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_profile_items_fragment, fragment)
        fragmentTransaction.commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        initComponents(view)
        return view
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.fragment_profile_button_log_out_btn -> logOutUser()
            R.id.fragment_profile_photo_profile_iv -> openEditPhotoDialog()
            R.id.dialog_profile_open_camera_btn -> openCamera()
            R.id.dialog_profile_open_gallery_btn -> openGallery()
        }
    }

    private fun logOutUser() {
        Auth.logUserOut()
        LoggedUserData.setLoggedUser(null)
        LoggedUserProfilePhoto.resetProfilePhoto()
        LoggedUserGoals.setGoals(null)
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }

    private fun setProfilePhoto(view: View) {
        profilePhoto = view.findViewById(R.id.fragment_profile_photo_profile_iv)
        profilePhoto.setImageBitmap(LoggedUserProfilePhoto.getProfilePhoto())
        profilePhoto.setOnClickListener(this)
    }

    private fun openEditPhotoDialog() {
        val view = LayoutInflater
            .from(requireActivity())
            .inflate(R.layout.dialog_profile_fragment_edit_photo, null, false)

        val cameraBtn: Button = view.findViewById(R.id.dialog_profile_open_camera_btn)
        cameraBtn.setOnClickListener(this)
        val galleryBtn: Button = view.findViewById(R.id.dialog_profile_open_gallery_btn)
        galleryBtn.setOnClickListener(this)
        savingPhotoPb = view.findViewById(R.id.dialog_profile_uploading_pb)
        editPhotoDialog = AlertDialog
            .Builder(requireContext())
            .setTitle("Edit your profile photo")
            .setView(view)
            .setNegativeButton(
                R.string.button_cancel
            ) { dialog, _ -> dialog!!.dismiss() }
            .create()

        editPhotoDialog.show()
    }

    private fun openCamera() {
        if (PermissionsChecker.isCameraAndStoragePermissionAccepted(requireActivity())) {
            currentImageUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName + ".provider",
                createImage()
            )
            cameraLauncher.launch(currentImageUri)
        } else if (!PermissionsChecker.isCameraPermissionAccepted(requireActivity())) {
            PermissionsChecker.askForCameraPermission(requireActivity())
        } else if (!PermissionsChecker.isStoragePermissionAccepted(requireActivity())) {
            PermissionsChecker.askForStoragePermission(requireActivity())
        }
    }

    private fun openGallery() {
        if (PermissionsChecker.isStoragePermissionAccepted(requireActivity())) {
            galleryLauncher.launch("image/*")
        } else {
            PermissionsChecker.askForStoragePermission(requireActivity())
        }
    }

    private fun createImage(): File {
        val workingDirectory = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${LoggedUserData.getLoggedUser().uuid}",
            ".jpg",
            workingDirectory
        )
    }

    private fun savePhotoToStorage(uri: Uri) {
        savingPhotoPb.visibility = View.VISIBLE
        LoggedUserProfilePhoto.saveProfilePhoto(uri,LoggedUserData.getLoggedUser().uuid) { saved ->
            if (saved) {
                LoggedUserProfilePhoto.setProfilePhoto(
                    requireContext(),
                    LoggedUserData.getLoggedUser().uuid
                ) {
                    profilePhoto.setImageBitmap(LoggedUserProfilePhoto.getProfilePhoto())
                    savingPhotoPb.visibility = View.INVISIBLE
                    editPhotoDialog.cancel()
                }
            } else {
                Toast.makeText(requireContext(), "Could not save the photo", Toast.LENGTH_SHORT)
                    .show()
                savingPhotoPb.visibility = View.INVISIBLE
                editPhotoDialog.cancel()
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        when (tab!!.position) {
            0 -> switchFragment(GoalsFragment())
            1 -> switchFragment(RecordsFragment())
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        //
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        //
    }
}