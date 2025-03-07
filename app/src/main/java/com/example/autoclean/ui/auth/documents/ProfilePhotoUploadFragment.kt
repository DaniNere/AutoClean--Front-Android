package com.example.autoclean.ui.auth.documents

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.autoclean.data.api.ApiClient
import com.example.autoclean.data.model.dto.UpdateAccountDto
import com.example.autoclean.databinding.FragmentProfilePhotoUploadBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfilePhotoUploadFragment : Fragment() {

    private var _binding: FragmentProfilePhotoUploadBinding? = null
    private val binding get() = _binding!!
    private var imageUrl: String? = null

    companion object {
        private const val TAG = "ProfilePhotoUploadFragment"
    }

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { intent ->
                    val imageUri = intent.data
                    if (imageUri != null) {
                        Log.d(TAG, "Image selected: $imageUri")
                        displayImagePreview(imageUri)
                        uploadImageToFirebase(imageUri)
                    } else {
                        showToast("Failed to process selected image.")
                    }
                }
            } else {
                showToast("No document selected.")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilePhotoUploadBinding.inflate(inflater, container, false)

        binding.uploadLinearLayout.setOnClickListener { openDocumentPicker() }

        binding.btnEnviar.setOnClickListener {
            imageUrl?.let { url ->
                updateProfilePictureInDatabase("9", url)
            } ?: showToast("Image has not been uploaded.")
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        selectImageLauncher.launch(intent)
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val storageReference = Firebase.storage.reference
        val imageRef = storageReference.child("images/${uri.lastPathSegment}")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    imageUrl = downloadUri.toString()
                    Log.d(TAG, "Image uploaded successfully. URL: $downloadUri")
                    showToast("Upload successful!")
                    binding.btnEnviar.isEnabled = true
                }
            }.addOnFailureListener { e ->
                Log.d(TAG, "Failed to upload image: ${e.message}")
                showToast("Failed to upload image.")
            }
    }

    private fun updateProfilePictureInDatabase(userId: String, imageUrl: String) {
        val updateAccountDto = UpdateAccountDto(photoUrl = imageUrl)
        val call = ApiClient.apiService.updateAccount(userId, updateAccountDto)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("URL successfully saved in DB.")
                } else {
                    showToast("Failed to save URL in DB.")
                }
                response.errorBody()?.close()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Server connection error.")
            }
        })
    }

    private fun displayImagePreview(uri: Uri) {
        binding.imagePreview.apply {
            setImageURI(uri)
            visibility = View.VISIBLE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}