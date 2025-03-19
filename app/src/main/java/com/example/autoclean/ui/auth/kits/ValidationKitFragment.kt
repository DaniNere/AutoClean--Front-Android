package com.example.autoclean.ui.auth.kits

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.autoclean.R
import com.example.autoclean.data.api.ApiClient
import com.example.autoclean.data.model.dto.UpdateDocumentsDto
import com.example.autoclean.databinding.FragmentValidationKitBinding
import com.example.autoclean.ui.auth.documents.AlertModal
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.NonCancellable.start
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ValidationKitFragment : Fragment() {
    private var _binding: FragmentValidationKitBinding? = null
    private val binding get() = _binding!!
    private var videoUrl: String? = null

    companion object {
        private const val TAG = "ValidationKitFragment"
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val storageGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true

        when {
            cameraGranted -> openCameraForVideo()
            storageGranted -> openGalleryForVideo()
            else -> {
                Log.d(TAG, "Permissions denied for camera or storage.")
                Toast.makeText(requireContext(), "Permissões negadas. Não é possível abrir a câmera ou galeria.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val takeVideoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d(TAG, "Vídeo gravado com sucesso: $uri")
                displayVideoPreview(uri)
                uploadVideoToFirebase(uri)
            }
        } else {
            showToast("Falha ao gravar o vídeo.")
        }
    }

    private val selectVideoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d(TAG, "Vídeo selecionado da galeria: $uri")
                displayVideoPreview(uri)
                uploadVideoToFirebase(uri)
            }
        } else {
            showToast("Nenhum vídeo foi selecionado.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentValidationKitBinding.inflate(inflater, container, false)

        binding.btnEnviar.isEnabled = false
        binding.uploadLinearLayout.setOnClickListener { handleActionButtonClick() }

        binding.btnEnviar.setOnClickListener {
            if (!videoUrl.isNullOrEmpty()) {
                updateProfilePictureInDatabase("4", videoUrl!!)
            } else {
                showToast("A URL do vídeo ainda não está pronta.")
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleActionButtonClick() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCameraForVideo()
        } else if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGalleryForVideo()
        } else {
            requestPermissionsIfNeeded()
        }
    }

    private fun requestPermissionsIfNeeded() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
            Log.d(TAG, "Camera permission needed.")
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            Log.d(TAG, "Storage permission needed.")
        }

        if (permissionsToRequest.isNotEmpty()) {
            Log.d(TAG, "Solicitando permissões.")
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun openCameraForVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        takeVideoLauncher.launch(intent)
    }

    private fun openGalleryForVideo() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        selectVideoLauncher.launch(intent)
    }

    private fun uploadVideoToFirebase(uri: Uri) {
        Log.d(TAG, "Início do upload de vídeo para Firebase com URI: $uri")
        val storageReference = Firebase.storage.reference
        val videoRef = storageReference.child("videos/${uri.lastPathSegment}")

        videoRef.putFile(uri)
            .addOnSuccessListener {
                Log.d(TAG, "Upload de vídeo bem-sucedido")
                videoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    videoUrl = downloadUri.toString()
                    Log.d(TAG, "URL do vídeo no Firebase: $videoUrl")
                    showToast("Upload de vídeo realizado com sucesso!")

                    updateProfilePictureInDatabase("14", videoUrl!!)
                }
            }.addOnFailureListener { e ->
                Log.d(TAG, "Falha ao enviar o vídeo: ${e.message}")
                showToast("Falha ao enviar o vídeo.")
            }
    }

    private fun updateProfilePictureInDatabase(userId: String, videoUrl: String) {
        Log.d(TAG, "Atualizando URL do vídeo no banco de dados.")
        val validationKitVideo = UpdateDocumentsDto(validationKitVideo = videoUrl)
        Log.d(TAG, "URL obtida após o upload: $validationKitVideo")

        ApiClient.apiService.updateValidationKit(userId, validationKitVideo).enqueue(object :
            Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showVideoUploadConfirmationDialog()
                } else {
                    showToast("Falha ao salvar URL no banco de dados.")
                }
                response.errorBody()?.close()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Erro de conexão com o servidor.")
            }
        })
    }

    private fun displayVideoPreview(uri: Uri) {
        Log.d(TAG, "Mostrando preview do vídeo: $uri")
        val videoView: VideoView = binding.videoPreview
        videoView.setVideoURI(uri)
        videoView.visibility = View.VISIBLE
        videoView.start()
    }

    private fun showToast(message: String) {
        Log.d(TAG, "Mostrando Toast: $message")
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showVideoUploadConfirmationDialog() {
        val dialog = AlertVerificationModal.newInstance()
        dialog.show(parentFragmentManager, "VideoUploadConfirmationDialog")
    }
}